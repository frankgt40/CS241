package edu.uci.ccai6.cs241.runtime.DLXInstructions;

import edu.uci.ccai6.cs241.ssa.Instruction;

public class NOOPInst extends DLXInstruction {
	public NOOPInst(Instruction instruction) {
		__val = noopInst();
		bellowValAssig(instruction);
	}

}
