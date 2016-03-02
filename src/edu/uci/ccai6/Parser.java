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
	InstPointer pointer;
	IRGenerator irg;
	
	Parser(Lexer l) {
		lex = l;
		irg = new IRGenerator();
		pointer = new InstPointer(0);
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
	
	protected InstPointer getInstPointerAndInc() {
		InstPointer curPointer = new InstPointer(pointer);
		pointer.inc();
		return curPointer;
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
	Result ident() throws ParserException {
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
		return new Result(ident);
	}
	
	Result number() {
		String tokenString = currentString();
		for(int i=0; i<tokenString.length(); i++) {
			char c = tokenString.charAt(i);
			if(!Character.isDigit(c)) {
				new ParserException("number", tokenString);
			}
		}
		int num = Integer.valueOf(tokenString);
		next();
		return new Result(num);
	}
	
	Result designator() throws ParserException {
		// TODO: what to do??
		Result left = ident();
		Result right = null;
		while(currentString().equals("[")) {
			next();
			right = expression();
			checkAndConsume("]");
			left = irg.addInstruction("+", left, right, getInstPointerAndInc());
			left.setArray();
			// TODO: have to mult by later size..
		}
		// return array location
		return left;
	}
	
	Result factor() throws ParserException {
		Result res = null;
		if(currentString().equals("(")) {
			next();
			res = expression();
			checkAndConsume(")");
		} else if(currentString().equals("call")) {
			funcCall();
		} else if(Character.isLetter(currentString().charAt(0))) {
			res = designator();
		} else if(Character.isDigit(currentString().charAt(0))) {
			return number();
		} else {
			throw new ParserException("factor", currentString());
		}
		
		return res;
		
	}
	
	Result term() throws ParserException {
		Result left, right;
		left = factor();
		while(currentString().equals("*") || currentString().equals("/")) {
			String op = currentString();
			next();
			right = factor();
			left = irg.addInstruction(op, left, right, getInstPointerAndInc());
		}
		return left;
	}
	Result expression() throws ParserException {
		Result left, right;
		left = term();
		while(currentString().equals("+") || currentString().equals("-")) {
			String op = currentString();
			next();
			right = term();
			left = irg.addInstruction(op, left, right, getInstPointerAndInc());
		}
		return left;
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
	Result relation() throws ParserException {
		Result left = expression();
		String in = currentString();
		if(!checkRelOp()) throw new ParserException("REL_OP", in);
//		next();
		Result right = expression();
		left = irg.addInstruction(in, left, right, getInstPointerAndInc());
		return left;
	}
	
	public void updatePointer(InstPointer nPointer) {
		if(nPointer == null) return;
		pointer = new InstPointer(nPointer);
	}
	
	Result assignment() throws ParserException {
		checkAndConsume(KEYWORD_LET);
		
		Result left = designator();
		
		// TODO: non-simple returns <- at once?
		checkAndConsume("<");
		checkAndConsume("-");
		
		Result right = expression();
		left = irg.addInstruction("move", left, right, getInstPointerAndInc());
		
		// TODO: need to find a better way to handle pointer
		// array will trigger at least 2 consecutive instructions
		if(left.isArray) pointer.inc();
		if(right.isArray) pointer.inc();
		
		return left;
		
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
		Result left, right, resIf, resElse = null;
		checkAndConsume(KEYWORD_IF);
		left = relation();
		resIf = irg.addInstruction(left.relOp.toString(), left, new Result(pointer), getInstPointerAndInc());
		checkAndConsume(KEYWORD_THEN);
		right = statSequence();
		if(currentString().equals(KEYWORD_ELSE)) {
			resElse = irg.addInstruction("bra", new Result(pointer), null, getInstPointerAndInc());
			next();
			right = statSequence();
			irg.fix("bra", new Result(pointer), null, resElse.getPointer());
		}
		irg.fix(left.relOp.toString(), left, right, resIf.getPointer());
		checkAndConsume(KEYWORD_FI);
	}
	void whileStatement() throws ParserException {
		Result left, right, res;
		checkAndConsume(KEYWORD_WHILE);
		left = relation();
		res = irg.addInstruction(left.relOp.toString(), left, new Result(pointer), getInstPointerAndInc());
		checkAndConsume(KEYWORD_DO);
		right = statSequence();
		checkAndConsume(KEYWORD_OD);
		irg.fix(left.relOp.toString(), left, new Result(pointer), res.getPointer());
	}
	void returnStatement() throws ParserException {
		// this is correct since only }, ; or expression comes after returnStatement
		checkAndConsume(KEYWORD_RETURN);
		if(currentString().equals("}") || currentString().equals(";")) return; 
		expression();
	}
	
	Result statement() throws ParserException {
		String[] validKeywords = new String[] {KEYWORD_LET, KEYWORD_CALL,
				KEYWORD_IF, KEYWORD_WHILE, KEYWORD_RETURN};
		String in = currentString();
		if(in.equals(KEYWORD_LET)) {
			return assignment();
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
		return null;
	}
	Result statSequence() throws ParserException {
		Result res = statement();
		while(currentString().equals(";")) {
			next();
			res = statement();
		}
		return res;
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

        irg.print();
	}

	public static void main(String[] args) throws ParserException {
//		File folder = new File("./testCases");
//		File[] listOfFiles = folder.listFiles();
//
//	    for (int i = 0; i < listOfFiles.length; i++) {
//	      if (listOfFiles[i].isFile()) {
//	    	if(listOfFiles[i].getName().charAt(0) == '0') continue;
//	        System.out.println("File ./" + listOfFiles[i].getName());
//			Parser parser = new Parser(new Lexer("./testCases/"+ listOfFiles[i].getName()));
//			parser.computation();
//	        System.out.println("done");
//	      }
//	    }
	    Parser parser = new Parser(new Lexer("./testCases/test005.txt"));
		parser.computation();
	}

}
