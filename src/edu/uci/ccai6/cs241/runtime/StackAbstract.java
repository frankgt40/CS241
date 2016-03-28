package edu.uci.ccai6.cs241.runtime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.uci.ccai6.cs241.ssa.Instruction;

public class StackAbstract {
	private static List<String> __staticAbstract = new ArrayList<String>();
	private static Map<String, FrameAbstract> __stackAbstract = new HashMap<String, FrameAbstract>(); //<Function name --> its frame builder>
	private static String __currFrame = "";
	public static FrameAbstract getFrame(String funcName) {
		if (!funcName.isEmpty())
			return __stackAbstract.get(funcName);
		else
			return null;
	}
	public static void addFrame(FrameAbstract frame) {
		__stackAbstract.put(frame.get__funcName(), frame);
		__currFrame = frame.get__funcName();
	}
	public static void print() {
		for (String name : __stackAbstract.keySet()) {
			System.out.println(name);
			FrameAbstract frame = __stackAbstract.get(name);
			for (String var : frame.__fakeRegToMem.keySet()) {
				System.out.println("\tVar: " + var + ", offset: " + frame.__fakeRegToMem.get(var));
			}
		}
	}
	public static boolean setCurrFrame(String funcName) {
		if (__stackAbstract.containsKey(funcName)) {
			__currFrame = funcName;
			return true;
		}
		return false;
	}
	public FrameAbstract getFrameByName(String frameName) {
		return __stackAbstract.get(frameName);
	}
//	public static Map<String, String> getRegToMem() {
//		return __fakeRegToMem;
//	}
	public static FrameAbstract getCurrFrame() {
		return __stackAbstract.get(__currFrame);
	}
//	public static String getMemInFrame(String fakeReg) {
//		// Needed to be done
//		if (__fakeRegToMem.containsKey(fakeReg)) {
//			// Computed before, this time we don't need to compute it again but directly return the result
//			return __fakeRegToMem.get(fakeReg);
//		} else {
//			// Haven't put this thing in memory before, have to compute it first time
//			int address = -1;
//			FrameAbstract frame = getCurrFrame();
//			List<String> locals = frame.getLocals();
//			if (locals.contains(fakeReg)) {
//				// Something is wrong!
//				System.err.println("Something is terribly wrong!");
//				System.exit(-1);
//				return null;
//			} else {
//				address =locals.size();
//				locals.add(fakeReg);
//				return address+"";
//			}
//		}
//	}
	
}
