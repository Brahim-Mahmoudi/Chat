/***
 * EchoClient
 * Example of a TCP client 
 * Date: 10/01/04
 * Authors:
 */
package stream;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.net.UnknownHostException;



public class EchoClient {

    static boolean stopThread = false;

  /**
  *  main method
  *  accepts a connection, receives a message from client then sends an echo to the client
  **/
    public static void main(String[] args) throws IOException {
        String username ="";
        Socket echoSocket = null;
        PrintStream socOut = null;
        BufferedReader stdIn = null;
        BufferedReader socIn = null;


        if (args.length != 3) {
          System.out.println("Usage: java EchoClient <EchoServer host> <EchoServer port> <username>");
          System.exit(1);

        }

        try {

            // creation socket ==> connexion
      	    echoSocket = new Socket(args[0],new Integer(args[1]).intValue());
            echoSocket.setSoTimeout(1000);
	        socIn = new BufferedReader(
	    		          new InputStreamReader(echoSocket.getInputStream()));    
	        socOut= new PrintStream(echoSocket.getOutputStream());
	        stdIn = new BufferedReader(new InputStreamReader(System.in));
            System.out.println("Socket created");
            System.out.println(echoSocket);
            //recupe le username
            username = args[2];
            socOut.println(username);

            if(stdIn.readLine().equals("Fail")){
                System.exit(1);
            }

            // creation Thread d'écoute
            //Créer un  thread pour lire l'entrée du terminal client
            ClientThread threadListener;
            threadListener = new ClientThread(echoSocket);
            threadListener.start();

            String line;

            while (true) {
                line=stdIn.readLine();
                if (line.equals(".")) {
                    stopThread = true;
                    while(threadListener.getState() == Thread.State.RUNNABLE) {}
                    break;
                }
                socOut.println(line);
                System.out.println("message send !");



                //System.out.println("echo out :" + socOut.println());
            }
            } catch (UnknownHostException e) {
                System.err.println("Don't know about host:" + args[0]);
                System.exit(1);
            } catch (IOException e) {
                System.err.println("Couldn't get I/O for "
                                   + "the connection to:"+ args[0]);
                System.exit(1);
            }
                             

      socOut.close();
      socIn.close();
      stdIn.close();
      echoSocket.close();
    }
}


