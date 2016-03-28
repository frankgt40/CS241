package edu.uci.ccai6.cs241.runtime.DLXInstructions;

import edu.uci.ccai6.cs241.runtime.Conf;
import edu.uci.ccai6.cs241.ssa.Arg;
import edu.uci.ccai6.cs241.ssa.ConstArg;
import edu.uci.ccai6.cs241.ssa.Instruction;
import edu.uci.ccai6.cs241.ssa.RegisterArg;
import edu.uci.ccai6.cs241.ssa.SpilledRegisterArg;

public class MOVEInst extends DLXInstruction {
	public MOVEInst(Instruction instruction) {
		int op = DLX.ADDI, arg1 = 0, arg2 = 0, arg3 = 0;
		Arg argI1 = instruction.arg0;
		Arg argI2 = instruction.arg1;
		Arg argI3 = instruction.arg2;

		if (argI2 instanceof SpilledRegisterArg) {
			// Have to store it into memory
			new DLXInstruction(new Instruction("1 STORE " + argI2.toString())); //NOT-FINISHED!!!!@@@@@@@@@@@@@@@@@@@@@@@
			
		} else if (argI2 instanceof RegisterArg) {
			arg2 = getRegNum(argI2.toString());
		}  else {
			wrong("MOV: target can only be register");
		}
		if (argI1 instanceof ConstArg) {
			// MOV 3 Reg
			// USE ADDi
			new DLXInstruction(new Instruction("1 ADDi " + Conf.ZERO_REG + " " + argI1 + " " + argI2.toString()));
		} else {
			// MOV REG REG
			// USE ADD
			new DLXInstruction(new Instruction("1 ADD " + Conf.ZERO_REG + " " + argI1 + " " + argI2.toString()));
		}
		return;
	}
}
