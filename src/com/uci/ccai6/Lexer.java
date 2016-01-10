/**
 * Author: Cheng Cai
 * Partner: Norrathep Rattanavipanon
 * Course: CS241
 * Our little compiler :-)
 */
package com.uci.ccai6;


import java.io.IOException;

import com.uci.ccai6.*;

/**
 * @author Frank
 *
 */
public class Lexer {
	private String fileName;
	private PairBuffer pb;
	private boolean isSimpleVersion = true;
	
	public Lexer(String fileName) {
		super();
		this.fileName = fileName;
		pb = new PairBuffer(fileName);
		
		ErrorWarningReporter.isVerbose = true;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	
	public Token simpleNextToken() {
		Token token  = new Token(Token.TokenType.NULL,"");
		char aChar = pb.getNextChar();
		while (true) {
			switch(aChar) {
			case ' ':
			case '\t':
			case '\n':
			case '\r':
				// white space
				// ignore
				pb.getAString(); //!!!!!!!!!!!!getAString should be executed before getNextChar();
				aChar = pb.getNextChar();
				continue;
			case '#':
				// Comment, ignore the whole line
				while (aChar != '\n') {
					aChar = pb.getNextChar();
				}
				new ErrorWarningReporter(ErrorWarningReporter.ReportType.VERBOSE, 
								this.getFileName(), 
								this.pb.getTotalLineNum(), 
								"Ignore comment: " + pb.getAString());
				continue;
						
			case '/':
				//
				if (pb.oracleNextChar() == '/') {
					// also comment!
					while (aChar != '\n') {
						aChar = pb.getNextChar();
					}
					new ErrorWarningReporter(ErrorWarningReporter.ReportType.VERBOSE, 
							this.getFileName(), 
							this.pb.getTotalLineNum(), 
							"Ignore comment: " + pb.getAString());
					continue;
				} else {
					// then it's the operator DIV
					token.setAll(Token.TokenType.OP, pb.getAString());
					new ErrorWarningReporter(ErrorWarningReporter.ReportType.VERBOSE, 
							this.getFileName(), 
							this.pb.getTotalLineNum(), token.print());
				}
				return token;
			case '(':
				//
				token.setAll(Token.TokenType.OP, pb.getAString());
				new ErrorWarningReporter(ErrorWarningReporter.ReportType.VERBOSE, 
						this.getFileName(), 
						this.pb.getTotalLineNum(), token.print());
				return token;
			case ')':
				//
				token.setAll(Token.TokenType.OP, pb.getAString());
				new ErrorWarningReporter(ErrorWarningReporter.ReportType.VERBOSE, 
						this.getFileName(), 
						this.pb.getTotalLineNum(), token.print());
				return token;
			case '[':
				//
				token.setAll(Token.TokenType.OP, pb.getAString());
				new ErrorWarningReporter(ErrorWarningReporter.ReportType.VERBOSE, 
						this.getFileName(), 
						this.pb.getTotalLineNum(), token.print());
				return token;
			case ']':
				//
				token.setAll(Token.TokenType.OP, pb.getAString());
				new ErrorWarningReporter(ErrorWarningReporter.ReportType.VERBOSE, 
						this.getFileName(), 
						this.pb.getTotalLineNum(), token.print());
				return token;
			case '{':
				//
				token.setAll(Token.TokenType.OP, pb.getAString());
				new ErrorWarningReporter(ErrorWarningReporter.ReportType.VERBOSE, 
						this.getFileName(), 
						this.pb.getTotalLineNum(), token.print());
				return token;
			case '}':
				//
				token.setAll(Token.TokenType.OP, pb.getAString());
				new ErrorWarningReporter(ErrorWarningReporter.ReportType.VERBOSE, 
						this.getFileName(), 
						this.pb.getTotalLineNum(), token.print());
				return token;
			case '.':
				//
				token.setAll(Token.TokenType.OP, pb.getAString());
				new ErrorWarningReporter(ErrorWarningReporter.ReportType.VERBOSE, 
						this.getFileName(), 
						this.pb.getTotalLineNum(), token.print());
				return token;
			case ',':
				//
				token.setAll(Token.TokenType.OP, pb.getAString());
				new ErrorWarningReporter(ErrorWarningReporter.ReportType.VERBOSE, 
						this.getFileName(), 
						this.pb.getTotalLineNum(), token.print());
				return token;
			case ';':
				//
				token.setAll(Token.TokenType.OP, pb.getAString());
				new ErrorWarningReporter(ErrorWarningReporter.ReportType.VERBOSE, 
						this.getFileName(), 
						this.pb.getTotalLineNum(), token.print());
				return token;
			case '+':
				//
				token.setAll(Token.TokenType.OP, pb.getAString());
				new ErrorWarningReporter(ErrorWarningReporter.ReportType.VERBOSE, 
						this.getFileName(), 
						this.pb.getTotalLineNum(), token.print());
				return token;
			case '-':
				//
				token.setAll(Token.TokenType.OP, pb.getAString());
				new ErrorWarningReporter(ErrorWarningReporter.ReportType.VERBOSE, 
						this.getFileName(), 
						this.pb.getTotalLineNum(), token.print());
				return token;
			case '*':
				//
				token.setAll(Token.TokenType.OP, pb.getAString());
				new ErrorWarningReporter(ErrorWarningReporter.ReportType.VERBOSE, 
						this.getFileName(), 
						this.pb.getTotalLineNum(), token.print());
				return token;
			default:
				while (aChar != ' ' && aChar != '\n' && aChar != '\r' && aChar != '{' && aChar != '}'&& aChar != '('&& aChar != ')'&& aChar != '['&& aChar != ']' && aChar != '+'&&aChar != '-'&& aChar != '*'&& aChar != '/'&& aChar != ','&& aChar != ';') {
					aChar = pb.getNextChar();
				}
				pb.descreaseForwardPointer();
				String tokenStr = pb.getAString();
				token.setAll(getTokenType(tokenStr.trim()), tokenStr.trim());
				new ErrorWarningReporter(ErrorWarningReporter.ReportType.VERBOSE, 
						this.getFileName(), 
						this.pb.getTotalLineNum(), token.print());
				}
				return token;
			}
		}
	public Token.TokenType getTokenType(String tokenValue) {
		Token.TokenType type = Token.TokenType.NULL;
		if (tokenValue.equals("let")|| tokenValue.equals("call")||tokenValue.equals("if")
				||tokenValue.equals("then") || tokenValue.equals("else")||tokenValue.equals("fi")
				||tokenValue.equals("while")||tokenValue.equals("do")||tokenValue.equals("od")||tokenValue.equals("return")
				||tokenValue.equals("var")||tokenValue.equals("array")||tokenValue.equals("function")||tokenValue.equals("procedure")
				||tokenValue.equals("main")) {
			type = Token.TokenType.KEYWORD;
		} else if (tokenValue.equals("InputNum")|| tokenValue.equals("OutputNum")||tokenValue.equals("OutputNewLine")) {
			type = Token.TokenType.PREDEFINED_FUNCTION;
		} else{
			type = Token.TokenType.VARIABLE;
		}
		return type;
	}
	public Token getAToken() {
		// for the simple version
		if (this.isSimpleVersion) {
			Token token = simpleNextToken();
			return token;
		}
		
		Token token  = new Token(Token.TokenType.NULL,"");
		char aChar = pb.getNextChar();
		while (true) {
			switch(aChar) {
			case ' ':
			case '\t':
			case '\n':
			case '\r':
				// white space
				// ignore
				pb.getAString(); //!!!!!!!!!!!!getAString should be executed before getNextChar();
				aChar = pb.getNextChar();
				break;
			case '#':
				// Comment, ignore the whole line
				while (aChar != '\n') {
					aChar = pb.getNextChar();
				}
				new ErrorWarningReporter(ErrorWarningReporter.ReportType.VERBOSE, 
								this.getFileName(), 
								this.pb.getTotalLineNum(), 
								"Ignore comment: " + pb.getAString());
				break;
			case '<':
				//
				return token;
			case '=':
				//
				return token;
			case '>':
				//
				return token;
			case '!':
				//
				return token;
			case 'l':
				//
				return token;
			case 'c':
				//
				return token;
			case 'i':
				//
				return token;
			case 't':
				//
				return token;
			case 'e':
				//
				return token;
			case 'f':
				//
				return token;
			case 'd':
				//
				return token;
			case 'o':
				//
				return token;
			case 'p':
				//
				return token;
			case 'v':
				//
				return token;
			case 'w':
				//
				return token;
			case 'r':
				//
				return token;
			case 'm':
				//
				return token;
			case 'a':
				//
				return token;
			case '+':
				//
				return token;
			case '-':
				//
				return token;
			case '*':
				//
				return token;
			case '/':
				//
				if (pb.oracleNextChar() == '/') {
					// also comment!
					while (aChar != '\n') {
						aChar = pb.getNextChar();
					}
					new ErrorWarningReporter(ErrorWarningReporter.ReportType.VERBOSE, 
							this.getFileName(), 
							this.pb.getTotalLineNum(), 
							"Ignore comment: " + pb.getAString());
					break;
				} else {
					// then it's the operator DIV
					token.setAll(Token.TokenType.OP, pb.getAString());
					new ErrorWarningReporter(ErrorWarningReporter.ReportType.VERBOSE, 
							this.getFileName(), 
							this.pb.getTotalLineNum(), token.print());
				}
				return token;
			case '(':
				//
				return token;
			case ')':
				//
				return token;
			case '[':
				//
				return token;
			case ']':
				//
				return token;
			case '{':
				//
				return token;
			case '}':
				//
				return token;
			case '.':
				//
				return token;
			default:
				return token;
				
			}
		}
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
//		try {
			Lexer lx = new Lexer("testCases/factorial.txt");
			while (!lx.pb.reachEOF()) {
				lx.getAToken();
			}
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
			
	}

}


