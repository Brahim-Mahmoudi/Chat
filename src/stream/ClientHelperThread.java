
package stream;

import java.io.*;
import java.net.*;
import java.util.Map;

/**
 * La classe permet de gérer un utilisateur, et notemment d'envoyer ses messages aux utilisateurs avec lesquels
 * il souhaite communiquer
 * @author Brahim Mahmoudi, El Yazid Dakhil
 */
public class ClientHelperThread
        extends Thread {

    private Socket clientSocket;
    private String username;
    private boolean isConnected ;
    private  BufferedReader socIn;
    private PrintStream socOut;
    private File fichierDeconnexion;
    private PrintWriter ecrireFichierDeco;


    /**
     * Constructeur de la classe ClientHelperThread
     * @param s, la socket
     * @param username, son nom d'utilisateur
     * @param socIn, le buffer d'entrée de la socket
     * @param socOut, le buffer de sortie de la socket
     */
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
     * Recoit un message de l'utilisateur, rajoute son nom d'utilisateur au debut du message, écrit ce message dans
     * l'historique de la conversation, et l'envoit aux autres utilisateurs.
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
                        if (!(client.getKey() == username) && (client.getValue().isConnected())  ) {
                            client.getValue().getSocOut().println(username + " :" + line);
                        }

                        else if(!(client.getKey() == username) && !(client.getValue().isConnected()) ){
                            //Rempli le fichier durant la deconnexion
                            FileWriter fw = new FileWriter("./src/files/"+client.getKey()+"Deconnexion.txt",true);
                            fw.write( username + " : " +line);
                            fw.write("\n");
                            fw.close();

                        }

                    }
                    //Remplir l'historique
                    FileWriter fh = new FileWriter("./src/files/Historique.txt",true);
                    fh.write( username + " : " +line);
                    fh.write("\n");
                    fh.close();


                }
                //L'utilisateur se reconnect après une déconnexion
                if(line.equals("connect")){
                    isConnected = true;
                    socOut.println("You are connected");
                    BufferedReader lecteurAvecBuffer = null;
                    String ligne;
                    // On lui affiche les messages qu'il n'a pas reçu
                    try
                    {
                        lecteurAvecBuffer = new BufferedReader(new FileReader("./src/files/"+username+"Deconnexion.txt"));
                        while ((ligne = lecteurAvecBuffer.readLine()) != null)
                        {
                            // Afficher le contenu du fichier
                            socOut.println (ligne);
                        }
                        lecteurAvecBuffer.close();
                        //Supprime ce qu'il y avait dans le fichier
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


    /**
     * Permet de recuperer l'attribut isConnected, qui permet de savoir si le client est connecte ou non
     * @return l'attribut isConnected
     */
    public synchronized boolean isConnected() {
        return isConnected;
    }

    /**
     * Permet de recuperer le buffer de sortie de la socket
     * @return le buffer de sortie de la socket
     */
    public PrintStream getSocOut() {
        return socOut;
    }


}