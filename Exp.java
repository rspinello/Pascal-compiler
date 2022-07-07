package Scanner;  

public class Exp {
	public int type; // the data type of an expression
	public boolean address; //is the exp an address (true) or a value (false)
	public int value; // symbol table address or numerical value
	public boolean number; //Is the expression a number such as 246
	public boolean bool; //is the expression a boolean?
	
	/**
	 * Default Constructor
	 */
	public Exp() {
		this.type = -1;
		this.address = false;
		this.value = -1;
		this.number = false;
		this.bool = false;
	}
	
	
	public Exp(int type, boolean address, int value, boolean number, boolean bool) {
		this.type = type;
		this.address = address;
		this.value = value;
		this.number = number;
		this.bool = bool;
	}
}
