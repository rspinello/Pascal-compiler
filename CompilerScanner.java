package Scanner; 

import java.io.*;
import java.util.Scanner;

public class CompilerScanner
{
    private int[][] fsm;//contains the state mappings of the FSM, read from file
    private String[] reserved ={"program","var","integer","bool","procedure","call","begin","end","if","then","else","while","do","and","or","not","read","write","writeln"};
    public StringTable stringTable = new StringTable();
    public SymbolTable symbolTable = new SymbolTable();
    public boolean isComment = false;
    
    File f;
    FileReader fileReader;
    BufferedReader br;
    ErrorLogger out;
    
    char ch = ' ';
    int line = 0;
    int state = 0;
    int scope = 0;
    

    public CompilerScanner(String fileName) throws IOException 
    {
        f = new File(fileName);
        fileReader = new FileReader(f);
        br = new BufferedReader(fileReader);
        //===================Error Log========================
        out = new ErrorLogger("ScannerErrorLog.txt");
        //===================Output to txt==========================
        FileReader fileReader2 = new FileReader(f);
        BufferedReader br3 = new BufferedReader(fileReader2);

        int increment = 1; 
        
        String output = br3.readLine();
        while(output != null) {
         if(output.isEmpty())
            break; 
         out.log(increment + " " + output + System.lineSeparator());
         output = br3.readLine();
         increment++;
        }
        //==========================================================

        fsm = new int[15][11];

        Scanner input = new Scanner(new File("FSM.txt"));
        for (int row = 0; row < 15; row++) {
            for (int col = 0; col < 11; col++){
                fsm[row][col] = input.nextInt();
            }
        }
        input.close();
    }
    
    public void incScope() {
    	scope++;
    }
    
    public void decScope() {
    	scope--;
    }

    public void error(int errorCode, int line) throws IOException{
        String errorString="";
        switch(errorCode){
            case 13:
                errorString="ERROR -- ILLEGAL CHARACTER";
                break;
            case 14:
                errorString="ERROR -- STRING NOT TERMINATED";
                break;
               //"Final state, scanner error, character.";
            case 15:
            	errorString="ERROR -- EXPECTING PROGRAM";
            	break;
            case 16:
            	errorString="ERROR -- EXPECTING PROGRAM NAME";
            case 17:
            	errorString="ERROR -- EXPECTING SEMI";
            default:
                errorString="ERROR";
                break;
        }
        out.log(errorString + " at line "+line);
        //accepts the error msg and line num
        //prints the msg and exits
        //msg is illegal character or string left unterminated by a '
        
    }

    public String removeSpacesAndComments(String stringPassed){
        String newString="";
        char currentChar;
        stringPassed = stringPassed.replaceAll(System.lineSeparator(), "");
        stringPassed = stringPassed.replaceAll(" ","");
        stringPassed = stringPassed.replaceAll("\n","");
        stringPassed = stringPassed.replaceAll("\r","");
        stringPassed = stringPassed.replaceAll("\f","");
        stringPassed = stringPassed.replaceAll("\t","");
        boolean isNotInString = true;
        for(int i =0; i<stringPassed.length(); i++){
            currentChar=stringPassed.charAt(i);
            if (currentChar == '\'') {
            	isNotInString = isNotInString ? false : true;//toggle every time we see a quote
            }
            	      
            if(currentChar =='!' && isNotInString){
            	return newString;//ignore rest of string as it is commented out
            }
                   newString=newString+currentChar;
        }
        return newString;
    }
    
    public int getCategory(char ch){
        if ((ch >='A' && ch <= 'Z') || (ch >='a' && ch <= 'z'))
        {
            return 0;//letter
        }
        if (ch >='0' && ch <= '9')
        {
            return 1;//Digit
        }
        switch(ch){
            case '\'':
                return 2;//Quote
            case ':':
                return 3;//colon
            case '\n':
                return 4;//New-line
            case '>':
                return 5;//GT
            case '<':
                return 6;//LT
            case '=':
                return 7;
            case '+':
            case '-':
            case '*':
            case '/':
            case '%':
            case '(':
            case ')':
            case ',':
            case '.':
            case ';':
                return 8;//punction 
            case ' '://whitespace
                return 9;

            default:
                return 10;//ERROR ILLEGAL CHARACTER
        }
    }

