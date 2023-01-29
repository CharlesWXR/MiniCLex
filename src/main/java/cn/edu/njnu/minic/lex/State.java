package cn.edu.njnu.minic.lex;

public interface State {
	State next(char word);

	Object execute(String _content) throws Exception;
}
