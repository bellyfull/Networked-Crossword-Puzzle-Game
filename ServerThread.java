package crosswordgame;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ServerThread extends Thread {
    private Socket clientsocket;
    private BufferedReader input; // input from client
    private PrintWriter output; // output to client
    private Server server;
    private int playerID;

    //constructor
    public ServerThread(Socket clientsocket, Server server, int playerID) {
        this.clientsocket = clientsocket;
        this.server = server;
        this.playerID = playerID;

        try { // i/o stream for connecting client
            input = new BufferedReader(new InputStreamReader(clientsocket.getInputStream()));
            output = new PrintWriter(clientsocket.getOutputStream(), true);
            output.println("Player " + playerID + "has joined");
        } catch (IOException e) {
            System.out.println("could not establish player " + playerID);
        }
    }

    @Override
    public void run() {
        try {
            while (true) {
            	// read client message
                String clientMessage = input.readLine();
                
                if (clientMessage == null) {
                	break;
                }

                // process guess
                if (clientMessage.startsWith("guess")) {
                    String guess = clientMessage.split(" ")[1];
                    server.checkGuess(guess, playerID);
                }
            }
        } catch (IOException e) {
            System.out.println("Player " + playerID + " disconnected");
        } finally {
            closeConnection();
        }
    }

    public void sendMessage(String message) {
        output.println(message);
    }
    
    public String receiveMessage() {
        try {
            return input.readLine();
        } catch (IOException e) {
            System.err.println("Error reading message from client: " + e.getMessage());
            return null;
        }
    }

    public void flushOutput() {
        output.flush();
    }

    // close all client input, output, and socket
    private void closeConnection() {
        try {
            input.close();
            output.close();
            clientsocket.close();
            System.out.println("disconnected Player " + playerID);
        } catch (IOException e) {
            System.out.println("could not disconnect Player " + playerID);
        }
    }
}
