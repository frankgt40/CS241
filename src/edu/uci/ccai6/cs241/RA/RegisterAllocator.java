package edu.uci.ccai6.cs241.RA;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.uci.ccai6.cs241.ssa.BasicBlock;
import edu.uci.ccai6.cs241.ssa.Instruction;
import edu.uci.ccai6.cs241.ssa.Instruction.Operation;
import edu.uci.ccai6.cs241.ssa.PointerArg;
import edu.uci.ccai6.cs241.ssa.RegisterArg;

public class RegisterAllocator {
	
	static final int MAX_COLORS = 8;
	

	/**
	 * simple register assignment
	 * always assign new register
	 * @param instruction
	 * @return instruction with assigned register
	 */
	public static List<Instruction> simpleAssign(List<BasicBlock> bbs) {
		Map<PointerArg, RegisterArg> regMaps = new HashMap<PointerArg, RegisterArg>();
		int regNo = 1;
		int numInsts = 0;
		for(BasicBlock bb : bbs) {
			numInsts += bb.instructions.size();
			for(Instruction inst : bb.instructions) {
				// check and assign for arg2
				if(inst.op != Operation.BRA && inst.arg0 != null && inst.arg0 instanceof PointerArg) {
					if(regMaps.containsKey(inst.arg0)) inst.arg0 = regMaps.get(inst.arg0);
					else {
						RegisterArg nReg = new RegisterArg(regNo++);
						regMaps.put((PointerArg) inst.arg0, nReg);
						inst.arg0 = nReg;
					}
				}
				// check and assign for arg1
				if(!inst.op.isBranch() && inst.arg1 != null && inst.arg1 instanceof PointerArg) {
					if(regMaps.containsKey(inst.arg1)) inst.arg1 = regMaps.get(inst.arg1);
					else {
						RegisterArg nReg = new RegisterArg(regNo++);
						regMaps.put((PointerArg) inst.arg1, nReg);
						inst.arg1 = nReg;
					}
				}
				// check and assign for arg2
				if(inst.arg2 != null && inst.arg2 instanceof PointerArg) {
					// it shouldnt exist...
//					if(regMaps.containsKey(inst.arg2)) inst.arg2 = regMaps.get(inst.arg2);
//					else {
//						RegisterArg nReg = new RegisterArg(regNo++);
//						regMaps.put((PointerArg) inst.arg2, nReg);
//						inst.arg2 = nReg;
//					}
				} 
				if(inst.op.hasOutput()) {
					// only assign if the operation will have an output
					if(regMaps.containsKey(inst.pointer)) inst.arg2 = regMaps.get(inst.pointer);
					else {
						RegisterArg nReg = new RegisterArg(regNo++);
						regMaps.put((PointerArg) inst.pointer, nReg);
						inst.arg2 = nReg;
					}
				}
			}
		}
		return fixPhis(bbs, numInsts);
	}
	
	private static List<Instruction> fixPhis(List<BasicBlock> bbs, int numInsts) {

		List<Instruction> out = new ArrayList<Instruction>();
		// now fix PHI's
		for(BasicBlock bb : bbs) {
			for(Instruction inst : bb.instructions) {
				if(inst.op != Operation.PHI) continue;
				// if its PHI R1 R2 R3 -> remove this and 
				// add MOVE R1->R3 at prevIndirect and MOVE R2->R3 at prevDirect
				// has to be 1 higher because there's a BRA instruction at the end of the block
				if(!inst.arg0.equals(inst.arg2))
					bb.prevIndirect.instructions.add(bb.prevIndirect.instructions.size()-1,
							new Instruction(numInsts+" MOVE "+inst.arg0+" "+inst.arg2));
				if(!inst.arg1.equals(inst.arg2))
					bb.prevDirect.instructions.add(
							new Instruction(numInsts+" MOVE "+inst.arg1+" "+inst.arg2));
				numInsts++;
				inst.op = Operation.NOOP;
				inst.arg0 = null;
				inst.arg1 = null;
				inst.arg2 = null;
			}
		}
		for(BasicBlock bb : bbs) {
			for(Instruction inst : bb.instructions) {
				out.add(inst);
			}
		}
		return out;
	}
	
