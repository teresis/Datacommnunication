
import java.io.*;
import java.net.*;
import java.util.Arrays;
import java.util.zip.CRC32;

class RcvThread extends Thread {
	final static int SEGMENT_SIZE = 518; 
    final static int dataSize = 496; 
	DatagramSocket socket;
	InetAddress inetaddr;
	boolean	sem = true;
	public static DatagramPacket dpack;//
	String data;
	CRC32 crc;
	LLC llc= new LLC();
	String ack;
	Signaling p;
    //static int windowSize = 8;
    byte seqNo = 1;
    byte seqNo2 = 1;
    boolean setseqNo2 = false;
    boolean setseqNo = false;
    
    public static DatagramPacket getDatagramPacket() {
    	return dpack;
    }
    public static InetAddress getInetAddress() {
    	return dpack.getAddress();
    }
	RcvThread (DatagramSocket s, Signaling pp) {
		socket = s;
		p=pp; 
	}	

	public void run() {
		byte[] seg = new byte[SEGMENT_SIZE];
		dpack = new DatagramPacket(seg, seg.length);
		
		
		while (sem) {
			try {
				
		       socket.receive(dpack);
		       int length = LPDUToInt(seg);
               byte[] seg1 = divideArray(seg, 0, length);
               System.out.println(byteArrayToHex(seg1));
               if(isCorrupted(seg1)){
               	System.out.println("corrupted");
               	continue;
               }
               else{
               	System.out.println("not corrupted!");
               }
		       if(getformat(seg) == (byte) 0xC6){

	               p.UAnotifying();
	    	   }
		       
		       //get ACK Frame
		       if(getformat(seg) == (byte) 0x80){

		               p.ACKnotifying();
		    	   
		       }
		       //get NAK Frame
		       else if(getformat(seg) == (byte) 0x90) {
		    	   p.NAKnotifying();
		       }

               dpack.setLength(seg.length); // reset the length of the packet before reusing it.
            } catch (IOException e) {
            	e.printStackTrace();
            }
		}
		
		System.out.println("grace out");
	}
	
	public void graceout(){
		sem = false;
	}
    // method to get seq number from packet
    public static byte getformat(byte[] h){
    	byte format = h[16];
    	return format;//(byte) (format & 0xC0);
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
    // method to convert byte array length 4 to integer
    public static int fourByteArrayToInt(byte[] b) {
        return b[3] & 0xFF
                | (b[2] & 0xFF) << 8
                | (b[1] & 0xFF) << 16
                | (b[0] & 0xFF) << 24;
    }
    public static int LPDUToInt(byte[] b) {
        return b[13] & 0xFF
                | (b[12] & 0xFF) << 8;
    }
    public static byte[] divideArray(byte[] wholeArray, int start, int end) {
        byte[] arr = new byte[end - start];
        arr = Arrays.copyOfRange(wholeArray, start, end);
        return arr;
    }
    static String byteArrayToHex(byte[] a)
	{
		StringBuilder sb = new StringBuilder();
		for (final byte b : a)
			sb.append(String.format("%02x ", b & 0xff));
		return sb.toString();
	}
} // end RcvThread class
