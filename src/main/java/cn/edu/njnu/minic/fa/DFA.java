package cn.edu.njnu.minic.fa;

import cn.edu.njnu.minic.utils.ArrayUtils;

import java.util.*;
import java.util.stream.Collectors;

public class DFA extends FA {
	private boolean isSimplified = false;
	private Map<Integer, List<Integer>> endMapper = new HashMap<Integer, List<Integer>>();

	public Map<Integer, List<Integer>> getEndMapper() {
		return endMapper;
	}

	public DFA() {
	}

	public DFA(NFA nfa) {
		convertNFA2DFA(nfa);
	}

	@Override
	public String toString() {
		return "DFA{" +
				"isSimplified=" + isSimplified +
				", endMapper=" + endMapper +
				", start=" + start +
				", end=" + end +
				", edges=" + edges +
				'}';
	}

	public boolean isEmpty() {
		return this.edges.size() == 0;
	}

	private ArrayList<Integer> getEClosure(NFA nfa, ArrayList<Integer> nodes) {
		// Get the E-Closure for the target nodes and sort them increasingly
		Map<Integer, List<Edge>> edges = nfa.getEdges();

		for (int i = 0; i < nodes.size(); i++) {
			if (!edges.containsKey(nodes.get(i)))
				continue;

			List<Edge> ed = edges.get(nodes.get(i));
			for (Edge e : ed) {
				if (e.isEEdge() && !nodes.contains(e.getEnd())) {
					nodes.add(e.getEnd());
				}
			}
		}

		Collections.sort(nodes, new Comparator<Integer>() {
			public int compare(Integer o1, Integer o2) {
				return o1 - o2;
			}
		});

		return nodes;
	}

	public void convertNFA2DFA(NFA nfa) {
		this.isSimplified = false;

		// Scanned new nodes
		ArrayList<ArrayList<Integer>> newNodes = new ArrayList<ArrayList<Integer>>();
		// Hash code for new nodes to simplify comparison
		ArrayList<Integer> hash = new ArrayList<Integer>();
		// Old edges in original nfa
		Map<Integer, List<Edge>> oldEdges = nfa.getEdges();

		// Generate the beginning state
		ArrayList<Integer> start = new ArrayList<Integer>();
		start.addAll(nfa.getStart());
		hash.add(ArrayUtils.getHashCode(start));
		newNodes.add(getEClosure(nfa, start));
		// Mind the index shift "+ nfa.getMinNodeIndex()" to avoid node conflict
		this.start.add(0 + nfa.getMinNodeIndex());
		boolean startAdded = false;
		for (Integer end : nfa.getEnd()) {
			if (start.contains(end)) {
				int shiftedIndex = 0 + nfa.getMinNodeIndex();
				if (!startAdded) {
					this.end.add(shiftedIndex);
					startAdded = true;
				}
				// Update end mapper, merge endStates to one node
				if (nfa.getEndMapper() != null) {
					if (!this.endMapper.containsKey(shiftedIndex))
						this.endMapper.put(shiftedIndex, new ArrayList<Integer>());
					int endState = nfa.getEndMapper().get(end);
					if (!this.endMapper.get(shiftedIndex).contains(endState))
						this.endMapper.get(shiftedIndex).add(endState);
				}
			}
		}

		// Loop until every new nodes are iterated and no new nodes generated
		for (int presentIndex = 0; presentIndex < newNodes.size(); presentIndex++) {
			ArrayList<Integer> nodes = newNodes.get(presentIndex);

			// Expand the nodes (the right side of the table)
			Map<Character, ArrayList<Integer>> dest = new HashMap<Character, ArrayList<Integer>>();
			for (int i = 0; i < nodes.size(); i++) {
				// Iterate every node except nodes connected with e edges
				if (oldEdges.containsKey(nodes.get(i))) {
					for (Edge e : oldEdges.get(nodes.get(i))) {
						// Skip e edges
						if (e.getData() == Edge.E_EDGE)
							continue;

						// Add not empty dest nodes into map in category of edge data
						if (dest.containsKey(e.getData())) {
							if (!dest.get(e.getData()).contains(e.getEnd())) {
								dest.get(e.getData()).add(e.getEnd());
							}
						} else {
							ArrayList<Integer> t = new ArrayList<Integer>(Arrays.asList(e.getEnd()));
							dest.put(e.getData(), t);
						}
					}
				}
			}

			for (Map.Entry<Character, ArrayList<Integer>> entry : dest.entrySet()) {
				// Add the expanded states into the state collection
				ArrayList<Integer> eClosure = getEClosure(nfa, entry.getValue());
				int i = ArrayUtils.contains(eClosure, newNodes, hash);
				if (i == newNodes.size()) {
					// Failed to find the existing array, add new one
					newNodes.add(eClosure);
					hash.add(ArrayUtils.getHashCode(eClosure));
					// Update ending nodes if contains original ending nodes
					Map<Integer, Integer> endMapper = nfa.getEndMapper();
					boolean added = false;
					for (Integer end : nfa.getEnd()) {
						if (eClosure.contains(end)) {
							// Add end nodes when contains old ending states
							// Mind the index shift "+ nfa.getMinNodeIndex()" in case of node conflict
							int shiftedIndex = i + nfa.getMinNodeIndex();
							if (!added) {
								this.end.add(shiftedIndex);
								added = true;
							}

							// Update end mapper, merge endStates to one node
							if (endMapper != null) {
								if (!this.endMapper.containsKey(shiftedIndex))
									this.endMapper.put(shiftedIndex, new ArrayList<Integer>());
								int endState = endMapper.get(end);
								if (!this.endMapper.get(shiftedIndex).contains(endState))
									this.endMapper.get(shiftedIndex).add(endState);
							}
						}
					}
				}
				// When found, i = targetIndex; When missing, i = oldCollectionSize = newIndex
				// Mind the index shift "+ nfa.getMinNodeIndex()" in case of node conflict
				if (!this.edges.containsKey(presentIndex + nfa.getMinNodeIndex())) {
					this.edges.put(presentIndex + nfa.getMinNodeIndex(), new ArrayList<Edge>());
				}
				this.edges.get(presentIndex + nfa.getMinNodeIndex()).add(new Edge(i + nfa.getMinNodeIndex(), entry.getKey()));
			} // for iterating the dest map
		} // for iterating the nodes collection

		simplify();
	}

