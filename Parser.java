package Scanner; 
import java.io.IOException;
import java.util.Scanner;

public class Parser{
    CompilerScanner s;
    Token symbol;
    ErrorLogger logger;
    Quads quads;
    int[] locals = new int[20]; //index by scope number
    int callCounter = 0;
 
    public Parser() throws Exception{
    	quads = new Quads();
        logger = new ErrorLogger("ParserErrorLog.txt");
        Scanner input = new Scanner(System.in);
        System.out.print("Enter program file name: ");
        String fileName = input.nextLine();
        //init scanner
        try {
            s = new CompilerScanner(fileName);
        } catch (IOException e) {
            System.out.println("Error: Parser Constructor::Scanner Init::problem with filename passed.");
            logger.customError("ERROR -- Parser Constructor::Scanner Init::problem with filename passed.", s);
            e.printStackTrace();
        }
        input.close();
        //read first token
        symbol = new Token();
        symbol = s.newNextToken();
        //move to start method
        program();
        
    }
    
    private void error(String string) {
        System.out.println(string);
    }
    
    // start grammar -----------------------------------------------------------------------------------------------------------------------------------------
    
    private void program() throws Exception {
        Token firstToken = symbol;                                              //Program
        Token secondToken = s.nextToken();                                      //Add
        Token thirdToken = s.nextToken();                                       //;
        
        if (firstToken.tokenType == T.PROGRAM && secondToken.tokenType == T.IDENTIFIER && thirdToken.tokenType == T.SEMI) {
            try{
            	s.symbolTable.table[s.symbolTable.currentAdd].name = secondToken.name;
            	s.symbolTable.table[s.symbolTable.currentAdd].kind = "program_name";
            	s.symbolTable.table[s.symbolTable.currentAdd].type = T.PROGRAM;
            	s.symbolTable.table[s.symbolTable.currentAdd].scope = 0;
            	s.symbolTable.table[s.symbolTable.currentAdd].declared = "true";
                //progresses to next token
                symbol = s.newNextToken();
                variable_declarations();

                int loc1 = quads.size(); //index of the next quad
                quads.insertQuad("BR", "-", "-", ""+(loc1+1)); //the 0 is a place holder
            
                subprogram_declarations();

                s.scope = 0; //set scope back to 0
                int loc2 = quads.size(); //location of the next quad

                
                //fill in quad at loc1 with location counter, same as while statement?
                Quad temp = quads.quads.get(loc1);
                temp.result = ""+ (quads.size());//maybe -1?
                quads.quads.set(loc1, temp);

                compound_statement();
                if (symbol.tokenType == T.PERIOD){
                    quads.insertQuad("END", "-", "-", "-");
                    quads.logQuads();//LOGS ALL QUADS TO THE TEXT FILE
                } else {
                	logger.customError("ERROR -- No period to end program", s);
                }
                
                //Prints results of symbol table and quads
                s.symbolTable.print();
                quads.printQuads();
                
            }catch(Exception e){
                e.printStackTrace();
            } 
        } else {
        	logger.customError("ERROR -- MISSING PROGRAM DECLARATION LINE", 0);
        }
    }
    
    private void identifier_list(Semantics sem) throws Exception {
    	sem.count = 0;
        if (symbol.tokenType == T.IDENTIFIER) {
        	sem.start = s.symbolTable.currentAdd;
        	sem.count++;
            symbol = s.newNextToken();
            s.symbolTable.table[s.symbolTable.currentAdd].declared = "true";
            locals[s.scope]++; //for locals
        	s.symbolTable.table[s.symbolTable.currentAdd].offset = locals[s.scope];//sets the offset
            while (symbol.tokenType == T.COMMA) {
                symbol = s.newNextToken();
                if (symbol.tokenType == T.IDENTIFIER) {
                	sem.count++;
                	symbol = s.newNextToken();
                	s.symbolTable.table[s.symbolTable.currentAdd].declared = "true";
                	locals[s.scope]++; //for locals
                	s.symbolTable.table[s.symbolTable.currentAdd].offset = locals[s.scope];//sets the offset
                } else {
                	logger.customError("ERROR -- EXPECTING ID", s);
                }
            }
        }else{
            logger.customError("ERROR -- identifier_list:: no identifier in list", s);
        }
    }
    