    public Token nextToken() throws Exception{
        // scanning algorithm
        int state = 0;

        int charClass = getCategory(ch);//column value for the FSM
        String buf = "";
        do{
            buf = buf + ch;
            ch = (char)br.read();//get the next character, ch
            if (ch=='!')
            	isComment = true;
            
            state = fsm[state][charClass];
            charClass = getCategory(ch);
            ///Check end of line?y/n
            br.mark(1000); 
            if(br.read() == '\n'){
            	isComment = false;
                line++;
            }
            br.reset();

        }while(fsm[state][charClass] > 0 || isComment);
        buf = removeSpacesAndComments(buf);
        if (!buf.equals("")) {
//            throw new Exception(" buf equals nothing");
	        System.out.print(buf+"\t");
	        int tokenType = finalState(state, buf);
	        if (tokenType == 13 || tokenType == 14 || tokenType == 15){
	            error(tokenType, line);
	            return null;
	        }
	        Token newToken;
	        if (tokenType == T.NUMBER)
	        	newToken = new Token(tokenType, Integer.parseInt(buf), buf);
	        else
	        	newToken = new Token(tokenType,symbolTable.currentAdd,buf);
	        return newToken;
        } 
        return null;
    }
    public Token newNextToken() throws Exception {
    	Token newToken = nextToken();
    	while (newToken == null) {
    		newToken = nextToken();
    	}
    	return newToken;
    }
    private boolean isResWord(String buf){
        buf = removeSpacesAndComments(buf);
        for (int i=0; i<reserved.length; i++){
            if (buf.equals(reserved[i])){
                return true;
            }
        }
        return false;
    }
    

    private int finalState(int state, String buf) {
        buf = removeSpacesAndComments(buf);
        switch(state){
            case 1://IDENTIFIER, Possible Reserve
                if (isResWord(buf)){
                    for(int i = 0; i < reserved.length; i ++) {
                        if (buf.equals(reserved[i])){
                            System.out.println(", "+i);
                            return i;
                            
                        }
                    }
                    return 44;//RESERVED WORD
                }//NOT RESERVED W0RD. STORE IN SYMBOL TABLE.
                
                int symbolLocation = symbolTable.insert(buf, "var", T.PLUS, scope, "false");
                System.out.println("Current Add: " + symbolTable.currentAdd);;
                return T.IDENTIFIER;// +", "+buf;//final state ID
            case 2:
                System.out.println(", "+T.NUMBER);
                return T.NUMBER;// +", "+buf;//final state NUMBER
            case 4:
            	int stringLocation = stringTable.insert(buf);
                System.out.println(", "+T.STRING + "\t String Location: " + stringLocation);
                return T.STRING;// +", "+buf;//final state STRING
            case 5:
                if (buf.equals(":=")){
                    System.out.println(", "+T.ASSIGN);
                    return T.ASSIGN;
                }
                System.out.println(", "+T.COLON);
                return T.COLON;// +", "+buf;//final state COLON
            case 6:
                System.out.println(", "+T.ASSIGN );
                return T.ASSIGN;// +", "+buf;//final state ASSIGN
            case 7:
                System.out.println(", "+T.GT );
                return T.GT;// +", "+buf;//final state GT
            case 8:
                System.out.println(", "+T.GE);
                return T.GE;// +", "+buf;//final state GE
            case 9:
                System.out.println(", "+T.LT );
                return T.LT;// +", "+buf;//final state LT
            case 10:
                System.out.println(", "+T.LE );
                return T.LE;// +", "+buf;//final state LE
            case 11:
                System.out.println(", "+T.NE );
                return T.NE;// +", "+buf;//final state NE
            case 12:
                if (buf.equals("=")){
                    System.out.println(", "+T.EQUAL );
                    return T.EQUAL;// +", "+buf;
                }
                if (buf.equals("+")){
                    System.out.println(", "+T.PLUS );
                    return T.PLUS;// +", "+buf;
                }
                if (buf.equals("-")){
                    System.out.println(", "+T.MINUS );
                    return T.MINUS;// +", "+buf;
                }
                if (buf.equals("/")){
                    System.out.println(", "+T.DIV );
                    return T.DIV;// +", "+buf;
                }
                if (buf.equals("*")){
                    System.out.println(", "+T.TIMES);
                    return T.TIMES;// +", "+buf;
                }
                if (buf.equals("%")){
                    System.out.println(", "+T.MOD );
                    return T.MOD;// +", "+buf;
                }
                if (buf.equals(",")){
                    System.out.println(", "+T.COMMA );
                    return T.COMMA;// +", "+buf;
                }
                if (buf.equals(";")){
                    System.out.println(", "+T.SEMI );
                    return T.SEMI;// +", "+buf;
                }
                if (buf.equals("(")){
                    System.out.println(", "+T.LPAREN );
                    return T.LPAREN;// +", "+buf;
                }
                if (buf.equals(")")){
                    System.out.println(", "+T.RPAREN);
                    return T.RPAREN;// +", "+buf;
                }
                if (buf.equals(".")){
                    System.out.println(", "+T.PERIOD );
                    return T.PERIOD;// +", "+buf;
                }
            case 13:
                return 13;//"Final state, scanner error, character.";
            case 14:
                return 14;//"Final state, scanner error, character.";
            default:
                return 15;
        }
    }

}