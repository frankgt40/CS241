package edu.uci.ccai6.cs241.runtime.DLXInstructions;

import edu.uci.ccai6.cs241.runtime.Conf;
import edu.uci.ccai6.cs241.ssa.Arg;
import edu.uci.ccai6.cs241.ssa.Instruction;

public class CALLInst extends DLXInstruction {
	public CALLInst(Instruction instruction) {
		int arg1 = 0, arg2 = 0, arg3 = 0;
		Arg argI1 = instruction.arg0;
		Arg argI2 = instruction.arg1;
		Arg argI3 = instruction.arg2;
		
		// Pre-defined functions: dealing with F2 type instructions
		if (argI1.toString().equals("OutputNum")) {
			new DLXInstruction(new Instruction("1 POP " + Conf.LOAD_REG_1));
			__val = DLX.F2(DLX.WRD, 0, getRegNum(Conf.LOAD_REG_1), 0);
		} else if (argI1.toString().equals("OutputNewLine")) {
			// Dealing with F1 type of instruction
			__val = DLX.F1(DLX.WRL, 0, 0, 0);
			bellowValAssig(instruction);
			return;
		} else if (argI1.toString().equals("InputNum")) {
			__val = DLX.F2(DLX.RDI, getRegNum(Conf.RETURN_VAL_REG), 0, 0);
		} else {
//			wrong("CALLInst: something is wrong!");
		}
		bellowValAssig(instruction);
		return;
	}
}