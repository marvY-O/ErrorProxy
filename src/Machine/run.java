package Machine;
import java.io.*;
import java.net.*;
import java.util.*;

public class run {
    public static void main (String args[]) throws IOException{
    	
    	String clientIP = "127.0.0.1";
        
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface iface = interfaces.nextElement();
                if (iface.isUp() && iface.getName().startsWith("w")) { // filter by WiFi interfaces
                    Enumeration<InetAddress> addresses = iface.getInetAddresses();
                    while (addresses.hasMoreElements()) {
                        InetAddress addr = addresses.nextElement();
                        if (addr.getAddress().length == 4) { // filter IPv4 addresses
                            clientIP = addr.getHostAddress();
                        }
                    }
                }
            }
        } catch (SocketException e) {
            System.out.println("Error getting network interfaces: " + e.getMessage());
        }
        
    	Scanner sc = new Scanner(System.in);
    	System.out.printf("Enter IP Address of the server: ");
    	String ac_address = sc.next();
        //String ac_address = "192.168.1.4";
       
        System.out.printf("Enter port of the server: ");
    	int port = sc.nextInt();
        //int  port = 5000;
        
        Machine m = new Machine(ac_address, port, clientIP);
        m.initiate();
    }
}
