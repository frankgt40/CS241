package edu.uci.ccai6.cs241.ssa;

public class SpilledRegisterArg extends RegisterArg {

	public SpilledRegisterArg(int n) {
		super(n);
	}

	public String toString() {
		return "SR"+num;
	}

}
