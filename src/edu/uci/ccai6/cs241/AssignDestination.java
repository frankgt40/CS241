package edu.uci.ccai6.cs241;

public class AssignDestination {
	private boolean __isArray = false;
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
}
