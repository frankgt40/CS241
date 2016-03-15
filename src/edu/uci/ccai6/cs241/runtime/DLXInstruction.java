package edu.uci.ccai6.cs241.runtime;

import java.util.ArrayList;
import java.util.List;

import edu.uci.ccai6.cs241.ssa.Arg;
import edu.uci.ccai6.cs241.ssa.ConstArg;
import edu.uci.ccai6.cs241.ssa.Instruction;
import edu.uci.ccai6.cs241.ssa.RegisterArg;
import edu.uci.ccai6.cs241.ssa.SpilledRegisterArg;

public class DLXInstruction extends DLX {
	private static List<DLXInstruction> __instructions = new ArrayList<DLXInstruction>();
	private static RuntimeEnv __system = new RuntimeEnv();
	private int __val = 0xFFFF;

	public static int[] getMachineCodes() {
		int[] rsl = new int[__instructions.size()];
		for (int i = 0; i < __instructions.size(); i++) {
			rsl[i] = __instructions.get(i).getVal();
		}
		return rsl;
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
			isSet = true;
			break;
		case MULi:
			op = DLX.MULI;
			isSet = true;
			break;
		case DIVi:
			op = DLX.DIVI;
			isSet = true;
			break;
		case SUBi:
			op = DLX.SUBI;
			isSet = true;
			break;
		case LOAD:
			op = DLX.LDW;
			isSet = true;
			break;
		case STORE:
			op = DLX.STW;
			isSet = true;
			break;
		case BGE:
			op = DLX.BGE;
			isSet = true;
			break;
		case BGT:
			op = DLX.BGT;
			isSet = true;
			break;
		case BLE:
			op = DLX.BLE;
			isSet = true;
			break;
		case BLT:
			op = DLX.BLT;
			isSet = true;
			break;
		case BNE:
			op = DLX.BNE;
			isSet = true;
			break;
		case BEQ:
			op = DLX.BEQ;
			isSet = true;
			break;
		case BRA:
			op = DLX.BSR; // Not so sure!
			isSet = true;
			break;
		case ADDA:
			op = DLX.ADDI; // Not sure at all!
			isSet = true;
			break;
		case MOVE:
			op = DLX.ADDI;
			isSet = true;
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
				__instructions.add(this);
				return;
			} else {
				// in register
				arg1 = getRegNum(argI1.toString());
				__val = DLX.F1(DLX.PSH, arg1, getRegNum(Conf.STACK_P), -Conf.BLOCK_LEN);
				__instructions.add(this);
				return;
			}
		case POP:
			op = DLX.POP;
			isSet = false;
			if (argI1 instanceof RegisterArg) {
				__val = DLX.F1(DLX.POP, getRegNum(argI1.toString()), getRegNum(Conf.STACK_P), -Conf.BLOCK_LEN);
				__instructions.add(this);
				return;
			} else {
				wrong("POP: I need a register!");
				return;
			}
		case MOV:
			op = DLX.ADDI;
			isSet = true;
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
			__instructions.add(this);
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
			isSet = true;
			break;
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
			__val = DLX.F2(op, arg1, arg2, arg3);
			__instructions.add(this);
			return;
		}

		// F3
		switch (instruction.op) {
		case CALL:
			// Pre-defined functions: dealing with F2 type instructions
			if (argI1.toString().equals("OutputNum")) {
				new DLXInstruction(new Instruction("1 POP " + Conf.LOAD_REG_1));
				__val = DLX.F2(DLX.WRD, 0, getRegNum(Conf.LOAD_REG_1), 0);
				__instructions.add(this);
				return;
			} else if (argI1.toString().equals("OutputNewLine")) {
				// Dealing with F1 type of instruction
				__val = DLX.F1(DLX.WRL, 0, 0, 0);
				__instructions.add(this);
				return;
			} else if (argI1.toString().equals("InputNum")) {
			}
			op = DLX.JSR;
			break;

		// Others
		case FUNC:
		case PHI:
		case NOOP:
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
