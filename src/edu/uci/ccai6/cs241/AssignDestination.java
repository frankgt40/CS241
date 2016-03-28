package edu.uci.ccai6.cs241;

import java.util.HashSet;
import java.util.Set;

public class AssignDestination {
  
    public enum Type {
      ARRAY, CONSTANT, POINTER, VARIABLE
    }
    // TODO: phi for array dont work...
    private Type type = null;
	private String __destination = "";
	private String __relOp = null;
	
	// this contains all used "variables", which can be combined 
	// with other AssignDestination to grow this set
	// We will use this to keep track on
	// 1) all variables assigned in both "if" branches -> create phi at the end
	// 2) all variables "used" in while relation -> create phi at the beginning
	private Set<String> __assignedVars = new HashSet<String>();
	
	public Type getType() {
		return type;
	}
	public AssignDestination(String str) {
		__destination = str;
		__assignedVars.add(str);
		type = Type.VARIABLE;
	}
	
	public AssignDestination(long pc) {
		__destination = "("+Long.toString(pc)+")";
		type = Type.POINTER;
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
	
	public String toString() {
		return getDestination();
	}
	
	public String getDestination() {
		return __destination;
	}
	public void setIsArray(boolean isTure) {
		type = Type.ARRAY;
		__assignedVars = new HashSet<String>();
	}
	public boolean isArray() {
		return type == Type.ARRAY;
	}
	public void setIsConstant(boolean isTrue) {
		type = Type.CONSTANT;
	}
	public boolean isConstant() {
		return type == Type.CONSTANT;
	}
	public boolean isPointer() {
		return type == Type.POINTER;
	}
	public Set<String> getAssignedVars() {
	  return __assignedVars;
	}

	// join Vars
    public AssignDestination join(AssignDestination two) {
      if(two == null && this != null) return this;
      __assignedVars.addAll(two.getAssignedVars());
      return this;
    }
    
    // intersect Vars
    public AssignDestination intersectVars(AssignDestination two) {
      if((this == null && two != null) || (two == null && this != null)) {
        this.__assignedVars = new HashSet<>();
        return this;
      }
      this.__assignedVars.retainAll(two.getAssignedVars());
      return this;
    }
}
