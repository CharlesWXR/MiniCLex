package cn.edu.njnu.minic.fa;

public class Edge {
	protected int end;
	protected char data;

	public static final char E_EDGE = (char)-1;

	public Edge(int end, char data) {
		this.end = end;
		this.data = data;
	}

	public int getEnd() {
		return end;
	}

	public void setEnd(int end) {
		this.end = end;
	}

	public char getData() {
		return data;
	}

	public void setData(char data) {
		this.data = data;
	}

	@Override
	public String toString() {
		return "Edge{" +
				"end=" + end +
				", data=" + data +
				'}';
	}

	public boolean isEEdge() {
		return this.data == E_EDGE;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof Edge))
			return false;
		Edge o = (Edge)obj;
		return this.data == o.data && (int)this.end == (int)o.end;
	}

	@Override
	public int hashCode() {
		return data * end ^ (data + end);
	}
}
