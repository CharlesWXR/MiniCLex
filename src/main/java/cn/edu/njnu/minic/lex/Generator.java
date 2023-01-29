package cn.edu.njnu.minic.lex;

import cn.edu.njnu.minic.annotation.GeneratorConfigure;
import cn.edu.njnu.minic.exception.LexException;
import cn.edu.njnu.minic.fa.DFA;
import cn.edu.njnu.minic.fa.Edge;
import cn.edu.njnu.minic.utils.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.*;
import java.util.stream.Collectors;

public class Generator {
	protected static Map<String, String> templateMapper = new HashMap<String, String>();

	static {
		try {
			// Open target templates
			String root = ".\\src\\main\\java\\cn\\edu\\njnu\\minic\\templates\\";
			String[] s = {"StateEnumTemplate", "StateTemplate", "CaseTemplate",
					"InitStartTemplate", "InitEndTemplate", "ScannerClassTemplate"};

			byte[] buffer = new byte[1024 * 1024];
			for (String name : s) {
				FileInputStream fis = new FileInputStream(new File(root + name + ".txt"));

				Arrays.fill(buffer, (byte) 0);
				int count = -1;
				StringBuffer sb = new StringBuffer();
				while ((count = fis.read(buffer)) != -1) {
					sb.append(new String(buffer).substring(0, count));
				}

				fis.close();

				templateMapper.put(name, sb.toString());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void generate() throws Exception {
		// Get the invoker's class name to generate new package name
		// and get the configuration from the annotation on target class
		StackTraceElement[] invokers = Thread.currentThread().getStackTrace();
		Class clazz = Class.forName(invokers[2].getClassName());
		if (!clazz.isAnnotationPresent(GeneratorConfigure.class)) {
			throw new LexException(LexException.MissingConfiguration + "GeneratorConfigure");
		}

		String packageName = clazz.getPackage().getName();

		// Get configuration
		GeneratorConfigure config = (GeneratorConfigure) clazz.getAnnotation(GeneratorConfigure.class);
		String filepath = config.lexFilePath();
		String[] supportClasses = config.supportClasses();

		// Process word for input lex file
		FileInputStream fis = new FileInputStream(new File(filepath));
		List<LexElement> elements = LexScanner.getElements(fis);
		Map<LexElementEnum, List<String>> map = LexScanner.classify(elements);

		// Generate the target DFA
		DFA dfa = LexScanner.convert2DFA(map.get(LexElementEnum.RegexExpression));

		// Builder for states
		StringBuffer stateBuffer = new StringBuffer();
		Map<String, String> stateMapper = new HashMap<String, String>();

		// Builder for cases
		StringBuffer caseBuffer = new StringBuffer();
		Map<String, String> caseMapper = new HashMap<String, String>();

		// DFA information
		Map<Integer, List<Edge>> edges = dfa.getEdges();
		List<Integer> start = dfa.getStart();
		List<Integer> end = dfa.getEnd();
		Map<Integer, List<Integer>> endMapper = dfa.getEndMapper();

		// States.java begin
		// Avoid nodes without out degrees
		Set<Integer> recordedNodes = new HashSet<Integer>();
		Set<Integer> unrecordedNodes = new HashSet<Integer>();

		// Traverse all the edges
		// Transform x-a->b => state x { switch: case a: return b }
		for (Map.Entry<Integer, List<Edge>> entry : edges.entrySet()) {
			caseBuffer.setLength(0);
			recordedNodes.add(entry.getKey());
			unrecordedNodes.remove(entry.getKey());
			for (Edge edge : entry.getValue()) {
				// Process escape characters
				char c = edge.getData();
				String cRes = "";
				switch (c) {
					case '\n': {
						cRes = "\\\\\\\\\\\\\\\\" + "n";
						break;
					}
					case '\t': {
						cRes = "\\\\\\\\\\\\\\\\" + "t";
						break;
					}
					case '\f': {
						cRes = "\\\\\\\\\\\\\\\\" + "f";
						break;
					}
					case '\r': {
						cRes = "\\\\\\\\\\\\\\\\" + "r";
						break;
					}
					case '\'': {
						cRes = "\\\\\\\\\\\\\\\\" + "\'";
						break;
					}
					case '\\': {
						// Jesus I give up thinking
						cRes = "\\\\\\\\\\\\\\\\" + "\\\\\\\\\\\\\\\\" + "\\\\";
						break;
					}
					default: {
						cRes = Character.toString(edge.getData());
						break;
					}
				}
				// case ${case_name}: {}
				caseMapper.put("case_name", cRes);
				// case x { return ${next_state_name}; }
				caseMapper.put("next_state_name", "States.S_" + edge.getEnd());
				String part = StringUtils.processTemplate(this.templateMapper.get("CaseTemplate"), caseMapper);
				// case 1: {}, case 2: {}...
				caseBuffer.append(part);
				if (!recordedNodes.contains(edge.getEnd()))
					unrecordedNodes.add(edge.getEnd());
			}
			// Put execution code into places
			StringBuffer exe = new StringBuffer();
			if (endMapper.containsKey(entry.getKey())) {
				for (Integer index : endMapper.get(entry.getKey())) {
					exe.append(map.get(LexElementEnum.Code).get(index) + "\n");
				}
			}

			// Assemble cases into a state
			// ${state_name} { switch cases... Object execute() { ${execute_slot} } }
			// The execution codes are processed, because an ending state may share multiple executable codes
			// and these codes may have many 'return', which may leading to a compile error
			stateMapper.put("execute_slot", StringUtils.processReturn(exe.toString().replaceAll("([\\n\\r]+)", "$1\t\t\t")));
			stateMapper.put("case_slot", caseBuffer.toString());
			stateMapper.put("state_name", "S_" + entry.getKey());
			stateBuffer.append(",\n" + StringUtils.processTemplate(this.templateMapper.get("StateTemplate"), stateMapper));
		}

		// Create states for nodes without out degrees
		for (int index : unrecordedNodes) {
			StringBuffer exe = new StringBuffer();
			if (endMapper.containsKey(index)) {
				for (Integer i : endMapper.get(index)) {
					exe.append(map.get(LexElementEnum.Code).get(i) + "\n");
				}
			}
			stateMapper.put("execute_slot", StringUtils.processReturn(exe.toString().replaceAll("([\\n\\r]+)", "$1\t\t\t")));
			stateMapper.put("case_slot", "");
			stateMapper.put("state_name", "S_" + index);
			stateBuffer.append(",\n" + StringUtils.processTemplate(this.templateMapper.get("StateTemplate"), stateMapper));
		}

		// Generate state enum
		Map<String, String> stateEnumMap = new HashMap<String, String>();
		stateEnumMap.put("package_name", packageName + ".scanner");
		stateEnumMap.put("support_classes",
				Arrays.stream(supportClasses)
						.collect(Collectors.joining(";\nimport ", "import ", ";")));
		stateEnumMap.put("state_slot", stateBuffer.toString());
		String res = StringUtils.processTemplate(this.templateMapper.get("StateEnumTemplate"), stateEnumMap);

		// Open the output file States.java
		OutputStreamWriter osw = new OutputStreamWriter(
				new FileOutputStream(new File("States.java"))
		);
		osw.write(res);
		osw.flush();
		osw.close();
		// States.java end

		// WordScanner.class begin
		Map<String, String> classMapper = new HashMap<String, String>();
		Map<String, String> initMapper = new HashMap<String, String>();

		// Set the static init code segment
		StringBuffer classBuffer = new StringBuffer();
		for (Integer s : start) {
			initMapper.put("NFA_start_element", Integer.toString(s));
			classBuffer.append(
					StringUtils.processTemplate(this.templateMapper.get("InitStartTemplate"), initMapper)
			);
		}
		for (Integer s : end) {
			initMapper.put("NFA_end_element", Integer.toString(s));
			classBuffer.append(
					StringUtils.processTemplate(this.templateMapper.get("InitEndTemplate"), initMapper)
			);
		}

		classMapper.put("init_static_slot", classBuffer.toString());
		res = StringUtils.processTemplate(this.templateMapper.get("ScannerClassTemplate"), classMapper);
		// Set the package name and support classes
		res = StringUtils.processTemplate(res, stateEnumMap);

		osw = new OutputStreamWriter(
				new FileOutputStream(new File("WordScanner.java"))
		);
		osw.write(res);
		osw.flush();
		osw.close();
		// WordScanner.class end
	}
}
