package edu.uci.ccai6.cs241.ssa;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
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
	 * Make sure all pointers are sequential
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
	 * @return list of assigned block number for each instruction
	 * with the same order as instructions
	 */
	private List<Integer> assignBlockNum(Set<Arg> jumpedAddrs) {

		List<Integer> bbNum = new ArrayList<Integer>();
		int curNum = 0;
		for(Instruction inst : instructions) {
			// create a new block and add inst when its a new function or 
			// the address can be reached by some jumpings
			if(inst.op == Operation.FUNC || jumpedAddrs.contains(inst.pointer)) {
				curNum++;
				bbNum.add(curNum);
				continue;
			}
			
			bbNum.add(curNum);
			
			// create a new block when its a cond branch
			// note: if its a BRA, then the first if takes care of it
			// since the next instruction is gonna be a jumped instruction
			if(inst.op.isCondJump()) {
				curNum++;
			}
			
		}
		return bbNum;
	}
	
	/**
	 * generate all basic blocks
	 * @param assignedBlockNum
	 * @param numBlocks
	 * @return list of basic blocks
	 */
	public List<BasicBlock> generateBasicBlocks(List<Integer> assignedBlockNum, int numBlocks) {
		List<BasicBlock> bbs = new ArrayList<BasicBlock>();
		BasicBlock head = null;
		BasicBlock prev = null;
		
		// first create all empty basic blocks with simple links
		// NOTE: some links (i.e. BRA) are not needed but will be fixed later
		for(int i=numBlocks-1; i>=0; i--) {
			head = new BasicBlock(i);
			head.nextDirect = prev;
			if(prev != null) prev.prevDirect = head;
			prev = head;
		}
		// store it into list for convenience
		while(head != null) {
			bbs.add(head);
			head = head.nextDirect;
		}
		
		// Now try to link correctly
		int curNum = 0;
		for(Instruction inst : instructions) {
			
			int curBb = assignedBlockNum.get(curNum);
			BasicBlock curBlock = bbs.get(curBb);
			curBlock.add(inst);
			
			if(inst.op.isCondJump()) {
				// for cond jump, it creates an indirect chain
				Arg jumpAddr = inst.arg1;
				int jumpBlock = assignedBlockNum.get(jumpAddr.pointer);
				curBlock.nextIndirect = bbs.get(jumpBlock);
				bbs.get(jumpBlock).prevIndirect = curBlock;
			} else if(inst.op == Operation.BRA) {
				Arg jumpAddr = inst.arg0;
				int jumpBlock = assignedBlockNum.get(jumpAddr.pointer);
				curBlock.nextIndirect = bbs.get(jumpBlock);
				bbs.get(jumpBlock).prevIndirect = curBlock;
				
				// now fix nextDirect since there's no next direct block at BRA
				bbs.get(curBb).nextDirect.prevDirect = null;
				bbs.get(curBb).nextDirect = null;
			}
			
			curNum++;
			
		}
//		for(int i=0; i<numBlocks; i++) {
//			BasicBlock bb = bbs.get(i);
//			if(bb.nextDirect != null)
//				System.out.println("Direct next: "+bb.index+"->"+bb.nextDirect.index);
//		}
//		for(int i=0; i<numBlocks; i++) {
//			BasicBlock bb = bbs.get(i);
//			if(bb.prevDirect != null)
//				System.out.println("Direct prev: "+bb.index+"<-"+bb.prevDirect.index);
//		}
//		
//		for(int i=0; i<numBlocks; i++) {
//			BasicBlock bb = bbs.get(i);
//			if(bb.nextIndirect != null)
//				System.out.println(bb.index+"->"+bb.nextIndirect.index);
//
//			if(bb.prevIndirect != null)
//				System.out.println(bb.prevIndirect.index+"<-"+bb.index);
//		}
		return bbs;
	}
	
	/**
	 * assign each instruction into basic block
	 * return the corresponding result
	 * @return
	 */
	public List<Integer> assignBlockNum() {
		
		// make sure instruction numbers are sequential
		remapPointer();
		
		// first need to find all branches locations
		// so that we know where we are going to split the blocks
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
		
		List<BasicBlock> bbs = generateBasicBlocks(bbNum, numBlocks);
		rename(bbs, bbNum);
		return bbNum;
	}
	
	/**
	 * Rename vars for every new MOVE instruction
	 * We rename it as var@index
	 * @param bbs
	 * @param bbInd
	 */
	public void rename(List<BasicBlock> bbs, List<Integer> bbInd) {
		
		// keeping track of latest index for all vars
		// We also keep track of variables in each basic block
		Map<String, Integer> vars = new HashMap<String, Integer>();
		for(int i=0; i<instructions.size(); i++) {
			Instruction inst = instructions.get(i);
			int bbNum = bbInd.get(i);
			BasicBlock curBb = bbs.get(bbNum);
			
			// PHI is tricky for arg0 and arg1, we will skip it for now
			if(inst.arg0 != null && inst.op != Operation.PHI && inst.arg0.type == Type.VARIABLE) {
				int varIdx = (vars.containsKey(inst.arg0.var) ? vars.get(inst.arg0.var) : 0);
				curBb.updateVar(inst.arg0.var, varIdx);
				inst.arg0.var += "@"+varIdx;
			}
			if(inst.arg1 != null && inst.op != Operation.PHI && inst.arg1.type == Type.VARIABLE) {
				int varIdx = (vars.containsKey(inst.arg1.var) ? vars.get(inst.arg1.var) : 0);
				if(inst.op == Operation.MOVE) {
					// create a new var name for arg1 at MOVE
					varIdx++;
					vars.put(inst.arg1.var, varIdx);
				}
				curBb.updateVar(inst.arg1.var, varIdx);
				inst.arg1.var += "@"+varIdx;
			}
			if(inst.arg2 != null && inst.op == Operation.PHI && inst.arg2.type == Type.VARIABLE) {
				// this one is easy since we always create a new var name
				// for last arg at PHI 
				int varIdx = (vars.containsKey(inst.arg2.var) ? vars.get(inst.arg2.var) : 0);
				varIdx++;
				vars.put(inst.arg2.var, varIdx);
				curBb.updateVar(inst.arg2.var, varIdx);
				inst.arg2.var += "@"+varIdx;
			}
		}
		
		// All variables in each basic block 
		// have to be latest variables reachable at this block
		for(BasicBlock bb : bbs) {
			if(bb.nextDirect != null) bb.nextDirect.mergeVars(bb);
			if(bb.nextIndirect != null) bb.nextIndirect.mergeVars(bb);
		}
		
//		for(BasicBlock bb : bbs) {
//			System.out.println(bb.index+" "+bb.ssaVars);
//		}
		
		// now we have to fix first two addresses in PHI
		for(int i=0; i<instructions.size(); i++) {
			Instruction inst = instructions.get(i);
			int bbNum = bbInd.get(i);
			BasicBlock curBb = bbs.get(bbNum);
			
			if(inst.op == Operation.PHI) {
				// fix arg0 -> from prevDirect and arg2 -> from prevIndirect
				// TODO: is this correct for while?? I think so but need to check
				// with lecture note
				inst.arg0.var += "@"+curBb.prevIndirect.ssaVars.get(inst.arg0.var);
				inst.arg1.var += "@"+curBb.prevDirect.ssaVars.get(inst.arg1.var);
			}
		}
		
	}
	
}
