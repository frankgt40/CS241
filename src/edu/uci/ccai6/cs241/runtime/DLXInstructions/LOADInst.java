package edu.uci.ccai6.cs241.runtime.DLXInstructions;

import edu.uci.ccai6.cs241.runtime.Conf;
import edu.uci.ccai6.cs241.ssa.Arg;
import edu.uci.ccai6.cs241.ssa.Instruction;

public class LOADInst extends DLXInstruction {
	public LOADInst(Instruction instruction) {
		Arg arg1, arg2;
		arg1 = instruction.arg0;
		arg2 = instruction.arg1;
		__val = DLX.F1(DLX.LDX, getRegNum(arg2.toString()), getRegNum(Conf.ZERO_REG), getRegNum(arg1.toString()));
		bellowValAssig(instruction);
		return;
	}
}
