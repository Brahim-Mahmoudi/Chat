/***
 * ClientThread
 * Example of a TCP server
 * Date: 14/12/08
 * Authors:
 */

package stream;

import java.io.*;
import java.net.*;

public class ClientThread
        extends Thread {

    private Socket clientSocket;



    ClientThread(Socket s) {
        this.clientSocket = s;
    }

    /**
     * receives a request from client then sends an echo to the client
     **/
    public void run() {
        BufferedReader socIn = null;
        PrintStream socOut = null;
        try {
            socIn = new BufferedReader(
                    new InputStreamReader(clientSocket.getInputStream()));

            while (!EchoClient.stopThread) {
                try {
                    String line = socIn.readLine();
                    if(line == null) break;
                    System.out.println(line);
                } catch (SocketTimeoutException e) {
                }

            }
        } catch (Exception e) {
            System.err.println("Error in ClientThread:" + e);
        }

        finally{


            try {
                if(socIn != null){
                    socIn.close();
                }

                if(socOut != null) {
                    socOut.close();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }



        }

    }

    public Socket getClientSocket() {
        return clientSocket;
    }
}