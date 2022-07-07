package Scanner;

public class testQuads {
    public static void main(String[] args){
        Quads quads = new Quads();
        quads.insertQuad("ADD", "X", "Y", "Z");
        quads.insertQuad("BR",null, null, "X");
        quads.insertQuad("DCL",null, null, "Z");
        quads.printQuads();
    }
}
