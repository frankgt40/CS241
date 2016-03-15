package edu.uci.ccai6.cs241.runtime;

import java.util.ArrayList;
import java.util.List;

import edu.uci.ccai6.cs241.ssa.Instruction;

public class RuntimeEnv {
	private static StackAbstract __stack = new StackAbstract();

	
	public static List<DLXInstruction> genCode(List<Instruction> instructions) {
		List<DLXInstruction> rsl = null;
		for (Instruction instruction : instructions) {
			new DLXInstruction(instruction);
		}
		rsl = DLXInstruction.getInstructions();
		return rsl;
	}


	public static StackAbstract getStack() {
		return __stack;
	}

	
}
