package edu.uci.ccai6.cs241.runtime;

import java.util.ArrayList;
import java.util.List;

public class Conf {
	public static final String RETURN_VAL_REG = "R28";
	public static final String FRAME_P = "R29";
	public static final String STACK_P = "R30";
	public static final int BLOCK_LEN = 4; // increase or decrease memory address by this value
	public static final int BYTE_LEN = 8;
	
	
	public static List<String> __savedRegs = new ArrayList<String>();

	{
		// Set the registers needed to be saved.
		__savedRegs.add("R1");
		__savedRegs.add("R2");
		__savedRegs.add("R3");
		__savedRegs.add("R4");
		__savedRegs.add("R5");
		__savedRegs.add("R6");
		__savedRegs.add("R7");
		__savedRegs.add("R8");
		__savedRegs.add("R9");
		__savedRegs.add("R10");
	}
}
