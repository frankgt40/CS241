package edu.uci.ccai6.cs241;

public class Parser {
	private Lexer __lx;
	private Token __currToken;
	private IRGenerator __IR = new IRGenerator();
	private FunctionUtil __funUtil = new FunctionUtil("");
	private String __SEP = "_";
	public static void main(String args[]) {
		Parser pa = new Parser("testCases/001.txt");
		pa.computation();
	}
	public Parser(String fileName) {
		__lx = new Lexer(fileName);
		
	}
	protected void computation() {
		 __currToken = __lx.nextToken();
		if (__currToken.getValue().equals("main")) {
			String funName = __currToken.getValue();
			__IR.putCode(".data");//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
			__currToken = __lx.nextToken();
			while (__currToken.getValue().equals("var") || __currToken.getValue().equals("array")) {
				// There are some varDecl
				varDecl(funName);
			} 
			while (__currToken.getValue().equals("function")  || __currToken.getValue().equals("procedure")) {
				// There are some funcDecl
				funcDecl();
			}

			__IR.putCode(".text");//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
			__IR.putCode("main:");//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
			//__IR.print();
			
			if (__currToken.getType() == Token.TokenType.L_BRACE) {
				__currToken = __lx.nextToken();
				// There is a statSequence
				statSequence(funName);
				if (__currToken.getType() == Token.TokenType.R_BRACE) {
					__currToken = __lx.nextToken();
					if (__currToken.getType() == Token.TokenType.DOT) {
						__IR.putCode("end");
						__IR.print();
						new Reporter(Reporter.ReportType.VERBOSE,__lx.fileName(), __lx.lineNum(), __lx.charPos(), "You have successfully compile this file!");
						System.exit(0);
					} else {
						reportError("Missing the dot!");
					}
				} else {
					reportError("Missing the right brace!");
				}
					
			} else {
				reportError("Something followed by \"main\" is wrong!");
			}
		} else {
			reportError("There should be \"main\" keyword!");
		}
	}
	protected void varDecl(String funName) {
		Result rsl = typeDecl();
		if (__currToken.getType() == Token.TokenType.VARIABLE) {
			//rsl.setFirstPart(rsl.getFirstPart() + __IR.getANewVarAddress() + ", "); //New var address in IR!
			//String fixed = rsl.fix(__IR.getScopeName()+__currToken.getValue(), __IR.getANewVarAddress()); 
			String fixed = rsl.fix(funName + __SEP +__currToken.getValue(), __IR.getANewVarAddress());
			__IR.putCode(fixed); //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
			/// To be continued...

			__currToken = __lx.nextToken();
			while (__currToken.getType() == Token.TokenType.COMMA) {
				__currToken = __lx.nextToken();
				if (__currToken.getType() == Token.TokenType.VARIABLE) {
					//rsl.setFirstPart(rsl.getFirstPart() + __IR.getANewVarAddress() + ", "); //New var address in IR!
					//fixed = rsl.fix(__IR.getScopeName()+__currToken.getValue(), __IR.getANewVarAddress());
					fixed = rsl.fix(funName + __SEP +__currToken.getValue(), __IR.getANewVarAddress());
					__IR.putCode(fixed); //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
				} else {
					reportError("There should be a variable!");
				}
				__currToken = __lx.nextToken();
			}
			
			if (__currToken.getType() != Token.TokenType.SEMICOLON) {
				reportError("Missing a semicolon!");
			} else {
				__currToken = __lx.nextToken();
			}
		} else {
			reportError("There should be a variable!");
		}		
	}
	protected void reportError(String msg) {
		new Reporter(Reporter.ReportType.ERROR,__lx.fileName(), __lx.lineNum(), __lx.charPos(), msg);
		System.exit(-1);
	}
	protected Result typeDecl() {
		Result rsl;
		if (__currToken.getValue().equals("var")){
			rsl = new Result("LOAD ", Result.Type.VAR); //FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF
			__currToken = __lx.nextToken();
			return rsl;
		} else if (__currToken.getValue().equals("array")) {
			__currToken = __lx.nextToken();
			String firstPart = "";
			while (__currToken.getType() == Token.TokenType.L_BRACKET) {
				__currToken = __lx.nextToken();
				if (__currToken.getType() == Token.TokenType.INSTANT) {
					// Got the number
					int number = Integer.parseInt(__currToken.getValue());
					firstPart += "\t\tDW ";
					for (int i = 0; i < number; i++) {
						firstPart += "0x0000 ";
					}
				} else {
					// Wrong syntax!
					reportError("There should be a number");
				}
				__currToken = __lx.nextToken();
				if (__currToken.getType() == Token.TokenType.R_BRACKET) {
					
				} else {
					reportError("There should be a right bracket");
				}
				firstPart += "\n";
				__currToken = __lx.nextToken();
			}
			rsl = new Result(firstPart, Result.Type.ARRAY); //FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF
			return rsl;
		}
		return null;
		
	}
	protected void funcDecl() {
		Result rsl = typeDecl();
		String functionName;
		if (__currToken.getValue().equals("function") || __currToken.getValue().equals("procedure")) {
			__currToken = __lx.nextToken();
			if (__currToken.getType() == Token.TokenType.VARIABLE) {
				functionName = __currToken.getValue();
				__IR.putCode(functionName + ":");
				__funUtil.setFunName(functionName);
				formalParam();
				if (__currToken.getType() == Token.TokenType.SEMICOLON) {
					__currToken = __lx.nextToken();
					funcBody(functionName);
					if (__currToken.getType() == Token.TokenType.SEMICOLON) {
						__currToken = __lx.nextToken();
					} else {
						reportError(" In function declaration, after the function body.Missing a semincolon!");						
					}
				} else {
					reportError("In function declaration, before the function body. Missing a semincolon! ");
				}
			} else {
				reportError("Missing function indentifier! In function declaration.");
			}
		}
	}
	protected void formalParam() {
		__currToken = __lx.nextToken();
		if (__currToken.getType() == Token.TokenType.L_PARENTHESIS) {
			// Then there is a formalParam
			__currToken = __lx.nextToken();
			if (__currToken.getType() == Token.TokenType.VARIABLE) {
				// There is a parameter, then go get it
				String tokenValue = __currToken.getValue();
				__funUtil.newVarName(tokenValue);
				__IR.putCode("LOAD " + __funUtil.findVarRealName(tokenValue) + ", " + __funUtil.getFunName() + __SEP + tokenValue); //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
				__currToken = __lx.nextToken();

				while (__currToken.getType() == Token.TokenType.COMMA) {
					__currToken = __lx.nextToken();
					if (__currToken.getType() == Token.TokenType.VARIABLE) {
						// There is a parameter, then go get it
						tokenValue = __currToken.getValue();
						__funUtil.newVarName(tokenValue);
						__IR.putCode("LOAD " + __funUtil.findVarRealName(tokenValue) + ", " + __funUtil.getFunName() + __SEP + tokenValue); //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
						__currToken = __lx.nextToken();
					} else {
						reportError("Missing a viarable! In function formal parameter part.");
					}
				}
				if (__currToken.getType() == Token.TokenType.R_PARENTHESIS) {
					__currToken = __lx.nextToken(); // Successfully finished
				} else {
					reportError("Missing a \')\'! In function formal parameter part.");
				}
			}
		} else {
			// Then there is no formalParam
		}
	}
	protected void funcBody(String funName) {
		while (__currToken.getValue().equals("var") || __currToken.getValue().equals("array")) {
			// There are some varDecl
			varDecl(funName);
		} 
		if (__currToken.getType() == Token.TokenType.L_BRACE) {
			// To be continued...
			__currToken = __lx.nextToken();
			statSequence(funName);
			if (__currToken.getType() == Token.TokenType.R_BRACE) {
				// Successfully finished
				__currToken = __lx.nextToken();
			} else {
				reportError("Missing a \'}\'! In function function body.");
			}
		} else {
			reportError("Missing a \'{\'! In function function body.");
		}
	}
	protected void statSequence(String funName) {
		statement(funName);
		while (__currToken.getType() == Token.TokenType.SEMICOLON) {
			__currToken = __lx.nextToken();
			statement(funName);
		}
	}
	protected void statement(String funName) {
		switch(__currToken.getValue()) {
		case "let":
			assignment(funName);
			break;
		case "call":
			funcCall(funName);
			break;
		case "if":
			ifStatement(funName);
			break;
		case "while":
			whileStatement(funName);
			break;
		case "return":
			returnStatement(funName);
			break;
		default:
			//reportError("Wrong statement!");
			return;
		}
	}
	protected void assignment(String funName) {
		if (__currToken.getValue().equals("let")) {
			String code = "";
			__currToken = __lx.nextToken();
			AssignDestination dst = designator(funName);
			if (dst.isConstant()) {
				reportError("In assignment, you can assign something to a constant!");
			}
			//__currToken = __lx.nextToken();
			if (__currToken.getType() == Token.TokenType.ASSIGN) {
				__currToken = __lx.nextToken();
				AssignDestination source = expression(funName);
				// Successfully finished
				//String to = funName + __SEP + dst.getDestination(); ////jiajiajiajiajiajiajijia
				String to = dst.getDestination(); ////jiajiajiajiajiajiajijia
				String from = source.getDestination();
//				if (source.isConstant()) {
//					from = source.getDestination();
//				} else {
//					from = funName + __SEP + source.getDestination();
//				}
				if (dst.isArray()) {
					// if the destination is array, then we need to store instead of move
					code = "STORE " + from + ", " + to;
					__IR.putCode(code); //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
				} else {
					// the destination is a variable, then we can move
					code = "MOVE " + from + ", " + to;
					__IR.putCode(code); //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
				}
				__currToken = __lx.nextToken();
			} else {
				reportError("In assignment, missing a \'<-\'!");
			}
		}
	}

