package edu.uci.ccai6.cs241;

public class Result {
	public enum Type {
		VAR,
		ARRAY
	}
	private String __firstPart = "";
	private String __fixedPart = "";
	private Type __fixedType;
	private String __rsl = "";
	public int __size = 4;
	public Result(String firstPart, Type fixedType) {
		__firstPart = firstPart;
		__fixedType = fixedType;
	}
	public String fix(String fixed, String varAddress) {
		switch (__fixedType) {
		case VAR:
			__rsl = __firstPart + " " + varAddress + ", " + fixed;
			return __rsl;
		case ARRAY:
			__rsl = fixed + ": " + __firstPart;
			return __rsl;
		}
		return null;
	}
	public void setFirstPart(String str) {
		__firstPart = str;
	}
	public String getFirstPart() {
		return __firstPart;
	}
}
