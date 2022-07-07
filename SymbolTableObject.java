package Scanner; 

public class SymbolTableObject {
	public String name;
	public String kind;
	public int type;
	public int scope;
	public String declared;
	public int offset;
	public int memAdd;
	public int start;
	public int numArgs; //only for params
	
	
	public SymbolTableObject(String name, String kind, int type, int scope, String declared, int offset, int memAdd, int start, int numArgs) {
		this.name = name;
		this.kind = kind;
		this.type = type;
		this.scope = scope;
		this.declared = declared;
		this.offset = offset;
		this.memAdd = memAdd;
		this.start = start;
		this.numArgs = numArgs;
	}
	
	public SymbolTableObject() {
		this.name = "Empty";
		this.kind = "Empty";
		this.type = -10;
		this.scope = -10;
		this.declared = "Empty";
		this.offset = 0;
		this.memAdd = -10;
		this.start = -10;
		this.numArgs = 0;
	}
	
	public SymbolTableObject(String name) {
		this.name = name;
		this.kind = "Empty";
		this.type = -10;
		this.scope = -10;
		this.declared = "Empty";
		this.offset = 0;
		this.memAdd = -10;
		this.start = -10;
		this.numArgs = 0;
	}
	
	public void printAsString() {
		System.out.println("");
		
		System.out.println("Name: " + name 
				+ "\n Kind: " + kind
				+ "\n Type: " + type
				+ "\n Scope: " + scope
				+ "\n Declared: " + declared
				+ "\n Offset: " + offset);
		
		System.out.println("");
	}
}
