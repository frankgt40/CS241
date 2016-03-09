package edu.uci.ccai6.cs241.RA;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.uci.ccai6.cs241.Parser;
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
	
	// TODO: this is clearly wrong -> check with test10
	// Do nothing now. YEAH! since phis are already fixed
	private static List<Instruction> fixPhis(List<BasicBlock> bbs, int numInsts) {

		List<Instruction> out = new ArrayList<Instruction>();
		// now fix PHI's
		for(BasicBlock bb : bbs) {
			for(Instruction inst : bb.instructions) {
				if(inst.op != Operation.PHI) continue;
				
				// TODO: the order of PHI MATTERS now when converting out of SSA
				// i.e in while loop
				// (152) PHI R3 R6 R6 
				// (153) PHI R6 R5 R5 -> supposed to use R6 in previous loop
				// (xxx) PHI R5 R6 R6 -> hopefully this case wouldnt happen -> deadlock here
				// thus MOVE R6 -> R5 has to come before MOVE R3 -> R6 when doing copy
				
				// if its PHI R1 R2 R3 -> remove this and 
				// add MOVE R1->R3 at prevIndirect and MOVE R2->R3 at prevDirect
				// has to be 1 higher because there's a BRA instruction at the end of the block
				if(!inst.arg0.equals(inst.arg2)) {
					// TODO: switch pointer of BGT and CMP?
					
					// put it at the end but before branch and CMP instruction
					for(int idx=bb.prevIndirect.instructions.size()-1; idx>=0; idx--) {
						Instruction prevBlockInst = bb.prevIndirect.instructions.get(idx);
						if(prevBlockInst.op.isBranch() || prevBlockInst.op == Operation.CMP) continue;

						bb.prevIndirect.instructions.add(idx+1, 
								new Instruction(numInsts+" MOVE "+inst.arg0+" "+inst.arg2));
						break;
					}
				}
				numInsts++;
				if(!inst.arg1.equals(inst.arg2)) {
					for(int idx=bb.prevDirect.instructions.size()-1; idx>=0; idx--) {
						Instruction prevBlockInst = bb.prevDirect.instructions.get(idx);
						if(prevBlockInst.op.isBranch() || prevBlockInst.op == Operation.CMP) continue;

						bb.prevDirect.instructions.add(idx+1, 
								new Instruction(numInsts+" MOVE "+inst.arg1+" "+inst.arg2));
						break;
						
					}
//					bb.prevDirect.instructions.add(
//							new Instruction(numInsts+" MOVE "+inst.arg1+" "+inst.arg2));
				}
				numInsts++;
//				inst.op = Operation.NOOP;
//				inst.arg0 = null;
//				inst.arg1 = null;
//				inst.arg2 = null;
			}
		}
		for(BasicBlock bb : bbs) {
			for(Instruction inst : bb.instructions) {
				out.add(inst);
			}
		}
		return out;
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
		for(BasicBlock bb : bbs) {
			for(Instruction inst : bb.instructions) {
				System.out.println(inst);
			}
		}
		phiClusters.printAll();
		Map<Integer, Integer> instructionColor = color(universe, sparseEdges, phiClusters, MAX_COLORS);
		System.out.println(instructionColor);
		List<Instruction> out = new ArrayList<Instruction>();
		for(BasicBlock bb : bbs) {
			for(Instruction inst : bb.instructions) {
				inst = assign(inst, instructionColor);
				out.add(inst);
				System.out.println(inst);
			}
		}
		return fixPhis(bbs, numInsts);
	}
	
	private static Instruction assign(Instruction inst, Map<Integer, Integer> colors) {
		// first color arg0 - ignore BRA
		if(inst.arg0 != null && inst.op != Operation.BRA 
				&& inst.arg0 instanceof PointerArg) {
			int ptr = ((PointerArg)inst.arg0).pointer;
			inst.arg0 = new RegisterArg(colors.get(ptr));
		}
		// color arg1 - ignore cond branch
		if(inst.arg1 != null && !inst.op.isBranch() 
				&& inst.arg1 instanceof PointerArg) {
			int ptr = ((PointerArg)inst.arg1).pointer;
			inst.arg1 = new RegisterArg(colors.get(ptr));
		}
		// color arg2 only if op has an output
		if(inst.op.hasOutput()) {
			int ptr = ((PointerArg)inst.pointer).pointer;
			/**
			 * special assignment for CMP and CALL
			 */
			if(inst.op == Operation.CMP) {
				inst.arg2 = new RegisterArg(Parser.CMP_REG);
			} else if(inst.op == Operation.CALL) {
				inst.arg2 = new RegisterArg(Parser.RTR_REG);
			} else {
			
				if(colors.containsKey(ptr)) {
					inst.arg2 = new RegisterArg(colors.get(ptr));
				} else inst.arg2 = new RegisterArg(1000);
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
		return universe.iterator().next();
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
		
		// we need to color x and xCluster the same color
		// remove all nodes in xCluster and their edges as well
		System.out.println("x: "+x);
		System.out.println("xCluster: "+xCluster);
		
		// map from node -> its neighborss
		Map<Integer, HashSet<Integer>> neighborSet = new HashMap<Integer, HashSet<Integer>>();
		
		// remove clusters from universe as well as their edges
		for(Integer xct : xCluster) {
			universe.remove(xct);
			HashSet<Integer> neighbors = new HashSet<Integer>();
			for(Integer i : universe) {
				SimpleEdge e = new SimpleEdge(xct,i);
				if(edges.contains(e)) {
					neighbors.add(i);
					edges.remove(e);
				}
			}
			neighborSet.put(xct, neighbors);
		}
		System.out.println(universe);
		Map<Integer, Integer> colors = color(universe, edges, phis, maxColors);
		
		int validColor = determineColor(colors, xCluster, neighborSet);
		for(Integer elt : xCluster) {
			universe.add(elt);
			
			// find elt's neighbor
			Set<Integer> neighbors = neighborSet.get(elt);
			
			// put edges back
			for(Integer nb : neighbors) {
				edges.add(new SimpleEdge(elt,nb));
			}
			
			// color elt
			colors.put(elt, validColor);
		}
		
		
//		TODO: delete when things work
//		// now put x back in and try to color x
//		universe.add(x);
//		int xColor = 1;
//		if(xNeighbors.isEmpty()) {
//			// no neighbor -> assign to reg1
//			out.put(x, xColor);
//		} else {
//			// assign different color from x's neighbor
//			List<Integer> neighborColors = new ArrayList<Integer>();
//			for(Integer nb : xNeighbors) {
//				neighborColors.add(out.get(nb));
//				edges.add(new SimpleEdge(x,nb));
//			}
//			
//			// a little trick here: 
//			// sort neighbors' color and assign to color+1 
//			// if there is no color+1 in the sorted list
//			// otherwise its color is the highest color + 1
//			System.out.println(neighborColors);
//			Collections.sort(neighborColors);
//			xColor = neighborColors.get(neighborColors.size()-1)+1;
//			for(int i=0; i<neighborColors.size()-1; i++) {
//				if(neighborColors.get(i+1)-neighborColors.get(i) > 1) {
//					xColor = neighborColors.get(i)+1;
//					break;
//				}
//			}
//			out.put(x, xColor);
//		}
//		
//		// now assign all elements in xCluster to same color
//		// also put them and their edges back to universe and edges
//		for(Integer xct : xCluster) {
//			if(x == xct) continue; // skip x
//			universe.add(xct);
//			List<Integer> neighborColors = new ArrayList<Integer>();
//			// put xct's edges back and get its neighbors' colors
//			for(Integer nb : universe) {
//				SimpleEdge e = new SimpleEdge(xct,nb);
//				if(edges.contains(e)) {
//					neighborColors.add(out.get(nb));
//					edges.add(new SimpleEdge(xct,nb));
//				}
//			}
//			out.put(xct, xColor);
//		}
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
	private static Integer determineColor(Map<Integer, Integer> colors,
			Set<Integer> xCluster, Map<Integer, HashSet<Integer>> neighborSet) {
		Set<Integer> invalidColors = new HashSet<Integer>();
		
		// get all colors from their neighbors and we shouldnt assign one from this list
		for(Integer x : xCluster) {
			HashSet<Integer> neighbors = neighborSet.get(x);
			for(Integer nb : neighbors) {
				invalidColors.add(colors.get(nb));
			}
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
		for(int i=0; i<invalid.size()-1; i++) {
			if(invalid.get(i+1)-invalid.get(i) > 1) {
				validColor = invalid.get(i)+1;
				break;
			}
		}
		return validColor;
		
	}
}
