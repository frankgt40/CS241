package edu.uci.ccai6.cs241.RA;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import edu.uci.ccai6.cs241.runtime.Conf;
import edu.uci.ccai6.cs241.ssa.Arg;
import edu.uci.ccai6.cs241.ssa.BasicBlock;
import edu.uci.ccai6.cs241.ssa.Instruction;
import edu.uci.ccai6.cs241.ssa.SpilledRegisterArg;
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
	
	/**
	 * when we dont assign the same color for all ops in PHI,
	 * insert copy to previous block
	 * @param bbs
	 * @param numInsts
	 * @return
	 */
	private static List<Instruction> fixPhis(List<BasicBlock> bbs, int numInsts) {

		List<Instruction> out = new ArrayList<Instruction>();
		// now fix PHI's
		for(BasicBlock bb : bbs) {
			List<Instruction> prevDirectMoveInst = new ArrayList<Instruction>();
			Set<RegisterArg> prevDirectDefReg = new HashSet<RegisterArg>();
			List<Instruction> prevIndirectMoveInst = new ArrayList<Instruction>();
			Set<RegisterArg> prevIndirectDefReg = new HashSet<RegisterArg>();
			for(Instruction inst : bb.instructions) {
				if(inst.op != Operation.PHI) continue;
				
				if(!inst.arg0.equals(inst.arg2)) {
					prevIndirectDefReg.add((RegisterArg) inst.arg2);
					prevIndirectMoveInst.add(new Instruction(numInsts+" MOV "+inst.arg0+" "+inst.arg2));
					numInsts++;
				}
				if(!inst.arg1.equals(inst.arg2)) {
					prevDirectDefReg.add((RegisterArg) inst.arg2);
					prevDirectMoveInst.add(new Instruction(numInsts+" MOV "+inst.arg1+" "+inst.arg2));
					numInsts++;
				}
				
				inst.op = Operation.NOOP;
				inst.arg0 = null;
				inst.arg1 = null;
				inst.arg2 = null;
			}
			
			insertMoves(prevDirectMoveInst, prevDirectDefReg, bb.prevDirect, true);
			insertMoves(prevIndirectMoveInst, prevIndirectDefReg, bb.prevIndirect, false);
		}
		for(BasicBlock bb : bbs) {
			for(Instruction inst : bb.instructions) {
				out.add(inst);
			}
		}
		return out;
	}
	
	/**
	 * Motivation: PHI R1 R2 R2 -> have to insert MOVE R1->R2 into prev block
	 * but the order of PHI MATTERS now when converting out of SSA
		 i.e in while loop
		 (152) PHI R3 R6 R6 
		 (153) PHI R6 R5 R5 -> supposed to use R6 in previous loop
		 (xxx) PHI R5 R6 R6 -> hopefully this case wouldnt happen -> deadlock here
		 -> this case will enter infinite loop which is very very bad
		 
		 
		 Above example:
		 MOVE R6 -> R5 has to come before MOVE R3 -> R6 when doing copy
	 * @param moveInsts
	 * @param defReg
	 * @param destBb
	 */
	private static void insertMoves(List<Instruction> moveInsts, 
			Set<RegisterArg> defReg, BasicBlock destBb, boolean prevDirect) {
		Deque<Instruction> moveStacks = new ArrayDeque<Instruction>();
		while(!moveInsts.isEmpty()) {
			Iterator<Instruction> moveItr = moveInsts.iterator();
			while(moveItr.hasNext()) {
	
				Instruction move = moveItr.next();
				if(move.op != Operation.MOV) {
					System.err.println(move+" is not move op.");
					System.exit(-1);
				}
				// push non-interference moves into the bottoms
				if(!defReg.contains(move.arg0)) {
					moveStacks.push(move);
					moveItr.remove();
					// now no longer can be interfered
					defReg.remove(move.arg1);
				}
			}
		}
		// insert the rest
		while(!moveStacks.isEmpty()) {
			insertMove(moveStacks.pop(), destBb);
		}
		
	}
	
	/**
	 * Insert a move instruction to the last location of dest block
	 * by last means, last but before branch or CMP
	 * @param move
	 * @param dest
	 */
	private static void insertMove(Instruction move, BasicBlock dest) {
		int idx;
		for(idx=dest.instructions.size()-1; idx>=0; idx--) {
			Instruction prevBlockInst = dest.instructions.get(idx);
			if(prevBlockInst.op.isBranch() || prevBlockInst.op == Operation.CMP || prevBlockInst.op == Operation.LOAD) continue;
			break;
		}
		dest.instructions.add(idx+1, move);
		return;
	}
	
	/**
	 * do a proper coloring by first constructing
	 * interference graph from live ranges
	 * then recursively color the nodes
	 * @param list of basic block
	 * @return colored instructions
	 */
	public static List<Instruction> assign(List<BasicBlock> bbs) {
		// all instructions
		Set<Integer> universe = new HashSet<Integer>();
		Set<SimpleEdge> sparseEdges = new HashSet<SimpleEdge>();
		Set<Integer> alive = new HashSet<Integer>();
		int numInsts = 0;
		Clusters phiClusters = new Clusters();
		
		// now fill edges
		for(int i=bbs.size()-1; i>=0; i--) {
			BasicBlock bb = bbs.get(i);
			
			// first check if this block is inside a loop
			// because if it is, then its arg0 would be defined at the head of the loop
			// and thus IS alive now
			// we put arg0 in universe and alive sets
			if(bb.nextIndirect != null && bb.nextIndirect.nextDirect != null
					&& bb.index == bb.nextIndirect.nextDirect.index) {
				// if its in while block, get def in PHI's in while statement
				for(Instruction whileInst : bb.nextIndirect.instructions) {
					if(whileInst.op != Operation.PHI) 
						break; // no more phi and we dont care about the rest, exit
					if(!(whileInst.arg0 instanceof PointerArg))
						continue;
					int num = ((PointerArg)whileInst.arg0).pointer;
					sparseEdges.addAll(SimpleEdge.createEdges(num, alive));
					alive.add(num);
					universe.add(num);
				}
			}
			numInsts += bb.instructions.size();
			for(int j=bb.instructions.size()-1; j>=0; j--) {
				Instruction inst = bb.instructions.get(j);
				// here's where the pointer is defined, no longer alive
				if(inst.op.hasOutput()) alive.remove(inst.pointer.pointer);
				
				// create a cluster if the instruction is PHI
				// all pointers in the same cluster have to be assigned the same color!
				if(inst.op == Operation.PHI) {
					Set<Integer> elts = new HashSet<Integer>();
					if(inst.arg0 instanceof PointerArg) {
						// check interference
						Integer n1 = ((PointerArg)inst.arg0).pointer;
						Integer n2 = ((PointerArg)inst.pointer).pointer;
						if(!sparseEdges.contains(new SimpleEdge(n1,n2)))
							elts.add(n1);
					}
					if(inst.arg1 instanceof PointerArg) {
						// check interference
						Integer n1 = ((PointerArg)inst.arg1).pointer;
						Integer n2 = ((PointerArg)inst.pointer).pointer;
						if(!sparseEdges.contains(new SimpleEdge(n1,n2)))
							elts.add(n1);
					}
					if(!elts.isEmpty()) {
						elts.add(((PointerArg)inst.pointer).pointer);
						phiClusters.addCluster(elts);
					}
				}
				
				// arg0 is used here, add edges to all alive pointers
				if(inst.arg0 != null && inst.op != Operation.BRA 
						&& inst.arg0 instanceof PointerArg) {
					// if its a while phi, ignore it since we alrdy handled that
					if(inst.op == Operation.PHI
							&& bb.nextDirect != null && bb.nextDirect.nextIndirect != null
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
		Map<Integer, Integer> instructionColor = color(universe, sparseEdges, phiClusters, MAX_COLORS);
		instructionColor = reassignInvalidColors(instructionColor, MAX_COLORS);
		System.out.println("colors: "+instructionColor);
		List<Instruction> out = new ArrayList<Instruction>();
		for(BasicBlock bb : bbs) {
			for(Instruction inst : bb.instructions) {
				inst = assign(inst, instructionColor, MAX_COLORS);
				out.add(inst);
			}
		}
		return fixPhis(bbs, numInsts);
	}
	
	/**
	 * need this method because if the color is in one of the reserved colors
	 * this can reassign the new color
	 * right now if color > maxColors, return color+100;
	 * @param colors
	 * @param maxColors
	 * @return
	 */
	private static Map<Integer, Integer> reassignInvalidColors(Map<Integer, Integer> colors, int maxColors) {
		Map<Integer, Integer> out = new HashMap<Integer, Integer>();
		for(Entry<Integer, Integer> entry : colors.entrySet()) {
			int color = entry.getValue();
			int ptr = entry.getKey();
			if(color > maxColors) {
				// use 100 as a base for fake register
				color += 100;
			}
			out.put(ptr, color);
		}
		return out;
	}
	
	/**
	 * assume color is alrdy pre-assigned : 2 cases
	 * 1) colors = {1-MAX_COLORS(8)}
	 * 2) colors > 100 then its in fake registers
	 * @param color
	 * @return
	 */
	private static Arg getRegister(int color) {
		// 31 is the hard cap
		if(color > 31) return new SpilledRegisterArg(color);
		return new RegisterArg(color);
	}
	
	private static Instruction assign(Instruction inst, Map<Integer, Integer> colors, int maxColors) {
		// first color arg0 - ignore BRA
		if(inst.arg0 != null && inst.op != Operation.BRA 
				&& inst.arg0 instanceof PointerArg) {
			int ptr = ((PointerArg)inst.arg0).pointer;
			inst.arg0 = getRegister(colors.get(ptr));
		}
		// color arg1 - ignore cond branch
		if(inst.arg1 != null && !inst.op.isBranch() 
				&& inst.arg1 instanceof PointerArg) {
			int ptr = ((PointerArg)inst.arg1).pointer;
			inst.arg1 = getRegister(colors.get(ptr));
		}
		// color arg2 only if op has an output
		if(inst.op.hasOutput()) {
			int ptr = ((PointerArg)inst.pointer).pointer;
			/**
			 * special assignment for CMP and CALL
			 */
			if(inst.op == Operation.CMP) {
				inst.arg2 = new RegisterArg(Conf.CMP_REG_NUM);
				colors.put(inst.pointer.pointer, Conf.CMP_REG_NUM);
			} else if(inst.op == Operation.CALL) {
				inst.arg2 = new RegisterArg(Conf.RETURN_VAL_NUM);
				colors.put(inst.pointer.pointer, Conf.RETURN_VAL_NUM);
			} else {
				// TODO: correct order?
				if(colors.containsKey(ptr)) {
					// arg1 is for LOAD and maybe sth else?
					if(inst.arg1 == null) inst.arg1 = getRegister(colors.get(ptr));
					else if(!(inst.arg2 instanceof RegisterArg)) inst.arg2 = getRegister(colors.get(ptr));
				} else if(inst.arg2 instanceof RegisterArg) {
					
				} else {
					inst.op = Operation.NOOP;
					inst.arg0 = null;
					inst.arg1 = null;
					inst.arg2 = null;
					//inst.arg2 = getRegister(1000);
				}
			}
		}
		return inst;
	}
	
	/**
	 * select next node
	 * right now pick whatever is available
	 * TODO: use heuristic when spilling happens...
	 * @param universe
	 * @param edges
	 * @param maxColors
	 * @return
	 */
	private static Integer selectNode(Set<Integer> universe, Set<SimpleEdge> edges, int maxColors) {
		int minNode = Integer.MAX_VALUE;
		int minNeighbors = Integer.MAX_VALUE;
		for(Integer x : universe) {
			int numNeighbors = 0;
			for(Integer i : universe) {
				if(x == i) continue;
				SimpleEdge e = new SimpleEdge(x,i);
				if(edges.contains(e)) {
					numNeighbors++;
				}
			}
			if(numNeighbors < maxColors) return x;
			if(minNeighbors > numNeighbors) {
				minNode = x;
				minNeighbors = numNeighbors;
			}
		}
		return minNode;
//		return universe.iterator().next();
	}
	
	/**
	 * exact coloring algorithm, recursion
	 * @param universe
	 * @param edges
	 * @param maxColors
	 * @return
	 */
	private static Map<Integer, Integer> color(Set<Integer> universe, Set<SimpleEdge> edges, Clusters phis, int maxColors){
		if(universe.isEmpty()) return new HashMap<Integer, Integer>();
		if(universe.size() == 1) {
			Map<Integer, Integer> out = new HashMap<Integer, Integer>();
			out.put(universe.iterator().next(), 1);
			return out;
		}
		Integer x = selectNode(universe, edges, maxColors);
		Set<Integer> xCluster = phis.getCluster(x);
		
		Set<Integer> clusterNeighbors = new HashSet<Integer>();
		Set<SimpleEdge> clusterEdges = new HashSet<SimpleEdge>(); 
		// remove nodes and edges
		// also keep track neighbors and edges
		for(Integer xct : xCluster) {
			universe.remove(xct);
			for(Integer i : universe) {
				if(xCluster.contains(i)) continue;
				SimpleEdge e = new SimpleEdge(xct,i);
				if(edges.contains(e)) {
					clusterNeighbors.add(i);
					clusterEdges.add(e);
					edges.remove(e);
				}
			}
		}
		Map<Integer, Integer> colors = color(universe, edges, phis, maxColors);
		
		int validColor = determineColor(colors, clusterNeighbors);
		
		// add nodes back
		universe.addAll(xCluster);
		
		// add edges back
		edges.addAll(clusterEdges);
		
		// color nodes
		for(Integer elt : xCluster) {
			colors.put(elt, validColor);
		}
		return colors;
	}
	
	/**
	 * find a color for this cluster
	 * need to find a color such that it's valid for all nodes in cluster
	 * 
	 * valid color for a node means its color is different from colors
	 * of its neighbors
	 * 
	 * @param colors
	 * @param xCluster
	 * @param neighborSet
	 * @return a valid color
	 */
	private static Integer determineColor(Map<Integer, Integer> colors, Set<Integer> neighbors) {
		Set<Integer> invalidColors = new HashSet<Integer>();
		
		// get all colors from their neighbors and we shouldnt assign one from this list
		for(Integer nb : neighbors) {
			invalidColors.add(colors.get(nb));
		}
		
		// empty check
		if(invalidColors.isEmpty()) return 1;
		
		List<Integer> invalid = new ArrayList<Integer>();
		invalid.addAll(invalidColors);
		Collections.sort(invalid);
		
		// a little trick here: 
		// sort invalid colors and iterate through the list
		// for each color c in list:
		// if there is no c+1 in the sorted list, assign validColor to c+1
		// otherwise, c = max(invalid)+1
		int validColor = invalid.get(invalid.size()-1)+1;
		for(int i=1; ; i++) {
			if(!invalidColors.contains(i)) {
				validColor = i;
				break;
			}
		}
//		int validColor = invalid.get(invalid.size()-1)+1;
//		for(int i=0; i<invalid.size()-1; i++) {
//			if(invalid.get(i+1)-invalid.get(i) > 1) {
//				validColor = invalid.get(i)+1;
//				break;
//			}
//		}
		return validColor;
		
	}
}
