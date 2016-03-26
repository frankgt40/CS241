package edu.uci.ccai6.cs241.runtime.DLXInstructions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.uci.ccai6.cs241.runtime.Conf;
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
		StackAbstract stack = __system.getStack();
		if (arg instanceof RegisterArg) {
			if (arg instanceof SpilledRegisterArg) {
				artStr = stack.getMemInFrame(arg.toString());
				Instruction tmp = new Instruction("LOAD " + loadReg + " " + artStr);
				new DLXInstruction(tmp);
				rsl = getRegNum(loadReg);
			} else {
				rsl = ((RegisterArg) arg).getNum();
			}
		} else {
			// Something is wrong
			wrong("there should be a register!");
		}
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
		// F1
		switch (instruction.op) {
		case ADDi:
			new ADDiInst(instruction);
			return;
		case MULi:
			new MULiInst(instruction);
			return;
		case DIVi:
			new DIViInst(instruction);
			return;
		case SUBi:
			new SUBiInst(instruction);
			return;
		case LOAD: //NOT-FINISHED!!!!@@@@@@@@@@@@@@@@@@@@@@@
			op = DLX.LDW;
			break;
		case STORE: //NOT-FINISHED!!!!@@@@@@@@@@@@@@@@@@@@@@@
			op = DLX.STW;
			break;
		case BGE: 
			new BGEInst(instruction);
			return;
		case BGT: 
			new BGTInst(instruction);
			return;
		case BLE: 
			new BLEInst(instruction);
			return;
		case BLT: 
			new BLTInst(instruction);
			return;
		case BNE:
			new BNEInst(instruction);
			return;
		case BEQ: 
			new BEQInst(instruction);
			return;
		case BRA:
			new BRAInst(instruction);
			return;
		case ADDA:
			new ADDAInst(instruction);
			return;
		case MOVE: 
			new MOVEInst(instruction);
			return;
		case PUSH:
			new PUSHInst(instruction);
			return;
		case POP:
			new POPInst(instruction);
			return;
		case MOV:
			new MOVEInst(instruction);
			return;
		// F2
		case ADD:
			new ADDInst(instruction);
			return;
		case MUL:
			new MULInst(instruction);
			return;
		case SUB:
			new SUBInst(instruction);
			return;
		case DIV:
			new DIVInst(instruction);
			return;
		case CMP:
			new CMPInst(instruction);
			return;
		// F3{
		case CALL:
			new CALLInst(instruction);
			return;
		// Others
		case FUNC:
		case PHI:
		case NOOP:
			new NOOPInst(instruction);
			return;
		case PTR:
		default:// means its value is the same as pointer's or constant
			return;
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
