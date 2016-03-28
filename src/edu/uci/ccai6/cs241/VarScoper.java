package edu.uci.ccai6.cs241;
import java.util.*;
import edu.uci.ccai6.cs241.*;
import edu.uci.ccai6.cs241.runtime.FrameAbstract;
import edu.uci.ccai6.cs241.runtime.Local;
import edu.uci.ccai6.cs241.runtime.LocalType;

public class VarScoper {
	private static List<String> __level1 = new ArrayList<String>();
	private static List<String> __level2 = new ArrayList<String>();
	private static List<String> __scopes = new ArrayList<String>();
	
	private static Map<String, Integer> __globalVarOffset = new HashMap<String, Integer>();
	private static Map<String, Integer> __localArrayOffset = new HashMap<String, Integer>();
	private static Map<String, Integer> __arraySize = new HashMap<String, Integer>();
	private static int __globalOffset = 0;
	private static int __localOffset = 0;

	private static boolean __inLevel2 = false;
	private static String __currScope = "";
	private static final String __CONNECTOR = "_";
	
	
	public static void enter(String scope) {
		if (__scopes.contains(scope)) {
			new Reporter(Reporter.ReportType.ERROR, "Redefined functions or procedures:" + scope + "!");
		} else {
			__scopes.add(scope);
			__currScope = scope;
			if (scope.equals("main")) {
				__inLevel2 = false;
			} else {
				__inLevel2 = true;
			}
		}
		
	}
	public static String getCurrScope() {
		if (__currScope.equals("")) {
			System.err.println("getCurrScope(): sth wrong!");
			return "";
		}
		return __currScope;
	}
	public static void exit() {
		if (__currScope.equals("main")) {
			__level2 = null;
			__level1 = null;
		} else {
			__currScope = "main";
			__level2 = new ArrayList<String>();
			__localArrayOffset.clear();
			__localOffset = 0;
			__inLevel2 = false;
		}
	}
	/**
	 * declare arrays, record it in a frame, and use it later
	 */
	public static void declareArray(String name, int size) {
		String currFuncName = VarScoper.__currScope;
		FrameAbstract frame = new FrameAbstract(currFuncName);
		Local thisArray = new Local();
		thisArray.__len = size;
		thisArray.__name = name;
		thisArray.__type = LocalType.ARRAY;
		int offset = frame.findCurrOffset() + size;
		frame.__fakeRegToMem.put(name, thisArray);
		frame.setCurrOffset(offset);
	}
	/**
	 * used when you want to declare array
	 * @param varName
	 * @param size
	 */
	public static void declare(String varName, int size) {
		declareArray(varName, size);
		declare(varName);
		String fullName = getFullName(varName);
		if(__arraySize.containsKey(fullName)) {
			new Reporter(Reporter.ReportType.ERROR, "sth wrong!");
		}
		__arraySize.put(fullName, size);
	}
	public static void declare(String varName) {
		if (__inLevel2) {
			if (__level2.contains(varName)) {
				new Reporter(Reporter.ReportType.ERROR, "Redefined variable:" + varName + "!");
			} else {
				__level2.add(varName);
			}
		} else {
			if (__level1.contains(varName)) {
				new Reporter(Reporter.ReportType.ERROR, "Redefined variable:" + varName + "!");
			} else {
				__level1.add(varName);
			}
		}
	}
	public static int getGlobalVarOffset(String fullName) {
	    return (__globalVarOffset.containsKey(fullName) ? __globalVarOffset.get(fullName) : -1);
	}
	private static void storeGlobalVar(String genName) {
	  if(!__globalVarOffset.containsKey(genName)) {
	    __globalVarOffset.put(genName, __globalOffset);
	    __globalOffset += (__arraySize.containsKey(genName) ? __arraySize.get(genName) : 4);
	  }
	}
	public static int storeLocalArray(String genName) {
		int offset = -1;
		// check if its an array
		if(__localArrayOffset.containsKey(genName)) {
			return __localArrayOffset.get(genName);
		} else if(__arraySize.containsKey(genName) && !__localArrayOffset.containsKey(genName)) {
			offset = __localOffset;
			__localArrayOffset.put(genName, __localOffset);
		    __localOffset += (__arraySize.containsKey(genName) ? __arraySize.get(genName) : 4);
		}
		return offset;
	}
	private static String getFullName(String varName) {
		if (__inLevel2) {
			if (__level2.contains(varName)) {
				return __currScope + __CONNECTOR + varName;
			} else if (__level1.contains(varName)) {
			    String genName = "main" + __CONNECTOR + varName;
			    
			    // for optimization, not all vars in main have to be global variable
				return genName;
			} else {
				new Reporter(Reporter.ReportType.ERROR, "Undefined variable:" + varName + "!");
			} 
		} else {
			if (__level1.contains(varName)) {
              String genName = "main" + __CONNECTOR + varName;
              
              // for optimization, not all vars in main have to be global variable
              if(!__currScope.equals("main")) storeGlobalVar(genName);
              return genName;
			} else {
				new Reporter(Reporter.ReportType.ERROR, "Undefined variable:" + varName + "!");
			}
		}
		return null;
	}
	public static String genVarName(String varName) {
		if (__inLevel2) {
			if (__level2.contains(varName)) {
				return __currScope + __CONNECTOR + varName;
			} else if (__level1.contains(varName)) {
			    String genName = "main" + __CONNECTOR + varName;
			    
			    // for optimization, not all vars in main have to be global variable
			    if(!__currScope.equals("main")) storeGlobalVar(genName);
				return genName;
			} else {
				new Reporter(Reporter.ReportType.ERROR, "Undefined variable:" + varName + "!");
			} 
		} else {
			if (__level1.contains(varName)) {
              String genName = "main" + __CONNECTOR + varName;
              
              // for optimization, not all vars in main have to be global variable
              if(!__currScope.equals("main")) storeGlobalVar(genName);
              return genName;
			} else {
				new Reporter(Reporter.ReportType.ERROR, "Undefined variable:" + varName + "!");
			}
		}
		return null;
	}
}
