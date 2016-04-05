package edu.uci.ccai6.cs241.runtime;

public class Local {
	public String __name = "";
	public LocalType __type = LocalType.NONE;
	public int __offset = -1;
	public int __len = -1;
	public boolean __isStored = false;
	public boolean isStored() {
		return __isStored;
	}
	public void setIsStored(boolean isStored) {
		__isStored = isStored;
	}
}
