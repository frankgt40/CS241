package edu.uci.ccai6;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import edu.uci.ccai6.ErrorWarningReporter;

public class PairBuffer {
	private static final int BUFFER_SIZE = 20;
	private String filename;
	private char[] buffer1;
	private char[] buffer2;
	private boolean endOfFile = false;
	BufferedReader br = null;
	private int currPointer = 0;
	private int forwardPointer = 0;
	private int loadSize = 0;
	private boolean isCurrPointerInBuffer1 = true;
	private boolean isForwardPointerInBuffer1 = true;	
	private boolean doesBuffer1HasEOF = false;
	private boolean doesBuffer2HasEOF = false;
	private long totalLineNum = 1;
	
	public long getTotalLineNum() {
		return this.totalLineNum;
	}
	public char oracleNextChar() {
		if (this.isForwardPointerInBuffer1) {
			return this.buffer1[this.forwardPointer];
		}  else {
			return this.buffer2[this.forwardPointer];
		}
	}
	public PairBuffer(String filename) {
		super();
		this.filename = filename;
		this.buffer1 = new char[BUFFER_SIZE];
		this.buffer2 = new char[BUFFER_SIZE];
		
		try {
			br = new BufferedReader(new FileReader(filename));
			loadBufferFirstTime();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public void loadBufferFirstTime() {
			loadBuffer(true); //load buffer1
			if (this.doesBuffer1HasEOF  && this.endOfFile) {
				// the file's length is less than one buffer's length
				// then, don't need to load buffer2				
			} else {
				loadBuffer(false);
			}	
	}
	/*
	 * position: the number currPointer moves
	 * return: 
	 * 	-false: currPointer is out of the boundry and currPointer is not changed
	 * 	-true: currPointer stays in current buffer and is add by position
	 */
	public boolean increaseCurrPointer(int position) {
		if (isCurrPointerOut(position)) {
			// currPointer is out of boundry
			return false;
		} else {
			// currPointer is less than the size of the buffer
			this.currPointer = this.currPointer + position;
			return true;
		}
	}
	/*
	 * position: the number forwardPointer moves
	 * return: 
	 * 	-false: forwardPointer is out of the boundry and currPointer is not changed
	 * 	-true: forwardPointer stays in current buffer and is add by position
	 */
	public void increaseForwardPointer (int position) {
		if (isForwardPointerOut(position)) {
			// forwardPointer is out of boundry
			this.forwardPointer = position - (BUFFER_SIZE - 1 - this.forwardPointer) - 1;
			this.isForwardPointerInBuffer1 = !this.isForwardPointerInBuffer1;
			return ;
		} else {
			// forwardPointer is less than the size of the buffer
			this.forwardPointer = this.forwardPointer + position;
			return ;
		}
	}
	public boolean isCurrPointerOut(int position) {
		return ((this.currPointer + position) > BUFFER_SIZE) ? true : false;
	}
	public boolean isForwardPointerOut(int position) {
		return ((this.forwardPointer + position) >= BUFFER_SIZE) ? true : false;
	}
	public String getFilename() {
		return filename;
	}
	public void setFilename(String filename) {
		this.filename = filename;
	}
	public int getCurrPointer() {
		return this.currPointer;
	}
	public int getForwardPointer() {
		return this.forwardPointer;
	}
	/*
	 * buffer1: load buffer1 or buffer2
	 * return:
	 * 	- false: failed
	 * 	- true: load buffer succesfully
	 */
	public boolean loadBuffer(boolean isBuffer1) {
		if (endOfFile) return false;
		
		if (isBuffer1) {
			try {
				this.loadSize = br.read(buffer1, 0, BUFFER_SIZE);
				if (loadSize < BUFFER_SIZE) {
					// reach EOF
					this.endOfFile = true;
					this.doesBuffer1HasEOF = true;
					return true;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			try {
				this.loadSize = br.read(buffer2, 0, BUFFER_SIZE);
				if (loadSize < BUFFER_SIZE) {
					// reach EOF
					this.endOfFile = true;
					this.doesBuffer2HasEOF = true;
					return true;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return true;
	}
	public char getNextChar() {
		if (exceedMaxLength()) {
			new ErrorWarningReporter(ErrorWarningReporter.ReportType.ERROR, 
							this.filename, 
							totalLineNum, 
							"Exceed the max length of variable! You should not enter variables longer than " + BUFFER_SIZE);
			System.exit(0);
		}
		char result;
		if(this.isForwardPointerInBuffer1) {
			// forwardPointer in buffer1
			result = this.buffer1[forwardPointer]; // read the char
			
			if (result == '\n') {
				// count the line number
				totalLineNum++;
			}
		
			if (this.endOfFile) {
				// if end of the file, then forwardPointer cannot exceed over loadSize
				if (this.doesBuffer1HasEOF && this.isForwardPointerInBuffer1) {
					if (this.forwardPointer < this.loadSize - 1) {
						increaseForwardPointer(1);
					}
					return result;
				} 
			}
			
			if (isForwardPointerOut(1)) {
				// next char is out of curr buffer's boundry
				isForwardPointerInBuffer1 = false; // jmp to buffer2
				this.forwardPointer = 0;
			} else {
				// forwardPointer stays in buffer1
				this.increaseForwardPointer(1);
			}
		} else {
			// forwardPointer in buffer2
			result = this.buffer2[forwardPointer];
			
			if (result == '\n') {
				// count the line number
				totalLineNum++;
			}
			
			if (this.endOfFile) {
				// if end of the file, then forwardPointer cannot exceed over loadSize
				if (this.doesBuffer1HasEOF && this.isForwardPointerInBuffer1) {
					if (this.forwardPointer < this.loadSize - 1) {
						increaseForwardPointer(1);
					}
					return result;
				} 
			}
			
			if (isForwardPointerOut(1)) {
				// next char is out of curr buffer's boundry
				isForwardPointerInBuffer1 = true; // jmp to buffer2
				this.forwardPointer = 0;
			} else {
				// forwardPointer stays in buffer1
				this.increaseForwardPointer(1);
			}
		}
		return result;
	}
	public boolean exceedMaxLength() {
		if (isForwardPointerInBuffer1 && !isCurrPointerInBuffer1) {
			if (this.forwardPointer >= BUFFER_SIZE/2 && this.currPointer <= BUFFER_SIZE/2)
				return true;
			else
				return false;
		} else if (!isForwardPointerInBuffer1 && isCurrPointerInBuffer1) {
			if (this.forwardPointer >= BUFFER_SIZE/2 && this.currPointer <= BUFFER_SIZE/2)
				return true;
			else
				return false;
		} else {
			return false;
		}
	}
	public boolean reachEOF() {
		if (this.doesBuffer1HasEOF && this.isForwardPointerInBuffer1 && this.forwardPointer >= this.loadSize -1) {
			return true;
		} else if (this.doesBuffer2HasEOF && !this.isForwardPointerInBuffer1 && this.forwardPointer >= this.loadSize-1) {
			return true;
		}
		return false;
	}
	public void descreaseForwardPointer() {
		if (this.forwardPointer > 0) {
			this.forwardPointer--;
		} else {
			if (this.isForwardPointerInBuffer1) {
				this.isForwardPointerInBuffer1 = false;
				this.forwardPointer = BUFFER_SIZE -1;
			} else if (!this.isForwardPointerInBuffer1) {
				this.isForwardPointerInBuffer1 = true;
				this.forwardPointer = BUFFER_SIZE - 1;
			}
		}
	}
	public String getAString() {
		String str;
		if ((this.isCurrPointerInBuffer1 && this.isForwardPointerInBuffer1) ||
			(!this.isCurrPointerInBuffer1 && !this.isForwardPointerInBuffer1)){
			if (this.forwardPointer == this.currPointer) {
				str = new String(this.buffer1, this.currPointer, 1);
				return str;
			}
		}
		if (this.isCurrPointerInBuffer1 && this.isForwardPointerInBuffer1) {
			str = new String(this.buffer1, this.currPointer, this.forwardPointer-this.currPointer);
		} else if (this.isCurrPointerInBuffer1 && !this.isForwardPointerInBuffer1) {
			String str1 = new String(this.buffer1, this.currPointer, BUFFER_SIZE - this.currPointer);
			String str2 = new String(this.buffer2, 0, this.forwardPointer);
			str = str1 + str2;
			this.loadBuffer(true); // forwardPointer in buffer2, and currPointer in buffer1, then after getting the string between them, buffer1 is useless and should load it with new data
		} else if (!this.isCurrPointerInBuffer1 && this.isForwardPointerInBuffer1) {
			String str1 = new String(this.buffer2, this.currPointer, BUFFER_SIZE - this.currPointer);
			String str2 = new String(this.buffer1, 0, this.forwardPointer);
			str = str1 + str2;
			this.loadBuffer(false); // forwardPointer in buffer1, and currPointer in buffer2, then after getting the string between them, buffer2 is useless and should load it with new data
		} else if (!this.isCurrPointerInBuffer1 && !this.isForwardPointerInBuffer1) {
			str = new String(this.buffer2, this.currPointer, this.forwardPointer-this.currPointer);			
		} else {
			str = null;
		}

//		if (this.increaseForwardPointer(1)) {
//			// in the current buffer
//			// decrease the forwardPointer
//			this.forwardPointer--;
//		} else {
//			// in another buffer
//			this.forwardPointer = 0;
//			this.isForwardPointerInBuffer1 = !this.isForwardPointerInBuffer1;
//		}
		this.isCurrPointerInBuffer1 = this.isForwardPointerInBuffer1;
		this.currPointer = this.forwardPointer;
		return str;
	}
	public char getChar(int i) {
		if (isForwardPointerInBuffer1) {
			return buffer1[i];
		} else {
			return buffer2[i];
		}
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		PairBuffer pb = new PairBuffer("testCases/factorial.txt");
		for (int i = 0; i < 35; i++) {
			System.out.print(pb.getNextChar());
		}
		System.out.print("\n@@@@@@@@@@@@@@@@@@@@@\n" + pb.getAString());
		
		System.out.print("\n!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!\n");
		for (int i = 0; i < 25; i++) {
			System.out.print(pb.getNextChar());
		}
		System.out.print("\n@@@@@@@@@@@@@@@@@@@@@\n" + pb.getAString());
		
		System.out.print("\n!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!\n");
		for (int i = 0; i < 25; i++) {
			System.out.print(pb.getNextChar());
		}
		System.out.print("\n@@@@@@@@@@@@@@@@@@@@@\n" + pb.getAString());
		System.out.print("\n!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!\n");
		for (int i = 0; i < 25; i++) {
			System.out.print(pb.getNextChar());
		}
		System.out.print("\n@@@@@@@@@@@@@@@@@@@@@\n" + pb.getAString());
		System.out.print("\n!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!\n");
		for (int i = 0; i < 25; i++) {
			System.out.print(pb.getNextChar());
		}
		System.out.print("\n@@@@@@@@@@@@@@@@@@@@@\n" + pb.getAString());
		System.out.print("\n!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!\n");
		for (int i = 0; i < 35; i++) {
			System.out.print(pb.getNextChar());
		}
		System.out.println("\n@@@@@@@@@@@@@@@@@@@@@\n" + pb.getAString());
		System.out.print("\n!!!!!!!!!!!!@@@@@@@@@@@@@@@@@@@@@\n" + pb.getNextChar());
		System.out.print("\n@@@@@@@@@@@@@@@@@@@@@\n" + pb.getAString());
		return;
	}

}
