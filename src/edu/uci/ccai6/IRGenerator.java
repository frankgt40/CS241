package edu.uci.ccai6;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class IRGenerator {
	
	List<IRInstruction> instructions = new ArrayList<IRInstruction>();
	Map<String, Integer> allVars = new HashMap<String, Integer>();

	
	public Result addInstruction(String op, Result arg0, Result arg1, InstPointer currentPointer) {
		String opString = op;
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
			} else if(arg0.isVar()) {
				String key = arg0.toString();
				int count = allVars.containsKey(key) ? allVars.get(key) : 0;
				allVars.put(key, count+1);

				try {
					arg0.rename(arg0+"_"+count);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		instructions.add(new IRInstruction(opString, arg0, arg1, currentPointer));
		return new Result(currentPointer);
	}
}
