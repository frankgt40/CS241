package edu.uci.ccai6.cs241.runtime.DLXInstructions;

import edu.uci.ccai6.cs241.ssa.Instruction;

public class STOREInst extends DLXInstruction {
	public STOREInst(Instruction instruction) {
		System.out.println("STORE COMMAND: " + instruction.toSimpleString());
	}
}
