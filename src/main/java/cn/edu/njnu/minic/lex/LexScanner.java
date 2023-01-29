package cn.edu.njnu.minic.lex;

import cn.edu.njnu.minic.exception.LexException;
import cn.edu.njnu.minic.fa.DFA;
import cn.edu.njnu.minic.fa.NFA;
import cn.edu.njnu.minic.re.RegexExpression;

import java.io.InputStream;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LexScanner {
	private static final Pattern patternSpace = Pattern.compile("^[\\s\n\r]+");
	private static final Pattern patternComment = Pattern.compile("^#.*?[\n\r]+");
	private static final Pattern patternCode = Pattern.compile("^%%\\[(.+?|[\n\r])*?%%]");
	private static final Pattern patternRE = Pattern.compile("^(.*?)[\\s\n\r]+%%");

	public static List<LexElement> getElements(InputStream in) throws Exception {
		byte[] buffer = new byte[1024];
		StringBuilder sb = new StringBuilder();
		List<LexElement> elements = new ArrayList<LexElement>();
		int len = -1;

		while (len != sb.length()) {
			len = sb.length();
			if (in.read(buffer) != -1) {
				sb.append(new String(buffer));
				Arrays.fill(buffer, (byte)0);
			}

			Matcher m = patternSpace.matcher(sb.toString());
			if (m.find()) {
				sb = sb.delete(0, m.end());
			}


			if (sb.toString().startsWith("#")) {
				m = patternComment.matcher(sb.toString());
				if (m.find()) {
					sb = sb.delete(0, m.end());
				} else {
					continue;
				}
			}

			m = patternCode.matcher(sb.toString());
			if (m.find()) {
				sb = sb.delete(0, m.end());
				String r = m.group(0);
				elements.add(new LexElement(r.substring(3, r.length() - 3).trim(), LexElementEnum.Code));
			}

			m = patternRE.matcher(sb.toString());
			if (!sb.toString().startsWith("%%[") && m.find()) {
				sb = sb.delete(0, m.end() - 2);
				elements.add(new LexElement(m.group(1), LexElementEnum.RegexExpression));
			}
		}

		if (sb.toString().trim().length() > 0)
			throw new LexException(LexException.IllegalComponent + sb.toString());

		return elements;
	}

	public static boolean verify(List<LexElement> elements) {
		if (elements.size() % 2 != 0)
			return false;

		for (int i = 0; i < elements.size(); i++) {
			if (i % 2 == 0) {
				if (elements.get(i).getType() != LexElementEnum.RegexExpression)
					return false;
			} else {
				if (elements.get(i).getType() != LexElementEnum.Code)
					return false;
			}
		}
		return true;
	}

	public static Map<LexElementEnum, List<String>> classify(List<LexElement> elements) throws Exception {
		if (verify(elements)) {
			Map<LexElementEnum, List<String>> res = new HashMap<LexElementEnum, List<String>>();
			res.put(LexElementEnum.Code, new ArrayList<String>());
			res.put(LexElementEnum.RegexExpression, new ArrayList<String>());
			for (LexElement element : elements) {
				res.get(element.getType()).add((String) element.getData());
			}
			return res;
		} else
			throw new LexException(LexException.MismatchedComponent);
	}

	public static DFA convert2DFA(List<String> elements) throws Exception {
		ArrayList<DFA> DFAs = new ArrayList<DFA>();
		int currentNodeIndex = 1;
		for (String element : elements) {
			NFA nfa = new NFA();
			currentNodeIndex = nfa.convertRE2NFA(new RegexExpression(element), currentNodeIndex);
			DFA dfa = new DFA(nfa);
			DFAs.add(dfa);
		}

		NFA nRes = DFA.mergeAll(DFAs);
		DFA res = new DFA(nRes);
		return res;
	}
}
