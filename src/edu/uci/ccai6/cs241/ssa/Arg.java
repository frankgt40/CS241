package edu.uci.ccai6.cs241.ssa;

public abstract class Arg {
  
    public static Arg clone(Arg a2) {
        if(a2 instanceof ConstArg) return ((ConstArg)a2).clone();
        if(a2 instanceof PointerArg) return ((PointerArg)a2).clone();
        if(a2 instanceof VarArg) return ((VarArg)a2).clone();
        return null;
    }
	
	public static Arg create(String str) {
		if(str.matches("^\\d+$")) {
            int num = Integer.parseInt(str);
		    return new ConstArg(num);
		} else if(str.charAt(0) == '(') {
			String pointerStr = str.substring(1, str.length()-1);
			if(!pointerStr.matches("^\\d+$")) {
				System.err.println(str+" : is not a digit");
				System.exit(-1);
			}
			int pointer = Integer.parseInt(pointerStr);
			return new PointerArg(pointer);
		} else if(str.charAt(0) == 'R'){
		    return new RegisterArg(Integer.parseInt(str.substring(1)));
		} else if(str.charAt(0) == 'S' && str.charAt(1) == 'R') {
		    return new RegisterArg(Integer.parseInt(str.substring(2)));
		} else {
		    return new VarArg(str);
		}
	}
	
}
