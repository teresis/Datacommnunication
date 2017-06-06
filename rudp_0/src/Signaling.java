

/* Signaling for Thread  Thread.waiting() - Thread.notify()
 * 
 * wait for (keyboard input, ack packet, timeout)
 * notifying when (keyboard input to be sent(seqNo), ack packet received(ackNo), timeout expired(seq No))
 */
public class Signaling {
	public static boolean ACKNOTIFY = false, TIMENOTIFY = false, NAKNOTIFY = false, TestCRC = false, UANOTIFY= false;
	
	//ACKNOTIFY setter
	public static void setACK(boolean a) {
		ACKNOTIFY = a;
	}
	
	//ACKNOTIFY getter
	public static boolean getACK() {
		return ACKNOTIFY;
	}
	public static void setUA(boolean a) {
		UANOTIFY = a;
	}
	
	//ACKNOTIFY getter
	public static boolean getUA() {
		return UANOTIFY;
	}
	
	
	//NAKTOTIFY setter
	public static void setNAK(boolean a) {
		NAKNOTIFY = a;
	}
	
	//NAKTOTIFY getter
	public static boolean getNAK() {
		return NAKNOTIFY;
	}
	
	//testCRC setter
	public static void setTestCRC(boolean a) {
		TestCRC = a;
	}
	
	//testCRC getter
	public static boolean getTestCRC() {
		return TestCRC;
	}
	
	public synchronized void Timeoutnotifying() { 
		TIMENOTIFY = false; 
		notifyAll(); 
	} 
	public synchronized void ACKnotifying() {
		ACKNOTIFY = true; 
		notifyAll(); 
	}
	public synchronized void UAnotifying() {
		UANOTIFY = true; 
		notifyAll(); 
	} 
	
	public synchronized void NAKnotifying() {
		NAKNOTIFY = true;
		notifyAll();
	}
	public synchronized void waitingACK() { 
		 try { 	wait(); 	} catch(InterruptedException e) { 
			System.out.println("InterruptedException caught"); 
		 } 
		}
}
