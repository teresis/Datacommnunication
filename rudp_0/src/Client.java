
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.zip.*;

public class Client {
	final static int SEGMENT_SIZE = 518; 
    final static int dataSize = 496; 
	static RcvThread rcThread; 
	public static Signaling p = new Signaling(); // ack signaling class
	static Timeout tclick; // timeout class
    static boolean sem = true;
    static LLC llc =new LLC();
    public static void main(String args[]) {
        byte[] data;

        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

        try {
        	System.out.print("Enter target Localhost name : ");
			String localhost = in.readLine();  
			System.out.print("Enter target Port No : ");
			int port = Integer.parseInt(in.readLine()); 
			
			InetAddress inetaddr = InetAddress.getByName(localhost); //InetAddress 
			InetAddress me = InetAddress.getLocalHost();
			/*NetworkInterface netif = NetworkInterface.getByInetAddress(inetaddr);
			byte[] mac= new byte[6];
			mac=netif.getHardwareAddress();*/
			NetworkInterface netif = NetworkInterface.getByInetAddress(me);
			byte[] mac= new byte[6];
			mac=netif.getHardwareAddress();
			llc.setSaddr(mac);

			
			System.out.println("Target IP Address is : " + inetaddr.getHostAddress());
			System.out.println("Target Port No is : " + port);
			byte[] sabme;
			llc.setCONTROL((byte)0xF6, (byte)0x00, 0);
			llc.setLPDU();
			
			sabme= llc.getLLC();
			System.out.println(byteArrayToHex(sabme));
			DatagramSocket ackSocket = new DatagramSocket();
			
			tclick = new Timeout();
			rcThread = new RcvThread(ackSocket, p);
			rcThread.start();
			 int total_bytes_sent = 0; //keep track of total bytes sent (include retransmitted)
	         int total_transmitted = 0;
            for(int j = 0; j < 3; j++) {
            	DatagramPacket packet = new DatagramPacket(sabme, sabme.length, inetaddr, port);
                ackSocket.send(packet);// send packet
                
               total_transmitted += 1;
               total_bytes_sent += sabme.length;
                if (j == 0)
                	System.out.println("\nPacket is Sent! (Sequence #" + getSeqNum(sabme) + ")" + "\n(Wait for ACK)");
                
                tclick.Timeoutset((byte)0, 100, p); 
                
                p.waitingACK(); 
            
                if(Signaling.getUA() == true) {

                	tclick.Timeoutcancel((byte)0);
                	Signaling.setUA(false);
                	break;
                }
                else {
                	
                	if(j == 2) {
	               		System.out.println("Transmission Failed!! Exit Program.");
	               		System.exit(-1);
	               	}	
                	
                	else {
		               	System.out.println("\nRetransmit Packet (Sequence #" + getSeqNum(sabme) + ")" + "\n(Wait for ACK)");
                	}
                }	
            } 
            netif =  NetworkInterface.getByInetAddress(inetaddr);
            mac = netif.getHardwareAddress();
            llc.setDaddr(mac);
			while(sem) {
				System.out.print("Input Data : "); 
				String input;
				input = in.readLine();
				
				byte[] byteArrayWhole = input.getBytes();
	            int len = byteArrayWhole.length; // determine file size
	            int numOfPacks = (len / dataSize) + 1; //determine number of packets needed
	            
	            int lastPackBodyLength = 1;

	            int bytesRead = 0;
	            //keep track of total transmitted number
	            for (int i = 0; i < numOfPacks; i++) {
	            	// not last packet
	                if (i != numOfPacks - 1) {
	                    data = new byte[dataSize]; // create a byte array of len=BODY_SIZE
	                    data = divideArray(byteArrayWhole, bytesRead, bytesRead + dataSize);
	                    bytesRead = bytesRead + dataSize;
	                }
	                // if last packet, adjust packet size
	                else {
	                    if(i == 0) {
	                    	lastPackBodyLength = len;
	                    }
	                    else {
	                    	lastPackBodyLength = len % bytesRead;
	                    }
	                    data = new byte[lastPackBodyLength];
	                    data = divideArray(byteArrayWhole, bytesRead, bytesRead + (lastPackBodyLength));
	                    bytesRead = bytesRead + (lastPackBodyLength);
	                }
	                llc.setCONTROL((byte)i, (byte)i, 0);
	                llc.setINFORM(data);
	                llc.setLPDU();
	             

	                byte[] wholePacket =llc.getLLC();
	                System.out.println("send to server\n"+ byteArrayToHex(wholePacket));
	                Timeout.timeoutcount = 0;//reset retry count to 0 for each packet
	                total_bytes_sent =0;
	         
	                for(int j = 0; j < 10; j++) {
	                	DatagramPacket packet = new DatagramPacket(wholePacket, wholePacket.length, inetaddr, port);
		                ackSocket.send(packet);// send packet
		                
		                total_transmitted += 1;
		                total_bytes_sent += wholePacket.length;
		                if (j == 0)
		                	System.out.println("\nPacket is Sent! (Sequence #" + getSeqNum(wholePacket) + ")" + "\n(Wait for ACK)");
		                
		                tclick.Timeoutset(getSeqNum(wholePacket), 100, p); 
		                
		                p.waitingACK(); 
		                

		                if(Signaling.getACK() == true) {
		                	
		                	tclick.Timeoutcancel(getSeqNum(wholePacket));
		                	Signaling.setACK(false);

		                	break;
		                }
		                
		                else {
		                	
		                	if(j == 9) {
			               		System.out.println("Transmission Failed!! Exit Program.");
			               		System.exit(-1);
			               	}	
		                	
		                	
		                	if(Signaling.getNAK() == true) {
		                		tclick.Timeoutcancel(getSeqNum(wholePacket));
		                		System.out.println("\nRetransmit Packet (Sequence #" + getSeqNum(wholePacket) + ")" + "\n(Wait for ACK)");
		                		
		                		Signaling.setNAK(false);
		                	}
		                	
		               
		                	else {
				               	System.out.println("\nRetransmit Packet (Sequence #" + getSeqNum(wholePacket) + ")" + "\n(Wait for ACK)");
		                	}
		                }	
	                }            
	                   
	                //if last ACK received, delivery completed
	                if (i == numOfPacks - 1) {
	                	System.out.println("\nDelivery Completed Successfully");
                        System.out.println("Total Data Size: " + len + " Bytes");
                        System.out.println("Total Sent Segments: " + numOfPacks);
                        System.out.println("Total Tramsmission Time: " + total_transmitted);
                        System.out.println("Total Bytes Attempted including Retransmission: " + total_bytes_sent + "Bytes");
                        System.out.println();
	                }
	            }//end for
			}
			ackSocket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void graceout(){
		sem = false;
	}
        
    // method to combine two byte arrays of length 2 each
    public static byte[] combineTwoByteArrays(byte[] first, byte[] second) {
        byte[] combined = new byte[first.length + second.length];

        for (int i = 0; i < combined.length; ++i) {
            combined[i] = i < first.length ? first[i] : second[i - first.length];
        }
        return combined;
    }
    //divide byte array from start index(inclusive) to end index(exclusive)
    public static byte[] divideArray(byte[] wholeArray, int start, int end) {
        byte[] arr = new byte[end - start];
        arr = Arrays.copyOfRange(wholeArray, start, end);
        return arr;
    }
    // method to get seq number from packet
    public static byte getSeqNum(byte[] h) {
        byte seqNum = h[16];
        return seqNum;
    }    
    // method go get checksum value from packet
    public static int getCheckSumValue(byte[] h) {
    	int cksum = (fourByteArrayToInt(Arrays.copyOfRange(h, h.length-4, h.length)));
    	return cksum;
    }
    // check whether received packet is corrupted through checksum value
    public static boolean isCorrupted(byte[] seg) {
		int cksum = getCheckSumValue(seg);
        byte[] arr = Arrays.copyOfRange(seg, 0, seg.length-4);
        CRC32 checkSum = new CRC32();
        checkSum.update(arr);
        int newcksum = (int) checkSum.getValue();
        if(cksum == newcksum) {
        	return false;
        }
        else {
        	return true;
        }
	}
    // method to convert byte array length 2 to integer
    public static int twoByteArrayToInt(byte[] b) {
        return b[1] & 0xFF
                | (b[0] & 0xFF) << 8;
    }

    // method to convert byte array length 4 to integer
    public static int fourByteArrayToInt(byte[] b) {
        return b[3] & 0xFF
                | (b[2] & 0xFF) << 8
                | (b[1] & 0xFF) << 16
                | (b[0] & 0xFF) << 24;
    }
    static String byteArrayToHex(byte[] a)
   	{
   		StringBuilder sb = new StringBuilder();
   		for (final byte b : a)
   			sb.append(String.format("%02x ", b & 0xff));
   		return sb.toString();
   	}
}

