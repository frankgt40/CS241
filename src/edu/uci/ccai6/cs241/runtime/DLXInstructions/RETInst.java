package edu.uci.ccai6.cs241.runtime.DLXInstructions;

import edu.uci.ccai6.cs241.runtime.Conf;
import edu.uci.ccai6.cs241.runtime.FrameAbstract;
import edu.uci.ccai6.cs241.runtime.StackAbstract;
import edu.uci.ccai6.cs241.ssa.Arg;
import edu.uci.ccai6.cs241.ssa.Instruction;

public class RETInst extends DLXInstruction {
	public RETInst(Instruction instruction) {
		for (String ins : Conf.getStatusRestoreSequences()){
			new DLXInstruction(new Instruction(ins));
		}

//		new DLXInstruction(new Instruction("1 MOV " + Conf.STACK_P + " " + Conf.FRAME_P));
		
		__val = DLX.F2(DLX.RET, 0, 0, getRegNum(Conf.RETURN_ADDRESS_REG));
		
		if (!StackAbstract.getLastFrameName().isEmpty()) {
			String funcName = StackAbstract.getLastFrameName();
			StackAbstract.setCurrFrame(funcName);
		}
//		// Dirty method! For getting the main function's body's first statement's address
//		FrameAbstract currFrame = StackAbstract.getFrame("main");
//		int address = DLXInstruction.__instructions.size();
//		currFrame.set__startAddress(address*Conf.BLOCK_LEN);
		bellowValAssig(instruction);
	}
}
