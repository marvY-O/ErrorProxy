package Authenticator;
import java.io.*;
import Database.*;
import Message.*;
import java.net.*;
import java.util.HashMap;
import java.util.Queue;
import java.security.SecureRandom;
import java.util.Random;
import java.math.*;

class ClientHandler implements Runnable {
    
    Socket s;
    public HashMap<InetAddress, Queue<Packet>> buffer;
    public HashMap<InetAddress, String> certIDStore;
	String serverIP;
	Boolean introduceErrors;
      
    public ClientHandler(Socket sc, HashMap<InetAddress, Queue<Packet>> buffer, HashMap<InetAddress, String> certIDStore, String serverIP, Boolean introduceErrors) throws IOException{
        s = sc;
        this.buffer = buffer;
        this.certIDStore = certIDStore;
		this.serverIP = serverIP;
		this.introduceErrors = introduceErrors;
    }

    public static String generate() {
    	String ALPHABET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        SecureRandom RANDOM = new SecureRandom();
        StringBuilder sb = new StringBuilder(10);
        for (int i = 0; i < 10; i++) {
            sb.append(ALPHABET.charAt(RANDOM.nextInt(ALPHABET.length())));
        }
        return sb.toString();
    }

    public static void randomlyManipulateBytes(byte[] byteArray, int numBytesToManipulate) {
    	Random random = new Random();
      
        for (int i = 0; i < numBytesToManipulate; i++) {
            int randomIndex = random.nextInt(byteArray.length);
            byteArray[randomIndex] = (byte) random.nextInt(256);
        }
    }
  
    @Override
    public void run() {
    	ObjectOutputStream oos;
    	ObjectInputStream ois;

        try{
    		
    		oos = new ObjectOutputStream(s.getOutputStream());
    		ois = new ObjectInputStream(s.getInputStream());
    		
    		SecurityCertificate cert;
    		while (true) {
    			try {
    				cert = (SecurityCertificate) ois.readObject();
    				if (cert != null) break;
    			} catch(IOException e) {
    				
    			} catch(ClassNotFoundException e) {
    				
    			}
    		}
    		
    		if (dbUsers.verify(cert.username, cert.password)) {
    			cert.CertificateID = generate();
    			certIDStore.put(s.getInetAddress(), cert.CertificateID);
    		}
    		else {
    			cert.CertificateID = "NULL";
    		}

			System.out.printf("%s -> %s\n", s.getInetAddress().getHostAddress(), cert.CertificateID);
    		
    		oos.writeObject(cert);
    		
    		Runnable receiver = new Runnable() {
                @Override
                public void run() {

                	while (true) {
                			Packet p;
							try {
								p = (Packet) ois.readObject();
								//if (p.destination_ip == InetAddress.getLocalHost().getHostAddress());
								if (p.pkt_id == -1){
									System.out.printf("%s sending file to %s\n", p.client_ip, p.destination_ip);
									dbUsers.addEntry(p.client_ip, p.destination_ip, p.payload.length);
								}
								else if (p.pkt_id == -2) {
									System.out.printf("Fetch request of %s from %s to %s\n", p.msg_name, p.client_ip, p.destination_ip);
								}
								InetAddress destAddr = InetAddress.getByName(p.destination_ip);
								if (!p.cert_id.equals(certIDStore.get(s.getInetAddress()))){
									System.out.println("FALSE SECURITY CERTIFICATE ID!!");
									continue;
								}
								// p.client_ip = serverIP;
								synchronized (buffer) {
									if (buffer.containsKey(destAddr)) {
										buffer.get(destAddr).add(p);
									}
								}
							} catch (ClassNotFoundException e) {
								// TODO Auto-generated catch block
								//e.printStackTrace();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								//e.printStackTrace();
							}
                	}
                	
                }
            };
            
            Runnable sender = new Runnable() {
                @Override
                public void run() {

                	while (true) {
                		if (buffer.get(s.getInetAddress()).size() == 0) continue;
                		Packet curPacket;
                		synchronized (buffer) {
	            			curPacket = buffer.get(s.getInetAddress()).peek();
	            			buffer.get(s.getInetAddress()).poll();
                		}
            			try {
            				curPacket.cert_id = certIDStore.get(s.getInetAddress());
            				if (curPacket.pkt_id >= 0 && introduceErrors) {
            					Random random = new Random();
            					int mbits = random.nextInt((int)Math.ceil(curPacket.payload.length * 0.3));
            					System.out.printf("Manipulated %d bytes\n", mbits);
            					randomlyManipulateBytes(curPacket.payload, mbits);
            				}
            				oos.writeObject(curPacket);
            			} catch(IOException e) {
            				e.printStackTrace();
            			}
                		
                	}
                	
                }
            };
            
            
            Thread recv = new Thread(receiver);
            recv.start();
            
            Thread send = new Thread(sender);
            send.start();
             
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }
       
    
                
}