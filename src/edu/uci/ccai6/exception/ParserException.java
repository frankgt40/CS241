package edu.uci.ccai6.exception;

public class ParserException extends Exception {
	
	public ParserException() { super("invalid syntax"); }
	public ParserException(String message) { super("invalid syntax: "+message); }
	public ParserException(String expected, String result) {
		super("Expect: "+expected+" but it is "+result);
	}
	public ParserException(String parserFunctionName, String expected, String result) {
		super(parserFunctionName+": Expect "+expected+" but it is "+result);
	}

}
