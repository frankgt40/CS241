package edu.uci.ccai6.cs241.runtime.DLXInstructions;

import edu.uci.ccai6.cs241.runtime.Conf;
import edu.uci.ccai6.cs241.ssa.Arg;
import edu.uci.ccai6.cs241.ssa.ConstArg;
import edu.uci.ccai6.cs241.ssa.Instruction;
import edu.uci.ccai6.cs241.ssa.RegisterArg;

public class DIViInst extends DLXInstruction {

	public DIViInst(Instruction instruction) {
		int op = DLX.DIVI, arg1 = 0, arg2 = 0, arg3 = 0;
		Arg argI1 = instruction.arg0;
		Arg argI2 = instruction.arg1;
		Arg argI3 = instruction.arg2;
		
		if (!(argI3 instanceof RegisterArg)) {
			wrong("AddiInst: target can only be register");
		} else {
			arg3 = getRegNum(argI3.toString());
		}
		if (argI1 instanceof ConstArg) {
			arg1 = Integer.parseInt(argI1.toString());
			if (argI2 instanceof ConstArg) {
				// Two operants are both const
				arg2 = Integer.parseInt(argI2.toString());
				arg1 = arg1 / arg2; //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
				// ADDi argI3 R0 arg1
				__val = DLX.F1(op, arg3, getRegNum(Conf.ZERO_REG), arg1);
				bellowValAssig(instruction);
				return;
			} else if (argI2 instanceof RegisterArg) {
				arg2 = getRegNum(argI2.toString());
				// ADDi argI3 argI2 argI1
				__val = DLX.F1(op, arg3, arg2, arg1);
				bellowValAssig(instruction);
				return;
			} else {
				wrong("AddiInst: argI2 can only be either resgister or const");
			}
		} else if (argI1 instanceof RegisterArg) {
			arg1 = getRegNum(argI1.toString());
			if (argI2 instanceof ConstArg) {
				arg2 = Integer.parseInt(argI2.toString());
				
				// ADDi argI3 argI1 argI2
				__val = DLX.F1(op, arg3, arg1, arg2);
				bellowValAssig(instruction);
				return;
			} else {
				wrong("AddiInst: argI2 can only be const");
			}
		} else {
			wrong("AddiInst: argI1 can only be either resgister or const");
		}
	}
}