    private void variable_declarations() throws Exception {
    	if (symbol.tokenType == T.VAR) {
            symbol = s.newNextToken();
            Semantics sem = new Semantics();
            variable_declaration(sem);
            for (int j = sem.start; j < (sem.start + sem.count); j++) {
            	quads.insertQuad("DCL", null, null, ""+j);
            	s.symbolTable.table[j].type = sem.type;
            }
            if (symbol.tokenType == T.SEMI) {
                symbol = s.newNextToken();
                while (symbol.tokenType == T.IDENTIFIER) {
                	variable_declaration(sem);
                	for (int j = sem.start; j < sem.count; j++) {
                		quads.insertQuad("DCL", null, null, ""+j);
                    	s.symbolTable.table[j].type = sem.type;
                    }
                	if (symbol.tokenType == T.SEMI) {
                		symbol = s.newNextToken();
                	} else {
                		logger.customError("ERROR -- Expecting ';'", s);
                	}
                }

            }else {
                logger.customError("ERROR -- variable_declarations::Missing ';' to end variable declaration", s);
            }
        }
    }
    
    private void variable_declaration(Semantics sem) throws Exception {
        identifier_list(sem);
        if (symbol.tokenType == T.COLON){
            symbol = s.newNextToken();
            type(sem);
        }else{
            logger.customError("ERROR -- variable_declaration::Missing ':' between identifier_list and type", s);
        }
    }
    
    private void type(Semantics sem) throws Exception {
        if (symbol.tokenType == T.INTEGER){
            symbol = s.newNextToken();
            sem.type = T.INTEGER;
        } else if (symbol.tokenType == T.BOOL) {
        	symbol = s.newNextToken();
        	sem.type = T.BOOL;
        } else{
            logger.customError("ERROR -- type::Missing type", s);
        }
    }
    
    private void subprogram_declarations() throws Exception {
        if (symbol.tokenType == T.PROCEDURE) {
        	subprogram_declaration();
            if (symbol.tokenType == T.SEMI){
                symbol = s.newNextToken();
                subprogram_declarations();
            } else {
            	logger.customError("ERROR -- Expecting ';'", s);
            }
        }
    }
    private void subprogram_declaration() throws Exception {
    	s.incScope();
        subprogram_head();
        variable_declarations();
        compound_statement();
        s.decScope();
        quads.insertQuad("EXIT", "-", "-", "-");
    }
    private void subprogram_head() throws Exception {
    	int place = 0; //address for the name in the symbol table
    	int loc1 = 0; //holds tge address of the next quads
        if (symbol.tokenType == T.PROCEDURE) {
            symbol = s.newNextToken();
            s.symbolTable.table[s.symbolTable.currentAdd].kind = "procedure_name";
            s.symbolTable.table[s.symbolTable.currentAdd].type = T.PROCEDURE;
            s.symbolTable.table[s.symbolTable.currentAdd].scope = 0;
            if (symbol.tokenType == T.IDENTIFIER) {
            	place = s.symbolTable.currentAdd;
            	loc1 = quads.size();//suppose to be the index of the next quad
            	s.symbolTable.table[place].start = loc1;
            	quads.insertQuad("PROCDEC", "-", "-", place+"");
            	s.symbolTable.table[place].type = T.PROCEDURE;
            	if (!s.symbolTable.table[place].declared.equals("false"))     // double check lol     		
            		logger.customError("Error -- variable already declared in subhead", s);
            	
                symbol = s.newNextToken();
                s.symbolTable.table[s.symbolTable.currentAdd].declared = arguments(place) + "";
                if (symbol.tokenType == T.SEMI) {
                    symbol = s.newNextToken();
                }else{
                    logger.customError("ERROR -- subprogram_head::Missing semicolon ';' after arguments", s);
                }
            }else{
                logger.customError("ERROR -- subprogram_head::Missing Identifier after procedure", s);
            }
            
        }else{
            logger.customError("ERROR -- subprogram_head::Missing 'procedure' declaration", s);
        }
    }
    
