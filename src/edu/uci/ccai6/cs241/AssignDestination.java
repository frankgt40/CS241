package edu.uci.ccai6.cs241;

public class AssignDestination {
	private boolean __isArray = false;
	private boolean __isConstant = false;
	private String __destination = "";
	
	public AssignDestination(String str) {
		__destination = str;
	}
	
	public void setDestination(String str) {
		__destination = str;
	}
	public String getDestination() {
		return __destination;
	}
	public void setIsArray(boolean isTure) {
		__isArray = isTure;
	}
	public boolean isArray() {
		return __isArray;
	}
	public void setIsConstant(boolean isTrue) {
		__isConstant = isTrue;
	}
	public boolean isConstant() {
		return __isConstant;
	}
}
