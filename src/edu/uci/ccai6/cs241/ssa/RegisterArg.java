package edu.uci.ccai6.cs241.ssa;

import edu.uci.ccai6.cs241.runtime.Conf;

public class RegisterArg extends Arg {

	int num;
	private boolean __isFake = false;
	public RegisterArg(int n, boolean fake) {
		num = n;
		__isFake = fake;
	}
	
	public RegisterArg(int n) {
		num = n;
		if (n > Conf.GEN_REG_NUM && n != Conf.getRegNum(Conf.LOAD_REG_1) && n != Conf.getRegNum(Conf.LOAD_REG_2)
				&& n != Conf.getRegNum(Conf.LOAD_REG_3)) {
			__isFake = true;
		}
	}

	public boolean isFake() {
		return __isFake;
	}

	public void setIsFake(boolean __isFake) {
		this.__isFake = __isFake;
	}

	public RegisterArg clone() {
		return new RegisterArg(num);
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof RegisterArg))
			return false;
		RegisterArg two = (RegisterArg) o;
		return num == two.num;
	}

	public String toString() {
		return "R" + num;
	}

	public int getNum() {
		return num;
	}

	public void setNum(int num) {
		this.num = num;
	}

	@Override
	public int hashCode() {
		int hash = 2;
		hash = hash * 5 + 0;
		hash = hash * 17 + num;
		return hash;
	}
}