    private int arguments(int place) throws Exception {//place is the address of the procedure
    	Semantics sem = new Semantics();
        if (symbol.tokenType == T.LPAREN) {
            symbol = s.newNextToken();
            sem = parameter_list(place);
            if (symbol.tokenType == T.RPAREN) {
                symbol = s.newNextToken();  
            }else{
                logger.customError("ERROR -- arguments::Missing right parenthesis after parameter list", s);
            }
        }else{
            logger.customError("ERROR -- arguments::Missing left parenthesis before parameter list", s);
        }
        return sem.count;
    }
    
    private Semantics parameter_list(int place) throws Exception{
    	Semantics sem = new Semantics();
    	
        identifier_list(sem);
        if(symbol.tokenType == T.COLON) {
        	symbol = s.newNextToken();
            type(sem);
            for (int j = sem.start; j < (sem.start + sem.count); j++) {
            	s.symbolTable.table[j].type = sem.type;
            	s.symbolTable.table[place].numArgs++; //incraments arguments for param
            	quads.insertQuad("PARAM","-","-", j+"");
            	s.symbolTable.table[j].offset = s.symbolTable.table[place].numArgs;
            	s.symbolTable.table[j].kind = "PARAM";

            }
            while (symbol.tokenType == T.SEMI) {
            	symbol = s.newNextToken();
            	identifier_list(sem);
            	if (symbol.tokenType == T.COLON) {
            		symbol = s.newNextToken();
            		type(sem);
            		for (int j = sem.start; j < (sem.start + sem.count); j++) {
            			s.symbolTable.table[j].type = sem.type;
                    	s.symbolTable.table[place].numArgs++; //incraments arguments for param
                    	quads.insertQuad("PARAM","-","-", j+"");
                    	s.symbolTable.table[j].offset = s.symbolTable.table[place].numArgs;
                    	s.symbolTable.table[j].kind = "PARAM";
                    	if (!s.symbolTable.table[place].declared.equals("false")) {
                    		logger.customError("ERROR -- already decalred", s);
                    	} else {
                    		s.symbolTable.table[j].declared = "true";
                    	}
                    }
            	} else {
            		logger.customError("ERROR -- Expecting ':'", s);
            	}
            }
            
        } else {
        	logger.customError("ERROR -- EXPECTING COLON", s);
        }
        return sem;
    }
    
    private void compound_statement() throws Exception {
        if (symbol.tokenType == T.BEGIN) {
            symbol = s.newNextToken();
            statement_list();
            if (symbol.tokenType == T.END) {
                symbol = s.newNextToken();
            }else{
                logger.customError("ERROR -- compound_statement::EXPECTING END  after statement_list in compound statement", s);
            }

        }else{
            logger.customError("ERROR -- compound_statement::EXPECTING BEGIN before statement_list in compound statement", s);
        }
    }
    
    private void statement_list() throws Exception{
        statement();
        while(symbol.tokenType == T.SEMI) {
            symbol = s.newNextToken();
            statement();
        }
    }
    
    private void statement() throws Exception {
        if (symbol.tokenType == T.IDENTIFIER) {
        	if (!s.symbolTable.table[s.symbolTable.currentAdd].declared.equals("false")) { //maybe change back to !=? ------
        		assignment_statement();
        	} else {
        		logger.customError("ERROR -- var not declared", s);
        	}
        }
        else if (symbol.tokenType == T.CALL) {
        	procedure_statement();
        }
        else if (symbol.tokenType == T.BEGIN) {
        	compound_statement();
        }
        else if (symbol.tokenType == T.IF) {
        	if_statement();
        }
        else if (symbol.tokenType == T.WHILE) {
        	while_statement();
        }
        else if (symbol.tokenType == T.READ) {
        	read_statement();
        }
        else if (symbol.tokenType == T.WRITE) {
        	write_statement();
        } else if (symbol.tokenType == T.WRITELN) {
        	writeln_statement();
        }

    }
    
