package edu.uci.ccai6.cs241.runtime;

import java.util.ArrayList;
import java.util.List;

public class FrameAbstract {
	private List<String> __parameters = new ArrayList<String>();
	private final String __returnValue = "R28";
	private List<String> __savedRegisters = new ArrayList<String>();
	private List<String> __locals = new ArrayList<String>();
	
	FrameAbstract() {
		// Set the registers needed to be saved.
		__savedRegisters.add("R1");
		__savedRegisters.add("R2");
		__savedRegisters.add("R3");
		__savedRegisters.add("R4");
		__savedRegisters.add("R5");
		__savedRegisters.add("R6");
		__savedRegisters.add("R7");
		__savedRegisters.add("R8");
		__savedRegisters.add("R9");
		__savedRegisters.add("R10");
		
	}
	
}
