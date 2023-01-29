package cn.edu.njnu.minic.re;

import cn.edu.njnu.minic.exception.REException;

import java.util.HashMap;
import java.util.Map;

public class RegexExpression {
	private static final Map<String, String> ProcessMapper = new HashMap<String, String>() {{
		put("\\\\w", "a-zA-Z0-9");
		put("\\\\d", "0-9");
		put("\\\\s", "(\\\\f| |\\\\n|\\\\r|\\\\t)");
	}};

	String re;

	public RegexExpression() {
	}

	public RegexExpression(String re) throws REException {
		this.re = re;
		processRegex();
	}

	public String getRe() {
		return re;
	}

	public void setRe(String re) {
		this.re = re;
	}

	public void processRegex() throws REException {
		// Process the regex to support a little bit more grammars
		StringBuilder sb = new StringBuilder(this.re);

		int lIndex = 0;
		while ((lIndex = sb.indexOf("[", lIndex)) != -1) {
			if (lIndex != 0 && sb.charAt(lIndex - 1) == '\\') {
				lIndex++;
				continue;
			}

			int rIndex = sb.indexOf("]", lIndex);
			if (rIndex == -1) {
				throw new REException(REException.InvalidRE + this.re);
			}
			else {
				// Convert [\x] to (x-x) or (x|x)
				String processed = sb.substring(lIndex, rIndex + 1);
				for (Map.Entry<String, String> entry : RegexExpression.ProcessMapper.entrySet()) {
					processed = processed.replaceAll("([^\\\\])" + entry.getKey(), "$1" + entry.getValue());
				}
				StringBuilder sbProcessed = new StringBuilder(processed);
				sbProcessed.setCharAt(0, '(');
				sbProcessed.setCharAt(sbProcessed.length() - 1, ')');
				// convert (x-x...x|x) to (x|...x|x)
				for (int i = 1; i < sbProcessed.length() - 1; i++) {
					// Skip the beginning ( and ending )
					if (sbProcessed.charAt(i) == '\\')
						// Skip 2 chars following, as the next valid - should be at least 2 characters away
						i += 2;
					else if (i >= 1 && i <= sbProcessed.length() - 2 && sbProcessed.charAt(i) == '-') {
						// Identify x-x
						char begin = sbProcessed.charAt(i - 1);
						char end = sbProcessed.charAt(i + 1);
						StringBuilder sbTemp = new StringBuilder();
						if (begin > end)
							throw new REException(REException.InvalidRE + this.re);

						for (char c = begin; c <= end - 1; c++) {
							// a-c => a|b| the ending character will process later
							sbTemp.append(c + "|");
						}
						// a-c => a|b|c without others following
						sbTemp.append(end);
						if (i < sbProcessed.length() - 3) {
							// a-c... => a|b|c| in case others following
							sbTemp.append('|');
						}
						// Replace the substitution into the temp sb
						sbProcessed.replace(i - 1, i + 2, sbTemp.toString());
						// Add the i with the difference of the new sb
						i += sbTemp.length() - 2;
					}
				}
				// processed = processed.replaceAll("^\\[(.*)]$", "($1)");
				sb = sb.replace(lIndex, rIndex + 1, sbProcessed.toString());
			}
			lIndex++;
		}
		this.re = sb.toString();
	}
}
