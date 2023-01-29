package cn.edu.njnu.minic.lex;

public class LexElement {
	private Object data;
	private LexElementEnum type;

	@Override
	public String toString() {
		return "LexElement{" +
				"data=" + data +
				", type=" + type +
				'}';
	}

	public LexElement() {
	}

	public LexElement(Object data, LexElementEnum type) {
		this.data = data;
		this.type = type;
	}

	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}

	public LexElementEnum getType() {
		return type;
	}

	public void setType(LexElementEnum type) {
		this.type = type;
	}
}
