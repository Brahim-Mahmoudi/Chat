package stream;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.net.UnknownHostException;

public class Conversation {

    private Socket client1;
    private Socket client2;

    public Conversation(Socket client1, Socket client2) {
        this.client1 = client1;
        this.client2 = client2;

    }

    public void lancerConversation(){
        try {
            //Buffer du client 1
            BufferedReader socInClient1 = null;
            socInClient1 = new BufferedReader(
                    new InputStreamReader(client1.getInputStream()));
            PrintStream socOutClient1 = new PrintStream(client1.getOutputStream());

            //Buffer du client 2
            BufferedReader socInClient2 = null;
            socInClient2 = new BufferedReader(
                    new InputStreamReader(client2.getInputStream()));
            PrintStream socOutClient2 = new PrintStream(client2.getOutputStream());

            while(true){
                String line1 = socInClient1.readLine();
                socOutClient2.println(line1);
                String line2 = socInClient2.readLine();
                socOutClient1.println(line2);

            }
        }
        catch (Exception e) {
            System.err.println("Error in Conversation:" + e);
        }
    }


}
