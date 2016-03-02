package edu.uci.ccai6.cs241;

public class Reporter {
	private static Lexer __lx;
	public static enum ReportType {ERROR,WARNING,VERBOSE};
	private ReportType __type;
	private String message;
	private String filename;
	private long lineNum;
	private int charPos;
	public static void initialize(Lexer lx) {
		__lx = lx;
	}
	public Reporter(ReportType __type,String message) {
		super();
		if (message == null) message = "";
		this.__type = __type;
		this.message = message;
		this.filename = __lx.fileName();
		this.lineNum = __lx.lineNum();
		this.charPos = __lx.charPos();
		emitReport();
		System.exit(-1);
	}
	public Reporter(ReportType __type,
			String filename, long lineNum, int charPos, String message) {
		super();
		if (message == null) message = "";
		this.__type = __type;
		this.message = message;
		this.filename = filename;
		this.lineNum = lineNum;
		this.charPos = charPos;
		emitReport();
	}

	public void emitReport() {
		switch(__type) {
		case ERROR:
			System.out.println("\n[ERROR]-[File: " + this.filename + "]-[Line: " + this.lineNum + "]-[Char at: " + this.charPos + "]\n\t" + this.message.trim() +".");
			break;
		case WARNING:
			System.out.println("\n[WARNING]-[File: " + this.filename + "]-[Line: " + this.lineNum + "]-[Char at: " + this.charPos + "]\n\t"+ this.message.trim() +".");
			break;
		case VERBOSE:
			if (Parser.__isVerbose)
			System.out.println("\n[VERBOSE]-[File: " + this.filename + "]-[Line: " + this.lineNum + "]-[Char at: " + this.charPos + "]\n\t"+ this.message.trim() +".");
			break;
		default:
			break;
		}
	}
}
