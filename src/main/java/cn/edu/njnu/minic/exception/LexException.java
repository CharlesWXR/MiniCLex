package cn.edu.njnu.minic.exception;

public class LexException extends Exception {
	public static final String IllegalComponent = "Illegal component: ";
	public static final String MismatchedComponent = "Mismatched component";
	public static final String MissingConfiguration = "Missing configuration annotation on invoker: ";
	public static final String MissingEndingSign = "Missing ending sign: ";

	public LexException(String message) {
		super(message);
	}
}
