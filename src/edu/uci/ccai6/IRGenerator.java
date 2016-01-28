package edu.uci.ccai6;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.uci.ccai6.Result.RelOp;

public class IRGenerator {
	
	List<IRInstruction> instructions = new ArrayList<IRInstruction>();
	Map<String, Integer> allVars = new HashMap<String, Integer>();
	
	public String getVarName(String varName, boolean assign) {
		int count = allVars.containsKey(varName) ? allVars.get(varName) : 0;
		if(assign)  {
			count++;
			allVars.put(varName, count);
		}
		return varName+"_"+count;
	}

	public void print() {
		for(IRInstruction i : instructions) {
			i.print();
		}
	}
	public void fix(String op, Result arg0, Result arg1, InstPointer pointer) {
		instructions.set(pointer.instLine, new IRInstruction(op, arg0, arg1, pointer));
	}
	
	public Result addInstruction(String op, Result arg0, Result arg1, InstPointer currentPointer) {
		// very bad code .... only used for showing quick results
		String opString = op;
		Result result;
		// first check if arg0 and arg1 are vars
		if(arg1 != null && arg1.isVar() && !arg1.isArray) {
			try {
				arg1.rename(getVarName(arg1.varName, false));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if(arg0.isVar() && !arg0.isArray) {
			try {
				arg0.rename(getVarName(arg0.varName, op.equals("move")));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		// check op
		if(op.equals("+")) {
			opString = "add";
		} else if(op.equals("*")) {
			opString = "mul";
		} else if(op.equals("-")) {
			opString = "sub";
		} else if(op.equals("/")) {
			opString = "div";
		} else if(op.equals("move")) {
			if(arg1.isArray) {
				instructions.add(new IRInstruction("load", arg1, null, currentPointer));
				arg1 = new Result(currentPointer);
				currentPointer.inc();
			}
			
			if(arg0.isArray) {
				instructions.add(new IRInstruction("store", arg0, arg1, currentPointer));
				currentPointer.inc();
				return new Result(currentPointer);
			}
		} else if(op.equals(">")) {
			instructions.add(new IRInstruction("cmp", arg0, arg1, currentPointer));
			result = new Result(currentPointer);
			result.setRelOp(RelOp.BLE);
			return result;
		} else if(op.equals("<")) {
			instructions.add(new IRInstruction("cmp", arg0, arg1, currentPointer));
			result = new Result(currentPointer);
			result.setRelOp(RelOp.BGE);
			return result;
		}
		
		instructions.add(new IRInstruction(opString, arg0, arg1, currentPointer));
		return new Result(currentPointer);
	}
}
