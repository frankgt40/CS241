package edu.uci.ccai6.cs241.runtime.DLXInstructions;

import edu.uci.ccai6.cs241.runtime.Conf;
import edu.uci.ccai6.cs241.runtime.FrameAbstract;
import edu.uci.ccai6.cs241.runtime.StackAbstract;
import edu.uci.ccai6.cs241.ssa.Arg;
import edu.uci.ccai6.cs241.ssa.Instruction;

public class CALLInst extends DLXInstruction {
	public CALLInst(Instruction instruction) {
		int arg1 = 0, arg2 = 0, arg3 = 0;
		Arg argI1 = instruction.arg0;
		Arg argI2 = instruction.arg1;
		Arg argI3 = instruction.arg2;
		String funcName = argI1.toString();
		
		new DLXInstruction(new Instruction("1 ADD " + Conf.STACK_P + " "  + Conf.ZERO_REG + " " + Conf.FRAME_P));
		// Pre-defined functions: dealing with F2 type instructions
		if (funcName.equals("OutputNum")) {
			new DLXInstruction(new Instruction("1 POP " + Conf.LOAD_REG_1));
			__val = DLX.F2(DLX.WRD, 0, getRegNum(Conf.LOAD_REG_1), 0);
			bellowValAssig(instruction);
			new DLXInstruction(new Instruction("1 POP "+ Conf.FRAME_P));
		} else if (funcName.equals("OutputNewLine")) {
			// Dealing with F1 type of instruction
			__val = DLX.F1(DLX.WRL, 0, 0, 0);
			bellowValAssig(instruction);
			new DLXInstruction(new Instruction("1 POP "+ Conf.FRAME_P));
			return;
		} else if (funcName.equals("InputNum")) {
			__val = DLX.F2(DLX.RDI, getRegNum(Conf.RETURN_VAL_REG), 0, 0);
			bellowValAssig(instruction);
			new DLXInstruction(new Instruction("1 POP "+ Conf.FRAME_P));
		} else {
			// Store R31 (return address)
			
			String lastFuncName = StackAbstract.getCurrFrame().get__funcName();
			StackAbstract.setLastFrame(lastFuncName);
			StackAbstract.setCurrFrame(funcName);
			
			FrameAbstract targetFrame = StackAbstract.getCurrFrame();
			int targetAddress = targetFrame.get__startAddress();
			//new DLXInstruction(new Instruction("1 ADDi " + targetAddress + " " + Conf.LOAD_REG_1 + " " + Conf.LOAD_REG_1));
			__val = DLX.F3(DLX.JSR,  targetAddress);

			bellowValAssig(instruction);

			// Restore the R31 (return address)
//			new DLXInstruction(new Instruction("1 POP " + Conf.RETURN_ADDRESS_REG));
			
		}

		return;
	}
}
