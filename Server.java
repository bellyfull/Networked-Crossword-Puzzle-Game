package crosswordgame;

import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Server {
	
    private static final int PORT = 3456;
    private List<ServerThread> players = new ArrayList<>();
    private int totalplayers;
    private Game game;
    private ServerSocket serverSocket;
    private GameBoard gameboard;

    private final String[][] acrosswords = new String[20][3];  
    private final String[][] downwords = new String[20][3];
    private int acrosscount = 0;
    private int downcount = 0;
    
    private int player1score = 0;
    private int player2score = 0;


    // opening message and server constructor for total # of players
    public Server(int totalplayers) {
        this.totalplayers = totalplayers;
        System.out.println("Listening on port " + PORT + ". Waiting for players...");
    }

    public void start() {
        try {
            serverSocket = new ServerSocket(PORT);
            gameboard = loadFile();

            
            while (players.size() < totalplayers || totalplayers == 0) {
                Socket clientSocket = serverSocket.accept();
                int playerID = players.size() + 1;
                
                
                // add player thread to list
                ServerThread playerthread = new ServerThread(clientSocket, this, playerID);
                players.add(playerthread);
                playerthread.start();

                
                // Connection from 127.0.0.1
                System.out.println("Connection from " + clientSocket.getInetAddress());

                
                // receive player input on # of players. But I used main() and initialized game to 2 players right away. 
                if (players.size() == 1 && totalplayers == 0) {
                    playerthread.sendMessage("How many players will there be?");
                    String playernummessage = playerthread.receiveMessage();

                    if (playernummessage != null && playernummessage.startsWith("players")) {
                        totalplayers = Integer.parseInt(playernummessage.split(" ")[1]);
                        System.out.println("Number of players: " + totalplayers);
                    }
                }

                // wait until all players join
                if (players.size() < totalplayers) {
                    System.out.println("Waiting for player " + (players.size() + 1) + "...");
                    broadcast("Waiting for player " + (players.size() + 1) + " to join...");
                } else {
                    System.out.println("Game can now begin.");
                    broadcast("The game is beginning.");
                    startGame();
                }
            }
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
        }
    }

    
    
    private void startGame() {
        game = new Game(gameboard, totalplayers);
        System.out.println("(initial unmasked board layout)");
        System.out.println(gameboard.displayunmasked());

        
        String masked = gameboard.displaymasked();
        // System.out.println("masked ver");
        System.out.println(masked);

        broadcastboard(masked);
        
        // player 1 starts first
        playerturn(1);
    }

    private void playerturn(int playerID) { // turn message
    	System.out.println("Player " + playerID + "'s turn.");
    	
        ServerThread currentPlayer = players.get(playerID - 1);
        currentPlayer.sendMessage("Player" + playerID + "'s turn.");
        currentPlayer.sendMessage("Would you like to answer across (a) or down (d)?"); // skip this cout in client thread
    }


    public void checkGuess(String guess, int playerID) {
        boolean isCorrect = game.processGuess(guess, playerID);
        String responsemessage;
        if (isCorrect) {
            responsemessage = "That is correct.";
        } else {
            responsemessage = "That is incorrect.";
        }

        System.out.println("Player " + playerID + " guessed \"" + guess + "\""); //server
        System.out.println(responsemessage); // that is correct
        broadcast("Player " + playerID + " guessed \"" + guess + "\""); 
        broadcast(responsemessage); // broadcast for both clients

        if (isCorrect) {
            gameboard.revealanswer(guess);  // reveal answer
            gameboard.addguessed(guess);       // mark word as guessed 

            // update points each time word is guessed
            if (playerID == 1) {
                player1score++;
            } else if (playerID == 2) {
                player2score++;
            }

            // Game finish check
            int totalwords = gameboard.getAcrossCount() + gameboard.getDownCount();
            int guessedWordsCount = gameboard.getGuessedWords().size();
            
            if (guessedWordsCount == totalwords) {
                //System.out.println("total words: " + totalwords);
                displayFinalScore(); 
                terminate();
                return;
            }


            // display the updated board after every guess
            broadcastboard(gameboard.displaymasked());
            playerturn(playerID);  // if correct, continue to play for current player
        } else {
            // Opponent's turn
            game.nextturn();
            int nextplayerID = game.getcurrentturn();

            // display updated board for opponent
            broadcastboard(gameboard.displaymasked());
            playerturn(nextplayerID);
        }
    }




    private synchronized void broadcast(String message) {
        for (ServerThread player : players) {
            player.sendMessage(message);
        }
    }

    private synchronized void broadcastboard(String board) {
        for (ServerThread player : players) {
            player.sendMessage("Sending game board:");
            player.flushOutput();
        }
        
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        String questionbankmessage = board +  // question hint bank
                "Across\n" +
                "1 What is USC’s mascot?\n" +
                "2 What professional baseball team is closest to USC?\n" +
                "3 What is the four-letter prefix for Computer Science?\n\n" +  // line break
                "Down\n" +
                "1 What is the name of USC’s white horse?\n" +
                "4 What is one of USC’s colors?\n" +
                "5 Who is USC’s School of Business named after?\n" +
                ".";

        for (ServerThread player : players) {
            player.sendMessage(questionbankmessage);
            player.flushOutput();
        }
    }
    
    
    
    private void displayFinalScore() {
        
    	broadcastboard(gameboard.displaymasked());
    	
        System.out.println("The game has concluded. Sending scores.");
        
        // FINAL_SCORE
        String scoreMessage = "FINAL_SCORE Final Score\n" +
                              "Player 1 – " + player1score + " correct answers.\n" +
                              "Player 2 – " + player2score + " correct answers.\n";
        
        // win or tie
        if (player1score > player2score) {
            scoreMessage += "Player 1 is the winner.\n";
        } else if (player2score > player1score) {
            scoreMessage += "Player 2 is the winner.\n";
        } else {
            scoreMessage += "The game is a tie!\n";
        }

        // send final score
        broadcast(scoreMessage.trim());
    }



    private void terminate() { // end game after all words have been guessed
    	
        System.out.println("<Client terminates>");
        
    }

    private GameBoard loadFile() {
    	// open directory
        File gameDataDir = new File("gamedata");
        if (!gameDataDir.exists()) {
            System.out.println("Unable to find directory");
            return null;
        }

        // find all csv files
        File[] csvFiles = gameDataDir.listFiles((dir, name) -> name.endsWith(".csv"));
        if (csvFiles == null || csvFiles.length == 0) {
            System.out.println("No CSV files were found in this directory");
            return null;
        }

        // choose random out of the csv files
        File selectedFile = csvFiles[new Random().nextInt(csvFiles.length)];
        System.out.println("Reading random game file. \n File read successfully.");
        System.out.println("Using game file: " + selectedFile.getName());

        
        
        //reading in file data
        try (BufferedReader reader = new BufferedReader(new FileReader(selectedFile))) {
            String section = "";
            String line;

            while ((line = reader.readLine()) != null) {
                line = line.trim();
                //System.out.println(line);

                //ACROSS,, <number>,<answer>,<question>
                if (line.equalsIgnoreCase("ACROSS,,")) {
                    section = "ACROSS";
                    //System.out.println("across section found");
                } else if (line.equalsIgnoreCase("DOWN,,")) {
                    section = "DOWN";
                    //System.out.println("down section found");
                } else {
                    String[] parts = line.split(",", 3);
                    //System.out.println("Arrays.toString(parts));

                    if (parts.length == 3) { //<index0>,<word1>,<hint2>
                        String index = parts[0].trim();
                        String word = parts[1].trim();
                        String hint = parts[2].trim();

                        if (index.isEmpty() == false && word.isEmpty() == false && hint.isEmpty() == false) {
                            if (section.equals("ACROSS") && acrosscount < 20) {
                                acrosswords[acrosscount++] = new String[]{index, word, hint};
                                //System.out.println("added to acrosswords: " + Arrays.toString(acrosswords[acrosscount - 1]));
                            } else if (section.equals("DOWN") && downcount < 20) {
                                downwords[downcount++] = new String[]{index, word, hint};
                                //System.out.println("added to downwords: " + Arrays.toString(downwords[downcount - 1]));
                            } else {
                                System.out.println("too many words for either across or down");
                            }
                        } else {
                            System.out.println("entry(s) are empty");
                        }
                    } else {
                        System.out.println("can't split into three parts");
                    }
                }
            }

        
//            for (int i = 0; i < acrosscount; i++) {
//                String[] word = acrosswords[i];
//                System.out.println("Index: " + word[0] + ", Word: " + word[1] + ", hint: " + word[2]);
//            }

//            for (int i = 0; i < downcount; i++) {
//                String[] word = downwords[i];
//                System.out.println("Index: " + word[0] + ", Word: " + word[1] + ", hint: " + word[2]);
//            }

            // create gameboard
            return new GameBoard(acrosswords, downwords, acrosscount, downcount);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        
        
        
    }

    public static void main(String[] args) {
        Server server = new Server(2); // intialized 2 players
        server.start();
    }
}
