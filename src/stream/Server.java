package stream;

/***
 * EchoServer
 * Example of a TCP server
 * Date: 10/01/04
 * Authors:
 */


import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

public class Server {
    //HashMap contenant les thread crée et le username du client correspondant en clé
    static HashMap<String, ClientHelperThread> clientThreads = new HashMap<>();
    static PrintWriter ecrireFichierHisto;

    static {
        try {
            ecrireFichierHisto = new PrintWriter("./src/files/Historique.txt");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args){
        ServerSocket listenSocket;

        if (args.length != 1) {
            System.out.println("Usage: java EchoServer <EchoServer port>");
            System.exit(1);
        }
        try {
            //Creation de la socket "principale" du serveur
            listenSocket = new ServerSocket(Integer.parseInt(args[0])); //port


            System.out.println("Server ready...");
            while (true) {

                //Recuperation demande de connexion du client
                Socket demandeConnexionCLient = listenSocket.accept();



                //Buffer de lecture de la socket
                BufferedReader socInClient = null;
                socInClient = new BufferedReader(
                        new InputStreamReader(demandeConnexionCLient.getInputStream()));

                //Buffer de sortie de la socket
                PrintStream socOutClient = null;
                socOutClient = new PrintStream(demandeConnexionCLient.getOutputStream());

                //On recupere le username de l'utilisateur
                String username = socInClient.readLine();

                //Si mon client existe déjà
                if(clientThreads.containsKey(username)){
                    socOutClient.println("Erreur, il y a dejà un utilisateur avec le même username connecte");
                }

                else{
                    //Creation du thread
                    ClientHelperThread ct = new ClientHelperThread(demandeConnexionCLient,username,socInClient,socOutClient);
                    clientThreads.put(username,ct);
                    System.out.println("Connexion from:" + demandeConnexionCLient.getLocalPort());
                    System.out.println("Bienvenue " + username);
                    ct.start();

                    // On lui affiche les messages de l'historique
                    String ligne;
                    try
                    {
                        BufferedReader lecteurAvecBuffer = new BufferedReader(new FileReader("./src/files/Historique.txt"));
                        while ((ligne = lecteurAvecBuffer.readLine()) != null)
                        {
                            // Afficher le contenu du fichier

                            ct.getSocOut().println (ligne);
                        }
                        lecteurAvecBuffer.close();
                    }
                    catch(FileNotFoundException exc)
                    {
                        System.out.println("Erreur d'ouverture");
                    }
                }


            }


        } catch (Exception e) {
            System.err.println("Error in EchoServerMultiThreaded:" + e);
        }
    }


}


