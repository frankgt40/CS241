package edu.uci.ccai6.cs241;

public class Token {
	public enum TokenType {
		INSTANT("INSTANT"),
		VARIABLE("VARIABLE"),
		KEYWORD("KEYWORD"),
		OP("OP"),
		NULL("NULL"),
		PREDEFINED_FUNCTION("PREDEFINED_FUNCTION"),
		EQ("EQ"),
		NE("NE"),
		LT("LT"),
		LE("LE"),
		GT("GT"),
		GE("GE"),
		ADD("ADD"),
		SUB("SUB"),
		MUL("MUL"),
		DIV("DIV"),
		COMMA("COMMA"),
		SEMICOLON("SEMICOLON"),
		DOT("DOT"),
		ASSIGN("ASSIGN"),
		L_BRACKET("L_BRACKET"),
		R_BRACKET("R_BRACKET"),
		L_BRACE("L_BRACE"),
		R_BRACE("R_BRACE"),
		L_PARENTHESIS("L_PARENTHESIS"),
		R_PARENTHESIS("R_PARENTHESIS");
		
		String value;

		TokenType(String value) {
			this.value = value;
		}

		public void setValue(String str) {
			this.value = str;
		}
	};

	private TokenType type;
	private String value;

	public TokenType getType() {
		return type;
	}

	public void setType(TokenType type) {
		this.type = type;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public Token(TokenType type, String value) {
		super();
		this.type = type;
		this.value = value;
	}
	public Token() {
		super();
		this.type = TokenType.NULL;
		this.value = null;
	}
	public void setAll(TokenType type, String value) {
		this.type = type;
		this.value = value;
	}
	// For development
	public void print() {
		System.out.print("[Type: " + this.getType().value + ", Value: " + this.getValue() + "]");
	}
}
