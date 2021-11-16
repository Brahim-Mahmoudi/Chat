/***
 * ClientThread
 * Example of a TCP server
 * Date: 14/12/08
 * Authors:
 */

package stream;

import java.io.*;
import java.net.*;
import java.util.Map;

public class ClientHelperThread
        extends Thread {

    private Socket clientSocket;
    private String username;
    private boolean isConnected ;
    private  BufferedReader socIn;
    private PrintStream socOut;



    ClientHelperThread(Socket s,String username,BufferedReader socIn,PrintStream socOut) {
        this.clientSocket = s;
        this.username = username;
        this.isConnected = true;
        this.socIn = socIn;
        this.socOut = socOut;

    }

    /**
     * receives a request from client then sends an echo to the client
     **/
    public void run() {

        try {


            String line;
            while (true) {
                line = socIn.readLine();
                System.out.println(line);

                for(Map.Entry<String,ClientHelperThread> client : Server.clientThreads.entrySet()){
                    //On envoie le message à tout le monde sauf à soi même
                    if(!(client.getKey() == username)) {
                        client.getValue().writeAMessage(line);
                    }
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

    public synchronized void killThread(){
        try {
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized void setIsConnected(boolean isConnected){
        this.isConnected = isConnected;
    }

    public synchronized void writeAMessage (String message){
        socOut.println(message);
    }
}