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
	public Map<String, Local> __parameters = new HashMap<String, Local>();
//	private List<String> __locals = new ArrayList<String>();
	public  Map<String, Local> __fakeRegToMem = new HashMap<String, Local>(); // <fake register name --> memory address in frames>
	private String __funcName = null;
	
	private int __parametersLen = 0;
	private int __savedStatusLen = 0;
	private int __localsLen = 0;
	private int __currLocation = 0;
	private int __currParamLocation = 0;
	private int __startAddress = -1;
	private boolean __hasReturnValue = false;
	
	
	
	public boolean hasReturnValue() {
		return __hasReturnValue;
	}
	public void set__hasReturnValue(boolean __hasReturnValue) {
		this.__hasReturnValue = __hasReturnValue;
	}
	public int get__startAddress() {
		return __startAddress;
	}
	public void set__startAddress(int __startAddress) {
		this.__startAddress = (__startAddress-1)*Conf.BLOCK_LEN;
	}
	public FrameAbstract(String name) {
		__funcName = name;
		__savedStatusLen = Conf.__savedRegs.size();
	}
	public void setCurrOffset(int currLocation){
		this.__currLocation = currLocation;
	}
	
	public int getCurrParameterOffset() {
		return this.__currParamLocation;
	}
	public int findCurrOffset() {
		return __currLocation;
	}
	public int getCurrOffset() {
		return findCurrOffset();
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
	public void addParameter(String name, Local var) {
		if (!__parameters.containsKey(name)) {
			__parameters.put(name, var);
			__currParamLocation += Conf.STACK_GROW_DELTA;
		}
	}
//	public void setParameters(List<Local> parameters) {
//		__parameters = parameters;
//		__parametersLen = __parameters.size();
//	}
	// Called after setParameters(List<String> parameters)
//	public List<String> getCallerSequences() {
//		List<String> rsl = null;
//		if (__parameters.isEmpty()) {
//			// No parameters needed to be pushed
//		} else {
//			rsl = new ArrayList<String>();
//			for (Local parameter : __parameters) {
//				rsl.add("PUSH " + parameter.__name);
//			}
//		}
//		//
//		return rsl;
//	}
	
	
	
}
