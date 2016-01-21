package edu.uci.ccai6.cs241;

public class Lexer {
	PBuffers __pBuffer;
	
	public static void main(String args[]) {
		Lexer lx = new Lexer("testCases/001.txt");
		while (!lx.reachEOF()) {
			lx.nextToken().print();
			System.out.println();
		}
		
	}
	public Lexer(String fileName) {
		__pBuffer = new PBuffers(fileName);
	}
	public boolean reachEOF() {
		return __pBuffer.hasNext() ? false : true;
	}
	protected boolean isDigit(char ch) {
		return (ch >= '0' && ch <= '9') ? true : false;
	}
	protected boolean isLetter(char ch) {
		return (ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z') ? true : false;
	}
	protected boolean isWS(char ch) {
		return (ch == ' ' || ch == '\t' || ch == '\n' || ch == '\r') ? true : false;
	}
	public int lineNum() {
		return this.__pBuffer.get__lineNum();
	}
	public int charPos() {
		return this.__pBuffer.get__charPos();
	}
	public String fileName() {
		return this.__pBuffer.get__fileName();
	}
	public Token nextToken() {
		Token token = new Token();
		while (true) {
			if (reachEOF()) {
				new Reporter(Reporter.ReportType.WARNING,fileName(), lineNum(), charPos(), "We have reached the end of the file! Nothing is needed to be parsed!");
				token.setAll(Token.TokenType.NULL, "");
				return token;
			}
			String lexeme = "";
			char ch = __pBuffer.next();
			lexeme += ch;
			// Very first: 1.eat all the WS 2.ignore all the comments
			// Eat all the WS
			if (isWS(ch)) 
				continue;
			// Eat all the comments
			if (ch == '/' && __pBuffer.oracle() == '/') {
				while ((ch = __pBuffer.next()) != '\n')
					lexeme += ch;
				new Reporter(Reporter.ReportType.VERBOSE,fileName(), lineNum(), charPos(), "Ignored comments: " + lexeme);
				continue;
			}
			
			
			// Then if Token is a legal symbol
			// Then everything else is wrong!
			if (isLetter(ch)) {
				// First if Token is a identifier
				while (isLetter(__pBuffer.oracle()) || isDigit(__pBuffer.oracle())) {
					ch = __pBuffer.next();
					lexeme += ch;
				}

				// Then if Token is a keyword or a predefined function
				// Identifier
				// 'let', 'call', 'if', 'then', 'else', 'while', 'do', 'od',
				// 'return', 'var', 'array,
				// , 'procedure', 'main', 'InputNum', 'OutputNum',
				// 'OutputNewLine'
				// 'function','fi'
				switch (lexeme) {
				case "let":
				case "call":
				case "if":
				case "then":
				case "else":
				case "while":
				case "do":
				case "od":
				case "return":
				case "var":
				case "array":
				case "procedure":
				case "main":
				case "function":
				case "fi":
					// These are keywords
					token.setAll(Token.TokenType.KEYWORD, lexeme);
					return token;
				case "InputNum":
				case "OutputNum":
				case "OutputNewLine":
					// These are predefined function identifier
					token.setAll(Token.TokenType.PREDEFINED_FUNCTION, lexeme);
					return token;
				default:
					// These are variable identifiers
					token.setAll(Token.TokenType.VARIABLE, lexeme);
					return token;
				}
			} else if (isDigit(ch)) {
				// Then if Token is a number, but 12a is not a number!
				while (isDigit(ch = __pBuffer.oracle())) {
					ch = __pBuffer.next();
					lexeme += ch;
				}
				token.setAll(Token.TokenType.INSTANT, lexeme);
				return token;
			} else {
				/*
				 * ch could be: =,!,<,>,-,+,/,*,(,),[,],{,},',',;,.,
				 */
				switch (ch) {
				case '=':
					token.setAll(Token.TokenType.EQ, lexeme);
					return token;
				case '-':
					token.setAll(Token.TokenType.SUB, lexeme);
					return token;
				case '+':
					token.setAll(Token.TokenType.ADD, lexeme);
					return token;
				case '/': // Can't be comment! Because we have check it above!
					token.setAll(Token.TokenType.DIV, lexeme);
					return token;
				case '*':
					token.setAll(Token.TokenType.MUL, lexeme);
					return token;
				case '(':
					token.setAll(Token.TokenType.L_PARENTHESIS, lexeme);
					return token;
				case ')':
					token.setAll(Token.TokenType.R_PARENTHESIS, lexeme);
					return token;
				case '[':
					token.setAll(Token.TokenType.L_BRACKET, lexeme);
					return token;
				case ']':
					token.setAll(Token.TokenType.R_BRACKET, lexeme);
					return token;
				case '{':
					token.setAll(Token.TokenType.L_BRACE, lexeme);
					return token;
				case '}':
					token.setAll(Token.TokenType.R_BRACE, lexeme);
					return token;
				case ',':
					token.setAll(Token.TokenType.COMMA, lexeme);
					return token;
				case ';':
					token.setAll(Token.TokenType.SEMICOLON, lexeme);
					return token;
				case '.':
					// These are all Symbols
					token.setAll(Token.TokenType.DOT, lexeme);
					return token;
				case '<':
					if ((ch = __pBuffer.oracle()) == '-') {
						ch = __pBuffer.next();
						lexeme += ch;
						token.setAll(Token.TokenType.ASSIGN, lexeme);
						return token;
					} else if (ch == '=') {
						ch = __pBuffer.next();
						lexeme += ch;
						token.setAll(Token.TokenType.LE, lexeme);
						return token;
					} else {
						token.setAll(Token.TokenType.LT, lexeme);
						return token;
					}
				case '>':
					if ((ch = __pBuffer.oracle()) == '=') {
						ch = __pBuffer.next();
						lexeme += ch;
						token.setAll(Token.TokenType.GE, lexeme);
						return token;
					} else {
						token.setAll(Token.TokenType.GT, lexeme);
						return token;
					}
				case '!':
					if ((ch = __pBuffer.oracle()) == '=') {
						ch = __pBuffer.next();
						lexeme += ch;
						token.setAll(Token.TokenType.NE, lexeme);
						return token;
					} else {
						// Error part
						new Reporter(Reporter.ReportType.ERROR,fileName(), lineNum(), charPos(), "Unexpected character: " + lexeme);
						token.setAll(Token.TokenType.NULL, "");
						return token;
					}
				default:
					// Error part
					new Reporter(Reporter.ReportType.ERROR,fileName(), lineNum(), charPos(), "Unexpected character: " + lexeme);
					token.setAll(Token.TokenType.NULL, "");
					return token;

				}
			}
		}
	}
}
