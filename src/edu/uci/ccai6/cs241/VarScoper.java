package edu.uci.ccai6.cs241;
import java.util.*;
import edu.uci.ccai6.cs241.*;

public class VarScoper {
	private static List<String> __level1 = new ArrayList<String>();
	private static List<String> __level2 = new ArrayList<String>();
	private static List<String> __scopes = new ArrayList<String>();
	
	private static Map<String, Integer> __globalVarOffset = new HashMap<String, Integer>();
	private static int offset = 0;

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
	public static void exit() {
		if (__currScope.equals("main")) {
			__level2 = null;
			__level1 = null;
		} else {
			__currScope = "main";
			__inLevel2 = false;
		}
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
	    __globalVarOffset.put(genName, offset);
        offset += 4;
	  }
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
