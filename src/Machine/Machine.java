package Machine;
import java.io.*;  
import java.util.*;
import java.net.*; 
import Message.*; 
//import org.apache.commons.lang3.ArrayUtils;

public class Machine{
    //private InetAddress selfAddress;
    String ac_address, clientIP;
    int ac_port;
    Queue<Packet> buffer;
    Queue<Packet> receiveBuffer;

    public Machine(String ac_address, int ac_port, String clientIP) throws IOException{
        //selfAddress  = Binder.getAddress();
        this.ac_address = ac_address;
        this.clientIP = clientIP;
        this.ac_port = ac_port;
        this.buffer = new LinkedList<Packet>();
        this.receiveBuffer = new LinkedList<Packet>();
    }
    
    public static byte[] readFile(String filePath) throws IOException {
        File file = new File(filePath);
        if (!file.exists()) {
        	return null;
            //throw new FileNotFoundException("File " + filePath + " does not exist so creating new one!");
        }
        byte[] buffer = new byte[(int) file.length()];
        try (FileInputStream fis = new FileInputStream(file)) {
            fis.read(buffer);
        }
        return buffer;
    }
    
    public static int hammingDistance(byte[] a, byte[] b) {
        if (a.length != b.length) {
            throw new IllegalArgumentException("Byte arrays must be of equal length");
        }
        int distance = 0;
        for (int i = 0; i < a.length; i++) {
            int xor = a[i] ^ b[i];
            for (int j = 0; j < 8; j++) {
                distance += (xor >> j) & 1;
            }
        }
        return distance;
    }

