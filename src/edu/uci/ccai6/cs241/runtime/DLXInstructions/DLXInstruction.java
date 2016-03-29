package edu.uci.ccai6.cs241.runtime.DLXInstructions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.uci.ccai6.cs241.runtime.Conf;
import edu.uci.ccai6.cs241.runtime.FrameAbstract;
import edu.uci.ccai6.cs241.runtime.Local;
import edu.uci.ccai6.cs241.runtime.LocalType;
import edu.uci.ccai6.cs241.runtime.RuntimeEnv;
import edu.uci.ccai6.cs241.runtime.StackAbstract;
import edu.uci.ccai6.cs241.ssa.Arg;
import edu.uci.ccai6.cs241.ssa.ConstArg;
import edu.uci.ccai6.cs241.ssa.Instruction;
import edu.uci.ccai6.cs241.ssa.RegisterArg;
import edu.uci.ccai6.cs241.ssa.SpilledRegisterArg;

public class DLXInstruction extends DLX {
	protected static List<DLXInstruction> __instructions = new ArrayList<DLXInstruction>();
	protected static Map<String, String> __map1 = new HashMap<String, String>(); //<"(20)", "(20) NOOP">
	protected static Map<String, Integer> __map2 = new HashMap<String, Integer>(); //<"(20) NOOP", 24>
	protected static Map<Integer, String> __lateComputePos = new HashMap<Integer, String>(); // <24, "1101">
	protected static RuntimeEnv __system = new RuntimeEnv();
	protected int __val = 0xFFFF;

//	public static void runHook() {
//		for (String instruction : Conf.getHook()) {
//			new DLXInstruction(new Instruction(instruction));
//		}
//	}
	public static Map<String, String> getMap1() {
		return __map1;
	}
	public static Map<String, Integer> getMap2() {
		return __map2;
	}
	public static void preRun(List<Instruction> instructions) {
		for (Instruction inst : instructions) {
			switch (inst.op) {
			case BGE:
			case BGT:
			case BLE:
			case BLT:
			case BNE:
			case BEQ:
				String lable = inst.arg1.toString();
				String target = "";
				if (!__map1.containsKey(lable)) {
					for (Instruction one : instructions) {
						if (("(" + one.pointer.pointer + ")").equals(lable)) {
							target = one.toString();
						}
					}
					__map1.put(lable, target);
				}
				break;
			case BRA:
				lable = inst.arg0.toString();
				target = "";
				if (!__map1.containsKey(lable)) {
					for (Instruction one : instructions) {
						if (("(" + one.pointer.pointer+")").equals(lable)) {
							target = one.toString();
						}
					}
					__map1.put(lable, target);
				}
				break;
			default:
				break;
			}
		}
	}
	public static int[] getMachineCodes() {
		int[] rsl = new int[__instructions.size()];
		for (int i = 0; i < __instructions.size(); i++) {
			rsl[i] = __instructions.get(i).getVal();
		}
		return rsl;
	}

	public static Map<Integer, String> getLateComputePos() {
		return __lateComputePos;
	}
	public static List<DLXInstruction> getInstructions() {
		return __instructions;
	}

	protected void wrong(String args) {
		System.err.println("Wrong! " + args);
		System.exit(-1);
	}

	public int getRegNum(String reg) {
		reg = reg.replaceFirst("R", "");
		return Integer.parseInt(reg);
	}

	public int getArg(Arg arg, String loadReg) {
		String artStr = null;
		int rsl = 0;
//		StackAbstract stack = __system.getStack();
//		if (arg instanceof RegisterArg) {
//			if (arg instanceof SpilledRegisterArg) {
////				artStr = stack.getMemInFrame(arg.toString());
//				Instruction tmp = new Instruction("LOAD " + loadReg + " " + artStr);
//				new DLXInstruction(tmp);
//				rsl = getRegNum(loadReg);
//			} else {
//				rsl = ((RegisterArg) arg).getNum();
//			}
//		} else {
//			// Something is wrong
//			wrong("there should be a register!");
//		}
		return rsl;
	}
	protected int noopInst() {
		return DLX.F2(0, 0, 0, 0);
	}
	protected void insertMap2(Instruction inst) {
		for (String key : __map1.keySet()) {
			if (inst.toString().equals(__map1.get(key))) {
				__map2.put(inst.toString(), __instructions.size()+1);
			}
		}
	}
	public void bellowValAssig(Instruction inst) {
		insertMap2(inst);
		__instructions.add(this);
	}
	