	public static List<Instruction> assign(List<BasicBlock> bbs) {
		Set<Integer> alive = new HashSet<Integer>();
		Set<Integer> universe = new HashSet<Integer>();
		
		// now fill edges
		Set<SimpleEdge> sparseEdges = new HashSet<SimpleEdge>();
		int numInsts = 0;
		for(int i=bbs.size()-1; i>=0; i--) {
			BasicBlock bb = bbs.get(i);
			
			// first check if this block is inside a loop
			// because if it is, then it would be defined at the head of the loop
			// which we havent reached yet
			if(bb.nextIndirect != null && bb.nextIndirect.nextDirect != null
					&& bb.index == bb.nextIndirect.nextDirect.index) {
				// if its in while block, get def in PHI's in while statement
				for(Instruction whileInst : bb.nextIndirect.instructions) {
					if(whileInst.op != Operation.PHI || !(whileInst.arg0 instanceof PointerArg)) 
						continue;
					int num = ((PointerArg)whileInst.arg0).pointer;
					alive.add(num);
				}
			}
			numInsts += bb.instructions.size();
			for(int j=bb.instructions.size()-1; j>=0; j--) {
				Instruction inst = bb.instructions.get(j);
				// here's where the pointer is defined, no longer alive
				// TODO: except for while!!
				if(inst.op.hasOutput()) alive.remove(inst.pointer.pointer);
				
				// arg0 is used here, add edges to all alive pointers
				if(inst.arg0 != null && inst.op != Operation.BRA 
						&& inst.arg0 instanceof PointerArg) {
					// if its a while phi, ignore it since we alrdy handled that
					if(inst.op == Operation.PHI
							&&bb.nextDirect != null && bb.nextIndirect != null
							&& bb.nextDirect.nextIndirect.index == bb.index)
						continue;
					
					int num = ((PointerArg)inst.arg0).pointer;
					sparseEdges.addAll(SimpleEdge.createEdges(num, alive));
					alive.add(num);
					universe.add(num);
				}
				if(inst.arg1 != null && !inst.op.isBranch() 
						&& inst.arg1 instanceof PointerArg) {
					int num = ((PointerArg)inst.arg1).pointer;
					sparseEdges.addAll(SimpleEdge.createEdges(num, alive));
					alive.add(num);
					universe.add(num);
				}
			}
		}
		Map<Integer, Integer> instructionColor = color(universe, sparseEdges, MAX_COLORS);
		
		List<Instruction> out = new ArrayList<Instruction>();
		for(BasicBlock bb : bbs) {
			for(Instruction inst : bb.instructions) {

				if(inst.arg0 != null && inst.op != Operation.BRA 
						&& inst.arg0 instanceof PointerArg) {
					int ptr = ((PointerArg)inst.arg0).pointer;
					inst.arg0 = new RegisterArg(instructionColor.get(ptr));
				}
				if(inst.arg1 != null && !inst.op.isBranch() 
						&& inst.arg1 instanceof PointerArg) {
					int ptr = ((PointerArg)inst.arg1).pointer;
					inst.arg1 = new RegisterArg(instructionColor.get(ptr));
				}
				if(inst.op.hasOutput()) {
					// only assign if the operation will have an output
					int ptr = ((PointerArg)inst.pointer).pointer;
					if(instructionColor.containsKey(ptr)) {
						inst.arg2 = new RegisterArg(instructionColor.get(ptr));
					} else inst.arg2 = new RegisterArg(1000);
				}
				out.add(inst);
			}
		}
		return fixPhis(bbs, numInsts);
	}
	
	/**
	 * exact coloring algorithm, recursion
	 * @param universe
	 * @param edges
	 * @param maxColors
	 * @return
	 */
	private static Map<Integer, Integer> color(Set<Integer> universe, Set<SimpleEdge> edges, int maxColors){
		if(universe.size() == 1) {
			Map<Integer, Integer> out = new HashMap<Integer, Integer>();
			out.put(universe.iterator().next(), 1);
			return out;
		}
		Integer x = universe.iterator().next();
		universe.remove(x);
		List<Integer> neighbors = new ArrayList<Integer>();
		for(Integer i : universe) {
			SimpleEdge e = new SimpleEdge(x,i);
			if(edges.contains(e)) {
				neighbors.add(i);
				edges.remove(e);
			}
		}
		Map<Integer, Integer> out = color(universe, edges, maxColors);
		universe.add(x);
		if(neighbors.isEmpty()) {
			out.put(x, 1);
			return out;
		}
		List<Integer> neighborColors = new ArrayList<Integer>();
		for(Integer nb : neighbors) {
			neighborColors.add(out.get(nb));
			edges.add(new SimpleEdge(x,nb));
		}
		
		Collections.sort(neighborColors);
		int xColor = neighborColors.get(neighborColors.size()-1)+1;
		for(int i=0; i<neighborColors.size()-1; i++) {
			if(neighborColors.get(i+1)-neighborColors.get(i) > 1) {
				xColor = out.get(neighborColors.get(i))+1;
				break;
			}
		}
		out.put(x, xColor);
		return out;
	}
}
