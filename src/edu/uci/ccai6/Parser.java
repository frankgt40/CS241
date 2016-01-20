package edu.uci.ccai6;

import java.io.File;
import java.util.Arrays;


import edu.uci.ccai6.Token.TokenType;
//import edu.uci.ccai6.Token.TokenType;
import edu.uci.ccai6.exception.ParserException;

public class Parser {
	
	static String KEYWORD_IF = "if";
	static String KEYWORD_LET = "let";
	static String KEYWORD_CALL = "call";
	static String KEYWORD_THEN = "then";
	static String KEYWORD_ELSE = "else";
	static String KEYWORD_FI = "fi";
	static String KEYWORD_WHILE = "while";
	static String KEYWORD_DO = "do";
	static String KEYWORD_OD = "od";
	static String KEYWORD_RETURN = "return";
	static String KEYWORD_VAR = "var";
	static String KEYWORD_ARRAY = "array";
	static String KEYWORD_FUNCTION = "function";
	static String KEYWORD_PROCEDURE = "procedure";
	static String KEYWORD_MAIN = "main";
	
	Lexer lex = null;
	boolean loaded = false;
	Token currentToken = null;
	
	Parser(Lexer l) {
		lex = l;
	}
	
	protected void next() {
		loaded = false;
	}
	
	protected Token current() {
		if(!loaded) {
			currentToken = lex.getAToken();
			loaded = true;
		}
		return currentToken;
	}
	
	protected String currentString() {
		return current().getValue();
	}

	void checkAndConsume(String s) throws ParserException {
		if(!currentString().equals(s)) throw new ParserException(s, currentString());
		next();
	}
	/*
	 * letter{letter | digit}
	 * TODO: maybe we should implement our own isLetter and isDigit..
	 */
	void ident() throws ParserException {
		if(current().getType() == TokenType.KEYWORD) {
			throw new ParserException("dont use reserved keyword");
		}
		String ident = currentString();
		if(!Character.isLetter(ident.charAt(0))) {
			throw new ParserException("identifier", ident);
		}
		for(int i=1; i<ident.length(); i++) {
			char c = ident.charAt(i);
			if(!Character.isLetter(c) && !Character.isDigit(c)) {
				throw new ParserException("identifier", ident);
			}
		}
		next();
	}
	
	void number() {
		String tokenString = currentString();
		for(int i=0; i<tokenString.length(); i++) {
			char c = tokenString.charAt(i);
			if(!Character.isDigit(c)) {
				new ParserException("number", tokenString);
			}
		}
		int num = Integer.valueOf(tokenString);
		next();
	}
	
	void designator() throws ParserException {
		ident();
		while(currentString().equals("[")) {
			next();
			expression();
			checkAndConsume("]");
		}
	}
	
	void factor() throws ParserException {
		if(currentString().equals("(")) {
			next();
			expression();
			checkAndConsume(")");
		} else if(currentString().equals("call")) {
			funcCall();
		} else if(Character.isLetter(currentString().charAt(0))) {
			designator();
		} else if(Character.isDigit(currentString().charAt(0))) {
			number();
		} else {
			throw new ParserException("factor", currentString());
		}
		
	}
	
	void term() throws ParserException {
		factor();
		while(currentString().equals("*") || currentString().equals("/")) {
			next();
			factor();
		}
	}
	void expression() throws ParserException {
		term();
		while(currentString().equals("+") || currentString().equals("-")) {
			next();
			term();
		}
	}
	
	boolean checkRelOp() {
		String in = currentString();
		if(in.equals("=") || in.equals("!")) {
			next();
			if(currentString().equals("=")) {
				next();
				return true;
			}
		} else if(in.equals("<") || in.equals(">")) {
			next();
			if(currentString().equals("=")) next();
			return true;
		} else if(Arrays.asList(new String[] {"<=",">=","==","!="}).contains(in)) {
			next();
			return true;
		}
		return false;
		
	}
	void relation() throws ParserException {
		expression();
		String in = currentString();
		if(!checkRelOp()) throw new ParserException("REL_OP", in);
//		next();
		expression();
	}
	
