package edu.uci.ccai6.cs241.ssa;

public class Arg {

	public enum Type {
		CONSTANT, POINTER, VARIABLE
	}
	
	int num;
	int pointer;
	String var;
	Type type;
	
	public Arg(Arg a2) {
		num = a2.num;
		pointer = a2.pointer;
		var = a2.var;
		type = a2.type;
	}
	
	public Arg(String str) {
		if(str.matches("^\\d+$")) {
			num = Integer.parseInt(str);
			type = Type.CONSTANT;
		} else if(str.charAt(0) == '(') {
			String pointerStr = str.substring(1, str.length()-1);
			if(!pointerStr.matches("^\\d+$")) {
				System.err.println(str+" : is not a digit");
				System.exit(-1);
			}
			pointer = Integer.parseInt(pointerStr);
			type = Type.POINTER;
		} else {
			var = str;
			type = Type.VARIABLE;
		}
	}
	
	public void setPointer(int p) {
		if(type == Type.POINTER)
			pointer = p;
	}
	
	public String toString() {
		switch(type) {
		case CONSTANT:
			return num+"";
		case POINTER:
			return "("+pointer+")";
		case VARIABLE:
			return var;
		}
		return null;
	}
	
	@Override
	public boolean equals(Object o) {
		if(o == null || !(o instanceof Arg)) return false;
		Arg two = (Arg) o;
		if(two.type != type) return false;
		switch(type) {
		case CONSTANT:
			return two.num == num;
		case POINTER:
			return two.pointer == pointer;
		case VARIABLE:
			return two.var.equals(var);
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		int hash = 1;
		hash = hash*17+num;
		hash = hash*17+pointer;
		hash = hash*31+(var == null ? 0 : var.hashCode());
		hash = hash*4+type.hashCode();
		return hash;
	}
}
