package stream;

/***
 * EchoServer
 * Example of a TCP server
 * Date: 10/01/04
 * Authors:
 */


import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;

public class Server {
    //HashMap contenant les thread crée et le username du client correspondant en clé
    static HashMap<String, ClientHelperThread> clientThreads = new HashMap<>();

    public static void main(String[] args){
        ServerSocket listenSocket;
        ArrayList<Integer> portLibre = new ArrayList<Integer>();


        portLibre.add(1200);
        portLibre.add(1201);
        portLibre.add(1202);
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

                //Creation de la socket avec le constructeur vide car on veut changer son numero de port (bind)
                Socket clientSocket = new Socket();

                //Creation de la socketadress qui permettre de realiser le bind
                SocketAddress socketPourBind = new InetSocketAddress(demandeConnexionCLient.getLocalAddress(),portLibre.get(0));
                clientSocket.bind(socketPourBind);

                //On a utilisé le premier port libre de la liste on le supprime donc car il n'est plus libre
                portLibre.remove(0);


                System.out.println("Connexion from:" + clientSocket.getLocalPort());

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
                    System.out.println("Erreur, il y a dejà un utilisateur avec le même username connecte");
                }

                else{
                    //Creation du thread
                    ClientHelperThread ct = new ClientHelperThread(demandeConnexionCLient,username,socInClient,socOutClient);
                    clientThreads.put(username,ct);
                    System.out.println("Bienvenue " + username);
                    ct.start();
                }


            }


        } catch (Exception e) {
            System.err.println("Error in EchoServerMultiThreaded:" + e);
        }
    }


}


