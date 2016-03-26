package edu.uci.ccai6.cs241.runtime.DLXInstructions;

import edu.uci.ccai6.cs241.runtime.Conf;
import edu.uci.ccai6.cs241.ssa.Arg;
import edu.uci.ccai6.cs241.ssa.ConstArg;
import edu.uci.ccai6.cs241.ssa.Instruction;
import edu.uci.ccai6.cs241.ssa.RegisterArg;

public class CMPInst extends DLXInstruction {
	public CMPInst(Instruction instruction) {
		int op = DLX.CMP, arg1 = 0, arg2 = 0, arg3 = 0;
		Arg argI1 = instruction.arg0;
		Arg argI2 = instruction.arg1;
		Arg argI3 = instruction.arg2;

		if (argI1 instanceof ConstArg) {
			new DLXInstruction(new Instruction("1 MOV " + argI1.toString() + " " + Conf.LOAD_REG_1));
			arg1 = getRegNum(Conf.LOAD_REG_1);
		} else {
			arg1 = getRegNum(argI1.toString());
		}
		if (argI2 instanceof ConstArg) {
			new DLXInstruction(new Instruction("1 MOV " + argI2.toString() + " " + Conf.LOAD_REG_2));
			arg2 = getRegNum(Conf.LOAD_REG_2);
		} else {
			arg2 = getRegNum(argI2.toString());
		}
		if (!(argI3 instanceof RegisterArg)) {
			wrong("CMP: argI3 should be a register");
		}
		arg3 = getRegNum(argI3.toString());
		__val = DLX.F1(op, arg3, arg1, arg2);
		bellowValAssig(instruction);
		return;
	}

}
