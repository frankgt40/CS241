package edu.uci.ccai6.cs241.runtime;

import edu.uci.ccai6.cs241.ssa.Instruction;

public class DLXInstruction extends DLX {
	private int __val = 0xFFFF;
	DLXInstruction(Instruction instruction) {
		int op, arg1, arg2, arg3;
		switch (instruction.op) {
		// F1
		case ADDi:
			op = DLX.ADDI;
			break;
		case MULi:
			op = DLX.MULI;
			break;
		case DIVi:
			op = DLX.DIVI;
			break;
		case SUBi:
			op = DLX.SUBI;
			break;
		case LOAD:
			op = DLX.LDW;
			break;
		case STORE:
			op = DLX.STW;
			break;
		case BGE:
			op = DLX.BGE;
			break;
		case BGT:
			op = DLX.BGT;
			break;
		case BLE:
			op = DLX.BLE;
			break;
		case BLT:
			op = DLX.BLT;
			break;
		case BNE:
			op = DLX.BNE;
			break;
		case BEQ:
			op = DLX.BEQ;
			break;
		case BRA:
			op = DLX.BSR; // Not so sure!
			break;
		case ADDA:
			op = DLX.ADDI; // Not sure at all!
			break;
		case MOVE:
			op = DLX.ADDI;
			break;
		case PUSH:
			op = DLX.PSH;
			break;
		case POP:
			op = DLX.POP;
			break;
		case MOV:
			op = DLX.ADDI;
			break;
		
		// F2
		case ADD:
			op = DLX.ADD;
			break;
		case MUL:
			op = DLX.MUL;
			break;
		case SUB:
			op = DLX.SUB;
			break;
		case DIV:
			op = DLX.DIV;
			break;
		case CMP:
			op = DLX.CMP;
			break;

		// F3
		case CALL:
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
