package edu.uci.ccai6.cs241.ssa;

public class PointerArg extends Arg {

  public int pointer;

  public PointerArg(int n) {
    pointer = n;
  }
  
  public PointerArg clone() {
    return new PointerArg(pointer);
  }

  @Override
  public boolean equals(Object o) {
      if(o == null || !(o instanceof PointerArg)) return false;
      PointerArg two = (PointerArg) o;
      return pointer == two.pointer;
  }
  
  public String toString() {
    return "("+pointer+")";
  }

  @Override
  public int hashCode() {
      int hash = 1;
      hash = hash*5+1;
      hash = hash*17+pointer;
      return hash;
  }
}
