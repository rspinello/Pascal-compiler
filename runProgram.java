package Scanner;

import java.io.IOException;
import java.util.Scanner;

public class runProgram {

	public static void main(String[] args) throws IOException {
		Scanner input = new Scanner(System.in);
        System.out.print("Enter program file name: ");
        String fileName = input.nextLine();

        CompilerScanner scan = new CompilerScanner(fileName); 

        runScannerTest(scan);
        input.close();
        

	}
    public static void checkSpaceStripping(CompilerScanner scan){
        Scanner input = new Scanner(System.in);
        String stringIn = "";
        String stringOut = "";
        do{
            System.out.print("Enter a string to be stripped('X' to quit)): ");
            stringIn = input.nextLine();
            stringOut = scan.removeSpacesAndComments(stringIn);
            System.out.println("stringPassed:\n'"+ stringIn + "'\nstringStripped:\n'" + stringOut +"'");
        }while(!stringIn.equals("X"));
        input.close();
    }
    public static void runScannerTest(CompilerScanner scan){
        Token t = new Token();
        while(t.tokenType != T.PERIOD ){
            try{
                t = scan.nextToken();
                if (t == null){//error 
                    System.out.print("----------EOF-------------");
                    break;
                }
            }catch (Exception e){

            }
            
            
        }
        
    }
}
