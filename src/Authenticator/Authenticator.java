package Authenticator;
import java.io.IOException;
import java.io.*;
import java.net.*;
import java.util.*;
import Message.Packet;
import Database.*;

public class Authenticator {
    int port;
    HashMap<InetAddress, Queue<Packet>> buffer;
    HashMap<InetAddress, String> certIDStore;
    String serverIP;
    
    public Authenticator(int port, String serverIP) {
        this.port = port;
        buffer = new HashMap<InetAddress, Queue<Packet>>();
        certIDStore = new HashMap<InetAddress, String>();
        this.serverIP = serverIP;
    }

    public void start() throws IOException{
        ServerSocket ss = new ServerSocket(port);
        System.out.printf("Server started at %s:%d\n", serverIP, port);
        while (true){
            Socket s = null; 
            try {
                s = ss.accept();
                System.out.printf("A new client is connected : %s\n", (String) s.getInetAddress().getHostAddress());
                //System.out.println();
                
                synchronized (buffer) {
                	buffer.put(s.getInetAddress(), new LinkedList<Packet>());
                }
                
                ClientHandler clientNew = new ClientHandler(s, buffer, certIDStore, serverIP);
                
                Thread t = new Thread(clientNew);
                t.start();                  
                
            }
            catch (Exception e){
                s.close();
                e.printStackTrace();
            }
        }
    }

}
    

