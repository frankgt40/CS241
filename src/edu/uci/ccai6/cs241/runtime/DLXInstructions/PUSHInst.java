package edu.uci.ccai6.cs241.runtime.DLXInstructions;

import edu.uci.ccai6.cs241.runtime.Conf;
import edu.uci.ccai6.cs241.ssa.Arg;
import edu.uci.ccai6.cs241.ssa.ConstArg;
import edu.uci.ccai6.cs241.ssa.Instruction;
import edu.uci.ccai6.cs241.ssa.RegisterArg;

public class PUSHInst extends DLXInstruction{
	public PUSHInst(Instruction instruction) {
		int op = DLX.PSH, arg1 = 0, arg2 = 0, arg3 = 0;
		Arg argI1 = instruction.arg0;
		Arg argI2 = instruction.arg1;
		Arg argI3 = instruction.arg2;

		if (argI1 instanceof ConstArg) {
			// is const
			// MOV Load_REG_1 const
			new DLXInstruction(new Instruction("1 ADDi " + Conf.ZERO_REG + " " + argI1 + " " + Conf.LOAD_REG_1)); 
			//
			
			arg1 = Integer.parseInt(argI1.toString());
			__val = DLX.F1(DLX.PSH, getRegNum(Conf.LOAD_REG_1), getRegNum(Conf.STACK_P), 0);
			bellowValAssig(instruction);
			
			new DLXInstruction(new Instruction("1 ADDi " + Conf.BLOCK_LEN + " " + Conf.STACK_P + " " + Conf.STACK_P));
		} else if (argI1 instanceof RegisterArg){
			// in register
			arg1 = getRegNum(argI1.toString());
			__val = DLX.F1(DLX.PSH, arg1, getRegNum(Conf.STACK_P), 0);
			bellowValAssig(instruction);
			
			new DLXInstruction(new Instruction("1 ADDi " + Conf.BLOCK_LEN + " " + Conf.STACK_P + " " + Conf.STACK_P));
		} else {
			wrong("PUSH: wrong!");
		}
//		bellowValAssig(instruction);
		return;
	}
}
