package edu.uci.ccai6.cs241.ssa;

public class ConstArg extends Arg {
  
  int num;
  
  public ConstArg(int n) {
    num = n;
  }
  
  public ConstArg clone() {
    return new ConstArg(num);
  }

  @Override
  public boolean equals(Object o) {
      if(o == null || !(o instanceof ConstArg)) return false;
      ConstArg two = (ConstArg) o;
      return num == two.num;
  }
  
  public String toString() {
    return num+"";
  }

  @Override
  public int hashCode() {
      int hash = 1;
      hash = hash*5+0;
      hash = hash*17+num;
      return hash;
  }
}
