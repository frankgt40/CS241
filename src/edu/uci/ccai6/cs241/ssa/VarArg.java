package edu.uci.ccai6.cs241.ssa;

public class VarArg extends Arg {

  String name;
  
  public VarArg(String n) {
    name = n;
  }
  
  public VarArg clone() {
    return new VarArg(name);
  }

  @Override
  public boolean equals(Object o) {
      if(o == null || !(o instanceof VarArg)) return false;
      VarArg two = (VarArg) o;
      return name.equals(two.name);
  }
  
  public String toString() {
    return name;
  }
  
  @Override
  public int hashCode() {
      int hash = 1;
      hash = hash*5+2;
      hash = hash*31+(name == null ? 0 : name.hashCode());
      return hash;
  }
}
