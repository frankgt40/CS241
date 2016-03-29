package edu.uci.ccai6.cs241.runtime.DLXInstructions;

import edu.uci.ccai6.cs241.runtime.Conf;
import edu.uci.ccai6.cs241.runtime.FrameAbstract;
import edu.uci.ccai6.cs241.runtime.StackAbstract;
import edu.uci.ccai6.cs241.ssa.Arg;
import edu.uci.ccai6.cs241.ssa.Instruction;

public class RETInst extends DLXInstruction {
	public RETInst(Instruction instruction) {
		Arg arg1 = instruction.arg0;
		

		// Restore the R31 (return address)
		new DLXInstruction(new Instruction("1 POP " + Conf.RETURN_ADDRESS_REG));
		
		__val = DLX.F2(DLX.RET, 0, 0, getRegNum(Conf.RETURN_ADDRESS_REG));
		bellowValAssig(instruction);
	}
}