    private void assignment_statement() throws Exception {
    	int place = symbol.value;//location of symbol in table
    	if (s.symbolTable.table[place].declared.equals("false")) {
    		logger.customError("ERROR -- indentifier is not declared in assignment_statement call", s);
    	}
    	if (symbol.tokenType == T.IDENTIFIER) {
    		String currentName = symbol.name;
    		symbol = s.newNextToken();
    		if (symbol.tokenType == T.ASSIGN) {
    			symbol = s.newNextToken();
    			Exp x = new Exp();
            	expression(x);
            	
            	if (s.symbolTable.table[place].type != x.type) { // if theyre not the same then error. added extra info for debugging the help
            		logger.customError("ERROR -- table.type differs from x.type \n"
            				+ "Table.type = " + s.symbolTable.table[place].type
            				+ "X.type = " + x.type, s);
            	} else { //not an error
            		if (x.number)
            			quads.insertQuad("ASSIGN", "*"+x.value+"", "-", s.symbolTable.search(currentName, s.scope)+"");
            		else
            			quads.insertQuad("ASSIGN", x.value+"", "-", s.symbolTable.search(currentName, s.scope)+"");
            	}
            	
            } else {
            	logger.customError("ERROR -- EXPECTING ASSIGNMENT OP", s);
            }
    	} else {
    		logger.customError("ERROR -- EXPECTING IDENTIFIER", s);
    	}
    }
    
    private void if_statement() throws Exception {
        int loc1, loc2;//NOTE: We might not need the locations because we are using an ArrayList for our quads

        if (symbol.tokenType == T.IF) {
        	symbol = s.newNextToken();

            Exp exp = new Exp();
        	expression(exp);

            if (exp.type != T.BOOL)
                logger.customError("ERROR -- EXPECTING BOOL", s);

            loc1 = quads.size();
            quads.insertQuad("BR0", exp.value+"", "-", "0");

        	if (symbol.tokenType == T.THEN) {
        		symbol = s.newNextToken();
        		statement();
        		symbol = s.newNextToken();
        		if (symbol.tokenType == T.ELSE) {
        			symbol = s.newNextToken();

                    loc2 = quads.size();//-1?
                    quads.insertQuad("BR", "-", "-", "-");
                    
                    //fill in quad at loc1 with location counter, same as while statement?
                    Quad temp = quads.quads.get(loc1);
                    temp.result = ""+ (quads.size());//maybe -1?
                    quads.quads.set(loc1, temp);
                    
                    statement();
                    
                    //fill in quad at loc2 with location counter, same as while statemet?
                    Quad temp2 = quads.quads.get(loc2);
                    temp2.result = ""+ (quads.size());//maybe -1?
                    quads.quads.set(loc2, temp2);
                    
        		} else {
        			//fill in quad at loc1 with location counter, same as while statement?
                    Quad temp = quads.quads.get(loc1);
                    temp.result = ""+ (quads.size());//maybe -1?
                    quads.quads.set(loc1, temp);
        		}
        	} else {
        		logger.customError("ERROR -- EXPECTING THEN", s);
        	}
        } else {
        	logger.customError("ERROR -- EXPECTING IF STATEMENT", s);
        }
    }
    
    private void while_statement() throws Exception{
        int loc1, loc2; //NOTE: We might not need the locations because we are using an ArrayList for our quads

        if(symbol.tokenType == T.WHILE) {
        	symbol = s.newNextToken();

            loc1 = quads.size(); //location of the next quad
            Exp exp = new Exp();
            expression(exp);

            if (exp.type != T.BOOL)
                logger.customError("ERROR -- EXPECTING BOOL", s);

            loc2 = quads.size();//location of the next quad
            quads.insertQuad("BR0", exp.value+"", "-", "0");

            if(symbol.tokenType == T.DO) {
            	symbol = s.newNextToken();

            	statement();
                quads.insertQuad("BR", "-", "-", loc1+"");
               
                Quad temp = quads.quads.get(loc2);
                temp.result = ""+ (quads.size());//maybe -1
                quads.quads.set(loc2, temp);
            } else {
            	logger.customError("ERROR -- EXPECTING DO", s);
            }
        } else {
        	logger.customError("ERROR -- EXPECTING WHILE", s);
        }
    }
    
