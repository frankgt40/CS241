package edu.uci.ccai6.cs241;

public class AssignDestination {
	private boolean __isArray = false;
	private boolean __isConstant = false;
	private boolean __isPointer = false;
	private String __destination = "";
	private String __relOp = null;
	
	public AssignDestination(String str) {
		__destination = str;
	}
	
	public AssignDestination(long pc) {
		__destination = "("+Long.toString(pc)+")";
		__isPointer = true;
	}
	
	public void setDestination(String str) {
		__destination = str;
	}
	
	public void setRelOp(String ro) {
		__relOp = ro;
	}
	
	public String getRelOp() {
		return __relOp;
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
	public boolean isPointer() {
		return __isPointer;
	}
}