	public void simplify() {
		if (this.isSimplified || this.end.size() == 0)
			return;

		// Generate the node set containing all the nodes
		Map<Character, ArrayList<SimplifyEdge>> categoryEdges = new HashMap<Character, ArrayList<SimplifyEdge>>();
		Set<Integer> nodeSet = new HashSet<Integer>();
		for (Map.Entry<Integer, List<Edge>> entry : this.edges.entrySet()) {
			for (Edge e : entry.getValue()) {
				if (!categoryEdges.containsKey(e.getData())) {
					categoryEdges.put(e.getData(), new ArrayList<SimplifyEdge>());
				}
				categoryEdges.get(e.getData()).add(new SimplifyEdge(entry.getKey(), e.getEnd()));
				nodeSet.add(e.getEnd());
			}
			nodeSet.add(entry.getKey());
		}

		List<Integer> nodes = new ArrayList(nodeSet);
		List<List<Integer>> edgeSets = new ArrayList<List<Integer>>();
		edgeSets.add(this.end);
		List<Integer> diff = ArrayUtils.getDiff(nodes, this.end);
		if (diff.size() > 0)
			edgeSets.add(diff);

		boolean changed = true;
		// Loop until no change took place
		L:
		while (changed) {
			changed = false;
			for (int index = 0; index < edgeSets.size(); index++) {
				for (Map.Entry<Character, ArrayList<SimplifyEdge>> entry : categoryEdges.entrySet()) {
					ArrayList<SimplifyEdge> e = entry.getValue();
					// Generate the projection of nodes on sets
					List<Integer> divided = edgeSets.get(index)
							.stream()
							.map(i -> {
								for (SimplifyEdge edge : e) {
									if (edge.start == i) {
										for (int j = 0; j < edgeSets.size(); j++) {
											if (edgeSets.get(j).contains(edge.end))
												return j;
										}
										break;
									}
								}
								return -1;
							})
							.collect(Collectors.toList());

					// Decompose the set into sets of different nodes endings
					List<Integer> set = edgeSets.get(index);
					// Indicate if the set was broken into 2
					int flag = 0;
					while (set.size() > 0) {
						flag++;
						// Select all nodes pointing to the same target with 0th node
						// and divide the set
						int tar = divided.get(0);
						List<Integer> newSet = new ArrayList<Integer>();
						for (int i = 0; i < divided.size(); i++) {
							if (divided.get(i) == tar) {
								newSet.add(set.get(i));
							}
						}
						edgeSets.add(newSet);
						divided = divided.stream()
								.filter(i -> i != tar)
								.collect(Collectors.toList());
						set = ArrayUtils.getDiff(set, newSet);
					}

					if (flag > 1) {
						// A set was broken into 2, start a new round to test
						edgeSets.remove(index);
						changed = true;
						continue L;
					} else {
						edgeSets.remove(edgeSets.size() - 1);
					}
				}
			}

			// Merge elements in one set
			for (List<Integer> set : edgeSets) {
				int tar = set.get(0);
				List<Edge> temp = new ArrayList<Edge>();
				for (int i = 1; i < set.size(); i++) {
					int sub = set.get(i);
					// Substitute edges' ending nodes to tar
					for (Map.Entry<Integer, List<Edge>> entry : this.edges.entrySet()) {
						boolean c = false;
						for (Edge edge : entry.getValue()) {
							if (edge.getEnd() == sub) {
								c = true;
								edge.setEnd(tar);
							}
						}
						// Drop duplicated edges
						if (c) {
							entry.setValue(
									entry.getValue()
											.stream()
											.distinct()
											.collect(Collectors.toList())
							);
						}
					}
					for (Edge e : temp) {
						if (e.getEnd() == sub)
							e.setEnd(tar);
					}

					// Substitute start node
					if (this.start.contains(tar)) {
						// If already contains the target node, remove the other substitutions
						this.start = this.start.stream().filter(e -> e != sub).collect(Collectors.toList());
					} else {
						// If no target node included, replace the substitution with target
						for (int j = 0; j < this.start.size(); j++)
							if (this.start.get(j) == sub) {
								this.start.set(j, tar);
								break;
							}
					}

					// Substitute end node
					if (this.end.contains(tar)) {
						// If already contains the target node, remove the other substitutions
						this.end = this.end.stream().filter(e -> e != sub).collect(Collectors.toList());
						// Merge the endState of substitution with target node
						if (this.endMapper.containsKey(sub)) {
							List<Integer> t = this.endMapper.get(tar);
							for (Integer e : this.endMapper.get(sub))
								if (!t.contains(e))
									t.add(e);
							this.endMapper.remove(sub);
						}
					} else {
						// If the target node wasn't in the set, replace the sub with tar
						for (int j = 0; j < this.end.size(); j++)
							if (this.end.get(j) == sub) {
								this.end.set(j, tar);
								this.endMapper.put(tar, this.endMapper.get(sub));
								this.endMapper.remove(sub);
								break;
							}
					}

					// Substitute edges' starting nodes by merging it with each other
					if (this.edges.containsKey(set.get(i))) {
						List<Edge> edges = this.edges.get(sub);
						this.edges.remove(sub);
						temp = ArrayUtils.mergeSet(edges, temp);
					}
				}
				// Merge all other nodes with tar to substitute the starting node
				temp = ArrayUtils.mergeSet(temp, this.edges.get(tar));
				this.edges.replace(tar, temp);
			} // for
		} // while

		this.isSimplified = true;
	}

	public static NFA mergeAll(ArrayList<DFA> targets) {
		// Merge all the DFAs into one
		NFA res = new NFA();
		int index = 0;
		res.setEndMapper(new HashMap<Integer, Integer>());
		for (DFA target : targets) {
			res.start.addAll(target.start);
			res.end.addAll(target.end);
			res.edges.putAll(target.edges);
			// Map ending node i -> result index
			for (Integer i : target.end) {
				res.getEndMapper().put(i, index);
			}
			index++;
		}
		return res;
	}
}

class SimplifyEdge {
	public int start, end;

	public SimplifyEdge(int start, int end) {
		this.start = start;
		this.end = end;
	}
}