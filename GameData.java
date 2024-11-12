package crosswordgame;

public class GameData {
    private String[][] acrosswords; 
    private String[][] downwords;
    private int acrossnum;
    private int downnum;

    
    public GameData() {
        acrosswords = new String[20][3]; // 20 across clues
        downwords = new String[20][3];   // 20 down clues
        acrossnum = 0;
        downnum = 0;
    }

    
    public boolean addEntry(String line, String section) {
        // number word question
        String[] parts = line.split(",");

        // if valid format
        if (parts.length == 3) { 
            if (section.equals("ACROSS")) {
            	//acrosswords
                acrosswords[acrossnum++] = parts;
            } 
            else if (section.equals("DOWN")) {
                //downwords
                downwords[downnum++] = parts;
            } 
            
        } else {
            return false;  // wrong format
        }
        return true;
    }

    
   // acrosswords getter
    public String[][] getacrosswords() {
        return acrosswords;
    }
    
    // downwords getter
    public String[][] getdownwords() {
        return downwords;
    }
}
