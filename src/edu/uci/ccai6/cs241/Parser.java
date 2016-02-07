package edu.uci.ccai6.cs241;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class Parser {
	public static boolean __isVerbose = true; // Can be configured later!
	public static boolean __isWriteToFile = true; // Can be configured later!
	private Lexer __lx;
	private Token __currToken;
	private IRGenerator __IR = new IRGenerator();
	private FunctionUtil __funUtil = new FunctionUtil("");
	public static final String __SEP = "_";
	private String __inFile;
	private String __outFile;
	private PrintWriter __out;
	public static void main(String args[]) {
		Parser pa = new Parser("testCases/001.txt");
		pa.setOutFile("output/001.out.txt");
		pa.computation();
		if (__isWriteToFile) {
			List<String> codeList = pa.getIR().getIRBuffer();
			for (String codeLine : codeList) {
				pa.getOut().println(codeLine);
			}
			pa.getOut().close();
		}
	}
	public IRGenerator getIR() {
		return __IR;
	}
	public String getInFile() {
		return __inFile;
	}
	public PrintWriter getOut() {
		return __out;
	}
	public void setOutFile(String fileName) {
		__outFile = fileName;
		try {
			__out = new PrintWriter(__outFile);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public Parser(String fileName) {
		__inFile = fileName;
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
						//System.exit(0);
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
	protected AssignDestination statSequence(String funName) {
		AssignDestination left = statement(funName);
		AssignDestination right = null;
		while (__currToken.getType() == Token.TokenType.SEMICOLON) {
			__currToken = __lx.nextToken();
			right = statement(funName);
			if(left != null) left = left.join(right);
			else left = right;
		}
		return left;
	}
	protected AssignDestination statement(String funName) {
		switch(__currToken.getValue()) {
		case "let":
			return assignment(funName);
		case "call":
			funcCall(funName);
			break;
		case "if":
			return ifStatement(funName);
		case "while":
			return whileStatement(funName);
		case "return":
			returnStatement(funName);
			break;
		default:
			//reportError("Wrong statement!");
			return null;
		}
		return null;
	}
	protected AssignDestination assignment(String funName) {
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
				//__currToken = __lx.nextToken();
			} else {
				reportError("In assignment, missing a \'<-\'!");
			}
			return dst;
		}
		return null;
	}

	protected AssignDestination expression(String funName) {
		AssignDestination left = term(funName);
		AssignDestination right;
		String op;
		boolean allConstant = false;
		int rsl = 0;
		int tmpR = 0;
		int tmpL = 0;
		String code = null;
		while (__currToken.getType() == Token.TokenType.ADD || __currToken.getType() == Token.TokenType.SUB) {
			op = __currToken.getType() == Token.TokenType.ADD ? "ADD" : "SUB";
			__currToken = __lx.nextToken();
			right = term(funName);
			if (right.isConstant()) {
				tmpR  = Integer.parseInt(right.getDestination());
				if (left.isConstant()) {
					tmpL = Integer.parseInt(left.getDestination()); 
					if (op.equals("ADD")) {
						// ADD
						rsl = tmpL + tmpR;
					} else {
						// SUB
						rsl = tmpL - tmpR;
					}
					left.setDestination(new Integer(rsl).toString());
					left.setIsConstant(true);
					allConstant = true;
				} else {
					allConstant = false;
					// left is not constant!
					code = op + "i " + left.getDestination() + ", " + tmpR + ", " + left.getDestination();
				}
			} else {
				allConstant = false;
				if (left.isConstant()) {
					tmpL = Integer.parseInt(left.getDestination());
					code = op + "i " + tmpL + ", " + right.getDestination() + ", " + right.getDestination();
					left = right; // Important!
				} else {
					code = op + " " + left.getDestination() + ", " + right.getDestination() + ", " + left.getDestination();
				}
			}
			if (!allConstant) {
				left = __IR.putCode(code); //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
				left.join(right);
			}
		}
		if (allConstant) {
			left.setDestination(new Integer(rsl).toString());
			left.setIsConstant(true);
		}
		return left;
		
	}
	protected AssignDestination term(String funName) {
		AssignDestination left = factor(funName);
		AssignDestination right;
		String op;
		String code = null;
		boolean allConstant = false;
		int rsl = 0;
		int tmpR = 0;
		int tmpL = 0;
		while (__currToken.getType() == Token.TokenType.MUL || __currToken.getType() == Token.TokenType.DIV) {
			op = __currToken.getType() == Token.TokenType.MUL ? "MUL" : "DIV";
			__currToken = __lx.nextToken();
			right = factor(funName);
			if (right.isConstant()) {
				tmpR  = Integer.parseInt(right.getDestination());
				if (left.isConstant()) {
					tmpL = Integer.parseInt(left.getDestination()); 
					if (op.equals("MUL")) {
						// MUL
						rsl = tmpL * tmpR;
					} else {
						// DIV
						rsl = tmpL / tmpR;
					}
					left.setDestination(new Integer(rsl).toString());
					left.setIsConstant(true);
					allConstant = true;
				} else {
					// a * 3
					allConstant = false;
					code = op + "i " + left.getDestination() + ", " + tmpR + ", " + left.getDestination();
				}
			} else {
				allConstant = false;
				if (left.isConstant()) {
					tmpL = Integer.parseInt(left.getDestination()); 
					// 3 * a
					code = op + "i " + tmpL + ", " + right.getDestination() + ", " + right.getDestination();
					left = right; // Important!
				} else {
					// a * b
					code = op + " " + left.getDestination() + ", " + right.getDestination() + ", " + left.getDestination();
				}
			}
			if (!allConstant)
				left = __IR.putCode(code); //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
		}
		if (allConstant) {
			left.setDestination(new Integer(rsl).toString());
			left.setIsConstant(true);
		}
		return left;
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
			__currToken = __lx.nextToken();
			return dst;
		case L_PARENTHESIS:
			__currToken = __lx.nextToken();
			dst = expression(funName);
			if (__currToken.getType() == Token.TokenType.R_PARENTHESIS) {
				__currToken = __lx.nextToken();
				return dst;
			} else {
				reportError("In factor, missing a \')\'!");
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
		//return dst;
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
				code = "MULi " + num.getDestination() + ", " + IRGenerator.__DW + ", " + num.getDestination(); 		
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
	
	void next() {
		__currToken = __lx.nextToken();
	}
	
	AssignDestination relation(String funName) {
		AssignDestination left = expression(funName);
		if(!Arrays.asList(new String[] {"<",">","<=",">=","==","!="}).contains(__currToken.getValue())) {
			reportError("where's relop??");
		}
		Token relOp = __currToken;
		next();
		AssignDestination right = expression(funName);
		AssignDestination res = __IR.putCode("CMP "+left.getDestination()+" "+right.getDestination());
		res.setRelOp(relOp.getValue());
		return res.join(left).join(right);
		
	}
	private String getBranchOp(AssignDestination resRel) {

		if(resRel.getRelOp() == null) {
			reportError("relop is invalid");
		}
		
		// check relOp and convert/invert it to corresponding branch
		String op = null;
		switch(resRel.getRelOp()) {
		case "<":
			op = "BGE";
			break;
		case "<=":
			op = "BGT";
			break;
		case ">":
			op = "BLE";
			break;
		case ">=":
			op = "BLT";
			break;
		case "==":
			op = "BNE";
			break;
		case "!=":
			op = "BEQ";
			break;
		}
		return op;
	}
	protected AssignDestination ifStatement(String funName) {
		if(!__currToken.getValue().equals("if")) {
			reportError("where's if keyword??");
		}
		next();
		AssignDestination resRel = relation(funName);
		
		// check relOp and convert/invert it to corresponding branch
		String relOp = getBranchOp(resRel);
		long cmpPtr = __IR.getCurrPc()+1; // one instruction below CMP
		if(!__currToken.getValue().equals("then")) {
			reportError("where's then keyword??");
		}
		next();
		AssignDestination ifSS = statSequence(funName);
		
		// now we are done with if(rel) then statSequence(..)
		// so if there's no else, instruction number of follow is currPc+2
		//
		// again this advanced ptr is dangerous 
		// so call putCode(Code) before calling putCode(Code,Index) after calling IfStatement

		if(__currToken.getValue().equals("else")) {
			long elsePtr = __IR.getCurrPc()+1;
			next();
			AssignDestination elseSS = statSequence(funName);
			
			// now followPtr should point to either first Phi if existed or first follow inst
			long followPtr = __IR.getCurrPc()+3; 
			__IR.putCode("BRA"+" "+new AssignDestination(followPtr).getDestination(), elsePtr);

			// put relOp at CMP+1
			// 1st arg is result of relation
			// 2nd is first instruction of merged block := currPc-sizeof(PHI)+1
			__IR.putCode(relOp+" "+resRel.getDestination()+" "+new AssignDestination(followPtr).getDestination(), cmpPtr);
			
			ifSS = ifSS.intersectVars(elseSS);
			for(String pv : ifSS.getAssignedVars()) {
	          __IR.putCode("PHI "+pv+" "+pv+" "+pv);
			}
		} else {
			long followPtr = __IR.getCurrPc()+2;
			// put relOp at CMP+1
			// 1st arg is result of relation
			// 2nd is first instruction of merged block := currPc-sizeof(PHI)+1
			__IR.putCode(relOp+" "+resRel.getDestination()+" "+new AssignDestination(followPtr).getDestination(), cmpPtr);
		}
		
		if(!__currToken.getValue().equals("fi")) {
			reportError("where's fi keyword??");
		}
		next();
		return ifSS;
	}
	protected AssignDestination whileStatement(String funName) {
		if(!__currToken.getValue().equals("while")) reportError("where's while?");
		next();
		long cmpPtr = __IR.getCurrPc()+1; // location of CMP op, which will be loc of PHI's later
		AssignDestination whileRel = relation(funName);
		String branchOp = getBranchOp(whileRel);
		
		if(!__currToken.getValue().equals("do")) reportError("where's do?");
		next();
		AssignDestination whileSS = statSequence(funName);
		if(!__currToken.getValue().equals("od")) reportError("where's od?");
		next();
		
		// compute intersection of assigned vars in both blocks
		Set<String> intersectVars = whileRel.intersectVars(whileSS).getAssignedVars();
		// point to CMP if no PHI's exist or first PHI pointer otherwise
		// location of next PHI is curr+1+sizeof(PHIS)
		__IR.putCode("BRA "+new AssignDestination(intersectVars.size() == 0 ? cmpPtr : __IR.getCurrPc()+1+intersectVars.size()).getDestination());

		// add all phis
		for(String pv : whileRel.intersectVars(whileSS).getAssignedVars()) {
          __IR.putCode("PHI "+pv+" "+pv+" "+pv, cmpPtr);
		}
		
		// insert branchOp right after CMP
		// first argument is pointer of CMP
		// second argument is jumping address, which is (currPc+1 (actual currPC))+1(for next I)
		// put this code after all PHI's
		
		// Note: The idea of using advanced pointer (i.e. currPointer+2) is dangerous
		// because the next pointer has to be created by putCode(String)
		// and thus is below after current instruction
		// If the next pointer is inserted into higher location (i.e. putCode(String,Index))
		// then this will be incorrect
		//
		// TLDR: dont use putCode(String,Index) right after calling while statement
		__IR.putCode(branchOp+" "+whileRel.getDestination()+" "+new AssignDestination(__IR.getCurrPc()+2).getDestination(), cmpPtr+1+intersectVars.size());

		
		return whileSS;
	}
	protected void returnStatement(String funName) {

	}
	
}
