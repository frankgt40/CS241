package edu.uci.ccai6;

public class IRInstruction {
	
	String op, arg0, arg1, lineNum;
	
	public void print() {
		System.out.println(lineNum+": "+op+" "+arg0+" "+arg1);
	}

	public IRInstruction(String op, Result arg0, Result arg1, InstPointer currentPointer) {
		this.op = op;
		this.arg0 = arg0.toString();
		this.arg1 = (arg1 == null) ? "" : arg1.toString();
		this.lineNum = currentPointer.toString();
		
		print();
	}
}
