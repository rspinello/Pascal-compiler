package Scanner;  

import java.util.Optional;

public class Quad {
    public String operator, arg1, arg2, result;
   
    public Quad(String _operator, String _arg1, String _arg2, String _result){
        operator = _operator;
        arg1 = _arg1;
        arg2 = _arg2;
        result = _result;
    }
    
    public void print(){
        System.out.println(operator +" "+ arg1 + " " + arg2 + " " + result);
    }
    
    public String returnFullString(){
        return operator +" "+ arg1 + " " + arg2 + " " + result;
    }
}