    public void initiate() throws IOException{
    	Scanner sc = new Scanner(System.in);
        try{

            Socket s = new Socket(ac_address, ac_port);
            
            ObjectInputStream ois = new ObjectInputStream(s.getInputStream());
            ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());
            System.out.printf("Connected to server %s:%d\n", ac_address, ac_port);
            
            
            String certID;
            SecurityCertificate cert = new SecurityCertificate();
            
            System.out.printf("Enter username: ");
            cert.username = sc.next();
            
            System.out.printf("Enter password: ");
            cert.password = sc.next();
            
            oos.writeObject(cert);
            
            //SecurityCertificate cert;
    		while (true) {
    			try {
    				cert = (SecurityCertificate) ois.readObject();
    				if (cert != null) break;
    			} catch(IOException e) {
    				
    			} catch(ClassNotFoundException e) {
    				
    			}
    		}
    		
    		if (cert.CertificateID.equals("NULL")) {
    			System.out.println("Wrong Credentials provided!!");
    			return;
    		}	
            
    		certID = cert.CertificateID;
    		System.out.printf("Security ID: %sn\n",certID);
            
            System.out.printf("1.Get file\n2.Idle state\n\n>>");
            int x = sc.nextInt();

            if (x == 1) {
            	Packet first = new Packet(0);
            	String path, destIP;
                
                System.out.printf("Name file to fetch: ");
                path = sc.next();
                //path = "test.jpg";
                
                System.out.printf("IP of Destination: ");
                destIP = sc.next();
                
                first.cert_id = certID;
                first.client_ip = clientIP;
                first.destination_ip = destIP;
                first.pkt_id = -2;
                first.pkt_no = -2;
                first.msg_name = path;
                
                oos.writeObject(first);
                
                System.out.printf("Waiting for file..");
                
            	
				Runnable receivePackets = new Runnable() {
					@Override
					public void run() {
						Packet p;
						int totalPkts = 0;
						int byteLength = 0;

						while (true) {
							try {
								p = (Packet) ois.readObject();
								if (p != null) {
									if (!p.cert_id.equals(certID)) {
										System.out.println("SECURITY CERTIFICATE MISMATCH!\n");
										return;
									}
									if (p.pkt_id == -1) {
										totalPkts = Integer.parseInt(p.msg_name);
										byteLength = p.pkt_no;
										System.out.println("Ready to receive "+totalPkts+" from "+p.client_ip);
									}
									else if (p.pkt_id == totalPkts) {
										buffer.add(p);
										System.out.printf("Received %d packets from %s\n",totalPkts, p.client_ip);
										break;
									}
									else {
										int cnt = Math.round(p.pkt_id*20/totalPkts);
										String cur = "|";
										for (int i=0; i<20; i++) {
											if (i < cnt) {
												cur += "=";
											}
											else if (i == cnt) {
												cur += ">";
											}
											else {
												cur += " ";
											}
										}
										cur +="|" + p.pkt_id + "/" + totalPkts + "\r";
										System.out.printf(cur);
										
										receiveBuffer.add(p);
									}
								}
								
								
							} catch (ClassNotFoundException e) {
								System.out.printf("Error reading packets (Undefined Format): ");
								e.printStackTrace();
								try {
									oos.close();
									ois.close();
									s.close();
								} catch (IOException e1) {
									System.out.printf("Error closing connection: ");
									e1.printStackTrace();
								}
							} catch (IOException e) {
								System.out.printf("Error receiving packets!!");
								e.printStackTrace();
								try {
									oos.close();
									ois.close();
									s.close();
								} catch (IOException e1) {
									System.out.printf("Error closing connection: ");
									e1.printStackTrace();
								}
								
							}
						}

						String outputPath = receiveBuffer.peek().msg_name; 
						
						System.out.printf("Received "+outputPath+" from "+receiveBuffer.peek().client_ip+"\n");
						
					   byte[] byteFile = new byte[byteLength];
					   int i = 0;
					   int j = 0;
					   while (!receiveBuffer.isEmpty()){
						   Packet pkt = receiveBuffer.poll();
						   if (pkt.pkt_id != j) {
							   receiveBuffer.add(pkt);
							   continue;
						   }
						   for (int j=0; j<pkt.payload.length && i<byteLength; j++){
							   byteFile[i++] = pkt.payload[j];
						   }
						   j++;
					   }
					   
					   byte[] orgFile = null;
					   
					   try {
						   orgFile = readFile(outputPath);
					   } catch(IOException e) {
						   e.printStackTrace();
					   }
					   
					   if (orgFile != null) {
						   //check dist;
						   int hd = hammingDistance(orgFile, byteFile);
						   System.out.printf("Hamming Distance = %d\n\n", hd);
						   String baseName = outputPath.substring(0, outputPath.lastIndexOf('.'));
						   String extension = outputPath.substring(outputPath.lastIndexOf('.'));
						   outputPath = baseName + Integer.toString(2) + extension;
					   }
					   
						try {
							FileOutputStream fos = new FileOutputStream(outputPath);
							fos.write(byteFile);
							fos.close();
							System.out.printf("File Saved to disk!\n");
						}
						catch(IOException e) {
							System.out.println("Error Saving file to disk!");
						}
					   
					}
				};
				
            	Thread receivePacketsThread = new Thread(receivePackets);
                receivePacketsThread.start();
                
//                System.out.println("Listening for packets..");
				return;
            }
            else if (x == 2) {
//            	String path, destIP;
//                
//                System.out.printf("Name file to send: ");
//                path = sc.next();
//                //path = "test.jpg";
//                
//                System.out.printf("IP of Destination: ");
//                destIP = sc.next();
//                //destIP = "192.168.1.27";
//                
            	
            	Packet first = (Packet) ois.readObject();
            	String path = first.msg_name;
            	String destIP = first.client_ip;
            	
                byte[] file;
                File fileobj = new File(path);

                try {
                    FileInputStream fis = new FileInputStream(fileobj);
                    file = new byte[(int) fileobj.length()];
                    fis.read(file);
                    fis.close();
                }
                catch(IOException e) {
                    System.out.print("Exception while opening file: ");
                    e.printStackTrace();
                    ois.close();
                    oos.close();
                    s.close();
                    return;
                }
                
                int pyld_size = 1000;
                final int pkt_total = file.length/pyld_size + (file.length%pyld_size == 0 ? 0 : 1);

                System.out.printf("\nMessage Size: %d Payload Size: %d Total Packets: %d\n\n",file.length, pyld_size, pkt_total);

        		int index = 0;
        		Packet initPkt = new Packet(0);
        		initPkt.destination_ip = destIP;
        		initPkt.client_ip = clientIP;
        		initPkt.msg_name = Integer.toString(pkt_total);
        		initPkt.pkt_id = -1;
        		initPkt.pkt_no = file.length;
        		initPkt.cert_id = certID;
        		buffer.add(initPkt);
        		
                for (int i=0; i<pkt_total; i++){
                    Packet pkt= new Packet(pyld_size);
                    pkt.client_name = "localhost";
                    pkt.client_ip = clientIP;
                    pkt.destination_ip = destIP;
                    pkt.pkt_no = i+1;
                    pkt.pkt_id = i+1;
                    pkt.msg_name = path;
                    pkt.cert_id = certID;
                    int j=0;
                    for (; j<pyld_size && index<file.length; j++){
                        pkt.payload[j] = (byte) file[index];
                        index++;
                    }
                    if (index==file.length && j<pyld_size){
                        pkt.payload[j] = (byte) '\0';
                    }
                    buffer.add(pkt);
                }

        		int totalPkts = 0;
        		while (true) {
            		
            			if (!buffer.isEmpty()) {
            				try {
            					Packet p = buffer.peek();
            					oos.writeObject(buffer.poll());
            					if (p.pkt_id == -1) {
            						totalPkts = Integer.parseInt(p.msg_name);
            						System.out.printf("Sending %d packets to %s\n", totalPkts, p.destination_ip);
            					}
            					else if (p.pkt_id == totalPkts) {
            						System.out.printf("Sent %d packets to %s\n",totalPkts, p.destination_ip);
									break;
            					}
            					else {
        							int cnt = Math.round(p.pkt_id*20/totalPkts);
        							String cur = "|";
        				            for (int i=0; i<20; i++) {
        				    			if (i < cnt) {
        				                    cur += "=";
        				    			}
        				    			else if (i == cnt) {
        				                    cur += ">";
        				    			}
        				    			else {
        				                    cur += " ";
        				    			}
        				    		}
        				            cur +="|" + p.pkt_id + "/" + totalPkts + "\r";
        				    		System.out.printf(cur);
        							
            					}
            					//Thread.sleep(1);
            				}
            				catch (IOException e) {
            					System.out.printf("Error sending packets: ");
            					e.printStackTrace();
            		            
            		            try {
									s.close();
									ois.close();
	            		            oos.close();
								} catch (IOException e1) {
									System.out.printf("Error closing connection: ");
									e1.printStackTrace();
								}
            				} 
            			}
            		
        		}
            }
                		

        }catch(Exception e){
        	System.out.printf("There was an error connecting to the server: ");
            e.printStackTrace();
        }
    }
}
