package edu.uci.ccai6.cs241.RA;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.uci.ccai6.cs241.ssa.Arg;
import edu.uci.ccai6.cs241.ssa.BasicBlock;
import edu.uci.ccai6.cs241.ssa.Instruction;
import edu.uci.ccai6.cs241.ssa.Instruction.Operation;
import edu.uci.ccai6.cs241.ssa.PointerArg;
import edu.uci.ccai6.cs241.ssa.RegisterArg;

public class RegisterAllocator {
	

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
				// has to be 1 higher because there's a BRA instruction at the end of the block
				bb.prevIndirect.instructions.add(bb.prevIndirect.instructions.size()-1,
						new Instruction(numInsts+" MOVE "+inst.arg0+" "+inst.arg2));
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
		Set<Integer> sparseEdges = new HashSet<Integer>();
		int numInsts = 0;
		for(int i=bbs.size()-1; i>=0; i--) {
			BasicBlock bb = bbs.get(i);
			numInsts += bb.instructions.size();
			for(int j=bb.instructions.size()-1; j>=0; j--) {
				Instruction inst = bb.instructions.get(j);
				// here's where the pointer is defined, no longer alive
				// TODO: except for while!!
				if(inst.op.hasOutput()) alive.remove(inst.pointer.pointer);
				
				// arg0 is used here, add edges to all alive pointers
				if(inst.arg0 != null && inst.op != Operation.BRA 
						&& inst.arg0 instanceof PointerArg) {
					int num = ((PointerArg)inst.arg0).pointer;
					sparseEdges.addAll(createEdges(num, alive));
					alive.add(num);
					universe.add(num);
				}
				if(inst.arg1 != null && !inst.op.isBranch() 
						&& inst.arg1 instanceof PointerArg) {
					int num = ((PointerArg)inst.arg1).pointer;
					sparseEdges.addAll(createEdges(num, alive));
					alive.add(num);
					universe.add(num);
				}
			}
		}
		Map<Integer, Integer> instructionColor = color(universe, sparseEdges, 8);
		System.out.println(instructionColor);
		
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
	
	private static Map<Integer, Integer> color(Set<Integer> universe, Set<Integer> edges, int maxColors){
		if(universe.size() == 1) {
			Map<Integer, Integer> out = new HashMap<Integer, Integer>();
			out.put(universe.iterator().next(), 1);
			return out;
		}
		Integer x = universe.iterator().next();
		universe.remove(x);
		List<Integer> neighbors = new ArrayList<Integer>();
		for(Integer i : universe) {
			int hash = hash(x,i);
			if(edges.contains(hash)) {
				neighbors.add(i);
				edges.remove(hash(x,i));
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
			neighborColors.add(nb);
			edges.add(hash(x,nb));
		}
		
		Collections.sort(neighborColors);
		int xColor = out.get(neighborColors.get(neighborColors.size()-1))+1;
		for(int i=0; i<neighborColors.size()-1; i++) {
			if(neighborColors.get(i+1)-neighborColors.get(i) > 1) {
				xColor = out.get(neighborColors.get(i))+1;
				break;
			}
		}
		out.put(x, xColor);
		return out;
	}
	
	private static int hash(int i, int j) {
		int hash = 1;
		if(i<j) {
			int tmp = j;
			j = i;
			i = tmp;
		}
		hash = hash*31+i;
		hash = hash*17+j;
		return hash;
	}
	
	private static Set<Integer> createEdges(int i, Set<Integer> j) {
		Set<Integer> sparseEdges = new HashSet<Integer>();
		for(Integer jj : j) {
			if(i==jj) continue;
			int edgeIndex = hash(i,jj);
			sparseEdges.add(edgeIndex);
		}
		return sparseEdges;
	}
}
