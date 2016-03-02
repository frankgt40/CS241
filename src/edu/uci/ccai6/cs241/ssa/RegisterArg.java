package edu.uci.ccai6.cs241.ssa;

public class RegisterArg extends Arg {

	int num;
	  
  public RegisterArg(int n) {
    num = n;
  }
  
  public RegisterArg clone() {
    return new RegisterArg(num);
  }

  @Override
  public boolean equals(Object o) {
      if(o == null || !(o instanceof RegisterArg)) return false;
      RegisterArg two = (RegisterArg) o;
      return num == two.num;
  }
  
  public String toString() {
    return "R"+num;
  }

  @Override
  public int hashCode() {
      int hash = 2;
      hash = hash*5+0;
      hash = hash*17+num;
      return hash;
  }
}
