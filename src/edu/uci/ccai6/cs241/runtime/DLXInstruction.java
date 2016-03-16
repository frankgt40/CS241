package edu.uci.ccai6.cs241.runtime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.uci.ccai6.cs241.ssa.Arg;
import edu.uci.ccai6.cs241.ssa.ConstArg;
import edu.uci.ccai6.cs241.ssa.Instruction;
import edu.uci.ccai6.cs241.ssa.RegisterArg;
import edu.uci.ccai6.cs241.ssa.SpilledRegisterArg;

public class DLXInstruction extends DLX {
	private static List<DLXInstruction> __instructions = new ArrayList<DLXInstruction>();
	private static Map<String, String> __map1 = new HashMap<String, String>(); //<"(20)", "(20) NOOP">
	private static Map<String, Integer> __map2 = new HashMap<String, Integer>(); //<"(20) NOOP", 24>
	private static Map<Integer, String> __lateComputePos = new HashMap<Integer, String>(); // <24, "1101">
	private static RuntimeEnv __system = new RuntimeEnv();
	private int __val = 0xFFFF;

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

	private void wrong(String args) {
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
	private int noopInst() {
		return DLX.F2(0, 0, 0, 0);
	}
	private void insertMap2(Instruction inst) {
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
			code += (alpha-1)*Conf.BLOCK_LEN;
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
	DLXInstruction(Instruction instruction) {
		boolean isSet = false;
		int op = 0, arg1 = 0, arg2 = 0, arg3 = 0;
		Arg argI1 = instruction.arg0;
		Arg argI2 = instruction.arg1;
		Arg argI3 = instruction.arg2;
		String artStr1 = null;
		String artStr2 = null;
		// F1
		switch (instruction.op) {
		case ADDi:
			op = DLX.ADDI;
			isSet = false;
			if (argI1 instanceof ConstArg) {
				if (argI2 instanceof RegisterArg) {
					// Special case!
					arg2 = getRegNum(argI2.toString());
					new DLXInstruction(new Instruction("1 MOV " + argI1.toString() + " " + Conf.LOAD_REG_1));
					// Then arg1 and arg2 are both registers
					// We use F2 type
					__val = DLX.F2(DLX.ADD, getRegNum(argI3.toString()), getRegNum(Conf.LOAD_REG_1), arg2);
					bellowValAssig(instruction);
//					__instructions.add(this);
					return;
				}
				arg1 = Integer.parseInt(argI1.toString());
			} else {
				arg1 = getRegNum(argI1.toString());
			}
			if (argI2 instanceof ConstArg) {
				arg2 = Integer.parseInt(argI2.toString());
			} else {
				arg2 = getRegNum(argI2.toString());
			}
			arg3 = getRegNum(argI3.toString());
			__val = DLX.F1(op, arg3, arg1, arg2);
			bellowValAssig(instruction);
//			__instructions.add(this);
			return;
		case MULi:
			op = DLX.MULI;
			if (argI1 instanceof ConstArg) {
				if (argI2 instanceof RegisterArg) {
					// Special case!
					arg2 = getRegNum(argI2.toString());
					new DLXInstruction(new Instruction("1 MOV " + argI1.toString() + " " + Conf.LOAD_REG_1));
					// Then arg1 and arg2 are both registers
					// We use F2 type
					__val = DLX.F2(DLX.MUL, getRegNum(argI3.toString()), getRegNum(Conf.LOAD_REG_1), arg2);
					bellowValAssig(instruction);
//					__instructions.add(this);
					return;
				}
				arg1 = Integer.parseInt(argI1.toString());
			} else {
				arg1 = getRegNum(argI1.toString());
			}
			if (argI2 instanceof ConstArg) {
				arg2 = Integer.parseInt(argI2.toString());
			} else {
				arg2 = getRegNum(argI2.toString());
			}
			arg3 = getRegNum(argI3.toString());
			__val = DLX.F1(op, arg3, arg1, arg2);
			bellowValAssig(instruction);
//			__instructions.add(this);
			return;
		case DIVi:
			op = DLX.DIVI;
			if (argI1 instanceof ConstArg) {
				if (argI2 instanceof RegisterArg) {
					arg2 = getRegNum(argI2.toString());
					// Special case!
					new DLXInstruction(new Instruction("1 MOV " + argI1.toString() + " " + Conf.LOAD_REG_1));
					// Then arg1 and arg2 are both registers
					// We use F2 type
					__val = DLX.F2(DLX.DIV, getRegNum(argI3.toString()), getRegNum(Conf.LOAD_REG_1), arg2);
					bellowValAssig(instruction);
//					__instructions.add(this);
					return;
				}
				arg1 = Integer.parseInt(argI1.toString());
			} else {
				arg1 = getRegNum(argI1.toString());
			}
			if (argI2 instanceof ConstArg) {
				arg2 = Integer.parseInt(argI2.toString());
			} else {
				arg2 = getRegNum(argI2.toString());
			}
			arg3 = getRegNum(argI3.toString());
			__val = DLX.F1(op, arg3, arg1, arg2);
			bellowValAssig(instruction);
//			__instructions.add(this);
			return;
		case SUBi:
			op = DLX.SUBI;
			isSet = false;
			if (argI1 instanceof ConstArg) {
				if (argI2 instanceof RegisterArg) {
					// Special case!
					arg2 = getRegNum(argI2.toString());
					new DLXInstruction(new Instruction("1 MOV " + argI1.toString() + " " + Conf.LOAD_REG_1));
					// Then arg1 and arg2 are both registers
					// We use F2 type
					__val = DLX.F2(DLX.SUB, getRegNum(argI3.toString()), getRegNum(Conf.LOAD_REG_1), arg2);
					bellowValAssig(instruction);
//					__instructions.add(this);
					return;
				}
				arg1 = Integer.parseInt(argI1.toString());
			} else {
				arg1 = getRegNum(argI1.toString());
			}
			if (argI2 instanceof ConstArg) {
				arg2 = Integer.parseInt(argI2.toString());
			} else {
				arg2 = getRegNum(argI2.toString());
			}
			arg3 = getRegNum(argI3.toString());
			__val = DLX.F1(op, arg3, arg1, arg2);
			bellowValAssig(instruction);
//			__instructions.add(this);
			return;
		case LOAD: //NOT-FINISHED!!!!@@@@@@@@@@@@@@@@@@@@@@@
			op = DLX.LDW;
			isSet = false;
			break;
		case STORE: //NOT-FINISHED!!!!@@@@@@@@@@@@@@@@@@@@@@@
			op = DLX.STW;
			isSet = false;
			break;
		case BGE: //NOT-FINISHED!!!!@@@@@@@@@@@@@@@@@@@@@@@
			op = DLX.BGE;
			isSet = false;
//			int c = __map2.get(__map1.get(instruction.arg1));
			int a = getRegNum(instruction.arg0.toString());
			__val = DLX.F1(op, a, 0, 0); //C is computed later
			__lateComputePos.put(__instructions.size()+1, instruction.arg1.toString());
			bellowValAssig(instruction);
			return;
		case BGT: //NOT-FINISHED!!!!@@@@@@@@@@@@@@@@@@@@@@@
			op = DLX.BGT;
//			c = __map2.get(__map1.get(instruction.arg1));
			a = getRegNum(instruction.arg0.toString());
			__val = DLX.F1(op, a, 0, 0); //C is computed later
			__lateComputePos.put(__instructions.size()+1, instruction.arg1.toString());
			bellowValAssig(instruction);
			return;
		case BLE: //NOT-FINISHED!!!!@@@@@@@@@@@@@@@@@@@@@@@
			op = DLX.BLE;
//			c = __map2.get(__map1.get(instruction.arg1));
			a = getRegNum(instruction.arg0.toString());
			__val = DLX.F1(op, a, 0, 0); //C is computed later
			__lateComputePos.put(__instructions.size()+1, instruction.arg1.toString());
			bellowValAssig(instruction);
			return;
		case BLT: //NOT-FINISHED!!!!@@@@@@@@@@@@@@@@@@@@@@@
			op = DLX.BLT;
//			c = __map2.get(__map1.get(instruction.arg1));
			a = getRegNum(instruction.arg0.toString());
			__val = DLX.F1(op, a, 0, 0); //C is computed later
			__lateComputePos.put(__instructions.size()+1, instruction.arg1.toString());
			bellowValAssig(instruction);
			return;
		case BNE: //NOT-FINISHED!!!!@@@@@@@@@@@@@@@@@@@@@@@
			op = DLX.BNE;
//			c = __map2.get(__map1.get(instruction.arg1));
			a = getRegNum(instruction.arg0.toString());
			__val = DLX.F1(op, a, 0, 0); //C is computed later
			__lateComputePos.put(__instructions.size()+1, instruction.arg1.toString());
			bellowValAssig(instruction);
			return;
		case BEQ: //NOT-FINISHED!!!!@@@@@@@@@@@@@@@@@@@@@@@
			op = DLX.BEQ;
//			c = __map2.get(__map1.get(instruction.arg1));
			a = getRegNum(instruction.arg0.toString());
			__val = DLX.F1(op, a, 0, 0); //C is computed later
			__lateComputePos.put(__instructions.size()+1, instruction.arg1.toString());
			bellowValAssig(instruction);
			return;
		case BRA: //NOT-FINISHED!!!!@@@@@@@@@@@@@@@@@@@@@@@
			op = DLX.JSR; // Not so sure!
//			c = __map2.get(__map1.get(instruction.arg1));
			__val = DLX.F1(op, 0, 0, 0); //C is computed later
			__lateComputePos.put(__instructions.size()+1, instruction.arg0.toString());
			bellowValAssig(instruction);
			return;
		case ADDA: //NOT-FINISHED!!!!@@@@@@@@@@@@@@@@@@@@@@@
			op = DLX.ADDI; // Not sure at all!
			isSet = false;
			break;
		case MOVE: //NOT-FINISHED!!!!@@@@@@@@@@@@@@@@@@@@@@@
			op = DLX.ADDI;
			isSet = false;
			break;
		case PUSH:
			op = DLX.PSH;
			isSet = false;
			if (argI1 instanceof ConstArg) {
				// is const
				// MOV Load_REG_1 const
				new DLXInstruction(new Instruction("1 ADDi " + Conf.ZERO_REG + " " + argI1 + " " + Conf.LOAD_REG_1)); 
				//
				arg1 = Integer.parseInt(argI1.toString());
				__val = DLX.F1(DLX.PSH, getRegNum(Conf.LOAD_REG_1), getRegNum(Conf.STACK_P), Conf.BLOCK_LEN);
				bellowValAssig(instruction);
//				__instructions.add(this);
				return;
			} else if (argI1 instanceof RegisterArg){
				// in register
				arg1 = getRegNum(argI1.toString());
				__val = DLX.F1(DLX.PSH, arg1, getRegNum(Conf.STACK_P), Conf.BLOCK_LEN);
				bellowValAssig(instruction);
//				__instructions.add(this);
				return;
			} else {
				wrong("PUSH: wrong!");
			}
		case POP:
			op = DLX.POP;
			isSet = false;
			if (argI1 instanceof RegisterArg) {
				__val = DLX.F1(DLX.POP, getRegNum(argI1.toString()), getRegNum(Conf.STACK_P), -Conf.BLOCK_LEN);
				bellowValAssig(instruction);
//				__instructions.add(this);
				return;
			} else {
				wrong("POP: I need a register!");
				return;
			}
		case MOV:
			op = DLX.ADDI;
			isSet = false;

			if (argI2 instanceof RegisterArg) {
				arg2 = getRegNum(argI2.toString());
			} else if (argI2 instanceof SpilledRegisterArg) {
				// Have to store it into memory
				new DLXInstruction(new Instruction("1 STORE " + argI2.toString())); //NOT-FINISHED!!!!@@@@@@@@@@@@@@@@@@@@@@@
			} else {
				wrong("MOV: target can only be register");
			}
			if (argI1 instanceof ConstArg) {
				// MOV 3 Reg
				// USE ADDi
				new DLXInstruction(new Instruction("1 ADDi " + Conf.ZERO_REG + " " + argI1 + " " + argI2.toString()));
			} else {
				// MOV REG REG
				// USE ADD
				new DLXInstruction(new Instruction("1 ADD " + Conf.ZERO_REG + " " + argI1 + " " + argI2.toString()));
			}
			break;
		default:
			break;
		}
		if (isSet) {
			if (argI2 instanceof RegisterArg) {
				// Something is wrong
				wrong("there should be a constant!");
			}
			arg1 = getArg(argI1, Conf.LOAD_REG_1);
			arg3 = getArg(argI3, Conf.LOAD_REG_2);
			arg2 = Integer.parseInt(argI2.toString());
			__val = DLX.F1(op, arg3, arg1, arg2);
			bellowValAssig(instruction);
			return;
		}

		// F2
		switch (instruction.op) {
		case ADD:
			op = DLX.ADD;
			isSet = true;
			break;
		case MUL:
			op = DLX.MUL;
			isSet = true;
			break;
		case SUB:
			op = DLX.SUB;
			isSet = true;
			break;
		case DIV:
			op = DLX.DIV;
			isSet = true;
			break;
		case CMP:
			op = DLX.CMP;
			if (argI1 instanceof ConstArg) {
				new DLXInstruction(new Instruction("1 MOV " + argI1.toString() + " " + Conf.LOAD_REG_1));
				arg1 = getRegNum(Conf.LOAD_REG_1);
			} else {
				arg1 = getRegNum(argI1.toString());
			}
			if (argI2 instanceof ConstArg) {
				new DLXInstruction(new Instruction("1 MOV " + argI2.toString() + " " + Conf.LOAD_REG_2));
				arg2 = getRegNum(Conf.LOAD_REG_2);
			} else {
				arg2 = getRegNum(argI2.toString());
			}
			if (!(argI3 instanceof RegisterArg)) {
				wrong("CMP: argI3 should be a register");
			}
			arg3 = getRegNum(argI3.toString());
			__val = DLX.F1(op, arg3, arg1, arg2);
			bellowValAssig(instruction);
			return;
		default:
			break;
		}
		if (isSet) {
			if (!(argI3 instanceof RegisterArg)) {
				// Something is wrong
				wrong("there should be a register!");
			}
			arg1 = getArg(argI1, Conf.LOAD_REG_1);
			arg2 = getArg(argI2, Conf.LOAD_REG_2);
			arg3 = getArg(argI3, Conf.LOAD_REG_3);
			__val = DLX.F2(op, arg3, arg1, arg2);
			bellowValAssig(instruction);
//			__instructions.add(this);
			return;
		}

		// F3
		switch (instruction.op) {
		case CALL:
			// Pre-defined functions: dealing with F2 type instructions
			if (argI1.toString().equals("OutputNum")) {
				new DLXInstruction(new Instruction("1 POP " + Conf.LOAD_REG_1));
				__val = DLX.F2(DLX.WRD, 0, getRegNum(Conf.LOAD_REG_1), 0);
				bellowValAssig(instruction);
//				__instructions.add(this);
				return;
			} else if (argI1.toString().equals("OutputNewLine")) {
				// Dealing with F1 type of instruction
				__val = DLX.F1(DLX.WRL, 0, 0, 0);
				bellowValAssig(instruction);
//				__instructions.add(this);
				return;
			} else if (argI1.toString().equals("InputNum")) {
				__val = DLX.F2(DLX.RDI, getRegNum(Conf.RETURN_VAL_REG), 0, 0);
				bellowValAssig(instruction);
//				__instructions.add(this);
				return;
			}
			op = DLX.JSR;
			break;

		// Others
		case FUNC:
		case PHI:
		case NOOP:
			__val = noopInst();
			bellowValAssig(instruction);
//			__instructions.add(this);
			return;
		case PTR:
		default:// means its value is the same as pointer's or constant
			break;
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
