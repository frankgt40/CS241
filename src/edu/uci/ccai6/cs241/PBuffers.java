package edu.uci.ccai6.cs241;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class PBuffers {
	private char[] __buffer1, __buffer2;
	public static final int __SIZE = 4;
	private int __p = 0; 
	private int __finalLoadSize = 0;
	private int __lineNum = 0;
	private int __charPos = 0;
	private BufferedReader __reader = null;
	private String __fileName;
	private boolean __pInBuffer1 = true;
	private boolean __hasEOF = false;
	private boolean __EOFInBuffer1 = false; // True: in buffer1; FALSE: in buffer2
	
	
	public PBuffers(String name) {
		super();
		__buffer1 = new char[__SIZE];
		__buffer2 = new char[__SIZE];
		try {
			__reader = new BufferedReader(new FileReader(name));
			__fileName = name;
			
			loadFirstTime();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	// Load the buffers first time
	private void loadFirstTime() {
		try {
			// First load buffer1
			__finalLoadSize = __reader.read(__buffer1, 0, __SIZE);
			if (__finalLoadSize < __SIZE) {
				//Didn't load the whole buffer1
				__hasEOF = true;
				__EOFInBuffer1 = true;
			} else {
				// Then we need to load things into buffer2
				__finalLoadSize = __reader.read(__buffer2, 0, __SIZE);
				if (__finalLoadSize < __SIZE) {
					// Didn't load the whole buffer2
					__hasEOF = true;
					__EOFInBuffer1 = false;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public boolean hasNext() {
		// If __p is in the buffer has EOF
		if (__hasEOF && (__EOFInBuffer1 == __pInBuffer1)) {
			// If __p is not out of the loadsize, then we still have chars to get
			if (__p < __finalLoadSize) {
				return true;
			} else {
				// If __p is out of the loadSize, then we don't have new chars
				return false;
			}
		} else {
			return true;
		}
	}
	protected void updateLineNumber(char ch) {
		if (ch == '\n') {
			__lineNum++;
			__charPos = 0;
		}
	}
	protected void increaseCharPos() {
		__charPos++;
	}
	
	public int get__lineNum() {
		return __lineNum;
	}
	public int get__charPos() {
		return __charPos;
	}
	// Will return the current char, and advance __p by 1
	public char next() {
		if (!hasNext()) return '\0';
		char rsl = '\0'; // Bad output
		// If __p is in the buffer has EOF
		if (__hasEOF && (__EOFInBuffer1 == __pInBuffer1)) {
			rsl = __pInBuffer1 ? __buffer1[__p] : __buffer2[__p];
			// If __p is not out of the loadsize, then advance it
			if (__p < __finalLoadSize) {
				__p++;
			} else {
				// If __p is out of the loadSize, then don't advance it
			}
		} else {
			// If __p is in a buffer which is full of chars
			rsl = __pInBuffer1 ? __buffer1[__p] : __buffer2[__p];
			// If __p will be out of the buffer it is in
			if (__p >= __SIZE - 1) {
				__p = 0;
				// If we already have EOF, then don't reload buffers anymore, otherwise, we can reload it
				if (!__hasEOF)
					loadBuffer(__pInBuffer1); // Reload the current buffer
				__pInBuffer1 = !__pInBuffer1; // , and move __p to another buffer
			} else {
				// Else just simply advance the __p
				__p++;
			}
		}
		updateLineNumber(rsl);
		increaseCharPos();
		return rsl;
	}
	protected void loadBuffer(boolean buffer1) {
		try {
			// If buffer1 needs to be reload,
			if (buffer1) {
				// then load buffer1
				__finalLoadSize = __reader.read(__buffer1, 0, __SIZE);
				if (__finalLoadSize < __SIZE) {
					// Didn't load the whole buffer1
					__hasEOF = true;
					__EOFInBuffer1 = true;
				}
			} else {
				// then load buffer2
				__finalLoadSize = __reader.read(__buffer2, 0, __SIZE);
				if (__finalLoadSize < __SIZE) {
					// Didn't load the whole buffer1
					__hasEOF = true;
					__EOFInBuffer1 = false;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	// One step: step == 1
	public char oracle() {
		return oracle(1);
	}
	public char oracle(int step) {
		if (!hasNext()) return '\0';
		step--;
		char rsl= '\0';
		// If the oracled one is in the same buffer
		if ((__p + step) < __SIZE) {
			rsl = __pInBuffer1 ? __buffer1[__p+step] : __buffer2[__p+step];
		} else {
			// the oracled one will be in the another buffer
			rsl = __pInBuffer1 ? __buffer2[(__p+step)%__SIZE] : __buffer1[(__p+step)%__SIZE];
		}
		return rsl;
	}
	public BufferedReader get__reader() {
		return __reader;
	}
	public String get__fileName() {
		return __fileName;
	}
	public static int getSize() {
		return __SIZE;
	}
	public static void main(String args[]) {
		PBuffers pbs = new PBuffers("testCases/test001.txt");
		System.out.println("Oracled one: " + pbs.oracle(PBuffers.__SIZE+1));
		while (pbs.hasNext()) {
			System.out.print(pbs.next());
		}
		
	}
}
