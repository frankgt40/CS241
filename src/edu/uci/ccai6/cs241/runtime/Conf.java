package edu.uci.ccai6.cs241.runtime;

import java.util.ArrayList;
import java.util.List;

public class Conf {
	public static final String RETURN_VALUE_REGISTER = "R28";
	public static final String FRAME_P = "R29";
	public static final String STACK_P = "R30";
	public static final int BLOCK_LEN = 4; // increase or decrease memory address by this value
	public static final int BYTE_LEN = 8;
	
	
	public static List<String> __savedRegisters = new ArrayList<String>();

	{
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
