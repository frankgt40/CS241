package edu.uci.ccai6.cs241.ssa;

public class Instruction {
	
	public enum Operation {
		FUNC,
		NONE,
		LOAD, STORE,
		ADD, ADDi, MUL, MULi, DIV, DIVi,
		MOVE,
		BGE, BGT, BLE, BLT, BNE, BEQ,
		BRA,
		CMP,
		PHI;
		
		public boolean isBranch() {
			return isCondJump() || this == BRA;
		}
		
		public boolean isCondJump() {
			return this == BGE || this == BGT 
					|| this == BLE || this == BLT
					|| this == BNE || this == BEQ;
		}
	}

	Arg pointer;
	public Operation op;
	Arg arg0, arg1, arg2;
	String funcName;
	int numArgs;
	
	public Instruction(String str) {
		String[] splited = str.split("\\s+");
		pointer = new Arg("("+splited[0]+")");
		try {
			op = Operation.valueOf(splited[1]);
		} catch(Exception e) {
			if(splited[1].charAt(splited[1].length()-1) == ':') { 
				op = Operation.FUNC;
				funcName = splited[1];
			} else op = Operation.NONE;
			
		}
		
		numArgs = splited.length-2;
		switch(numArgs) {
		case 3:
			arg2 = new Arg(splited[4]);
		case 2:
			arg1 = new Arg(splited[3]);
		case 1:
			arg0 = new Arg(splited[2]);
		}
	}
	
	public String toString() {
		String out = pointer+" ";
		out += op+" ";
		if(arg0 != null) out += arg0+" ";
		if(arg1 != null) out += arg1+" ";
		if(arg2 != null) out += arg2+" ";
		return out;
	}
	
}
