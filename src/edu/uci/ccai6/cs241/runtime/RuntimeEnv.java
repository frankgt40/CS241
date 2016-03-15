package edu.uci.ccai6.cs241.runtime;

import java.util.ArrayList;
import java.util.List;

import edu.uci.ccai6.cs241.ssa.Instruction;

public class RuntimeEnv {
	private StackAbstract __stack = new StackAbstract();

	
	public List<DLXInstruction> genCode(List<Instruction> instructions) {
		List<DLXInstruction> rsl = new ArrayList<DLXInstruction>();
		for (Instruction instruction : instructions) {
			rsl.add(new DLXInstruction(instruction));
		}
		return null;
	}
}
