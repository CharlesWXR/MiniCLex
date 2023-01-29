package cn.edu.njnu.minic.fa;

import cn.edu.njnu.minic.exception.REException;
import cn.edu.njnu.minic.re.RegexExpression;

import java.util.*;

public class NFA extends FA {
	private int minNodeIndex;
	private Map<Integer, Integer> endMapper = null;

	public NFA() {

	}

	public NFA(RegexExpression regex, int nodeIndex) throws REException {
		convertRE2NFA(regex, nodeIndex);
	}

	public int getMinNodeIndex() {
		return minNodeIndex;
	}

	public Map<Integer, Integer> getEndMapper() {
		return this.endMapper;
	}

	public void setEndMapper(Map<Integer, Integer> endMapper) {
		this.endMapper = endMapper;
	}

	@Override
	public String toString() {
		return "NFA{" +
				"minNodeIndex=" + minNodeIndex +
				", endMapper=" + endMapper +
				", start=" + start +
				", end=" + end +
				", edges=" + edges +
				'}';
	}

	public int concat(NFA nfa, int currentNodeIndex) {
		//    (1)...(2)       (a)...(b)
		// => (1)...(2)->(n)->(a)...(b)
		if (nfa.isEmpty())
			return currentNodeIndex;

		if (this.isEmpty()) {
			this.end.addAll(nfa.end);
			this.start.addAll(nfa.start);
			this.edges.putAll(nfa.edges);
			return currentNodeIndex;
		}

		if (this.end.size() == 1) {
			int end = this.end.get(0);
			if (!this.edges.containsKey(end))
				this.edges.put(end, new ArrayList<Edge>());
			for (Integer start : nfa.start) {
				this.edges.get(end).add(new Edge(start, Edge.E_EDGE));
			}
			this.edges.putAll(nfa.edges);
			this.end = nfa.end;
			return currentNodeIndex;
		}


		if (nfa.start.size() == 1) {
			int start = nfa.start.get(0);
			for (Integer end : this.end) {
				if (!this.edges.containsKey(end))
					this.edges.put(end, new ArrayList<Edge>());
				this.edges.get(end).add(new Edge(start, Edge.E_EDGE));
			}
			this.edges.putAll(nfa.edges);
			this.end = nfa.end;
			return currentNodeIndex;
		}

		for (Integer end : this.end) {
			if (!this.edges.containsKey(end))
				this.edges.put(end, new ArrayList<Edge>());
			this.edges.get(end).add(new Edge(currentNodeIndex, Edge.E_EDGE));
		}

		this.edges.put(currentNodeIndex, new ArrayList<Edge>());
		for (Integer start : nfa.start) {
			this.edges.get(currentNodeIndex).add(new Edge(start, Edge.E_EDGE));
		}

		this.edges.putAll(nfa.edges);
		this.end = nfa.end;

		return currentNodeIndex + 1;
	}

	public int concat(char c, int currentNodeIndex) {
		if (!isEmpty()) {
			//    (s) -> ... -> [e]     (c)
			// => (s) -> ... -> (e)  -> [c]
			for (int end : this.end) {
				// Connect all the old endings to new ending
				if (this.edges.containsKey(end)) {
					this.edges.get(end).add(new Edge(currentNodeIndex, c));
				} else {
					List<Edge> e = new ArrayList<Edge>(Arrays.asList(new Edge(currentNodeIndex, c)));
					this.edges.put(end, e);
				}
			}
			this.end.clear();
			this.end.add(currentNodeIndex);
			// New ending node
			return currentNodeIndex + 1;
		} else {
			// e => (s) -c-> [e]
			this.start.add(currentNodeIndex);
			this.end.add(currentNodeIndex + 1);
			List<Edge> e = new ArrayList<Edge>(Arrays.asList(new Edge(currentNodeIndex + 1, c)));
			this.edges.put(currentNodeIndex, e);
			// New ending and starting node
			return currentNodeIndex + 2;
		}
	}

