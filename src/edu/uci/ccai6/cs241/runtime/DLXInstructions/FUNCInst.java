package edu.uci.ccai6.cs241.runtime.DLXInstructions;

import edu.uci.ccai6.cs241.runtime.FrameAbstract;
import edu.uci.ccai6.cs241.runtime.StackAbstract;
import edu.uci.ccai6.cs241.ssa.Arg;
import edu.uci.ccai6.cs241.ssa.Instruction;

// Build the Frame structure to be used later
public class FUNCInst extends DLXInstruction {
	public FUNCInst(Instruction instruction) {

		FrameAbstract frame = null;
		String funName = instruction.funcName;
		if (funName.equals("data")) {
			// Ignore it!
//		} else if (funName.equals("main")) {
//			// Remeber! Put every vars in global static area
//			// When call main, needs to set up stack and frame pointers
//			frame = new FrameAbstract(funName);
//			StackAbstract.addFrame(frame);
		} else {
			// Other frames
			
			if (StackAbstract.getFrame(funName)!=null){
				StackAbstract.setCurrFrame(funName);
			} else {
				frame = new FrameAbstract(funName);
				StackAbstract.addFrame(frame);
			}
		}
	}
}
