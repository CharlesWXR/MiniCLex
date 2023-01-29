package cn.edu.njnu.minic.exception;

public class REException extends Exception {
	public static final String MismatchedBrackets = "Mismatched brackets encountered: ";
	public static final String InvalidRE = "Invalid regex expression: ";
	public static final String IllegalEscape = "Illegal escape character: ";

	public REException(String message) {
		super(message);
	}
}
