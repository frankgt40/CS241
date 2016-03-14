package edu.uci.ccai6.cs241.runtime;

import java.util.ArrayList;
import java.util.List;

/*
 * 2.Locals
 * 1.Saved status
 * 0.Parameters
 */
public class FrameAbstract {
	private List<String> __parameters = new ArrayList<String>();
	private List<String> __locals = new ArrayList<String>();
	private String __funcName = null;
	
	private int __parametersLen = 0;
	private int __savedStatusLen = 0;
	private int __localsLen = 0;
	
	FrameAbstract(String name) {
		__funcName = name;
		
	}

	public String get__funcName() {
		return __funcName;
	}
	
	public List<String> getCallerSequences() {
		List<String> rsl = new ArrayList<String>();
		//
		return rsl;
	}
	
	public List<String> getCalleeSequences() {
		List<String> rsl = new ArrayList<String>();
		//
		return rsl;
	}
	
}
