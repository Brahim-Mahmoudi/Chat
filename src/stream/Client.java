/***
 * EchoClient
 * Example of a TCP client 
 * Date: 10/01/04
 * Authors:
 */
package stream;

import java.io.*;
import java.net.*;



public class Client {


  /**
  *  main method
  *  accepts a connection, receives a message from client then sends an echo to the client
  **/

  static BufferedReader socIn = null;
  static PrintStream socOut = null;
  static BufferedReader stdIn = null;
    public static void main(String[] args) throws IOException {
        String username = "";
        Socket echoSocket = null;


        if (args.length != 3) {
            System.out.println("Usage: java EchoClient <EchoServer host> <EchoServer port> <username>");
            System.exit(1);

        }

        try {
            // creation socket ==> connexion
            echoSocket = new Socket(args[0], new Integer(args[1]).intValue());
            socIn = new BufferedReader(
                    new InputStreamReader(echoSocket.getInputStream()));
            socOut = new PrintStream(echoSocket.getOutputStream());
            stdIn = new BufferedReader(new InputStreamReader(System.in));
            //recupe le username
            username = args[2];
            socOut.println(username);


        } catch (UnknownHostException e) {
            System.err.println("Don't know about host:" + args[0]);
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for "
                    + "the connection to:" + args[0]);
            System.exit(1);
        }


        //ICI COMMENCE LES THREADS POUR L'ENVOIE ET LA RECEPTION DE MESSSAGE

        //Thread d'envoie de messages à un ou plusieurs clients

        Thread sendMessage = new Thread(new Runnable() {

            @Override
            public void run() {
                while (true) {
                    try {
                        String message = stdIn.readLine();
                        socOut.println(message);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

        });

        //Thread de lecture de messages reçuent

        Thread readMessage = new Thread(new Runnable() {

            @Override
            public void run() {
                while (true) {
                    try {

                        String message = socIn.readLine();
                        System.out.println(message);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        //On demarre les threads
        sendMessage.start();
        readMessage.start();

    }
}