	public int or(NFA nfa, int currentNodeIndex) {
		//   (s) -> ... -> [e] (a) -> ... -> [z]
		// =>(s') ->   ...     -> [e']
		//    |e                e |
		//    -> (a) ->...->(z) ->-
		//    |e                e |
		//    -> (s) -> ...->[e]->-
		if (nfa.isEmpty())
			return currentNodeIndex;

		this.edges.putAll(nfa.edges);
		if (!isEmpty()) {
//			this.edges.put(currentNodeIndex, new ArrayList<Edge>());
//			for (int start : this.start) {
//				this.edges.get(currentNodeIndex).add(new Edge(start, Edge.E_EDGE));
//			}
//			for (int start : nfa.start) {
//				this.edges.get(currentNodeIndex).add(new Edge(start, Edge.E_EDGE));
//			}
//			for (int end : this.end) {
//				if (!this.edges.containsKey(end))
//					this.edges.put(end, new ArrayList<Edge>());
//				this.edges.get(end).add(new Edge(currentNodeIndex + 1, Edge.E_EDGE));
//			}
//			for (int end : nfa.end) {
//				if (!this.edges.containsKey(end))
//					this.edges.put(end, new ArrayList<Edge>());
//				this.edges.get(end).add(new Edge(currentNodeIndex + 1, Edge.E_EDGE));
//			}
//			this.start.clear();
//			this.start.add(currentNodeIndex);
//			this.end.clear();
//			this.end.add(currentNodeIndex + 1);
//			return currentNodeIndex + 2;
			this.start.addAll(nfa.start);
			this.end.addAll(nfa.end);
			return currentNodeIndex;
		} else {
			this.start = nfa.start;
			this.end = nfa.end;
			return currentNodeIndex;
		}
	}

	public int closure(int currentNodeIndex) {
		// currentNodeIndex: the vacant index for nodes
		if (isEmpty()) {
			return currentNodeIndex;
		}
		List<Edge> e1 = new ArrayList<Edge>();
		for (int start : this.start) {
			e1.add(new Edge(start, Edge.E_EDGE));
		}
		e1.add(new Edge(currentNodeIndex + 1, Edge.E_EDGE));
		this.edges.put(currentNodeIndex, e1);

		for (Integer end : this.end) {
			List<Edge> e2 = new ArrayList<Edge>();
			e2.add(new Edge(currentNodeIndex + 1, Edge.E_EDGE));
			if (this.edges.containsKey(this.end))
				e2.addAll(this.edges.get(this.end));
			this.edges.put(end, e2);
		}

		List<Edge> e3 = new ArrayList<Edge>();
		e3.add(new Edge(currentNodeIndex, Edge.E_EDGE));
		this.edges.put(currentNodeIndex + 1, e3);

		this.start.clear();
		this.start.add(currentNodeIndex);
		this.end.clear();
		this.end.add(currentNodeIndex + 1);

		return currentNodeIndex + 2;
	}

	public void clear() {
		this.start.clear();
		this.end.clear();
		this.edges.clear();
	}

	public boolean isEmpty() {
		return this.edges.isEmpty();
	}