	protected AssignDestination expression(String funName) {
		AssignDestination dst = term(funName);
		AssignDestination source;
		String op;
		while (__currToken.getType() == Token.TokenType.ADD || __currToken.getType() == Token.TokenType.SUB) {
			op = __currToken.getType() == Token.TokenType.ADD ? "ADD" : "SUB";
			__currToken = __lx.nextToken();
			source = term(funName);
			String code;
			if (source.isConstant()) {
				code = op + "i " + dst.getDestination() + ", " + source.getDestination() + ", " + dst.getDestination();
			} else {
				code = op + " " + dst.getDestination() + ", " + source.getDestination() + ", " + dst.getDestination();
			}
			__IR.putCode(code);
		}
		return dst;
		
	}
	protected AssignDestination term(String funName) {
		AssignDestination dst = factor(funName);
		AssignDestination source;
		//__currToken = __lx.nextToken();
		String op;
		while (__currToken.getType() == Token.TokenType.MUL || __currToken.getType() == Token.TokenType.DIV) {
			op = __currToken.getType() == Token.TokenType.MUL ? "MUL" : "DIV";
			__currToken = __lx.nextToken();
			source = factor(funName);
			String code;
			if (source.isConstant()) {
				code = op + "i " + dst.getDestination() + ", " + source.getDestination() + ", " + dst.getDestination();
			} else {

				code = op + " " + dst.getDestination() + ", " + source.getDestination() + ", " + dst.getDestination();
			}
			__IR.putCode(code);
			__currToken = __lx.nextToken();
		}
		return dst;
	}
	protected AssignDestination factor(String funName) {
		AssignDestination dst;
		switch(__currToken.getType()) {
		case VARIABLE:
			dst = designator(funName);
			return dst;
		case INSTANT:
			dst = new AssignDestination(__currToken.getValue());
			dst.setIsConstant(true);
			return dst;
		case L_PARENTHESIS:
			dst = expression(funName);
			if (__currToken.getType() == Token.TokenType.R_PARENTHESIS) {
				return dst;
			} else {
				
			}
		case KEYWORD:
			if (__currToken.getValue().equals("call")) {
				dst = funcCall(funName);
				return dst;
			} else {
				reportError("In factor, factor can only be a variable, instant, (expression), or function call!");
			}
		default:
			reportError("In factor, factor can only be a variable, instant, (expression), or function call!");
			return null;
		}
	}
	protected AssignDestination funcCall(String funName) {  //?????????????????????????????????????????????
		AssignDestination dst;
		if (!__currToken.getValue().equals("call")) {
			return null;
		} else {
			reportError("In funcCall, missing the \'call\' keyword!");
			return null;
		}
	}
	protected AssignDestination designator(String funName) {
		if (__currToken.getType() == Token.TokenType.VARIABLE) {
			String idName = __currToken.getValue();
			idName = funName + __SEP +idName;
			__currToken = __lx.nextToken();
			//String nestNo = funName+".";
			String code = "";
			AssignDestination num = new AssignDestination(idName);
			while (__currToken.getType() == Token.TokenType.L_BRACKET) {
				__currToken = __lx.nextToken();
				num = expression(funName);
				num.setIsArray(true); // Set is the array
				code = "MUL " + num.getDestination() + ", " + IRGenerator.__DW + ", " + num.getDestination(); 
				__IR.putCode(code); //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
				code = "ADDA " + idName + ", " + num.getDestination() + ", " + idName; //a[i]= a + i * 4;
				__IR.putCode(code); //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
				//__currToken = __lx.nextToken();
				if (__currToken.getType() == Token.TokenType.R_BRACKET) {
					// successfully finished this round
					__currToken = __lx.nextToken();
				} else {
					reportError("In designator, missing a \']'!");
				}
			}
			//__IR.print();
			// return the idName who has the address of the array item
			num.setDestination(idName);
			return num;
		} else {
			reportError("In designator, missing a variable!");
		}
		return null;
		
	}
	protected void ifStatement(String funName) {

	}
	protected void whileStatement(String funName) {

	}
	protected void returnStatement(String funName) {

	}
	
}
