package edu.uci.ccai6.cs241.runtime.DLXInstructions;

import edu.uci.ccai6.cs241.ssa.Instruction;

public class LOADInst extends DLXInstruction {
	public LOADInst(Instruction instruction) {
		System.out.println("LOAD COMMAND: " + instruction.toSimpleString());
	}
}
