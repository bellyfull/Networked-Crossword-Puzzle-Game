package crosswordgame;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    private Socket clientsocket;
    private BufferedReader input;
    private PrintWriter output;
    private Scanner scanner;

    public Client(String serverIP, int port) {
        try {
        	
            clientsocket = new Socket(serverIP, port);
            input = new BufferedReader(new InputStreamReader(clientsocket.getInputStream()));
            output = new PrintWriter(clientsocket.getOutputStream(), true);
            scanner = new Scanner(System.in);
            
            

            // thread that listens to server messages
            new Thread(() -> {
                try {
                    String servermessage;

                    // reading messages in a loop
                    while ((servermessage = input.readLine()) != null) {
                        
                        System.out.println(servermessage);

                        // final score message and end game
                        if (servermessage.startsWith("FINAL_SCORE")) { // final score message
                            // System.out.println("final score message found");
                            
                            // final score message
                            StringBuilder finalscoremessage = new StringBuilder(servermessage + "\n");
                            
                            
                            while ((servermessage = input.readLine()) != null && servermessage.isEmpty() == false) {
                                finalscoremessage.append(servermessage).append("\n");
                                System.out.println(servermessage);
                            }
                            
                            // output final score to clients
                            System.out.println(finalscoremessage.toString().trim());
                            
                            break;  
                        }
                        

                        // user input to choose position
                        if (servermessage.contains("Would you like to answer across (a) or down (d)?")) { // response message
                        	//
                            //System.out.println("Would you like to answer across (a) or down (d)?");
                            String direction = scanner.nextLine();
                            output.println("direction " + direction);

                            System.out.println("Which number?");
                            String number = scanner.nextLine();
                            output.println("number " + number);

                            System.out.println("What is your guess?");
                            String guess = scanner.nextLine();
                            output.println("guess " + guess);
                        }
                    }
                } catch (IOException e) {
                    System.out.println("Disconnected from server.");
                } finally {
                    // closed client session
                    try {
                        input.close();
                        output.close();
                        clientsocket.close();
                        System.out.println("client session ended");
                    } catch (IOException e) {
                        System.out.println("could not close client session");
                    }
                }
            }).start();


        } catch (IOException e) {
            System.out.println("Unable to connect to server");
        }
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // hostname and port
        System.out.print("Enter the server hostname: ");
        String hostname = scanner.nextLine();

        System.out.print("Enter the server port: ");
        int port = Integer.parseInt(scanner.nextLine());

        // connect to the server
        new Client(hostname, port);
    }
}