    private void procedure_statement() throws Exception {
    	int paramCount = 0;
    	int start = 0;
    	String name;
        if (symbol.tokenType == T.CALL) {
        	callCounter++;
        	symbol = s.newNextToken();
        	if (symbol.tokenType == T.IDENTIFIER) {
        		s.symbolTable.table[symbol.value].declared = "true";//this fixes recursive programs but if it breaks it then ?
        		name = symbol.name;
        		start = s.symbolTable.search(name, 0);
        		
        		symbol = s.newNextToken();
        		if (symbol.tokenType == T.LPAREN) {
        			symbol = s.newNextToken();
        			paramCount = expression_list();
        			if (symbol.tokenType == T.RPAREN) {
        				symbol = s.newNextToken();
        				if (s.symbolTable.table[start].numArgs == paramCount) {
        					quads.insertQuad("CALL", start+"", paramCount+"","-");
        				} else {
        					logger.customError("ERROR -- ParamCount is not the same as argCount in procedure_statement() call", s);
        				}
        				
        			} else {
        				logger.customError("ERROR -- EXPECTING RIGHT PARAM", s);
        			}
        		} else {
        			logger.customError("ERROR -- EXPECTING LEFT PAREN", s);
        		}
        	} else {
        		logger.customError("ERROR -- EXPECING IDENTIFIER", s);
        	}
        } else {
        	logger.customError("ERROR -- EXPECTING CALL", s);
        }
    }
    
    private int expression_list() throws Exception{
    	int count = 0;
    	Exp x = new Exp();
        expression(x);
        quads.insertQuad("ARG", "-", "-", x.value+"");
        if (x.type != T.INTEGER)
        	logger.customError("ERROR -- x.type != T.INTEGER in expression_list() call", s);
        
        count++;
        while(symbol.tokenType == T.COMMA) {
            symbol = s.newNextToken();
            x = new Exp();
            expression(x);
            quads.insertQuad("ARG", "-", "-", x.value+"");
            if (x.type != T.INTEGER)
            	logger.customError("ERROR -- x.type != T.INTEGER in expression_list() call", s);
            
            count++;
        }
        return count;//number of parameters
    }
    
    private void expression(Exp x) throws Exception {
    	simple_expression(x); // the Exp object,x, gets passed to simpleExpression
        if ((symbol.tokenType == T.LT) || (symbol.tokenType == T.GT) || (symbol.tokenType == T.EQUAL) || (symbol.tokenType == T.GE) || (symbol.tokenType == T.LE) || (symbol.tokenType == T.NE)) {
        	//Converts the tokenType to a String
        	int opCode = symbol.tokenType;
        	symbol = s.newNextToken();
        	Exp w = new Exp();
        	simple_expression(w);
        	
        	if (w.type != T.INTEGER) {
        		logger.customError("ERROR -- Expression call expecting an INTEGER", s);
        	} else {
        		int tempAddress = s.symbolTable.getTemp();
        		s.symbolTable.table[tempAddress].type = T.INTEGER; //maybe needs to be bool? 
        		s.symbolTable.table[tempAddress].kind = "TEMP";
        		s.symbolTable.table[tempAddress].scope = s.scope;
        		locals[s.scope]++;
        		s.symbolTable.table[tempAddress].offset = locals[s.scope];
        		
        		String xVal = x.value+"";
        		String wVal = w.value+"";
        		if (x.number)
        			xVal = "*"+x.value;
        		if (w.number)
        			wVal = "*"+w.value;
        		quads.insertQuad(convertValue(opCode), xVal, wVal, tempAddress+"");
        		x.type = T.BOOL;
        		x.value = tempAddress;
        		x.number = false;
        	}
        }
    }
    
