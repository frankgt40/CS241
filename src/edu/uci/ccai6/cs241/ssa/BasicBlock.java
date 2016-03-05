package edu.uci.ccai6.cs241.ssa;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Here's BasicBlock construction
 * Direct always spawned to the left and indirect is to the right
 * Thus, given a block, an incoming direct should be from right
 * and an incoming indirect is from left
 * 
 * Example:
 * 
 * For if-else:
 * 				Beginning Block
 * 					/   \
 * 		  Direct   /     \ Indirect
 * 				  If	 Else
 *                 \     /
 *        Indirect  \   /  Direct
 *                  Joined
 * 
 * For if-no else:
 * 				Beginning Block
 * 					/   \
 * 		  Direct   /     \ 
 * 				  If	  |
 *      ------    /       |
 *     /      \  /        | Indirect
 *     |     Joined       |
 *     \                  /
 *      ------------------
 * For while:
 * 
 * 								  	 Beginning
 * 							   Direct   /
 * 									   /
 * 							   -------/----------
 * 								\    /			 \
 * 							 While Comp			 |
 * 						Direct	/   \ Indirect   |  Indirect
 * 							   /     \			 |
 * 						   In while   \			 |
 * 								\	  Follow	 /
 *  							 ----------------
 * @author norrathep
 *
 */
public class BasicBlock {
	
	public int index = 0;
	String scope = null;
	public BasicBlock nextDirect, nextIndirect;
	public BasicBlock prevDirect, prevIndirect;
	
	// contains latest variables reachable at this block
	Map<String, Integer> ssaVars = new HashMap<String, Integer>();
	public List<Instruction> instructions = new LinkedList<Instruction>();
	
	public BasicBlock(int ind) {
		index = ind;
	}
	
	public void updateVar(String var, int indx) {
		if(!ssaVars.containsKey(var) || ssaVars.get(var) < indx) {
			ssaVars.put(var, indx);
		}
	}
	
	public void add(Instruction inst) {
		instructions.add(inst);
	}
	
	public void mergeVars(BasicBlock two) {
		for(Entry<String, Integer> e2 : two.ssaVars.entrySet()) {
			if(!ssaVars.containsKey(e2.getKey()) || ssaVars.get(e2.getKey()) < e2.getValue()) {
				ssaVars.put(e2.getKey(), e2.getValue());
			}
		}
	}
	
	
}
