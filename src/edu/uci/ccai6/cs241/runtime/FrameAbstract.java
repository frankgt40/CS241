package edu.uci.ccai6.cs241.runtime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.uci.ccai6.cs241.ssa.Arg;
import edu.uci.ccai6.cs241.ssa.SpilledRegisterArg;

/*
 * 2.Locals (maybe also add locals for parameters)
 * 1.Saved status
 * 0.Parameters 
 */
public class FrameAbstract {
	private List<Local> __parameters = new ArrayList<Local>();
//	private List<String> __locals = new ArrayList<String>();
	public  Map<String, Local> __fakeRegToMem = new HashMap<String, Local>(); // <fake register name --> memory address in frames>
	private String __funcName = null;
	
	private int __parametersLen = 0;
	private int __savedStatusLen = 0;
	private int __localsLen = 0;
	private int __currLocation = 0;
	public FrameAbstract(String name) {
		__funcName = name;
		__savedStatusLen = Conf.__savedRegs.size();
	}
	public void setCurrOffset(int currLocation){
		this.__currLocation = currLocation;
	}
	public int findCurrOffset() {
		return __currLocation;
	}
	public Local addOrGetLocal(Arg arg) {
		if (arg instanceof SpilledRegisterArg) {
			String key = arg.toString();
			if (__fakeRegToMem.containsKey(key)) {
				return __fakeRegToMem.get(key);
			} else {
				Local local = new Local();
				local.__name = key;
				local.__type = LocalType.VAR;
				local.__offset = findCurrOffset();
				__currLocation += Conf.STACK_GROW_DELTA;
				__fakeRegToMem.put(key, local);
				return local;
			}
		} else {
			System.err.println("addLocal: must add a SpilledRegisterArg!");
			return null;
		}
	}
//	public List<String> getLocals() {
//		return __locals;
//	}
	public String get__funcName() {
		return __funcName;
	}
	public void setParameters(List<Local> parameters) {
		__parameters = parameters;
		__parametersLen = __parameters.size();
	}
	// Called after setParameters(List<String> parameters)
	public List<String> getCallerSequences() {
		List<String> rsl = null;
		if (__parameters.isEmpty()) {
			// No parameters needed to be pushed
		} else {
			rsl = new ArrayList<String>();
			for (Local parameter : __parameters) {
				rsl.add("PUSH " + parameter.__name);
			}
		}
		//
		return rsl;
	}
	
	// called after setFakeRegs(List<String> regs)
	public static List<String> getCalleeSequences() {
		List<String> rsl = new ArrayList<String>();
		
		for (String regs : Conf.__savedRegs) {
			rsl.add("PUSH " + regs);
		}
		
		
		
		//
		return rsl;
	}
	
}
