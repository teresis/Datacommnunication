package server;
import java.net.*;
import java.io.*;
public class UDPEchoServer {
	final int MAXBUFFER =512;
	
	public static void main(String[] args){
		int arg_port =Integer.parseInt(args[0]);
		new UDPEchoServer().work(arg_port);
	}
	void work(int arg_port){
		int port =arg_port;
		byte buffer[] =new byte[MAXBUFFER];
		try {
			DatagramSocket socket = new DatagramSocket(port);
			DatagramPacket recv_packet;
			System.out.println("Running the UDP Echo Server....");
			while(true){
				recv_packet = new DatagramPacket(buffer,buffer.length);
				socket.receive(recv_packet);
				DatagramPacket send_packet = new DatagramPacket
						(recv_packet.getData(),recv_packet.getLength(),
						recv_packet.getAddress(),recv_packet.getPort());
				socket.send(send_packet);
			}
		} catch (IOException e){
			System.out.println(e);
		}

	}
}
