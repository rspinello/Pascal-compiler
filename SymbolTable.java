package Scanner; 

public class SymbolTable {
	public SymbolTableObject[] table;
	public int currentAdd;
	public int tempNum = 0;
	ErrorLogger logger = new ErrorLogger("ParserErrorLog.txt");
	
	/**
	 * Default constructor
	 */
	public SymbolTable() {
		table = new SymbolTableObject[100];
		currentAdd = 0;
	}
	
	//Returns how many actual non null objects are in the array
	public int contentSize() {
		int counter = 0;
		for (int i = 0; i < table.length; i++) {
			if (table[i] != null)
				counter++;
		}
		return counter;
	}
	
	/**
	 * Search
	 * @param name = the symbol to search for
	 * @param scope = the symbols scope
	 * @return location of the symbol in the table
	 */
	public int search(String name, int scope) {
		for (int i = 0; i < table.length; i++) {
			if (table[i] != null && table[i].name.equals(name) && table[i].scope == scope) {
				return i;
			}
		}
		//logger.customError("Error -- not found in search of symbol table", null);
		return -1; // not found in table
	}
	
	/**
	 * 
	 * @param name = name of symbol to store
	 * @param scope = scope of the symbol to store
	 * @return returns the location in the table (may already be stored in table)
	 */
	public int insert(String name, String kind, int type, int scope, String declared) {
		SymbolTableObject newSymbol = new SymbolTableObject(name, kind, type, scope, declared, 0, -10, -10, 0);
		for (int i = 0; i < table.length; i++) {
			if (table[i] == null) {
				table[i] = newSymbol;
				currentAdd = i;
				return i;
			}
			else if (table[i].name.equals(name) && table[i].scope == scope) {
				//Symbol is already in table, return the index
				return i;
			}
		}
		logger.customError("ERROR in symbol table -- could not find empty location or already existing symbol", null);
		return -1; // error, could not find empty location or already existing symbol
	}
	
	public void print() {
		for (int i = 0; i<table.length;i++) {
			if (table[i] == null)
				System.exit(0);
			System.out.println("Name: " + table[i].name +
					"\t Kind: " + table[i].kind + 
					"\t Type: " + table[i].type + 
					"\t Scope: " + table[i].scope +
					"\t Declared: " + table[i].declared + 
					"\t NumArgs: " + table[i].numArgs);
		}
	}
	
	/**
	 * Insert blank symbol and return location
	 * @return location of temp symbol
	 */
	public int getTemp() {
		SymbolTableObject temp = new SymbolTableObject("@t" + tempNum);
		tempNum++;
		return insert(temp.name, temp.kind, temp.type, temp.scope, "true");
	}
}
