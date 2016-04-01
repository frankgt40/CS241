package edu.uci.ccai6.cs241.runtime.DLXInstructions;

import edu.uci.ccai6.cs241.runtime.Conf;
import edu.uci.ccai6.cs241.ssa.Arg;
import edu.uci.ccai6.cs241.ssa.ConstArg;
import edu.uci.ccai6.cs241.ssa.Instruction;
import edu.uci.ccai6.cs241.ssa.RegisterArg;

public class STOREInst extends DLXInstruction {
	public STOREInst(Instruction instruction) {
		Arg arg1, arg2;
		arg1 = instruction.arg0;
		arg2 = instruction.arg1;
		if (arg1 instanceof RegisterArg) {
			if (arg2 instanceof ConstArg) {
			__val = DLX.F2(DLX.STX,  getRegNum(arg1.toString()), getRegNum(Conf.FRAME_P), Integer.parseInt(arg2.toString()));
			} else {
				wrong("STOREInst: arg2 must be a const!");
			}
		}  else {
//			new DLXInstruction(new Instruction("1 MOV " + getRegNum(arg1.toString()) + " " + Conf.LOAD_REG_1));
//			__val = DLX.F1(DLX.STX, getRegNum(Conf.LOAD_REG_1), getRegNum(Conf.ZERO_REG), getRegNum(arg2.toString()));	
			wrong("STOREInst: arg1 must be a target register!");
		}
		bellowValAssig(instruction);
		return;
	}
}
