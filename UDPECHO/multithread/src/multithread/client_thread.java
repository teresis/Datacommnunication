package multithread;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class client_thread {

  public static void main(String[] args) throws Exception {
	  if(args.length!=2){
			System.out.println("사용법 : java UDPecho localhost port 3000");
			System.exit(0);
	}
	String hostname = args[0];
	int port = Integer.parseInt(args[1]);
    InetAddress ia = InetAddress.getByName(hostname);
    SenderThread sender = new SenderThread(ia, port);
    sender.start();
    Thread receiver = new ReceiverThread(sender.getSocket());
    receiver.start();
  }

}

class SenderThread extends Thread {

  private InetAddress server;

  private DatagramSocket socket;

  private boolean stopped = false;

  private int port;

  public SenderThread(InetAddress address, int port) throws SocketException {
    this.server = address;
    this.port = port;
    this.socket = new DatagramSocket();
    this.socket.connect(server, port);
  }

  public void halt() {
    this.stopped = true;
  }

  public DatagramSocket getSocket() {
    return this.socket;
  }

  public void run() {

    try {
      BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));
      while (true) {
        if (stopped)
          return;
        String theLine = userInput.readLine();
        if (theLine.equals("."))
          break;
        byte[] data = theLine.getBytes();
        DatagramPacket output = new DatagramPacket(data, data.length, server, port);
        socket.send(output);
        Thread.yield();
      }
    }
    catch (IOException ex) {
      System.err.println(ex);
    }
  }
}

class ReceiverThread extends Thread {
  DatagramSocket socket;

  private boolean stopped = false;

  public ReceiverThread(DatagramSocket ds) throws SocketException {
    this.socket = ds;
  }

  public void halt() {
    this.stopped = true;
  }

  public void run() {
    byte[] buffer = new byte[65507];
    while (true) {
      if (stopped)
        return;
      DatagramPacket dp = new DatagramPacket(buffer, buffer.length);
      try {
        socket.receive(dp);
        String s = new String(dp.getData(), 0, dp.getLength());
        System.out.println("reply : "+s);
        Thread.yield();
      } catch (IOException ex) {
        System.err.println(ex);
      }
    }
  }
}
