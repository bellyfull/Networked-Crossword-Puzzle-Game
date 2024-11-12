package crosswordgame;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.ArrayList;

public class GameBoard {
    private String[][] board;
    private List<String> guessedWords = new ArrayList<>();
    private final String[][] acrosswords;
    private final String[][] downwords;
    private String[][] cellstatus;
    private int acrosscount;
    private int downcount;
    private boolean[] acrossPlaced;
    private boolean[] downPlaced;

    
    // cell states
    private static final String EMPTY = " ";
    private static final String UNFOUND = "_";
    private static final String FOUND = "R";


    // GameBoard constructor
    public GameBoard(String[][] acrosswords, String[][] downwords, int acrosscount, int downcount) {
        this.acrosscount = acrosscount;
        this.downcount = downcount;
        
        this.acrosswords = new String[acrosscount][3];
        this.downwords = new String[downcount][3];
        
        this.acrossPlaced = new boolean[acrosscount];
        this.downPlaced = new boolean[downcount];
        
        for (int i = 0; i < acrosscount; i++) {
            this.acrosswords[i] = Arrays.copyOf(acrosswords[i], acrosswords[i].length);
        }
        for (int i = 0; i < downcount; i++) {
            this.downwords[i] = Arrays.copyOf(downwords[i], downwords[i].length);
        }

        this.cellstatus = new String[12][12];
        this.board = new String[12][12];
        
        //initialize to EMPTY cells
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                board[i][j] = EMPTY;
                cellstatus[i][j] = EMPTY;
            }
        }

        //sort by length. longest -> shortest
        Arrays.sort(this.acrosswords, 0, acrosscount, Comparator.comparingInt((String[] arr) -> arr[1].length()).reversed());
        Arrays.sort(this.downwords, 0, downcount, Comparator.comparingInt((String[] arr) -> arr[1].length()).reversed());

        sharedindexcheck();
        remaining();
    }

    // checks for words that share a start index
    private void sharedindexcheck() {
        for (int i = 0; i < acrosscount; i++) {
            String acrossWord = acrosswords[i][1];
            int acrossNum = Integer.parseInt(acrosswords[i][0]); // get idx

            // check each downword
            for (int j = 0; j < downcount; j++) {
                String downWord = downwords[j][1];
                int downNum = Integer.parseInt(downwords[j][0]); // get idx

                // check if same letter and same idx
                if (acrossNum == downNum && acrossWord.charAt(0) == downWord.charAt(0)) {
                    if (sharedindex(acrossWord, downWord, acrossNum)) { //place
                        acrossPlaced[i] = true;
                        downPlaced[j] = true;
                        break;
                    }
                }
            }
        }
    }
    

    // place words with shared index first
    private boolean sharedindex(String acrossWord, String downWord, int idx) {
    	// board limits
        int maxrow = board.length - downWord.length(); // board height - downword's height
        int maxcol = board[0].length - acrossWord.length(); // board width - acrossword's length

        // iterate thru every cell on board
        for (int row = 1; row < maxrow; row++) {
            for (int col = 1; col < maxcol; col++) {
                // check if in bounds
                if (canplaceacross(acrossWord, row, col) && canplacedown(downWord, row, col)) {
                    //place at same start position
                    placeacross(acrossWord, row, col, idx);
                    placedown(downWord, row, col, idx);
                    return true;
                }
            }
        }
        return false; // no valid place for commonly indexed across+down word
    }



    // places remaining words who dont have shared starting index
    private void remaining() {
        boolean placed;
        do {
            placed = false;
            // acrosswords
            for (int i = 0; i < acrosscount; i++) {
                if (acrossPlaced[i] == false) {
                    String acrossWord = acrosswords[i][1];
                    int idx = Integer.parseInt(acrosswords[i][0]);
                    
                    if (intersect(acrossWord, true, idx)) {
                        acrossPlaced[i] = true;
                        placed = true;
                    } else {
                        shiftRight();
                    }
                }
            }
            // downwords
            for (int j = 0; j < downcount; j++) {
                if (downPlaced[j] == false) {
                    String downWord = downwords[j][1];
                    int idx = Integer.parseInt(downwords[j][0]);
                    
                    if (intersect(downWord, false, idx)) {
                        downPlaced[j] = true;
                        placed = true;
                    } else {
                        shiftRight();
                    }
                }
            }
        } while (placed);
    }
    
    
    // shift whole board to right to create room for dodgers
    private void shiftRight() {
        for (int row = 0; row < board.length; row++) {
            for (int col = board[row].length - 1; col > 0; col--) {
                board[row][col] = board[row][col - 1]; // place +1 to the right 
            }
            board[row][0] = " ";
        }
    }
    
    
 // places across and down words that intersect
    private boolean intersect(String word, boolean isAcross, int idx) {
    	// for each cell,
        for (int row = 0; row < board.length; row++) {
            for (int col = 0; col < board[row].length; col++) {
            	// check each letter to find intersections
                for (int letter = 0; letter < word.length(); letter++) {
                	// current pos == word letter?
                    if (board[row][col].equals(String.valueOf(word.charAt(letter)))) { // match
                    	
                        if (isAcross) { // acrossword
                            if (canplaceacross(word, row, col - letter)) {
                                placeacross(word, row, col - letter, idx);
                                return true;
                            }
                        } else { // downword
                            if (canplacedown(word, row - letter, col)) {
                                placedown(word, row - letter, col, idx);
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false; // could not find an intersection
    }
    
    
    // place acrosswords on board
    private void placeacross(String word, int row, int startCol, int idx) {
        
    	board[row][startCol] = idx + String.valueOf(word.charAt(0));  // first letter of cell has question number 1_
        
    	//rest of letters
    	for (int col = 1; col < word.length(); col++) {
            board[row][startCol + col] = String.valueOf(word.charAt(col));  // fill cells with remaining letters
            cellstatus[row][startCol + col] = UNFOUND; // set cell status to "_"
        }
    }

    // place downwords on board
    private void placedown(String word, int startRow, int col, int idx) {
    	
        board[startRow][col] = idx + String.valueOf(word.charAt(0));  // first letter of cell has question number 
        
        // rest of letters
        for (int row = 1; row < word.length(); row++) {
            board[startRow + row][col] = String.valueOf(word.charAt(row));  
            cellstatus[startRow + row][col] = UNFOUND;
        }
    }

    // check if word can be placed across
    private boolean canplaceacross(String word, int row, int startCol) {
    	
        if (startCol < 0 || startCol + word.length() > board[row].length) {
        	return false; // within boundaries of board
        }
        
        for (int col = 0; col < word.length(); col++) { // if another letter is not already there
            String cell = board[row][startCol + col];
            
            if (cell.equals(" ") == false && cell.equals(String.valueOf(word.charAt(col))) == false) {
                return false;
            }
        }
        return true;
    }

    private boolean canplacedown(String word, int startRow, int col) {
    	
        if (startRow < 0 || startRow + word.length() > board.length) {
        	return false; 
        }
        
        for (int row = 0; row < word.length(); row++) {
            String cell = board[startRow + row][col];
            
            if (cell.equals(" ") == false && cell.equals(String.valueOf(word.charAt(row))) == false) {
                return false;
            }
        }
        return true;
    }

    // masked ver
    public String displaymasked() {
        StringBuilder boardString = new StringBuilder();
        
        for (int row = 0; row < board.length; row++) {
            for (int col = 0; col < board[row].length; col++) {
                String cell = board[row][col];
                String status = cellstatus[row][col];
                
                if (status.equals(FOUND)) {
                    // if is number, shift to left to align 
                    if (Character.isDigit(cell.charAt(0))) {
                        boardString.append(" " + String.format("%-3s", cell));
                    } else {
                        // just letter
                        boardString.append(String.format(" %-3s", cell));
                    }
                } else if (cell == null || cell.equals(" ")) {
                    boardString.append("    "); // add spaces
                } else if (Character.isDigit(cell.charAt(0))) {
                    // display number with masked "_"
                    boardString.append(" " + cell.charAt(0) + "_  ");
                } else {
                    boardString.append("_   ");
                }
            }
            boardString.append("\n"); 
        }
        return boardString.toString();
    }


    // unmasked ver
    public String displayunmasked() {
        StringBuilder boardString = new StringBuilder();
        
        for (String[] row : board) {
            for (String cell : row) {
                if (cell == null || cell.equals(" ")) {
                    boardString.append("    "); 
                } else if (Character.isDigit(cell.charAt(0))) {
                    boardString.append(String.format("%-3s ", cell)); // idx number + letter 
                } else {
                    boardString.append(String.format(" %-3s", cell)); // just letters
                }
            }
            boardString.append("\n");
        }
        return boardString.toString();
    }

    public void revealanswer(String guessedWord) {
        String idx = null;
        boolean isAcross = false;

        
        // find if guessed word is in acrosswords or downwords
        for (String[] entry : acrosswords) {
            if (entry[1].equalsIgnoreCase(guessedWord)) {
                idx = entry[0];
                isAcross = true;
                break;
            }
        }
        if (idx == null) {
            for (String[] entry : downwords) {
                if (entry[1].equalsIgnoreCase(guessedWord)) {
                    idx = entry[0];
                    isAcross = false;
                    break;
                }
            }
        }

        // if in neither 
        if (idx == null) {
            // System.out.println("word not found in acrosswords or downwords");
            return;
        }

        // find start pos each word on board
        int[] startPos = findstart(idx, guessedWord);
        if (startPos == null) {
            // System.out.println("cant find start position for question " + idx);
            return;
        }

        int startRow = startPos[0];
        int startCol = startPos[1];
        //System.out.println("revealing word " + guessedWord + " which starts at " + startRow + "," + startCol);

        for (int i = 0; i < guessedWord.length(); i++) {
            String letter = (i == 0 ? idx : "") + guessedWord.charAt(i);
            
            if (isAcross) {
                board[startRow][startCol + i] = letter;
                cellstatus[startRow][startCol + i] = FOUND;
            } else {
                board[startRow + i][startCol] = letter;
                cellstatus[startRow + i][startCol] = FOUND;
            }
        }
    }



    // Starting idx locator
    private int[] findstart(String idx, String word) {
    	// each cell in board
        for (int row = 0; row < board.length; row++) {
            for (int col = 0; col < board[row].length; col++) {
                String cell = board[row][col];
                

                // check if cell starts with the idx number and has first letter of the word
                if (cell != null && cell.startsWith(idx) && cell.endsWith(String.valueOf(word.charAt(0)))) {
                   
                    if (cell.equals(idx + word.charAt(0))) {
                        return new int[]{row, col};
                    }
                }
            }
        }
        // System.out.println("start position not found for idx " + idx + ", word " + word );
        return null; // not found
    }



    public void addguessed(String word) {
        if (!guessedWords.contains(word)) {  // add only one of each word to list
            guessedWords.add(word);
            revealanswer(word);  
            // System.out.println("guessed words list" + guessedWords);
        }
    }

    
    //getters
    public List<String> getGuessedWords() {
        return guessedWords;
    }

    
    public String[][] getAcrossWords() {
        return acrosswords;
    }

    public String[][] getDownWords() {
        return downwords;
    }

    public int getAcrossCount() {
        return acrosscount;
    }

    public int getDownCount() {
        return downcount;
    }

    


}