    private void simple_expression(Exp x) throws Exception {
    	if (symbol.tokenType == T.MINUS) {
    		symbol = s.newNextToken();
    	}
    	int type1; //type of first operand
    	int type2; //type of second operand
    	int tempAddress = 0; //holds a temp address
    	Exp y = new Exp();
    	Exp z = new Exp();
    	
        term(y);
        while ((symbol.tokenType == T.PLUS) || (symbol.tokenType == T.MINUS) || (symbol.tokenType == T.OR)) {
        	int opCode = symbol.tokenType;
        	symbol = s.newNextToken();
        	term(z);
        	type1 = y.type;
        	type2 = z.type;
        	
        	if (type1 != type2)
        		logger.customError("Error -- incompatable types in simple_expression", s);
        	
        	tempAddress = s.symbolTable.getTemp();
        	tempAddress = tempAddress - callCounter;;
        	s.symbolTable.table[tempAddress].type = type1;
        	
        	String yVal = y.value+"";
    		String zVal = z.value+"";
    		if (y.number)
    			yVal = "*"+y.value;
    		if (z.number)
    			zVal = "*"+z.value;
        	
        	if (opCode == T.PLUS) {
        		if (type1 != T.INTEGER || type2 != T.INTEGER) {
        			logger.customError("Error -- incompatable types in simple_exp plus. Type 1: " + type1 + ", Type 2: " + type2, s);
        		} else {
        			quads.insertQuad("ADD", yVal, zVal, tempAddress+"");
        		}
        	}
        	
        	if (opCode == T.MINUS) {
        		if (type1 != T.INTEGER || type2 != T.INTEGER) {
        			logger.customError("Error -- incompatable types in simp_exp minus", s);
        		} else {
        			quads.insertQuad("SUB", yVal, zVal, tempAddress+"");
        		}
        	}
        	
        	if (opCode == T.OR) {
        		if (type1 != T.BOOL || type2 != T.BOOL) {
        			logger.customError("Error -- incompatable types in simp_exp or", s);
        		} else {
        			quads.insertQuad("OR", yVal, zVal, tempAddress+"");
        		}
        	}
        	
        	y.type = type1;
        	y.value = tempAddress;
        	y.number = false;//packet doesn have a bool for this but it does for term? if broken maybe try a bool?
        }//end while loop
        x.type = y.type;//bool ditto as above ^^^
        x.value = y.value;
        x.number = y.number;
    }
    
    private void term(Exp x) throws Exception {
    	int type1;
    	int type2;
    	Exp y = new Exp();
    	Exp z = new Exp();
    	int tempAddress;
    	
        factor(y);
        while ((symbol.tokenType == T.TIMES) || (symbol.tokenType == T.DIV)|| (symbol.tokenType == T.MOD) || (symbol.tokenType == T.AND)) {
        	int opCode = symbol.tokenType;
        	symbol = s.newNextToken();
        	factor(z); //now we have info about two operands
        	
        	type1 = y.type;
        	type2 = z.type;
        	
        	if (type1 != type2)
        		logger.customError("Error -- incompatable types", s);
        	
        	tempAddress = s.symbolTable.getTemp();
        	tempAddress = tempAddress - callCounter;
        	s.symbolTable.table[tempAddress].type = type1;
        	
        	String yVal = y.value+"";
    		String zVal = z.value+"";
    		if (y.number)
    			yVal = "*"+y.value;
    		if (z.number)
    			zVal = "*"+z.value;
        	
        	if (opCode == T.DIV) {
        		if (type1 != T.INTEGER || type2 != T.INTEGER) {
        			logger.customError("Error -- incompatable types in term div", s);
        		} else {
        			quads.insertQuad("DIV", yVal, zVal, tempAddress+"");
        		}
        	} else if (opCode == T.TIMES) {
        		if (type1 != T.INTEGER || type2 != T.INTEGER) {
        			logger.customError("Error -- incompatable types in term times", s);
        		} else {
        			quads.insertQuad("TIMES", yVal, zVal, tempAddress+"");
        		}
        	} else if (opCode == T.MOD) {
        		if (type1 != T.INTEGER || type2 != T.INTEGER) {
        			logger.customError("Error -- incompatable types in term mod", s);
        		} else {
        			quads.insertQuad("MOD", yVal, zVal, tempAddress+"");
        		}
        	} else if (opCode == T.AND) {
        		if (type1 != T.BOOL || type2 != T.BOOL) {
        			logger.customError("Error -- incompatable types in term and", s);
        		} else {
        			quads.insertQuad("AND", yVal, zVal, tempAddress+"");
        		}
        	}//end of operator if statements
        	
        	y.type = type1;
        	y.value = tempAddress;
        	y.bool = false;
        	y.number = false;
        }//end of while loop
        x.type = y.type; //these values sent back to the caller
        x.value = y.value;
        x.bool = y.bool;
        x.number = y.number;
    }
    
