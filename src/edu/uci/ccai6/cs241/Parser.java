package edu.uci.ccai6.cs241;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.uci.ccai6.cs241.Token.TokenType;
import edu.uci.ccai6.cs241.RA.RegisterAllocator;
import edu.uci.ccai6.cs241.ssa.BasicBlock;
import edu.uci.ccai6.cs241.ssa.Instruction;
import edu.uci.ccai6.cs241.ssa.SSAConverter;

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
	public static final int GLB_REG = 30; // return reg
	public static final String GLOBAL_VAR_REG = "R"+GLB_REG; 
	public static final int CMP_REG = 27;
	public static final String CMP_VAR_REG = "R"+CMP_REG; 
	public static final int RTR_REG = 28; // return reg
	public static final String RTR_VAR_REG = "R"+RTR_REG; 
	
	public static void main(String args[]) {
		Parser pa = new Parser("testCases/final.txt");
		pa.setOutFile("output/final.out.txt");
		pa.computation();
		if (__isWriteToFile) {
			List<String> codeList = pa.getIR().getIRBuffer();
			for (String codeLine : codeList) {
				pa.getOut().println(codeLine);
			}
			pa.getOut().close();
			SSAConverter cnv = new SSAConverter(codeList);

		      List<Integer> bbNum = cnv.assignBlockNum();
		      int numBlocks = bbNum.get(bbNum.size()-1)+1;
		      List<BasicBlock> bbs = cnv.generateBasicBlocks(bbNum, numBlocks);
		      cnv.rename(bbs, bbNum);
		      cnv.copyProp();
		      cnv.cse();
		      cnv.copyProp();
//		      cnv.deadCodeElimination();
		      cnv.killPtrOp();
			for(int i=0; i<bbNum.size(); i++) {
				System.out.println(bbNum.get(i)+" : "+cnv.instructions.get(i)+" : "+cnv.instructions.get(i).funcName);
			}
			List<Instruction> insts = RegisterAllocator.assign(cnv.generateBasicBlocks(bbNum, numBlocks));
			for(Instruction inst : insts) System.out.println(inst);
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
		 Reporter.initialize(__lx);
		if (__currToken.getValue().equals("main")) {
			VarScoper.enter("main");
			String funName = __currToken.getValue();
			__IR.putCode("data:");//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
			__currToken = __lx.nextToken();
			while (__currToken.getValue().equals("var") || __currToken.getValue().equals("array")) {
				// There are some varDecl
				varDecl();
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
				statSequence();
				if (__currToken.getType() == Token.TokenType.R_BRACE) {
					__currToken = __lx.nextToken();
					if (__currToken.getType() == Token.TokenType.DOT) {
						VarScoper.exit();
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
	protected void varDecl() {
		Result rsl = typeDecl();
		if (__currToken.getType() == Token.TokenType.VARIABLE) {
			//rsl.setFirstPart(rsl.getFirstPart() + __IR.getANewVarAddress() + ", "); //New var address in IR!
			//String fixed = rsl.fix(__IR.getScopeName()+__currToken.getValue(), __IR.getANewVarAddress()); 
			VarScoper.declare(__currToken.getValue());
			String varName = VarScoper.genVarName(__currToken.getValue());
			String fixed = rsl.fix(varName, __IR.getANewVarAddress());
			//__IR.putCode(fixed); //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
			/// To be continued...

			__currToken = __lx.nextToken();
			while (__currToken.getType() == Token.TokenType.COMMA) {
				__currToken = __lx.nextToken();
				if (__currToken.getType() == Token.TokenType.VARIABLE) {
					VarScoper.declare(__currToken.getValue());
					//rsl.setFirstPart(rsl.getFirstPart() + __IR.getANewVarAddress() + ", "); //New var address in IR!
					//fixed = rsl.fix(__IR.getScopeName()+__currToken.getValue(), __IR.getANewVarAddress());
					fixed = rsl.fix(VarScoper.genVarName(__currToken.getValue()), __IR.getANewVarAddress());
//					__IR.putCode(fixed); //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
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
				VarScoper.enter(functionName);
				__IR.putCode(functionName + ":");
				__funUtil.setFunName(functionName);
				formalParam();
				if (__currToken.getType() == Token.TokenType.SEMICOLON) {
					__currToken = __lx.nextToken();
					funcBody();
					if (__currToken.getType() == Token.TokenType.SEMICOLON) {
						__currToken = __lx.nextToken();
						VarScoper.exit();
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
				VarScoper.declare(tokenValue);
				__funUtil.newVarName(tokenValue);

		          long basePtr = __IR.getCurrPc()+1; // we need to pop params backwards
			      __IR.putCode("POP "+__funUtil.getFunName() + __SEP + tokenValue);
				//__IR.putCode("LOAD " + __funUtil.findVarRealName(tokenValue) + " " + __funUtil.getFunName() + __SEP + tokenValue); //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
				__currToken = __lx.nextToken();

				while (__currToken.getType() == Token.TokenType.COMMA) {
					__currToken = __lx.nextToken();
					if (__currToken.getType() == Token.TokenType.VARIABLE) {
						// There is a parameter, then go get it
						tokenValue = __currToken.getValue();
						VarScoper.declare(tokenValue);
						__funUtil.newVarName(tokenValue);
						  // popping backwards
					      __IR.putCode("POP "+__funUtil.getFunName() + __SEP + tokenValue, basePtr);
						//__IR.putCode("LOAD " + __funUtil.findVarRealName(tokenValue) + " " + __funUtil.getFunName() + __SEP + tokenValue); //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
						__currToken = __lx.nextToken();
					} else {
						reportError("Missing a viarable! In function formal parameter part.");
					}
				}
			}
			if (__currToken.getType() == Token.TokenType.R_PARENTHESIS) {
				__currToken = __lx.nextToken(); // Successfully finished
			} else {
				reportError("Missing a \')\'! In function formal parameter part.");
			}
		} else {
			// Then there is no formalParam
		}
	}
	protected void funcBody() {
		while (__currToken.getValue().equals("var") || __currToken.getValue().equals("array")) {
			// There are some varDecl
			varDecl();
		} 
		if (__currToken.getType() == Token.TokenType.L_BRACE) {
			// To be continued...
			__currToken = __lx.nextToken();
			statSequence();
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
	protected AssignDestination statSequence() {
		AssignDestination left = statement();
		AssignDestination right = null;
		while (__currToken.getType() == Token.TokenType.SEMICOLON) {
			__currToken = __lx.nextToken();
			right = statement();
			if(left != null) left = left.join(right);
			else left = right;
		}
		return left;
	}
	protected AssignDestination statement() {
		switch(__currToken.getValue()) {
		case "let":
			return assignment();
		case "call":
			funcCall();
			break;
		case "if":
			return ifStatement();
		case "while":
			return whileStatement();
		case "return":
			returnStatement();
			break;
		default:
			//reportError("Wrong statement!");
			return null;
		}
		return null;
	}
	protected AssignDestination assignment() {
		if (__currToken.getValue().equals("let")) {
			String code = "";
			__currToken = __lx.nextToken();
			AssignDestination dst = designator(true);
			if (dst.isConstant()) {
				reportError("In assignment, you cannot assign something to a constant!");
			}
			//__currToken = __lx.nextToken();
			if (__currToken.getType() == Token.TokenType.ASSIGN) {
				__currToken = __lx.nextToken();
				AssignDestination source = expression();
				// Successfully finished
				//String to = funName + __SEP + dst.getDestination(); ////jiajiajiajiajiajiajijia
				String to = dst.getDestination(); ////jiajiajiajiajiajiajijia
				String from = source.getDestination();
//				if (source.isConstant()) {
//					from = source.getDestination();
//				} else {
//					from = funName + __SEP + source.getDestination();
//				}
				int offset;
				if (dst.isArray() || dst.isPointer()) {
					// if the destination is array, then we need to store instead of move
					code = "STORE " + from + " " + to;
					__IR.putCode(code); //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
				} else {
					// the destination is a variable, then we can move
					code = "MOVE " + from + " " + to;
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

	protected AssignDestination expression() {
		AssignDestination left = term();
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
			right = term();
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
					code = op + "i " + left.getDestination() + " " + tmpR;
				}
			} else {
				allConstant = false;
				if (left.isConstant()) {
					tmpL = Integer.parseInt(left.getDestination());
					code = op + "i " + tmpL + " " + right.getDestination();
					left = right; // Important!
				} else {
					code = op + " " + left.getDestination() + " " + right.getDestination();
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
	protected AssignDestination term() {
		AssignDestination left = factor();
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
			right = factor();
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
					code = op + "i " + left.getDestination() + " " + tmpR;
				}
			} else {
				allConstant = false;
				if (left.isConstant()) {
					tmpL = Integer.parseInt(left.getDestination()); 
					// 3 * a
					code = op + "i " + tmpL + " " + right.getDestination();
					left = right; // Important!
				} else {
					// a * b
					code = op + " " + left.getDestination() + " " + right.getDestination();
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
	protected AssignDestination factor() {
		AssignDestination dst;
		switch(__currToken.getType()) {
		case VARIABLE:
			dst = designator();
			return dst;
		case INSTANT:
			dst = new AssignDestination(__currToken.getValue());
			dst.setIsConstant(true);
			__currToken = __lx.nextToken();
			return dst;
		case L_PARENTHESIS:
			__currToken = __lx.nextToken();
			dst = expression();
			if (__currToken.getType() == Token.TokenType.R_PARENTHESIS) {
				__currToken = __lx.nextToken();
				return dst;
			} else {
				reportError("In factor, missing a \')\'!");
			}
		case KEYWORD:
			if (__currToken.getValue().equals("call")) {
				dst = funcCall();
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
	protected AssignDestination funcCall() {  
		if (__currToken.getValue().equals("call")) {
		    next();
		    String funcName = __currToken.getValue();
		    next();
		    
		    // TODO: push base ptr here? and move stack ptr to base ptr here too?
		    if(__currToken.getType() != TokenType.L_PARENTHESIS) return __IR.putCode("CALL "+funcName);
		    next(); // (
		    while(__currToken.getType() != TokenType.R_PARENTHESIS) {
		      AssignDestination param_in = expression();
		      __IR.putCode("PUSH "+param_in);
		      if(__currToken.getType() == TokenType.COMMA) next();
		      // this is correct but bad for error detection
		    }
		    next(); // )
		 
			return __IR.putCode("CALL "+funcName);
		} else {
			reportError("In funcCall, missing the \'call\' keyword!");
			return null;
		}
	}
	protected AssignDestination designator() {
	  return designator(false);
	}
	protected AssignDestination designator(boolean isDest) {
		if (__currToken.getType() == Token.TokenType.VARIABLE) {
			String originalIdName = __currToken.getValue();
			String idName = VarScoper.genVarName(originalIdName);

            __currToken = __lx.nextToken();
            //String nestNo = funName+".";
            AssignDestination num = new AssignDestination(idName);
            
            
			int offset;
            if( (offset = VarScoper.getGlobalVarOffset(idName)) != -1) {
              // get offset of global variable
              num = __IR.putCode("SUBi "+GLOBAL_VAR_REG+" "+offset);
            }

            // TODO: global array is not working atm
            String code = "";
			boolean isArray = false;
			while (__currToken.getType() == Token.TokenType.L_BRACKET) {
			    isArray = true;
				__currToken = __lx.nextToken();
				num = expression();
				num.setIsArray(true); // Set is the array
				code = "MULi " + num.getDestination() + " " + IRGenerator.__DW; 		
				num = __IR.putCode(code); //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
				code = "ADDA " + idName + " " + num.getDestination(); //a[i]= a + i * 4;
				num = __IR.putCode(code); //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
				//__currToken = __lx.nextToken();
				if (__currToken.getType() == Token.TokenType.R_BRACKET) {
					// successfully finished this round
					__currToken = __lx.nextToken();
				} else {
					reportError("In designator, missing a \']'!");
				}
			}
			
			// we LOAD if
			// 1) is a right side in assignment AND
			// 2) is in memory => either array or global variable
            if(!isDest && (isArray || (offset != -1))) num = __IR.putCode("LOAD "+num.getDestination());
			//__IR.print();
			// return the idName who has the address of the array item
//			num.setDestination(idName);
			return num;
		} else {
			reportError("In designator, missing a variable!");
		}
		return null;
		
	}
	
	void next() {
		__currToken = __lx.nextToken();
	}
	
	AssignDestination relation() {
		AssignDestination left = expression();
		if(!Arrays.asList(new String[] {"<",">","<=",">=","==","!="}).contains(__currToken.getValue())) {
			reportError("where's relop??");
		}
		Token relOp = __currToken;
		next();
		AssignDestination right = expression();
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
	protected AssignDestination ifStatement() {
		if(!__currToken.getValue().equals("if")) {
			reportError("where's if keyword??");
		}
		next();
		AssignDestination resRel = relation();
		
		// check relOp and convert/invert it to corresponding branch
		String relOp = getBranchOp(resRel);
		long cmpPtr = __IR.getCurrPc()+1; // one instruction below CMP
		if(!__currToken.getValue().equals("then")) {
			reportError("where's then keyword??");
		}
		next();
		AssignDestination ifSS = statSequence();
		
		// now we are done with if(rel) then statSequence(..)
		// so if there's no else, instruction number of follow is currPc+2
		//
		// again this advanced ptr is dangerous 
		// so call putCode(Code) before calling putCode(Code,Index) after calling IfStatement

		if(__currToken.getValue().equals("else")) {
			long elsePtr = __IR.getCurrPc()+1;
			next();
			AssignDestination elseSS = statSequence();
			
			// now followPtr should point to either first Phi if existed or first follow inst
			long followPtr = __IR.getCurrPc()+3; 
			__IR.putCode("BRA"+" "+new AssignDestination(followPtr).getDestination(), elsePtr);

			// put relOp at CMP+1
			// 1st arg is result of relation
			// 2nd is first instruction of merged block := currPc-sizeof(PHI)+1
			__IR.putCode(relOp+" "+CMP_VAR_REG+" "+new AssignDestination(elsePtr).getDestination(), cmpPtr);
			if(ifSS != null) {
				ifSS = ifSS.join(elseSS);
				for(String pv : ifSS.getAssignedVars()) {
		          __IR.putCode("PHI "+pv+" "+pv+" "+pv);
				}
			}
		} else {
			long followPtr = __IR.getCurrPc()+2;
			// put relOp at CMP+1
			// 1st arg is result of relation
			// 2nd is first instruction of merged block := currPc-sizeof(PHI)+1
			__IR.putCode(relOp+" "+CMP_VAR_REG+" "+new AssignDestination(followPtr).getDestination(), cmpPtr);
			
			for(String pv : ifSS.getAssignedVars()) {
	          __IR.putCode("PHI "+pv+" "+pv+" "+pv);
			}
		}
		
		if(!__currToken.getValue().equals("fi")) {
			reportError("where's fi keyword??");
		}
		next();
		return ifSS;
	}
	protected AssignDestination whileStatement() {
		if(!__currToken.getValue().equals("while")) reportError("where's while?");
		next();
		// location of the beginning of the block - PHI should be here, if exist
		long startBlockPtr = __IR.getCurrPc()+1;
		AssignDestination whileRel = relation();
		long cmpPtr = __IR.getCurrPc(); // location of CMP op, which will be loc of PHI's later
		String branchOp = getBranchOp(whileRel);
		
		if(!__currToken.getValue().equals("do")) reportError("where's do?");
		next();
		AssignDestination whileSS = statSequence();
		if(!__currToken.getValue().equals("od")) reportError("where's od?");
		next();
		
		// compute intersection of assigned vars in both blocks
		Set<String> whileVars = whileSS.getAssignedVars();
		// point to CMP if no PHI's exist or first PHI pointer otherwise
		// location of next PHI is curr+1+sizeof(PHIS)
		__IR.putCode("BRA "+new AssignDestination(whileVars.size() == 0 ? startBlockPtr : __IR.getCurrPc()+1+whileVars.size()).getDestination());

		// add all phis
		for(String pv : whileVars) {
          __IR.putCode("PHI "+pv+" "+pv+" "+pv, startBlockPtr);
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
		cmpPtr = cmpPtr+1+whileVars.size(); // update ptr of CMP since it might be pushed by PHI
		__IR.putCode(branchOp+" "+CMP_VAR_REG+" "+new AssignDestination(__IR.getCurrPc()+2).getDestination(), cmpPtr);

		
		return whileSS;
	}
	protected String getCurrTokenVal() {
		return __currToken.getValue();
	}
	protected void returnStatement() {
		// Suppose we use register R28 to store the return value
		if (getCurrTokenVal().equals("return")) {
			next();
			String code = "";
			AssignDestination returnValue = expression();
			code += "MOV " + returnValue.getDestination() + " " + RTR_VAR_REG;
			__IR.putCode(code);
//			if (returnValue.isConstant()) {
//				code += "MOV " + returnValue.getDestination() + register;
//			} else if (returnValue.isArray()) {
//				returnValue.
//			} else if (returnValue.isPointer()) {
//				
//			} else {
//				
//			}
		} else {
			new Reporter(Reporter.ReportType.ERROR, "No return keyword!");
		}
	}
	
}
