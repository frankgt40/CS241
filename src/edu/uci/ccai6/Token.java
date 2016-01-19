package edu.uci.ccai6;

public class Token {
	public enum TokenType {
		INSTANT(""),
		VARIABLE(""),
		KEYWORD(""),
		OP(""),
		NULL("NULL"),
		PREDEFINED_FUNCTION(""),
		EQ("=="),
		NE("!="),
		LT("<"),
		LE("<="),
		GT(">"),
		GE(">="),
		ADD("+"),
		SUB("-"),
		MUL("*"),
		DIV("/"),
		COMMA(","),
		SEMICOLON(";"),
		DOT("."),
		ASSIGN("<-"),
		L_BRACKET("["),
		R_BRACKET("]"),
		L_BRACE("{"),
		R_BRACE("}"),
		L_PARENTHESIS("("),
		R_PARENTHESIS(")");
		
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
	public void setAll(TokenType type, String value) {
		this.type = type;
		this.value = value;
	}
	public String print() {
		return this.value;
//		String typeStr =  new String();
//		if (this.type == TokenType.INSTANT) {
//			typeStr = "INSTANT";
//		} else if (this.type == TokenType.VARIABLE) {
//			typeStr = "VARIABLE";
//		}else if (this.type == TokenType.KEYWORD) {
//			typeStr = "KEYWORD";
//		//}else if (this.type == TokenType.REL_OP) {
//		//	typeStr = "REL_OP";
//		}else if (this.type == TokenType.OP) {
//			typeStr = "OP";
//		}else if (this.type == TokenType.NULL) {
//			typeStr = "NULL";
//		} else if (this.type == TokenType.PREDEFINED_FUNCTION) {
//			typeStr = "PREDEFINED_FUNCTION";
//		}
//			case VARIABLE:
//				typeStr = "VARIABLE";
//				break;
//			case KEYWORD:
//				typeStr = "KEYWORD";
//				break;
//			case REL_OP:
//				typeStr = "REL_OP";
//				break;
//			case OP:
//				typeStr = "OP";
//				break;
//			case NULL:
//				typeStr = "NULL";
//				break;s
//		return ("[Token information]-[Type: " +  typeStr + "]-[Value: " + this.value + "]");
	}
}
