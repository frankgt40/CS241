package edu.uci.ccai6.cs241.runtime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StackAbstract {
	private List<String> __staticAbstract = new ArrayList<String>();
	private Map<String, FrameAbstract> __stackAbstract = new HashMap<String, FrameAbstract>(); //<Function name --> its frame builder>
	private Map<String, String> __fakeRToMem = new HashMap<String, String>(); // <fake register name --> memory address in frames>
	public FrameAbstract getFrame(String funcName) {
		if (!funcName.isEmpty())
			return __stackAbstract.get(funcName);
		else
			return null;
	}
	public void addFrame(FrameAbstract frame) {
		__stackAbstract.put(frame.get__funcName(), frame);
	}
}
