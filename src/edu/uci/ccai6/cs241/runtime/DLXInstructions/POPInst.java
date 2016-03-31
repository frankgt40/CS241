package edu.uci.ccai6.cs241.runtime.DLXInstructions;

import edu.uci.ccai6.cs241.runtime.Conf;
import edu.uci.ccai6.cs241.ssa.Arg;
import edu.uci.ccai6.cs241.ssa.Instruction;
import edu.uci.ccai6.cs241.ssa.RegisterArg;

public class POPInst extends DLXInstruction {
	public POPInst(Instruction instruction) {
		int op = DLX.POP, arg1 = 0, arg2 = 0, arg3 = 0;
		Arg argI1 = instruction.arg0;
		Arg argI2 = instruction.arg1;
		Arg argI3 = instruction.arg2;

		if (argI1 instanceof RegisterArg) {
			new DLXInstruction(new Instruction("1 SUBi " + Conf.FRAME_P + " " + Conf.BLOCK_LEN + " " + Conf.FRAME_P));
			__val = DLX.F1(DLX.POP, getRegNum(argI1.toString()), getRegNum(Conf.FRAME_P), 0);
//			new DLXInstruction(new Instruction("1 SUBi " +  Conf.STACK_GROW_DELTA + " " + Conf.STACK_P + " " + Conf.STACK_P));
		} else {
			wrong("POP: I need a register!");
		}
		bellowValAssig(instruction);
		return;
	}
}
