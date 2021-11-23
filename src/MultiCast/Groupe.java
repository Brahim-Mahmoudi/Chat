package MultiCast;


import java.io.*;
import java.net.*;
import java.util.HashSet;
import java.util.Set;

public class Groupe {

    //Nom de l'utilisateur qui rentre dans le groupe
    static String nomUtilisateur;

    //Boolean qui passe à true quand l'utilisateur quitte le groupe
    static boolean quitteLeGroupe = false;

    static String port= null;

    static Set<String[]> users = new HashSet<>(); //<NomUser,Port>

    public static void main(String[] args) throws IOException {

        try {

            //ETAPE 1 : Gestion des arguments en entrée du main

            //Lecture du nom d'utilisateur
            System.out.println("Bonjour, veuillez entrer votre nom d'utilisateur pour vous connecter");
            BufferedReader stdIn = null;
            stdIn = new BufferedReader(new InputStreamReader(System.in));
            String nom = null;

            while(true){
                nom = stdIn.readLine();
                boolean nomDejaPresent = false;
                System.out.println("size : "+users.size());
                for(String[] Object : users){
                    System.out.println("testttt");
                    if (Object[0] == nom && Object[1] == args[0]){
                        System.out.println("Cet utilisateur existe déja dans ce groupe");
                        nomDejaPresent=true;
                    }
                }

                if(nomDejaPresent){
                    System.out.println("Veuillez utiliser un autre nom d'utilisateur ");
                }else{
                    System.out.println("test2");
                    nomUtilisateur = nom;
                    String[] tab = new String[]{nom,args[0]};
                    System.out.println("tab[0] : "+ tab[0] + " "+ tab[1]);
                    users.add(tab);
                    System.out.println("size : "+users.size());
                    break;
                }

            }


            //Le premier argument entré par l'utilisateur au lancement est le numero de port
            int numeroDePort = Integer.parseInt(args[0]);

            //Le deuxième argument entré par l'utilisateur au lancement est l'adresse IP du groupe qu'il veut rejoindre
            InetAddress ipGroupe = InetAddress.getByName(args[1]);
            //Permet de nommer les fichiers historique
            port = args[0];

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



            while(true){
                String message;
                message = stdIn.readLine();

                //Si l'utilisateur tape "deconnexion", c'est qu'il désire quitter le groupe
                if(message.equals("deconnexion")){
                    quitteLeGroupe = true;
                    users.remove(new String[]{nomUtilisateur,port});
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
                fh.write( message);
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
