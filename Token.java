package Scanner;  

public class Token {
    public int tokenType;
    public int value; //address in symbol table or numerical value
    public String name;

    public Token(){
        tokenType = 0;
        value = 0;
        name = "blank";
    }
    public Token(int tokenTypePassed, int valuePassed, String name){
        this.tokenType = tokenTypePassed;
        this.value = valuePassed;
        this.name = name;
    }
}