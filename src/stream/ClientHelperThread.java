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
    private File fichierDeconnexion;
    private PrintWriter ecrireFichierDeco;



    ClientHelperThread(Socket s,String username,BufferedReader socIn,PrintStream socOut) {
        this.clientSocket = s;
        this.username = username;
        this.isConnected = true;
        this.socIn = socIn;
        this.socOut = socOut;
        this.fichierDeconnexion = new File("./src/files/"+username+"Deconnexion.txt");
        try {
            this.ecrireFichierDeco = new PrintWriter(fichierDeconnexion);
        }
        catch(IOException ex){
            ex.printStackTrace();
        }

    }

    /**
     * receives a request from client then sends an echo to the client
     **/
    public void run() {

        try {

            String line;
            while (true) {

                line = socIn.readLine();
                if(line.equals("disconnect")){
                    socOut.println("You are disconnected");
                    isConnected = false;
                }


                if(isConnected) {

                    for (Map.Entry<String, ClientHelperThread> client : Server.clientThreads.entrySet()) {
                        //On envoie le message à tout le monde sauf à soi même
                        if (!(client.getKey() == username) && (client.getValue().isConnected())) {
                            client.getValue().getSocOut().println(username + " :" + line);

                        }

                        else if(!(client.getKey() == username) && !(client.getValue().isConnected())){
                            System.out.println("zebi");

                            FileWriter fw = new FileWriter("./src/files/"+client.getKey()+"Deconnexion.txt",true);
                            fw.write( client.getKey() + " : " +line);
                            fw.write("\n");
                            fw.close();


                        }
                    }

                }

                if(line.equals("connect")){
                    isConnected = true;
                    socOut.println("You are connected");
                    BufferedReader lecteurAvecBuffer = null;
                    String ligne;

                    try
                    {
                        lecteurAvecBuffer = new BufferedReader(new FileReader("./src/files/"+username+"Deconnexion.txt"));
                        while ((ligne = lecteurAvecBuffer.readLine()) != null)
                        {
                            // Afficher le contenu du fichier
                            socOut.println (ligne);
                        }
                        lecteurAvecBuffer.close();
                        fichierDeconnexion.delete();
                        fichierDeconnexion.createNewFile();
                    }
                    catch(FileNotFoundException exc)
                    {
                        System.out.println("Erreur d'ouverture");
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
            this.interrupt();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized void setIsConnected(boolean isConnected){
        this.isConnected = isConnected;
    }



    public synchronized boolean isConnected() {
        return isConnected;
    }

    public PrintStream getSocOut() {
        return socOut;
    }

    public PrintWriter getEcrireFichierDeco() {
        return ecrireFichierDeco;
    }
}