package stream;

/***
 * EchoServer
 * Example of a TCP server
 * Date: 10/01/04
 * Authors:
 */


import java.io.*;
import java.net.*;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.HashMap;

public class EchoServerMultiThreaded  {

    public static void main(String[] args){
        ServerSocket listenSocket;
        ArrayList<Integer> portLibre = new ArrayList<Integer>();
        HashMap<String,ClientThread> clientThreads = new HashMap<String,ClientThread>();

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

                //Creation du thread
                ClientThread ct = new ClientThread(demandeConnexionCLient);
                ct.start();

                //Buffer de lecture de la socket pour recuperer le username
                BufferedReader socInClient = null;
                socInClient = new BufferedReader(
                        new InputStreamReader(demandeConnexionCLient.getInputStream()));

                //On recupere le username de l'utilisateur
                String username = socInClient.readLine();
                if(clientThreads.containsKey(username)){
                    System.out.println("Error, this user already exist");

                    PrintStream socOut= new PrintStream(demandeConnexionCLient.getOutputStream());
                    socOut.println("Fail");
                }else{
                    //On rajoute le thread à la liste des threads en cours
                    clientThreads.put(username,ct);
                    System.out.println(username);
                }




                if(clientThreads.size() > 1){

                    Conversation conversation = new Conversation(clientThreads.get("zebi").getClientSocket(),clientThreads.get("brams").getClientSocket());
                    conversation.lancerConversation();
                }



            }


        } catch (Exception e) {
            System.err.println("Error in EchoServerMultiThreaded:" + e);
        }
    }


}


