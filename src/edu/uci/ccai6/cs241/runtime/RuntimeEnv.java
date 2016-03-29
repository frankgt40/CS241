package edu.uci.ccai6.cs241.runtime;

import java.util.ArrayList;
import java.util.List;

import edu.uci.ccai6.cs241.runtime.DLXInstructions.DLXInstruction;
import edu.uci.ccai6.cs241.ssa.Instruction;

public class RuntimeEnv {
	private static StackAbstract __stack = new StackAbstract();

	
	public static int[] genCode(List<Instruction> instructions) {
		Conf.initialize();
		for (int val : Conf.getHook()) {
			DLXInstruction i = new DLXInstruction();
			i.setVal(val);
			DLXInstruction.getInstructions().add(i);
		}
		
		int[] rsl;
		for (Instruction instruction : instructions) {
			new DLXInstruction(instruction);
		}
		rsl = DLXInstruction.getMachineCodes();
		return rsl;
	}


	public static StackAbstract getStack() {
		return __stack;
	}

	
}