	public int convertRE2NFA(RegexExpression regex, int nodeIndex) throws REException {
		this.minNodeIndex = nodeIndex;
		int currentNodeIndex = nodeIndex;
		String re = regex.getRe();

		Stack<Character> opStack = new Stack<Character>();
		Stack<NFA> nfaStack = new Stack<NFA>();
		boolean isEscaped = false;
		int lastORIndex = -1;

		nfaStack.push(new NFA());

		for (int i = 0; i < re.length(); i++) {
			switch (re.charAt(i)) {
				case '|': {
					if (isEscaped) {
						currentNodeIndex = nfaStack.peek().concat('|', currentNodeIndex);
						isEscaped = false;
					} else {
						lastORIndex = i;
						opStack.push('|');
						nfaStack.push(new NFA());
					}
					break;
				}
				case '(': {
					if (isEscaped) {
						currentNodeIndex = nfaStack.peek().concat('(', currentNodeIndex);
						isEscaped = false;
					} else {
						if (i - 1 != lastORIndex) {
							opStack.push('+');
							nfaStack.push(new NFA());
						}
						opStack.push('(');
					}
					break;
				}
				case ')': {
					if (isEscaped) {
						currentNodeIndex = nfaStack.peek().concat(')', currentNodeIndex);
						isEscaped = false;
					} else if (!opStack.empty()) {
						char op;
						while (!opStack.empty() && (op = opStack.pop()) != '(') {
							switch (op) {
								case '|': {
									if (nfaStack.size() < 2)
										throw new REException(REException.InvalidRE + re);

									NFA o1 = nfaStack.pop();
									NFA o2 = nfaStack.pop();
									currentNodeIndex = o2.or(o1, currentNodeIndex);
									nfaStack.push(o2);
									break;
								}
								default: {
									throw new REException(REException.InvalidRE + re);
								}
							}
						}
						if (i < re.length() - 1 && re.charAt(i + 1) == '*') {
							currentNodeIndex = nfaStack.peek().closure(currentNodeIndex);
							i++;
						}
						if (!opStack.empty() && opStack.peek() == '+') {
							NFA o1 = nfaStack.pop();
							NFA o2 = nfaStack.pop();
							currentNodeIndex = o2.concat(o1, currentNodeIndex);
							nfaStack.push(o2);
							opStack.pop();
						}
					} else
						throw new REException(REException.MismatchedBrackets + re);

					break;
				}
				case '\\': {
					if (isEscaped) {
						currentNodeIndex = nfaStack.peek().concat('\\', currentNodeIndex);
						isEscaped = false;
					} else
						isEscaped = true;

					break;
				}
				case '*': {
					if (isEscaped) {
						currentNodeIndex = nfaStack.peek().concat('*', currentNodeIndex);
						isEscaped = false;
					} else if (nfaStack.empty())
						throw new REException(REException.InvalidRE + re);
					else
						currentNodeIndex = nfaStack.peek().closure(currentNodeIndex);

					break;
				}
				default: {
					char c = re.charAt(i);
					if (isEscaped) {
						switch (re.charAt(i)) {
							case 'b': {
								c = '\b';
								break;
							}
							case 'f': {
								c = '\f';
								break;
							}
							case 'r': {
								c = '\r';
								break;
							}
							case 'n': {
								c = '\n';
								break;
							}
							case 't': {
								c = '\t';
								break;
							}
							case '\'': {
								c = '\'';
								break;
							}
							case '\"': {
								c = '\"';
								break;
							}
							case '[': {
								c = '[';
								break;
							}
							default: {
								throw new REException(REException.IllegalEscape + re);
							}
						}
						isEscaped = false;
					}

					if (i + 1 < re.length() && re.charAt(i + 1) == '*') {
						NFA t = new NFA();
						currentNodeIndex = t.concat(c, currentNodeIndex);
						currentNodeIndex = t.closure(currentNodeIndex);
						currentNodeIndex = nfaStack.peek().concat(t, currentNodeIndex);
						i++;
					} else {
						currentNodeIndex = nfaStack.peek().concat(c, currentNodeIndex);
					}
					break;
				}
			} // switch
		} // for...

		while (!opStack.empty()) {
			char c = opStack.pop();
			if (c == '|') {
				if (nfaStack.size() < 2) {
					throw new REException(REException.InvalidRE + re);
				}

				NFA o1 = nfaStack.pop();
				NFA o2 = nfaStack.pop();
				currentNodeIndex = o2.or(o1, currentNodeIndex);
				nfaStack.push(o2);
			} else
				throw new REException(REException.InvalidRE + re);
		}

		NFA res = nfaStack.pop();
		this.edges = res.edges;
		this.start = res.start;
		this.end = res.end;

		return currentNodeIndex;
	}

	public static NFA mergeAll(ArrayList<NFA> targets) {
		NFA res = new NFA();
		int index = 0;
		res.endMapper = new HashMap<Integer, Integer>();
		for (NFA target : targets) {
			res.start.addAll(target.start);
			res.end.addAll(target.end);
			res.edges.putAll(target.edges);
			// Map ending node i -> result index
			for (Integer i : target.end) {
				res.endMapper.put(i, index);
			}
			index++;
		}
		return res;
	}
}