    private void factor(Exp x) throws Exception{
        if(symbol.tokenType == T.IDENTIFIER) {
            if (!s.symbolTable.table[symbol.value].declared.equals("false")) { 
            	symbol.value = s.symbolTable.search(symbol.name, s.scope);
        		x.type = s.symbolTable.table[symbol.value].type; //symbol.value? correct or?
        		x.value = symbol.value;
        		x.number = false;
        	} else { //id is not declared
        		s.symbolTable.table[symbol.value].printAsString();
        		logger.customError("ERROR -- var not declared in factor", s);
        	}
        	symbol = s.newNextToken();
        } else if(symbol.tokenType == T.NUMBER) {
        	x.type = T.INTEGER;
        	x.value = symbol.value;
        	x.number = true;
        	symbol = s.newNextToken();
        } else if (symbol.tokenType == T.BOOL) {
        	int tempAddress = s.symbolTable.getTemp();
        	s.symbolTable.table[tempAddress].kind = "TEMP";
        	s.symbolTable.table[tempAddress].scope = s.scope;
        	s.symbolTable.table[tempAddress].type = T.BOOL;
        	locals[s.scope]++;
    		s.symbolTable.table[tempAddress].offset = locals[s.scope];
        	quads.insertQuad("NEG", x.value+"", "-", tempAddress+""); //is NEG suppose to be BOOL?
        	x.type = T.BOOL;
        	x.value = tempAddress;
        	x.number = false;
        	symbol = s.newNextToken();
            s.symbolTable.table[s.symbolTable.currentAdd].type = T.BOOL; // this was a part of grammar not expression, does it belong?
        }	
        else if(symbol.tokenType == T.LPAREN) {
        	symbol = s.newNextToken();
            expression(x);
           if (symbol.tokenType == T.RPAREN) {
        	   symbol = s.newNextToken();
           } else {
        	   logger.customError("ERROR -- EXPECTING RIGHT PAREN", s);
           }
        }
        else if(symbol.tokenType == T.NOT) {
        	symbol = s.newNextToken();
            factor(x);
            if (x.type == T.INTEGER) {
            	logger.customError("Error -- Grammar expecting bool", s);
            }
        }
        else {
        	logger.customError("ERROR -- EXPECING SOME KIND OF FACTOR", s);
        }
    }
    
    private void read_statement() throws Exception{
        if(symbol.tokenType == T.READ) {
            symbol = s.newNextToken();
            if(symbol.tokenType == T.LPAREN) {
            	symbol = s.newNextToken();
                input_list();
        		if (symbol.tokenType == T.RPAREN) {
        			symbol = s.newNextToken();
        		} else {
        			logger.customError("ERROR -- EXPECTING RIGHT PAREN", s);
        		}
            } else {
            	logger.customError("ERROR -- EXPECTING LEFT PAREN", s);
            }
        } else {
        	logger.customError("ERROR -- EXPECTING READ", s);
        }
    }
    
