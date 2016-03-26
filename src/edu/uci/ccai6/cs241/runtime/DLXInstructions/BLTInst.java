package edu.uci.ccai6.cs241.runtime.DLXInstructions;

import edu.uci.ccai6.cs241.ssa.Instruction;

public class BLTInst extends DLXInstruction {
	public BLTInst(Instruction instruction) {
		op = DLX.BLT;
		a = getRegNum(instruction.arg0.toString());
		__val = DLX.F1(op, a, 0, 0); //C is computed later
		__lateComputePos.put(__instructions.size()+1, instruction.arg1.toString());
		bellowValAssig(instruction);
		return;
	}
}
