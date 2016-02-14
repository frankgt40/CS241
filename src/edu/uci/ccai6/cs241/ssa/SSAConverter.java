package edu.uci.ccai6.cs241.ssa;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.uci.ccai6.cs241.ssa.Arg.Type;
import edu.uci.ccai6.cs241.ssa.Instruction.Operation;

public class SSAConverter {

	public List<Instruction> instructions = new ArrayList<Instruction>();
	
	public SSAConverter(List<String> codes) {
		for(String s : codes) {
			instructions.add(new Instruction(s));
		}
	}
	
	/**
	 * Ideally we want instruction numbers to be sequential
	 */
	public void remapPointer() {
		Map<Arg, Arg> pointerMap = new HashMap<Arg, Arg>();
		List<Instruction> mappedInstructions = new ArrayList<Instruction>();
		
		// first get the mapping
		int instNum = 0;
		for(Instruction inst : instructions) {
			pointerMap.put(new Arg(inst.pointer), new Arg("("+instNum+")"));
			instNum++;
		}
		
		// now remap it
		for(Instruction inst : instructions) {
			Arg instPtr = pointerMap.get(inst.pointer);
			inst.pointer.setPointer(instPtr.pointer);
			if(inst.arg0 != null && inst.arg0.type == Type.POINTER) {
				Arg np = pointerMap.get(inst.arg0);
				inst.arg0.setPointer(np.pointer);
			}
			if(inst.arg1 != null && inst.arg1.type == Type.POINTER) {
				Arg np = pointerMap.get(inst.arg1);
				inst.arg1.setPointer(np.pointer);
			}
			if(inst.arg2 != null && inst.arg2.type == Type.POINTER) {
				Arg np = pointerMap.get(inst.arg2);
				inst.arg2.setPointer(np.pointer);
			}
			mappedInstructions.add(inst);
		}
		instructions = new ArrayList<Instruction>(mappedInstructions);
		
	}
	
	/**
	 * Now have a knowledge of addresses that can be jumped
	 * for handling while jumping
	 * @param jumpedAddrs
	 * @return
	 */
	private List<Integer> assignBlockNum(Set<Arg> jumpedAddrs) {

		List<Integer> bbNum = new ArrayList<Integer>();
		int curNum = 0;
		for(Instruction inst : instructions) {
			if(inst.op == Operation.FUNC || jumpedAddrs.contains(inst.pointer)) {
				curNum++;
				bbNum.add(curNum);
				continue;
			}
			
			bbNum.add(curNum);
			
			if(inst.op.isCondJump()) {
				curNum++;
			}
			
		}
		return bbNum;
	}
	
	public void generateBasicBlocks(List<Integer> assignedBlockNum, int numBlocks) {
		List<BasicBlock> bbs = new ArrayList<BasicBlock>();
		BasicBlock head = null;
		BasicBlock prev = null;
		for(int i=numBlocks-1; i>=0; i--) {
			head = new BasicBlock(i);
			head.nextDirect = prev;
			prev = head;
		}
		while(head != null) {
			System.out.print(head.index+"->");
			bbs.add(head);
			head = head.nextDirect;
		}
		System.out.println("end");
		
		int curNum = 0;
		for(Instruction inst : instructions) {
			
			int curBb = assignedBlockNum.get(curNum);
			
			if(inst.op.isCondJump()) {
				Arg jumpAddr = inst.arg1;
				int jumpBlock = assignedBlockNum.get(jumpAddr.pointer);
				bbs.get(curBb).nextIndirect = bbs.get(jumpBlock);
			} else if(inst.op == Operation.BRA) {
				Arg jumpAddr = inst.arg0;
				int jumpBlock = assignedBlockNum.get(jumpAddr.pointer);
				bbs.get(curBb).nextIndirect = bbs.get(jumpBlock);
			}
			
			bbs.get(curBb).add(inst);
			
			curNum++;
			
		}
		
		for(int i=0; i<numBlocks; i++) {
			if(bbs.get(i).nextIndirect != null)
				System.out.println(bbs.get(i).index+"->"+bbs.get(i).nextIndirect.index);
			
		}
	}
	
	/**
	 * assign each instruction into basic block
	 * only create a new basic block when there's a branch
	 * or a new function
	 * @return
	 */
	public List<Integer> assignBlockNum() {
		
		// remaining of the code assume sequential instruction numbers
		remapPointer();
		
		// first need to find all branches locations
		Set<Arg> jumpedAddrs = new HashSet<Arg>();
		for(Instruction inst : instructions) {
			
			if(inst.op.isCondJump()) {
				jumpedAddrs.add(inst.arg1);
			} else if(inst.op == Operation.BRA) {
				jumpedAddrs.add(inst.arg0);
			}
			
		}
		
		List<Integer> bbNum = assignBlockNum(jumpedAddrs);
		int numBlocks = bbNum.get(bbNum.size()-1)+1;
		
		generateBasicBlocks(bbNum, numBlocks);
		return bbNum;
	}
	
}
