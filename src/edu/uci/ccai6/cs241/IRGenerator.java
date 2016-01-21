package edu.uci.ccai6.cs241;

import java.util.ArrayList;
import java.util.List;

public class IRGenerator {
	private List<String> __IRBuffer;
	private long __pc = 0;
	private List<String> __scopeNames;
	private List<String> __functionScope;
	private int __scope = 0;
	private List<String> __varAddress;
	
	public static final int __DW = 4;
	
	public IRGenerator() {
		__IRBuffer = new ArrayList<String>();
		__scopeNames = new ArrayList<String>();
		__functionScope = new ArrayList<String>();
		__varAddress = new ArrayList<String>();
		
		__scopeNames.add(new Integer(__scope).toString());
	}
	public long getCurrPc() {
		return __pc - 1;
	}
	public void putCode(String code) {
		__IRBuffer.add(code);
		__pc++;
	}
	public List<String> getIRBuffer() {
		return __IRBuffer;
	}
	public String getANewVarAddress() {
		int count = __varAddress.size();
		__varAddress.add(new Integer(count).toString());
		return __varAddress.get(__varAddress.size()-1);
	}
	
	public void addFunctionScope(String functionName) {
		__functionScope.add(functionName);
	}
	public String getFunctionScope() {
		return __functionScope.get(__functionScope.size()-1);
	}
	
	public void print() {
		int i = 1;
		for (String codeLine : __IRBuffer) {
			System.out.println(i++ + ":" + codeLine);
		}
	}
	public void pushNewScope() {
		__scope++;
		__scopeNames.add(new Integer(__scope).toString());
	}
	public String getScopeName() {
		return __scopeNames.get(__scope)+".";
	}
	public void popScopeName() {
		__scopeNames.remove(__scope);
	}
}
