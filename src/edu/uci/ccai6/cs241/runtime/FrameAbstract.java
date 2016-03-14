package edu.uci.ccai6.cs241.runtime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
 * 2.Locals (maybe also add locals for parameters)
 * 1.Saved status
 * 0.Parameters 
 */
public class FrameAbstract {
	private List<String> __parameters = new ArrayList<String>();
	private List<String> __locals = new ArrayList<String>();
	private Map<String, String> __fakeRegToMem = new HashMap<String, String>(); // <fake register name --> memory address in frames>
	private String __funcName = null;
	
	private int __parametersLen = 0;
	private int __savedStatusLen = 0;
	private int __localsLen = 0;
	
	FrameAbstract(String name) {
		__funcName = name;
		__savedStatusLen = Conf.__savedRegs.size();
	}

	public String get__funcName() {
		return __funcName;
	}
	public void setParameters(List<String> parameters) {
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
			for (String parameter : __parameters) {
				rsl.add("PUSH " + parameter);
			}
		}
		//
		return rsl;
	}
	
	// called after setFakeRegs(List<String> regs)
	public List<String> getCalleeSequences() {
		List<String> rsl = new ArrayList<String>();
		
		for (String regs : Conf.__savedRegs) {
			rsl.add("PUSH " + regs);
		}
		rsl.add("PUSH " + Conf.FRAME_P);
		rsl.add("PUSH " + Conf.STACK_P);
		
		
		
		//
		return rsl;
	}
	
}
