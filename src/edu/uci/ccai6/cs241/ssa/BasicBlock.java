package edu.uci.ccai6.cs241.ssa;

import java.util.ArrayList;
import java.util.List;

public class BasicBlock {
	
	int index = 0;
	BasicBlock nextDirect, nextIndirect;
	
	List<Instruction> data = new ArrayList<Instruction>();
	
	public BasicBlock(int ind) {
		index = ind;
	}
	
	public void add(Instruction inst) {
		data.add(inst);
	}
}
