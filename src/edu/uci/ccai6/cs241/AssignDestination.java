package edu.uci.ccai6.cs241;

import java.util.HashSet;
import java.util.Set;

public class AssignDestination {
    // TODO: phi for array dont work...
	private boolean __isArray = false;
	private boolean __isConstant = false;
	private boolean __isPointer = false;
	private String __destination = "";
	private String __relOp = null;
	private Set<String> __assignedVars = new HashSet<String>();
	
	public AssignDestination(String str) {
		__destination = str;
		__assignedVars.add(str);
	}
	
	public AssignDestination(long pc) {
		__destination = "("+Long.toString(pc)+")";
		__isPointer = true;
	}
	
	public void setDestination(String str) {
		__destination = str;
	}
	
	public void setRelOp(String ro) {
		__relOp = ro;
	}
	
	public String getRelOp() {
		return __relOp;
	}
	
	public String getDestination() {
		return __destination;
	}
	public void setIsArray(boolean isTure) {
		__isArray = isTure;
		__assignedVars = new HashSet<String>();
	}
	public boolean isArray() {
		return __isArray;
	}
	public void setIsConstant(boolean isTrue) {
		__isConstant = isTrue;
	}
	public boolean isConstant() {
		return __isConstant;
	}
	public boolean isPointer() {
		return __isPointer;
	}
	public Set<String> getAssignedVars() {
	  return __assignedVars;
	}

    public AssignDestination join(AssignDestination two) {
      if(two == null && this != null) return this;
      __assignedVars.addAll(two.getAssignedVars());
      return this;
    }
    public AssignDestination intersectVars(AssignDestination two) {
      if((this == null && two != null) || (two == null && this != null)) {
        this.__assignedVars = new HashSet<>();
        return this;
      }
      this.__assignedVars.retainAll(two.getAssignedVars());
      return this;
    }
}
