package cn.edu.njnu.minic.fa;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FA {
	protected List<Integer> start = new ArrayList<Integer>();
	protected List<Integer> end = new ArrayList<Integer>();
	protected Map<Integer, List<Edge>> edges = new HashMap<Integer, List<Edge>>();

	public List<Integer> getStart() {
		return start;
	}

	public void setStart(List<Integer> start) {
		this.start = start;
	}

	public List<Integer> getEnd() {
		return end;
	}

	public void setEnd(List<Integer> end) {
		this.end = end;
	}

	public Map<Integer, List<Edge>> getEdges() {
		return edges;
	}

	public void setEdges(Map<Integer, List<Edge>> edges) {
		this.edges = edges;
	}
}
