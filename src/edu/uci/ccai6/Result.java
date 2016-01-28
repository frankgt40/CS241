package edu.uci.ccai6;

public class Result {
	
	public enum Type {
		CONSTANT, VAR, POINTER;
	}
	
	public enum RelOp {
		BNE("bne"), BEQ("beq"),
		BLE("ble"), BLT("blt"),
		BGE("bge"), BGT("bgt");
		
		String name = null;
		
		RelOp(String name) {
			this.name = name;
		}
		
		public String toString() {
			return name;
		}
	}

	Type type;
	int val = -1;
	String varName = null;
	InstPointer pointer;
	boolean isArray = false;
	RelOp relOp = null;
	
	public Result(int num) {
		val = num;
		type = Type.CONSTANT;
	}
	
	public void setRelOp(RelOp ro) {
		relOp = ro;
	}
	
	public void rename(String nn) throws Exception {
		if(type != Type.VAR) {
			System.out.println("cant rename non-var type");
			return;
		}
		varName = nn;
	}
	
	public boolean isPointer() {
		return (type == Type.POINTER);
	}
	
	public InstPointer getPointer() {
		if(type != Type.POINTER) {
			System.err.println("cant get pointer for non-pointer type");
			return null;
		}
		return pointer;
	}
	
	public Result(String vn) {
		varName = vn;
		type = Type.VAR;
	}
	
	public Result(InstPointer ip) {
		pointer = new InstPointer(ip);
		type = Type.POINTER;
	}
	
	public boolean isVar() {
		return varName != null;
	}
	
	public void setArray() {
		isArray = true;
	}
	
	public String toString() {
		if(type == Type.CONSTANT) return Integer.toString(val);
		if(type == Type.VAR) return varName;
		if(type == Type.POINTER) return pointer.toString();
		return null;
	}
}
