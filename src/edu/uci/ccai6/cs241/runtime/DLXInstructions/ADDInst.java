package edu.uci.ccai6.cs241.runtime.DLXInstructions;

import edu.uci.ccai6.cs241.runtime.Conf;
import edu.uci.ccai6.cs241.ssa.Arg;
import edu.uci.ccai6.cs241.ssa.ConstArg;
import edu.uci.ccai6.cs241.ssa.Instruction;
import edu.uci.ccai6.cs241.ssa.RegisterArg;

public class ADDInst extends DLXInstruction{
	public ADDInst(Instruction instruction) {
		int op = DLX.ADD, arg1 = 0, arg2 = 0, arg3 = 0;
		Arg argI1 = instruction.arg0;
		Arg argI2 = instruction.arg1;
		Arg argI3 = instruction.arg2;
		
		if (!(argI3 instanceof RegisterArg)) {
			wrong("AddInst: target can only be register");
		} else {
			arg3 = getRegNum(argI3.toString());
		}
		if (!(argI1 instanceof RegisterArg)) {
			wrong("AddInst: argI1 can only be register");
		} else {
			arg1 = getRegNum(argI1.toString());
		}
		
		if (!(argI2 instanceof RegisterArg)) {
			wrong("AddInst: argI2 can only be register");
		} else {
			arg2 = getRegNum(argI2.toString());
		}
		
		__val = DLX.F2(op, arg3, arg2, arg1);
		bellowValAssig(instruction);
		return;
	}
}