    private void write_statement() throws Exception{
    	if(symbol.tokenType == T.WRITE) {
            symbol = s.newNextToken();
            if(symbol.tokenType == T.LPAREN) {
            	symbol = s.newNextToken();
                output_item();
        		if (symbol.tokenType == T.RPAREN) {
        			symbol = s.newNextToken();
        		} else {
        			logger.customError("ERROR -- EXPECTING RIGHT PAREN", s);
        		}
            } else {
            	logger.customError("ERROR -- EXPECTING LEFT PAREN", s);
            }
        } else {
        	logger.customError("ERROR -- EXPECTING WRITE", s);
        }
    }
    
    private void writeln_statement() throws Exception{
    	if(symbol.tokenType == T.WRITELN) {
            symbol = s.newNextToken();
            if(symbol.tokenType == T.LPAREN) {
            	symbol = s.newNextToken();
                output_item();
        		if (symbol.tokenType == T.RPAREN) {
        			symbol = s.newNextToken();
        		} else {
        			logger.customError("ERROR -- EXPECTING RIGHT PAREN", s);
        		}
            } else {
            	logger.customError("ERROR -- EXPECTING LEFT PAREN", s);
            }
        } else {
        	logger.customError("ERROR -- EXPECTING WRITELN", s);
        }
    }
    
    private void output_item() throws Exception{
        if(symbol.tokenType == T.STRING) {
        	symbol = s.newNextToken();
        	
        	String stringLoc = s.stringTable.grabRecent();
        	if (stringLoc.equals("error"))
        		logger.customError("ERROR -- StringTable is empty", s);
        	
        	quads.insertQuad("WRITE", "-", "-", stringLoc);
        } else if ((symbol.tokenType == T.MINUS) || (symbol.tokenType == T.IDENTIFIER) || (symbol.tokenType == T.NUMBER) || (symbol.tokenType == T.BOOL) || (symbol.tokenType == T.LPAREN) || (symbol.tokenType == T.NOT)) {
        	Exp x = new Exp(); //ralph didnt put this on the sheet but expression needs an x so idk wtf he wants
        	expression(x);
        	quads.insertQuad("WRITE", "-", "-", x.value+"");//we had to make this ralph didnt give to us so may be wrong
        } else {
        	logger.customError("ERROR -- Missing expression", s);
        }
    }
    
    private void input_list() throws Exception{
        if(symbol.tokenType == T.IDENTIFIER) {
        	String currentName = symbol.name;
            symbol = s.newNextToken();
            quads.insertQuad("INPUT", "-", "-", s.symbolTable.search(currentName, s.scope)+"");
            while(symbol.tokenType == T.COMMA) {
                symbol = s.newNextToken();
                if (symbol.tokenType == T.IDENTIFIER) {
                	currentName = symbol.name;
                	symbol = s.newNextToken();
                	quads.insertQuad("INPUT", "-", "-", s.symbolTable.search(currentName, s.scope)+"");
                } else {
                	logger.customError("ERROR -- EXPECTING IDENTIFIER", s);
                }
            }
        } else {
        	logger.customError("ERROR -- EXPECTING IDENTIFIER", s);
        }
    }
    // end grammar -------------------------------------------------------------------------------------------------------------------------------------
    
    //Converts comparator to a string
    public String convertValue(int value) {
    	if (value == T.LT) {
    		return "LT";
    	} else if (value == T.GT) {
    		return "GT";
    	} else if (value == T.EQUAL) {
    		return "EQUAL";
    	} else if (value == T.GE) {
    		return "GE";
    	} else if (value == T.LE) {
    		return "LE";
    	} else if (value == T.NE) {
    		return "NE";
    	} else if (value == T.PLUS) {
    		return "PLUS";
    	} else if (value == T.MINUS) {
    		return "MINUS";
    	} else if (value == T.OR) {
    		return "OR";
    	} else if (value == T.PLUS) {
    		return "ADD";
    	} else if (value == T.MINUS) {
    		return "Sub";
    	} else if (value == T.DIV) {
    		return "Div";
    	} else if (value == T.MOD) {
    		return "Mod";
    	}
    	return "NOT IN CONVERT METHOD"; //isnt anything
    }

    public static void main(String[] args) throws Exception{
        Parser p = new Parser();
    }
}