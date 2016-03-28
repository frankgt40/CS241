package edu.uci.ccai6.cs241.runtime;

import java.util.ArrayList;
import java.util.List;

public class Conf {
	public static final int RETURN_VAL_NUM = 27;
	public static final String RETURN_VAL_REG = "R"+RETURN_VAL_NUM;
	public static final String FRAME_P = "R28";
	public static final String STATIC_P = "R30";
	public static final String STACK_P = "R29";
	public static final int BLOCK_LEN = 4; // increase or decrease memory address by this value
	public static final int BYTE_LEN = 8;
	public static final String STACK_REG_PRE = "SR";
	public static final String LOAD_REG_1 = "R26";
	public static final String LOAD_REG_2 = "R25";
	public static final String LOAD_REG_3 = "R24";
	public static final int CMP_REG_NUM = 24;
	public static final String CMP_REG = "R"+CMP_REG_NUM;
	public static final int GEN_REG_NUM = 8;
	public static final String ZERO_REG = "R0";
	public static final int STACK_GROW_DELTA = BLOCK_LEN;
	public static final String STORE_TARGET = LOAD_REG_3;
	
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
		__savedRegs.add(Conf.FRAME_P);
		__savedRegs.add(Conf.STACK_P);
	}
	public static int getRegNum(String reg) {
		reg = reg.replaceFirst("R", "");
		return Integer.parseInt(reg);
	}
}
