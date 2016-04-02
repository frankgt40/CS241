package edu.uci.ccai6.cs241.runtime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import edu.uci.ccai6.cs241.runtime.DLXInstructions.DLX;

public class Conf extends DLX{
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
	public static final String RETURN_ADDRESS_REG = "R31";
	public static final String END_REG = "0";
	public static final boolean IS_DEBUG = false;
	
	protected static boolean IS_INITIALIZED = false;
	public static List<String> __savedRegs = new ArrayList<String>();

	public static void initialize(){
		if (IS_INITIALIZED) return;
		
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
		__savedRegs.add(Conf.RETURN_ADDRESS_REG);
//		__savedRegs.add(Conf.FRAME_P);
//		__savedRegs.add(Conf.STACK_P);
		IS_INITIALIZED = true;
	}
	public static int getRegNum(String reg) {
		reg = reg.replaceFirst("R", "");
		return Integer.parseInt(reg);
	}
	
	// called after setFakeRegs(List<String> regs)
	public static List<String> getStatusSavingSequences() {
		List<String> rsl = new ArrayList<String>();
//		rsl.add("MOV " + Conf.STACK_P + " " + Conf.LOAD_REG_1); // temp store STACK_P value, will load into FRAME_P later
		for (String regs : Conf.__savedRegs) {
			rsl.add("PUSH " + regs);
		}
		FrameAbstract frame = StackAbstract.getCurrFrame();
		frame.setCurrOffset(frame.getCurrOffset()+Conf.__savedRegs.size()*Conf.BLOCK_LEN);
//		rsl.add("MOV " + Conf.FRAME_P + " " + Conf.STACK_P); // now store it to FRAME_P
//		rsl.add("ADDi " + Conf.__savedRegs.size() * Conf.STACK_GROW_DELTA + " " + Conf.STACK_P+ " " + Conf.STACK_P);
		return rsl;
	}
	
	public static List<String> getStatusRestoreSequences() {
		List<String> rsl = new ArrayList<String>();
		FrameAbstract frame = StackAbstract.getCurrFrame();
		
		// subtract saved registers since FP starts point to saved R1
		int len = frame.getCurrOffset()-Conf.__savedRegs.size()*Conf.STACK_GROW_DELTA;
		
		rsl.add("1 SUBi "+ Conf.STACK_P + " " + len + " " + Conf.STACK_P); // Erase all the locals
		
		for (int i = Conf.__savedRegs.size()-1; i >=0 ; i--) {
			// in reverse order to pop the saved registers
			rsl.add("1 POP " + Conf.__savedRegs.get(i));
		}
		
		rsl.add("1 SUBi " + Conf.STACK_P + " " + Conf.BLOCK_LEN*StackAbstract.getCurrFrame().__parameters.keySet().size() + " " + Conf.STACK_P); // Erase all the parameters
//		rsl.add("1 MOV " + Conf.STACK_P + " " +Conf.FRAME_P); // destroy the frame
		rsl.add("1 POP " + Conf.FRAME_P);
		

		
		return rsl;
	}
	
	public static int[] getHook() {
		int[] rsl = {
				0, // For Stack pointer
				0, // For frame pointer
				0, // ADDI R31 R0 main_function_address
				0, // JSR R31
//			DLX.F1(DLX.ADDI, Conf.getRegNum(Conf.RETURN_ADDRESS_REG), Conf.getRegNum(Conf.ZERO_REG) ,StackAbstract.getFrame("main").get__startAddress()),
//			DLX.F3(DLX.JSR, Conf.getRegNum(Conf.RETURN_ADDRESS_REG))
		};
//		rsl.add("MOV " + StackAbstract.getFrame("main").get__startAddress() + " " + Conf.RETURN_ADDRESS_REG);
//		rsl.add("RET " + Conf.RETURN_ADDRESS_REG);
		
		return rsl;
	}
}
