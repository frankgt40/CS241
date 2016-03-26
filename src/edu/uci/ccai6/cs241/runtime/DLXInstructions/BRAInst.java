package edu.uci.ccai6.cs241.runtime.DLXInstructions;

import edu.uci.ccai6.cs241.ssa.Instruction;

public class BRAInst extends DLXInstruction {
	public BRAInst(Instruction instruction) {
		op = DLX.JSR; // Not so sure!
		__val = DLX.F1(op, 0, 0, 0); //C is computed later
		__lateComputePos.put(__instructions.size()+1, instruction.arg0.toString());
		bellowValAssig(instruction);
		return;
	}
}