	void assignment() throws ParserException {
		checkAndConsume(KEYWORD_LET);
		
		designator();
		
		// TODO: non-simple returns <- at once?
		checkAndConsume("<");
		checkAndConsume("-");
		
		expression();
	}
	void funcCall() throws ParserException {
		checkAndConsume("call");
		
		ident();
		
		if(currentString().equals("(")) {
			next();
			if(currentString().equals(")")) {
				next();
				return;
			}
			expression();
			while(currentString().equals(",")) {
				next();
				expression();
			}
			checkAndConsume(")");
		}
		
	}
	void ifStatement() throws ParserException {
		checkAndConsume(KEYWORD_IF);
		relation();
		checkAndConsume(KEYWORD_THEN);
		statSequence();
		if(currentString().equals(KEYWORD_ELSE)) {
			next();
			statSequence();
		}
		checkAndConsume(KEYWORD_FI);
	}
	void whileStatement() throws ParserException {
		checkAndConsume(KEYWORD_WHILE);
		relation();
		checkAndConsume(KEYWORD_DO);
		statSequence();
		checkAndConsume(KEYWORD_OD);
	}
	void returnStatement() throws ParserException {
		// this is correct since only }, ; or expression comes after returnStatement
		checkAndConsume(KEYWORD_RETURN);
		if(currentString().equals("}") || currentString().equals(";")) return; 
		expression();
	}
	
	void statement() throws ParserException {
		String[] validKeywords = new String[] {KEYWORD_LET, KEYWORD_CALL,
				KEYWORD_IF, KEYWORD_WHILE, KEYWORD_RETURN};
		String in = currentString();
		if(in.equals(KEYWORD_LET)) {
			assignment();
		} else if (in.equals(KEYWORD_CALL)) {
			funcCall();
		} else if (in.equals(KEYWORD_IF)) {
			ifStatement();
		} else if (in.equals(KEYWORD_WHILE)) {
			whileStatement();
		} else if (in.equals(KEYWORD_RETURN)) {
			returnStatement();
		} else {
			throw new ParserException("keyword", currentString());
		}
	}
	void statSequence() throws ParserException {
		statement();
		while(currentString().equals(";")) {
			next();
			statement();
		}
	}
	
	void typeDecl() throws ParserException {
		if(currentString().equals(KEYWORD_ARRAY)) {
			next();
			checkAndConsume("[");
			number();
			checkAndConsume("]");
			while(currentString().equals("[")) {
				next();
				number();
				checkAndConsume("]");
			}
			
		} else if(currentString().equals(KEYWORD_VAR)) {
			next();
		} else {
			throw new ParserException("typeDecl keyword", currentString());
		}
	}
	void varDecl() throws ParserException {
		typeDecl();
		ident();
		while(currentString().equals(",")) {
			next();
			ident();
		}
		checkAndConsume(";");
	}
	void funcDecl() throws ParserException {
		if(currentString().equals(KEYWORD_FUNCTION) || currentString().equals(KEYWORD_PROCEDURE)) {
			next();
			ident();
			if(currentString().equals("(")) {
				formalParam();
			}
			checkAndConsume(";");
			funcBody();
			checkAndConsume(";");
			
		}
	}
	void formalParam() throws ParserException {
		checkAndConsume("(");
		if(Character.isLetter(currentString().charAt(0))) {
			ident();
			while(currentString().equals(",")) {
				next();
				ident();
			}
		}
		checkAndConsume(")");
	}
	void funcBody() throws ParserException {
		String[] varDeclKeywords = new String[] {KEYWORD_VAR, KEYWORD_ARRAY};
		while(Arrays.asList(varDeclKeywords).contains(currentString())){
			varDecl();
		}
		checkAndConsume("{");
		String[] statementKeywords = new String[] {KEYWORD_LET, KEYWORD_CALL,
				KEYWORD_IF, KEYWORD_WHILE, KEYWORD_RETURN};
		if(Arrays.asList(statementKeywords).contains(currentString())) {
			statSequence();
		}
		checkAndConsume("}");
		
	}
	
	void computation() throws ParserException {
		checkAndConsume(KEYWORD_MAIN);
		String[] varDeclKeywords = new String[] {KEYWORD_VAR, KEYWORD_ARRAY};
		while(Arrays.asList(varDeclKeywords).contains(currentString())){
			varDecl();
		}
		String[] funcDeclKeywords = new String[] {KEYWORD_FUNCTION, KEYWORD_PROCEDURE};
		while(Arrays.asList(funcDeclKeywords).contains(currentString())){
			funcDecl();
		}
		checkAndConsume("{");
		statSequence();
		checkAndConsume("}");
		checkAndConsume(".");
	}

	public static void main(String[] args) throws ParserException {
		File folder = new File("./testCases");
		File[] listOfFiles = folder.listFiles();

	    for (int i = 0; i < listOfFiles.length; i++) {
	      if (listOfFiles[i].isFile()) {
	    	if(listOfFiles[i].getName().charAt(0) == '0') continue;
	        System.out.println("File ./" + listOfFiles[i].getName());
			Parser parser = new Parser(new Lexer("./testCases/"+ listOfFiles[i].getName()));
			parser.computation();
	        System.out.println("done");
	      }
	    }
	}

}
