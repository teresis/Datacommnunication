
import java.io.*;
import java.util.*;
import java.util.zip.CRC32;
import java.net.*;

public class Server {
    final static int SEGMENT_SIZE = 518;
    final static int headerSize = 16;
    final static int dataSize = 496;
    static InetAddress[] remoteaddr = new InetAddress[2];
    static int[] remoteport = {0, 0, 0};
    static boolean sem = true;
    static LLC llc= new LLC();
    public static void main(String args[]) {
    	
        String data;
        //int windowSize = 8;
        
        BufferedReader input = new BufferedReader(new InputStreamReader(System.in));

        try {
        	System.out.print("Enter Your Port No : ");
			int port = Integer.parseInt(input.readLine()); 
			System.out.print("If you want to test TIMEOUT, Enter Server Latency Time (ms) (If not, enter 0): ");
			int latency = Integer.parseInt(input.readLine()); 
			
            // create segment size array;
            byte[] seg = new byte[SEGMENT_SIZE];
            DatagramPacket dpack = new DatagramPacket(seg, seg.length); 
            DatagramSocket dsock = new DatagramSocket(port); 
            InetAddress inetaddr = InetAddress.getLocalHost();
            
            NetworkInterface netif = NetworkInterface.getByInetAddress(inetaddr);
            byte[] mac = new byte[6];
            mac = netif.getHardwareAddress();
            llc.setSaddr(mac);
            
            DatagramPacket sendPacket = null;
            
         
      
            System.out.println("Your Host name is : " + inetaddr.getHostName());
			System.out.println("Your IP Address is : " + inetaddr.getHostAddress());
			System.out.println("Your Port No is : " + dsock.getLocalPort());
			System.out.println("Server is Running..");
			
            while (sem) {
                dsock.receive(dpack); // listen for packets
                NetworkInterface netif1 = NetworkInterface.getByInetAddress(dpack.getAddress());
                byte[] mac1 = netif1.getHardwareAddress();
                llc.setDaddr(mac1);
                //System.out.println("client mac is "+byteArrayToHex(mac1));
                System.out.println("\nPacket is Received from Client"  + "! (Sequence #" + getSeqNum(seg) + ")");
                int length = LPDUToInt(seg);
                byte[] seg1 = divideArray(seg, 0, length);
                System.out.println("packet sent data is \n" +byteArrayToHex(seg1));
                if(isCorrupted(seg1)){
                	System.out.println("corrupted");
                	continue;
                }
                else{
                	System.out.println("not corrupted!");
                }
                if(getformat(seg)==(byte)0xF6){
                	llc.setCONTROL((byte)0xC6, (byte)0, 1);
                	llc.setLPDU();
                }

                else if((getformat(seg)&(byte)0x80)==(byte)0x00){
                	llc.setCONTROL((byte)0x80, (byte)0, 1);
                	llc.setLPDU();
                }

                
                Thread.sleep(latency);
                	// create ACK packet (Send straight to Client)
                	byte[] ack =llc.getLLC();
                	System.out.println("send to client\n"+ byteArrayToHex(ack));
                	if(isCorrupted(ack))
                		System.out.println("fail");
                	else
                		System.out.println("true");
                    DatagramPacket ackPacket = new DatagramPacket(ack,
                           ack.length, dpack.getAddress(), dpack.getPort());
                    dsock.send(ackPacket);
                    
                    System.out.println("Acknowledgment is Sent to Client"  + "! (ACK #)");
                
                // reset the length of the packet before reusing it.
                dpack.setLength(seg.length);
            }//end while
            
            System.out.println("grace out");
            dsock.close();;

        } catch (Exception e) {
            e.printStackTrace();
        }
    }//end main
    
    public void graceout(){
		sem = false;
	}

    //divide byte array from start index(inclusive) to end index(exclusive)
    public static byte[] divideArray(byte[] wholeArray, int start, int end) {
        byte[] arr = new byte[end - start];
        arr = Arrays.copyOfRange(wholeArray, start, end);
        return arr;
    }
    
    public static byte getformat(byte[] h) {
        byte seqNum = h[16];
        return seqNum;
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

    public static int LPDUToInt(byte[] b) {
        return b[13] & 0xFF
                | (b[12] & 0xFF) << 8;
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
    
}//end Server class