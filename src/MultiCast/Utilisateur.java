package MultiCast;


import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Utilisateur {

    //Nom de l'utilisateur qui rentre dans le groupe
    static String nomUtilisateur;

    //Boolean qui passe à true quand l'utilisateur quitte le groupe
    static boolean quitteLeGroupe = false;

    static String port= null;


    public static void main(String[] args) throws IOException {

        try {

            //ETAPE 1 : Gestion des arguments en entrée du main
            //Le premier argument entré par l'utilisateur au lancement est le numero de port
            int numeroDePort = Integer.parseInt(args[0]);
            port = args[0];

            //Le deuxième argument entré par l'utilisateur au lancement est l'adresse IP du groupe qu'il veut rejoindre
            InetAddress ipGroupe = InetAddress.getByName(args[1]);



            // Cree un fichier qui stocke les noms des utilisateurs du groupe
            try {

                File nomDUtilisateurs = new File("../files/"+port+"NomDUtilisateurs.txt");
                nomDUtilisateurs.createNewFile();
            }
            catch (Exception e) {
                System.err.println(e);
            }

            //Lecture du nom d'utilisateur
            System.out.println("Bonjour, veuillez entrer votre nom d'utilisateur pour vous connecter");
            BufferedReader stdIn = null;
            stdIn = new BufferedReader(new InputStreamReader(System.in));
            String nom = null;
            nom = stdIn.readLine();



            boolean nomDejaPresent = false;

            while(true) {
                // On parcourt le fichier nom d'utilisateur du groupe
                BufferedReader lecteurNomUtilisateurs = null;
                String username;
                lecteurNomUtilisateurs = new BufferedReader(new FileReader("../files/"+port+"NomDUtilisateurs.txt"));
                while ((username = lecteurNomUtilisateurs.readLine()) != null) {
                    // Verifier que le nom d'utilisateur n'existe pas
                    if (username.equals(nom)) {
                        System.out.println("Ce nom d'utilisateur existe déjà, veuillez ressaisir un nom d'utilisateur");
                        nomDejaPresent = true;
                        break;

                    }

                }

                if(nomDejaPresent){
                    //On ressaisit le username
                    nom = stdIn.readLine();
                    nomDejaPresent = false;
                }

                else{
                    //Le username n'existe pas, on peut continuer
                    break;
                }

                lecteurNomUtilisateurs.close();
            }





            //Ajout de l'utilisateur au fichier
            nomUtilisateur = nom;

            FileWriter nouvelUtilisateur = new FileWriter("../files/"+port+"NomDUtilisateurs.txt",true);
            nouvelUtilisateur.write(nomUtilisateur);
            nouvelUtilisateur.write("\n");
            nouvelUtilisateur.close();

            //Creation de la socket multicast
            MulticastSocket socketMulticast = new MulticastSocket(numeroDePort);

            //Creation d'une SocketAdress qui contient les infos du groupe (ip et numero de port), qui sera utilisé plus tard
            SocketAddress adressGroup = new InetSocketAddress(ipGroupe, numeroDePort);

            //On rejoint le groupe
            socketMulticast.joinGroup(ipGroupe);

            //ETAPE 2: Creation du thread qui permet de lire les messages reçuent
            Thread lecture = new Thread(new ThreadLecture(socketMulticast,adressGroup));
            lecture.start();

            //ETAPE 3: Gestion de l'envoie des messages
            System.out.println("Vous êtes connecté au groupe, vous pouvez maintenant envoyer des messages");

            try {

                // Cree un fichier d'historique par groupe
                File f = new File("../files/"+port+"Historique.txt");
                f.createNewFile();
            }
            catch (Exception e) {
                System.err.println(e);
            }



            // ON AFFICHE L'Historique
            BufferedReader lecteurAvecBuffer = null;
            String ligne;
            // On lui affiche les messages qu'il n'a pas reçu


            lecteurAvecBuffer = new BufferedReader(new FileReader("../files/"+port+"Historique.txt"));
            while ((ligne = lecteurAvecBuffer.readLine()) != null)
            {
                // Afficher le contenu du fichier
                System.out.println(ligne);
            }
            lecteurAvecBuffer.close();


            //Gestion des messages envoyés
            while(true){
                String message;
                message = stdIn.readLine();

                //Si l'utilisateur tape "deconnexion", c'est qu'il désire quitter le groupe
                if(message.equals("deconnexion")){
                    quitteLeGroupe = true;

                    //On cherche à supprimer le nom de l'utilisateur qui se deconnecte du fichier qui stocke les noms d'utilisateurs du groupe
                    ArrayList<String> contenueFichierNomUtilisateur = new ArrayList<String>();
                    // On parcourt le fichier nom d'utilisateur du groupe
                    BufferedReader lecteurFichierNomUtilisateurs = null;
                    String ligneFichierNomUtilisateurs;
                    lecteurFichierNomUtilisateurs = new BufferedReader(new FileReader("../files/"+port+"NomDUtilisateurs.txt"));

                    //Parcourt du fichier
                    while ((ligneFichierNomUtilisateurs = lecteurFichierNomUtilisateurs.readLine()) != null) {

                        if (!(ligneFichierNomUtilisateurs.equals(nomUtilisateur))) {
                            contenueFichierNomUtilisateur.add(ligneFichierNomUtilisateurs);
                        }
                    }

                    lecteurFichierNomUtilisateurs.close();

                    //On ecrase le fichier avec le contenue de la liste dans laquelle on a stocke tous les nom d'utilisateurs sauf celui du user qui se deconnecte

                    FileWriter ecraseFichierNomUtilisateurs = new FileWriter("../files/"+port+"NomDUtilisateurs.txt",false);
                    for(String nomDUtilisateur : contenueFichierNomUtilisateur){
                        ecraseFichierNomUtilisateurs.write(nomDUtilisateur);
                        ecraseFichierNomUtilisateurs.write("\n");
                    }

                    ecraseFichierNomUtilisateurs.close();
                    socketMulticast.leaveGroup(ipGroupe);
                    socketMulticast.close();
                    break;

                }

                //On ajoute le nom d'utilisateur de la personne qui envoie le message pour l'afficher à la personne qui le recoit
                message = "-"+nomUtilisateur + ": " + message;
                byte[] buffer = message.getBytes();
                DatagramPacket bufferDEcriture = new
                        DatagramPacket(buffer,buffer.length,adressGroup);

                //On envoie le message dans le socket à tout les membre du groupe
                socketMulticast.send(bufferDEcriture);
                //Remplir l'historique
                FileWriter fh = new FileWriter("../files/"+port+"Historique.txt",true);
                fh.write(message);
                fh.write("\n");
                fh.close();
            }



        }

        catch(IOException ioException){
                ioException.printStackTrace();

        }



    }

    static class ThreadLecture implements Runnable{
        private MulticastSocket socketMulticast;
        private SocketAddress adressGroup;
        private static final int LONGUEUR_MAX = 1000;

        public ThreadLecture(MulticastSocket socketMulticast, SocketAddress adressGroup) {
            this.socketMulticast = socketMulticast;
            this.adressGroup = adressGroup;
        }


        @Override
        public void run() {

            //Tant que l'utilisateur est encore dans le groupe
            while (!quitteLeGroupe) {
                byte[] buffer = new byte[LONGUEUR_MAX];
                DatagramPacket bufferDeLecture = new
                        DatagramPacket(buffer, buffer.length, adressGroup);
                String message;
                try {
                    socketMulticast.receive(bufferDeLecture);
                    message = new
                            String(buffer, 0, bufferDeLecture.getLength(), "UTF-8");

                    //On ne reaffiche pas les messages que l'on a envoyé
                    if (!message.startsWith("-"+nomUtilisateur))
                        System.out.println(message);

                } catch (IOException ioException) {
                    System.out.println("Vous êtes déconnecté");
                }


            }

        }


    }
}
