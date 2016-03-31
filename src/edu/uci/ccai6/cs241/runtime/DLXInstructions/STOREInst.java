package edu.uci.ccai6.cs241.runtime.DLXInstructions;

import edu.uci.ccai6.cs241.runtime.Conf;
import edu.uci.ccai6.cs241.ssa.Arg;
import edu.uci.ccai6.cs241.ssa.Instruction;
import edu.uci.ccai6.cs241.ssa.RegisterArg;

public class STOREInst extends DLXInstruction {
	public STOREInst(Instruction instruction) {
		Arg arg1, arg2;
		arg1 = instruction.arg0;
		arg2 = instruction.arg1;
		if (arg1 instanceof RegisterArg) {
			__val = DLX.F2(DLX.STX, getRegNum(arg2.toString()), getRegNum(Conf.STACK_P), getRegNum(arg1.toString()));
		}  else {
			__val = DLX.F1(DLX.STW, getRegNum(arg2.toString()), getRegNum(Conf.STACK_P), Integer.parseInt(arg1.toString()));	
		}
		bellowValAssig(instruction);
		return;
	}
}
