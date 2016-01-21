package edu.uci.ccai6.cs241;

import java.util.HashMap;
import java.util.Map;

public class FunctionUtil {
	private Map<String, Map<String, String>> __nameMap = new HashMap<String, Map<String,String>>(); 
	private int __counter = 0;
	private String __funName = "";
	
	public FunctionUtil(String name) {
		__funName = name;
	}
	
	public int getCounter() {
		return __counter;
	}

	public void setCounter(int __counter) {
		this.__counter = __counter;
	}

	public String getFunName() {
		return __funName;
	}

	public void setFunName(String __funName) {
		this.__funName = __funName;
	}

	public Map<String, String> creatOne(String symbol, int counter) {
		Map<String, String> rsl = new HashMap<String,String>();
		rsl.put(symbol, new Integer(__counter).toString());
		return rsl;
	}
	public int newVarName(String funName, String symbol) {
		__nameMap.put(funName, creatOne(symbol, __counter) );
		__counter++;
		return __counter-1;
	}
	public int newVarName(String symbol) {
		__nameMap.put(__funName, creatOne(symbol, __counter) );
		__counter++;
		return __counter-1;
	}
	
	public String findVarRealName(String symbol) {
		return __funName + Parser.__SEP + __nameMap.get(__funName).get(symbol);
	}
}
