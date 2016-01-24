package edu.uci.ccai6;

public class InstPointer {

	int instLine = -1;
	
	public InstPointer(int il) {
		instLine = il;
	}
	
	public InstPointer(InstPointer il2) {
		instLine = il2.instLine;
	}
	
	public void inc() {
		instLine++;
	}
	
	public String toString() {
		return "("+instLine+")";
	}
}
