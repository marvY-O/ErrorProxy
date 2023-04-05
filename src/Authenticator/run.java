package Authenticator;
import java.io.IOException;
import java.net.*;
import java.util.*;

public class run {
    public static void main(String args[]) throws IOException{
        int ac_port = 5000;
        String serverIP = "127.0.0.1";

        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface iface = interfaces.nextElement();
                if (iface.isUp() && iface.getName().startsWith("w")) { // filter by WiFi interfaces
                    Enumeration<InetAddress> addresses = iface.getInetAddresses();
                    while (addresses.hasMoreElements()) {
                        InetAddress addr = addresses.nextElement();
                        if (addr.getAddress().length == 4) { // filter IPv4 addresses
                            serverIP = addr.getHostAddress();
                        }
                    }
                }
            }
        } catch (SocketException e) {
            System.out.println("Error getting network interfaces: " + e.getMessage());
        }
        
        Authenticator ac = new Authenticator(ac_port, serverIP);
        ac.start();
        
    }
}
//java --module-path bin -cp sqlite-jdbc-3.41.0.0.jar -m Authenticator.run 