	public static void postCompute() {
		// Initialize stack and frame pointer
		__instructions.get(0).setVal(DLX.F1(DLX.ADDI, Conf.getRegNum(Conf.STACK_P), Conf.getRegNum(Conf.ZERO_REG), (__instructions.size()+1)*Conf.BLOCK_LEN));
		__instructions.get(1).setVal(DLX.F1(DLX.ADDI, Conf.getRegNum(Conf.FRAME_P), Conf.getRegNum(Conf.ZERO_REG), (__instructions.size()+1)*Conf.BLOCK_LEN));
		__instructions.get(2).setVal(DLX.F1(DLX.ADDI, Conf.getRegNum(Conf.RETURN_ADDRESS_REG), Conf.getRegNum(Conf.ZERO_REG) ,StackAbstract.getFrame("main").get__startAddress()));
		__instructions.get(3).setVal(DLX.F3(DLX.JSR, Conf.getRegNum(Conf.RETURN_ADDRESS_REG)));
		for (Integer key : __lateComputePos.keySet()) {
			int code = __instructions.get(key-1).getVal();
			String tmp = __lateComputePos.get(key);
			tmp = __map1.get(tmp);
			int alpha = __map2.get(tmp);
			code += (alpha)*Conf.BLOCK_LEN;
			__instructions.get(key - 1).setVal(code);
		}
	}
	public static int[] getMachineCode() {
		int[] rsl = new int[__instructions.size()];
		for (int i = 0; i < __instructions.size(); i++) {
			rsl[i] = __instructions.get(i).getVal();
		}
		return rsl;
	}
	public void setVal(int val) {
		__val = val;
	}
	public DLXInstruction() {
		
	}
	public DLXInstruction(Instruction instruction) {
		// First, treat those fake registers
		Arg arg1 = instruction.arg0;
		Arg arg2 = instruction.arg1;
		Arg arg3 = instruction.arg2;
		FrameAbstract currFrame = StackAbstract.getCurrFrame();
		
		// If the src args have fake ones, load it here
		if (arg1 instanceof SpilledRegisterArg) {
			if (!currFrame.__fakeRegToMem.containsKey(arg1.toString())) {
				// If current frame has not recorded this variable in memory
				new DLXInstruction(new Instruction("1 ADDi " + Conf.STACK_GROW_DELTA + " " + Conf.STACK_P + " " + Conf.STACK_P));
				Local local = new Local();
				local.__len = Conf.BLOCK_LEN;
				local.__name = arg2.toString();
				local.__offset = currFrame.getCurrOffset();
				local.__type = LocalType.VAR;
				currFrame.setCurrOffset(currFrame.getCurrOffset() + Conf.STACK_GROW_DELTA);
				currFrame.__fakeRegToMem.put(arg2.toString(), local);
			}
			Local local = currFrame.addOrGetLocal(arg1);
			new DLXInstruction(new Instruction("1 LOAD " + local.__offset + " " + Conf.LOAD_REG_1));
			
			Arg argNew = new RegisterArg(Conf.getRegNum(Conf.LOAD_REG_1));
			instruction.setArg(1, argNew);
			
		}
		if (arg2 instanceof SpilledRegisterArg) {
			if (!currFrame.__fakeRegToMem.containsKey(arg2.toString())) {
				// If current frame has not recorded this variable in memory
				new DLXInstruction(new Instruction("1 ADDi " + Conf.STACK_GROW_DELTA + " " + Conf.STACK_P + " " + Conf.STACK_P));
				Local local = new Local();
				local.__len = Conf.BLOCK_LEN;
				local.__name = arg2.toString();
				local.__offset = currFrame.getCurrOffset();
				local.__type = LocalType.VAR;
				currFrame.setCurrOffset(currFrame.getCurrOffset() + Conf.STACK_GROW_DELTA);
				currFrame.__fakeRegToMem.put(arg2.toString(), local);
			}
			Local local = currFrame.addOrGetLocal(arg2);
			new DLXInstruction(new Instruction("1 LOAD " + local.__offset + " " + Conf.LOAD_REG_2));
			
			Arg argNew = new RegisterArg(Conf.getRegNum(Conf.LOAD_REG_2));
			instruction.setArg(2, argNew);
		}
		
		// If arg3 is fake, have to assign it with a resgister and store it later
		if (arg3 instanceof SpilledRegisterArg) {
			if (!currFrame.__fakeRegToMem.containsKey(arg3.toString())) {
				// If current frame has not recorded this variable in memory
				new DLXInstruction(new Instruction("1 ADDi " + Conf.STACK_GROW_DELTA + " " + Conf.STACK_P + " " + Conf.STACK_P));
				Local local = new Local();
				local.__len = Conf.BLOCK_LEN;
				local.__name = arg2.toString();
				local.__offset = currFrame.getCurrOffset();
				local.__type = LocalType.VAR;
				currFrame.setCurrOffset(currFrame.getCurrOffset() + Conf.STACK_GROW_DELTA);
				currFrame.__fakeRegToMem.put(arg2.toString(), local);
			}
			Arg argNew = new RegisterArg(Conf.getRegNum(Conf.STORE_TARGET));
			instruction.setArg(3, argNew);
		}
		
		// F1
		switch (instruction.op) {
		case ADDi:
			new ADDiInst(instruction);
			break;
		case MULi:
			new MULiInst(instruction);
			break;
		case DIVi:
			new DIViInst(instruction);
			break;
		case SUBi:
			new SUBiInst(instruction);
			break;
		case LOAD:
			new LOADInst(instruction);
			break;
		case STORE: 
			new STOREInst(instruction);
			break;
		case BGE: 
			new BGEInst(instruction);
			break;
		case BGT: 
			new BGTInst(instruction);
			break;
		case BLE: 
			new BLEInst(instruction);
			break;
		case BLT: 
			new BLTInst(instruction);
			break;
		case BNE:
			new BNEInst(instruction);
			break;
		case BEQ: 
			new BEQInst(instruction);
			break;
		case BRA:
			new BRAInst(instruction);
			break;
		case ADDA:
			new ADDAInst(instruction);
			break;
		case MOVE: 
			new MOVEInst(instruction);
			break;
		case PUSH:
			new PUSHInst(instruction);
			break;
		case POP:
			new POPInst(instruction);
			break;
		case MOV:
			new MOVEInst(instruction);
			break;
		// F2
		case ADD:
			new ADDInst(instruction);
			break;
		case MUL:
			new MULInst(instruction);
			break;
		case SUB:
			new SUBInst(instruction);
			break;
		case DIV:
			new DIVInst(instruction);
			break;
		case CMP:
			new CMPInst(instruction);
			break;
		// F3{
		case CALL:
			new CALLInst(instruction);
			break;
		// Others
		case FUNC:
			new FUNCInst(instruction);
			break;
		case NOOP:
			new NOOPInst(instruction);
			break;
		case RET:
			new RETInst(instruction);
			break;
		case PHI:
		case PTR:
		default:// means its value is the same as pointer's or constant
			break;
		}
		
		// If the dst arg (arg3) is fake, then store it!!!!
		if (arg3 instanceof SpilledRegisterArg) {
			Local local = currFrame.addOrGetLocal(arg3);
			// STORE VAL TARGET_ADDRESS
			new DLXInstruction(new Instruction("1 STORE " + Conf.STORE_TARGET+ " " + local.__offset));
		}
	}

	public int getVal() {
		return __val;
	}

	public static int str2int(String number) {
		String numberTmp = "";
		int len = number.length();
		if (len > 32) {
			int left = number.length() - 32;
			if (number.toCharArray()[left] == '0') {
				numberTmp = number.substring(left, len);
				return Integer.parseInt(numberTmp, 2);
			} else {
				for (char bit : number.substring(left, len).toCharArray()) {
					numberTmp += (bit == '0') ? "1" : "0";
				}
			}
		} else if (len <= 31){
			numberTmp = number;
			return Integer.parseInt(numberTmp, 2);
		} else {
			if (number.toCharArray()[0] == '0') {
				numberTmp = number;
				return Integer.parseInt(numberTmp, 2);
			} else {
				for (char bit : number.substring(0, len).toCharArray()) {
					numberTmp += (bit == '0') ? "1" : "0";
				}
			}
		}
		int rsl = Integer.parseInt(numberTmp, 2);
		rsl += 1;
		rsl = -rsl;
		return rsl;
	}

	public static void main(String[] args) {
		System.out.println(Integer.toBinaryString(str2int("01000000001000000000000000000011")));
	}
}
