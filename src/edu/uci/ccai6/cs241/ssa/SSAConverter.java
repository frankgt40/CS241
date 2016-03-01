package edu.uci.ccai6.cs241.ssa;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
		Map<PointerArg, PointerArg> pointerMap = new HashMap<PointerArg, PointerArg>();
		List<Instruction> mappedInstructions = new ArrayList<Instruction>();
		
		// first get the mapping
		int instNum = 0;
		for(Instruction inst : instructions) {
			pointerMap.put((PointerArg)Arg.clone(inst.pointer), new PointerArg(instNum));
			instNum++;
		}
		
		// now remap it
		for(Instruction inst : instructions) {
			inst.pointer = pointerMap.get(inst.pointer).clone();
			if(inst.arg0 != null && inst.arg0 instanceof PointerArg) {
	            inst.arg0 = pointerMap.get(inst.arg0).clone();
			}
			if(inst.arg1 != null && inst.arg1 instanceof PointerArg) {
			    PointerArg np = pointerMap.get(inst.arg1);
                inst.arg1 = new PointerArg(np.pointer);
			}
			if(inst.arg2 != null && inst.arg2 instanceof PointerArg) {
			    PointerArg np = pointerMap.get(inst.arg2);
                inst.arg2 = new PointerArg(np.pointer);
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
	
	// TODO: now just doesnt work since we dont have output function
	/**
	 * 
	 * @return life ranges of pointers
	 */
	public Map<Arg, Arg> deadCodeElimination() {
	  // traverse backward
	  Map<Arg, Arg> lastUsed = new HashMap<Arg, Arg>();
	  for(int j=instructions.size()-1; j>=0; j--) {
	      Instruction inst = instructions.get(j);
	      
	      // put used variable into lastUsed
	      if(inst.arg0 != null && !(inst.arg0 instanceof ConstArg) && !lastUsed.containsKey(inst.arg0)) {
	        lastUsed.put(inst.arg0, inst.pointer);
	      }
          if(inst.arg1 != null && !(inst.arg1 instanceof ConstArg) && !lastUsed.containsKey(inst.arg1)) {
            lastUsed.put(inst.arg1, inst.pointer);
          }
          if(inst.arg2 != null && !(inst.arg2 instanceof ConstArg) && !lastUsed.containsKey(inst.arg2)) {
            lastUsed.put(inst.arg2, inst.pointer);
          }
          
          // check if result of this instruction has been used
        if(!inst.op.isFuncCall() && !inst.op.isBranch() && !lastUsed.containsKey(inst.pointer)) {
          instructions.set(j, new Instruction(inst.pointer.pointer+" "+Operation.NOOP));
        }
	  }
	  return lastUsed;
	}
//	
//	public void allocateRegister() {
//	    Map<Arg, Integer> regs = new HashMap<Arg, Integer>();
//	    int counter = 1;
//	    for(Instruction inst : instructions) {
//	      if(inst.op == Operation.PHI) {
//	        regs.put(inst.pointer, counter);
//            regs.put(inst.arg0, counter);
//            regs.put(inst.arg1, counter);
//            counter++;
//	      }
//	    }
//	    
//	    for(int i=0; i<instructions.size(); i++) {
//	      Instruction inst = instructions.get(i);
//	      if(inst.op == Operation.READ) {
//	        inst.arg0 = new Arg("R"+regs.get(inst.pointer));
//	      } else if(inst.op.isBranch()) {
//            continue;
//          } else if(inst.op == Operation.PHI) {
//            inst = new Instruction(inst.pointer.pointer+" "+Operation.KILL);
//          } else {
//            if(inst.arg0 != null && inst.arg0 instanceof PointerArg && regs.containsKey(inst.arg0)) {
//              inst.arg0 = new Arg("R"+regs.get(inst.arg0));
//            }
//            if(inst.arg1 != null && inst.arg1 instanceof PointerArg && regs.containsKey(inst.arg1)) {
//              inst.arg1 = new Arg("R"+regs.get(inst.arg1));
//            }
//            if(inst.arg2 != null && inst.arg2 instanceof PointerArg && regs.containsKey(inst.arg2)) {
//              inst.arg2 = new Arg("R"+regs.get(inst.arg2));
//            }
//          }
//          instructions.set(i, inst);
//	    }
//	}
	
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
				PointerArg jumpAddr = (PointerArg) inst.arg1;
				int jumpBlock = assignedBlockNum.get(jumpAddr.pointer);
				curBlock.nextIndirect = bbs.get(jumpBlock);
				bbs.get(jumpBlock).prevIndirect = curBlock;
			} else if(inst.op == Operation.BRA) {
			    PointerArg jumpAddr = (PointerArg) inst.arg0;
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
			if(inst.arg0 != null && inst.op != Operation.PHI && inst.arg0 instanceof VarArg) {
			    String varName = ((VarArg) inst.arg0).name;
				int varIdx = (vars.containsKey(varName) ? vars.get(varName) : 0);
				curBb.updateVar(varName, varIdx);
				inst.arg0 = new VarArg(varName+"@"+varIdx);
			}
			if(inst.arg1 != null && inst.op != Operation.PHI && inst.arg1 instanceof VarArg) {
                String varName = ((VarArg) inst.arg1).name;
				int varIdx = (vars.containsKey(varName) ? vars.get(varName) : 0);
				if(inst.op == Operation.MOVE) {
					// create a new var name for arg1 at MOVE
					varIdx++;
					vars.put(varName, varIdx);
				}
				curBb.updateVar(varName, varIdx);
                inst.arg1 = new VarArg(varName+"@"+varIdx);
			}
			if(inst.arg2 != null && inst.op == Operation.PHI && inst.arg2 instanceof VarArg) {
				// this one is easy since we always create a new var name
				// for last arg at PHI 
                String varName = ((VarArg) inst.arg2).name;
				int varIdx = (vars.containsKey(varName) ? vars.get(varName) : 0);
				varIdx++;
				vars.put(varName, varIdx);
				curBb.updateVar(varName, varIdx);
                inst.arg2 = new VarArg(varName+"@"+varIdx);
			}
		}
		
		// All variables in each basic block 
		// have to be latest variables reachable at this block
		for(BasicBlock bb : bbs) {
			if(bb.nextDirect != null) bb.nextDirect.mergeVars(bb);
			if(bb.nextIndirect != null) bb.nextIndirect.mergeVars(bb);
		}
		
		// now we have to fix first two addresses in PHI
		for(int i=0; i<instructions.size(); i++) {
			Instruction inst = instructions.get(i);
			int bbNum = bbInd.get(i);
			BasicBlock curBb = bbs.get(bbNum);
			
			if(inst.op == Operation.PHI) {
				// fix arg0 -> from prevDirect and arg2 -> from prevIndirect
				// TODO: is this correct for while?? I think so but need to check
				// with lecture note
			    String varName = ((VarArg) inst.arg0).name;
                inst.arg0 = new VarArg(varName+"@"+curBb.prevIndirect.ssaVars.get(varName));
                varName = ((VarArg) inst.arg1).name;
                inst.arg1 = new VarArg(varName+"@"+curBb.prevDirect.ssaVars.get(varName));
			}
		}

		copyProp();
		cse();
        copyProp();
//		deadCodeElimination();
        killPtrOp();
	}
	
	public void killPtrOp() {
		for(int i=0; i<instructions.size(); i++) {
			Instruction inst = instructions.get(i);
			if(inst.op == Operation.PTR) {
				inst.op = Operation.NOOP;
				inst.arg0 = null;
				inst.arg1 = null;
				inst.arg2 = null;
			}
			instructions.set(i, inst);
		}
	}
	
	/**
	 * common subexpression elimination
	 * @param bbs
	 * @param bbInd
	 */
	public void cse() {
		// do local cse first
		Map<Integer, Integer> mapping = new HashMap<Integer, Integer>();
		for(int i=0; i<instructions.size(); i++) {
			Instruction inst = instructions.get(i);
			if(inst.op == Operation.PTR) continue;
			int hc = inst.hashCodeWoPointer();
			if(mapping.containsKey(hc)) {
				Integer dupe = mapping.get(hc);
				inst = new Instruction(inst.pointer.pointer+" "+Operation.PTR+" ("+dupe+")");
				instructions.set(i, inst);
			} else {
				mapping.put(hc, inst.pointer.pointer);
			}
		}
	}
	
	// TODO: need to consider control flow - basic blocks
	public void copyProp() {
		Map<Arg, Arg> mapping = new HashMap<Arg, Arg>();
		int idx = 0;
		for(Instruction inst : instructions) {
			if(inst.op == Operation.MOVE) {
				if(!mapping.containsKey(inst.arg0))	mapping.put(inst.arg1, inst.arg0);
				else mapping.put(inst.arg1, mapping.get(inst.arg0));
				inst.op = Operation.PTR;
				inst.arg1 = null;
				inst.arg2 = null;
				inst.numArgs = 1;
			} else if(inst.op == Operation.PHI) {
				if(!mapping.containsKey(inst.pointer))	mapping.put(inst.arg2, inst.pointer);
				else mapping.put(inst.arg2, mapping.get(inst.pointer));
				inst.arg2 = null;
				inst.numArgs = 2;
			} 
			else if(inst.op == Operation.PTR) {
				if(!mapping.containsKey(inst.arg0))	mapping.put(inst.pointer, inst.arg0);
				else mapping.put(inst.pointer, mapping.get(inst.arg0));
				System.out.println("PHI: "+inst.pointer+" "+inst.arg0);
			}
			instructions.set(idx, inst);
			idx++;
		}
		
		int index = 0;
		for(Instruction inst : instructions) {
			// BRA (ptr) shouldnt change
		    if(inst.arg0 != null && inst.op != Operation.BRA) inst.arg0 = (mapping.containsKey(inst.arg0)) ? mapping.get(inst.arg0) : inst.arg0;
		    // BGT (10) (ptr) ptr shouldnt change
			if(inst.arg1 != null && !inst.op.isBranch()) inst.arg1 = (mapping.containsKey(inst.arg1)) ? mapping.get(inst.arg1) : inst.arg1;
			if(inst.arg2 != null) inst.arg2 = (mapping.containsKey(inst.arg2)) ? mapping.get(inst.arg2) : inst.arg2;

			instructions.set(index, inst);
			index++;
		}
		
		for(Instruction inst : instructions) {
			System.out.println(inst);
		}
	}
	
}
