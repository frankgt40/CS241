package com.uci.ccai6;

public class ErrorWarningReporter {
	public static enum ReportType {ERROR,WARNING,VERBOSE};
	private ReportType __type;
	private String message;
	private String filename;
	private long lineNum;
	public static boolean isVerbose = false;
	public ErrorWarningReporter(ReportType __type,
			String filename, long lineNum, String message) {
		super();
		this.__type = __type;
		this.message = message;
		this.filename = filename;
		this.lineNum = lineNum;
		emitReport();
	}

	public void emitReport() {
		switch(__type) {
		case ERROR:
			System.out.println("\n[ERROR]-[File: " + this.filename + "]-[Line: " + this.lineNum + "]:\n\t" + this.message.trim() +".");
			break;
		case WARNING:
			System.out.println("\n[WARNING]-[File: " + this.filename + "]-[Line: " + this.lineNum + "]:\n\t"+ this.message.trim() +".");
			break;
		case VERBOSE:
			if (isVerbose)
				System.out.println("\n[VERBOSE]-[File: " + this.filename + "]-[Line: " + this.lineNum + "]:\n\t"+ this.message.trim() +".");
			break;
		default:
			break;
		}
	}
}
