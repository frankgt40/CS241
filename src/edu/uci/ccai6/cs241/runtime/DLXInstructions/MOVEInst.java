package edu.uci.ccai6.cs241.runtime.DLXInstructions;

import edu.uci.ccai6.cs241.runtime.Conf;
import edu.uci.ccai6.cs241.runtime.Local;
import edu.uci.ccai6.cs241.runtime.StackAbstract;
import edu.uci.ccai6.cs241.ssa.Arg;
import edu.uci.ccai6.cs241.ssa.ConstArg;
import edu.uci.ccai6.cs241.ssa.Instruction;
import edu.uci.ccai6.cs241.ssa.RegisterArg;
import edu.uci.ccai6.cs241.ssa.SpilledRegisterArg;

public class MOVEInst extends DLXInstruction {
	public MOVEInst(Instruction instruction) {
		// MOV argI1 to argI2
		int op = DLX.ADDI, arg1 = 0, arg2 = 0, arg3 = 0;
		Arg argI1 = instruction.arg0;
		Arg argI2 = instruction.arg1;
		if (argI2 instanceof SpilledRegisterArg) {
			Local local = StackAbstract.getCurrFrame().__fakeRegToMem.get(argI2.toString());
			if (!local.isStored()) {
				// Needs to allocate a space on the stack
				new DLXInstruction(new Instruction("1 PUSH " + 0)); // Increase the stack pointer
				local.setIsStored(true);
			}
			if (argI1 instanceof ConstArg) {
				new DLXInstruction(new Instruction("1 ADDi " + Conf.ZERO_REG + " " + argI1.toString() + " " + Conf.STORE_TARGET));
				arg1 = Conf.getRegNum(Conf.STORE_TARGET);
			} else {
				arg1 = Conf.getRegNum(argI1.toString());
			}
			arg2 = local.__offset;
			arg3 = Conf.getRegNum(Conf.STACK_P);
			__val = DLX.F1(DLX.STW, arg1, arg3, arg2);
			bellowValAssig(instruction);
			return;
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
