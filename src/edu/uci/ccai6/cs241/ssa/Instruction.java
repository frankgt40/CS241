package edu.uci.ccai6.cs241.ssa;

import javax.swing.plaf.basic.BasicInternalFrameTitlePane.MoveAction;

public class Instruction {
	
	public enum Operation {
		FUNC,
		LOAD, STORE,
		ADD, ADDi, MUL, MULi, DIV, DIVi, SUB, SUBi,
		ADDA,
		MOVE,
		BGE, BGT, BLE, BLT, BNE, BEQ,
		BRA,
		CMP,
		PHI,
		
		PUSH, POP,
		
		CALL,
		
		RET,
		
		MOV,
		
		NOOP,
		PTR // means its value is the same as pointer's or constant
		
		;
	  
	    public boolean isFuncCall() {
	      return this == CALL;
	    }
		
		public boolean isBranch() {
			return isCondJump() || this == BRA;
		}
		
		public boolean isCondJump() {
			return this == BGE || this == BGT 
					|| this == BLE || this == BLT
					|| this == BNE || this == BEQ;
		}
		
		public boolean hasOutput() {
		  return this != STORE && !this.isBranch() && this != PUSH && this != MOV
		      && this != NOOP && this != PTR && this != FUNC && this != POP
		      && this != RET;
		}
		
		public boolean canBeNoop() {
			return this != Operation.CMP && this != Operation.CALL && this != Operation.FUNC
					&& this != Operation.LOAD && this != Operation.STORE
					&& this != Operation.PUSH && this != Operation.POP
					&& !this.isBranch() && this != Operation.MOV && this != Operation.RET;
		}
	}

	public PointerArg pointer;
	public Operation op;
	public Arg arg0, arg1, arg2;
	public String funcName;
	int numArgs;
	
	public void setArg(int index, Arg value) {
		switch(index) {
		case 1:
			arg0 = value;
			break;
		case 2: 
			arg1 = value;
			break;
		case 3:
			arg2 = value;
			break;
		default:
			System.err.println("setArg: sth wrong!");
			break;
		}
	}
	public Instruction(Instruction two) {
		pointer = two.pointer;
		op = two.op;
		arg0 = two.arg0;
		arg1 = two.arg1;
		arg2 = two.arg2;
		funcName = two.funcName;
		numArgs = two.numArgs;
	}
	
	public Instruction(String str) {
		String[] splited = str.split("\\s+");
		pointer = (PointerArg) Arg.create("("+splited[0]+")");
		try {
			op = Operation.valueOf(splited[1]);
		} catch(Exception e) {
			if(splited[1].charAt(splited[1].length()-1) == ':') { 
				op = Operation.FUNC;
				funcName = splited[1].substring(0, splited[1].length()-1);
			} else op = Operation.NOOP;
			
		}
		
		numArgs = splited.length-2;
		switch(numArgs) {
		case 3:
			arg2 = Arg.create(splited[4]);
		case 2:
			arg1 = Arg.create(splited[3]);
		case 1:
			arg0 = Arg.create(splited[2]);
		}
	}
	
	public boolean skipOptimize() {
	  return (op == Operation.LOAD) || (op == Operation.CALL) || (op == Operation.FUNC)
	      || (op == Operation.STORE) || (arg0 != null && arg0 instanceof RegisterArg)
	      || (arg1 != null && arg1 instanceof RegisterArg) || (arg2 != null && arg2 instanceof RegisterArg);
	}
	
	public boolean skipCopyProp() {
		  return  (op == Operation.CALL) || (op == Operation.FUNC);
	}
	
	public String toString() {
		String out = pointer+" ";
		out += op+" ";
		if(op == Operation.FUNC) out += funcName+" ";
		if(arg0 != null) out += arg0+" ";
		if(arg1 != null) out += arg1+" ";
		if(arg2 != null) out += arg2+" ";
		return out;
	}
	
	/**
	 * 
	 * used this in CSE to find duplicate instructions
	 * @return toString but w/o pointer at the beginning
	 */
	public String toSimpleString() {
		String out = op+" ";
		if(op == Operation.FUNC) out += funcName+" ";
		if(arg0 != null) out += arg0+" ";
		if(arg1 != null) out += arg1+" ";
		if(arg2 != null) out += arg2+" ";
		return out;
		
	}
	
	public int hashCodeWoPointer() {
		int hash = 7;
//		hash = 5*hash + pointer.hashCode();
		hash = 31*hash + (funcName == null ? 0 : funcName.hashCode());
		hash = 5*hash + (arg0 == null ? 0 : arg0.hashCode());
		hash = 5*hash + (arg1 == null ? 0 : arg1.hashCode());
		hash = 5*hash + (arg2 == null ? 0 : arg2.hashCode());
		hash = 17*hash + op.hashCode();
		return hash;
	}
	
}
