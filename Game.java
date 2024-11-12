package crosswordgame;

//import java.util.HashSet;
//import java.util.Set;

public class Game {
    private GameBoard gameBoard;
    private int totalPlayers;
    private int currentturn;
//    private Set<String> guessedWords;
//    private int currentidx;
//    private boolean isacross;


    public Game(GameBoard gameBoard, int totalPlayers) {
        this.gameBoard = gameBoard;
        this.totalPlayers = totalPlayers;
        this.currentturn = 1; // player 1 goes first
        //this.guessedWords = new HashSet<>();
    }

    public boolean processGuess(String guess, int playerId) {
        //System.out.println(" processing Player " + playerId + "'s guess: " + guess);
        guess = guess.toLowerCase();  // convert all guesses to lowercase

        //acrosswords
        for (int i = 0; i < gameBoard.getAcrossCount(); i++) {
            String[] wordData = gameBoard.getAcrossWords()[i];
            if (wordData[1] != null && wordData[1].equalsIgnoreCase(guess)) {
                //System.out.println("match found for " + guess);
                gameBoard.addguessed(guess);
                return true;
            }
        }

        //downwords
        for (int i = 0; i < gameBoard.getDownCount(); i++) {
            String[] wordData = gameBoard.getDownWords()[i];
            if (wordData[1] != null && wordData[1].equalsIgnoreCase(guess)) {
                //System.out.println("match found for" + guess);
                gameBoard.addguessed(guess);
                return true;
            }
        }

        //System.out.println("could not find in word lists: " + guess);
        return false;
        
    }
    
    
    
//
//    public int getcurrentidx() {
//        return currentidx;
//    }
//
//    public boolean iscurrentacross() {
//        return isacross;
//    }
//



    public void nextturn() {
        currentturn = (currentturn % totalPlayers) + 1;
    }

    public int getcurrentturn() {
        return currentturn;
    }

}
