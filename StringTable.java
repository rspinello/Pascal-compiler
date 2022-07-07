package Scanner; 

public class StringTable {
	public String[] table;
	
	/**
	 * Default constructor
	 */
	public StringTable() {
		table = new String[100];
	}
	
	/**
	 * Search
	 * @param name = the string to search for
	 * @param scope = the string scope
	 * @return location of the string in the table
	 */
	public int search(String name) {
		for (int i = 0; i < table.length; i++) {
			if (table[i].equals(name)) {
				return i;
			}
		}
		return -1; // not found in table
	}
	
	/**
	 * 
	 * @param name = name of string to store
	 * @param scope = scope of the string to store
	 * @return returns the location in the table (may already be stored in table)
	 */
	public int insert(String name) {
		for (int i = 0; i < table.length; i++) {
			if (table[i] == null) {
				table[i] = name;
				return i;
			}
			else if (table[i].equals(name)) {
				//Symbol is already in table, return the index
				return i;
			}
		}
		System.out.println("ERROR");
		return -1; // error, could not find empty location or already existing symbol
	}
	
	//Grabs the most recent string in the table
	public String grabRecent() {
		for (int i = 99; i >= 0; i--) {
			if (table[i] != null)
				return i+1000+"";
		}
		return "error";
	}
	
}
