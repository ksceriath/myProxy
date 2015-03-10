package javaMan.core.sockets.proxy;

import java.io.*;
import java.net.*;

public class SimpleProxy2 {

	public static ServerProcess start() {
		ServerProcess sp = new ServerProcess(8080);
		Thread th = new Thread(sp);
		th.setName("ServerThread");
		th.start();
		return sp;
	}
	
	public static void main(String[] args) {
		ServerProcess serverProcess = start();
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		try {
			if(br.read() == 'a')
				serverProcess.isRunning = false;
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
	}

}

class ServerProcess implements Runnable {
	public boolean isRunning;
	public int port;
	
	public ServerProcess(int port) {
		this.port = port;
		isRunning = true;
	}
	
	public void run() {
		ServerSocket serverSocket = null;
		try {
				while(isRunning) {
				serverSocket = new ServerSocket(port);
				Socket incoming = serverSocket.accept();
				Thread t = new Thread(new SocketProcess(incoming));
				t.setName("SocketProcessingThread");
				t.start();
				}
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					serverSocket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
	}
}

class SocketProcess implements Runnable {
	public Socket client;
	
	public SocketProcess(Socket client) {
		this.client = client;
	}
	
	public void run() {
		/*
		 * Connect to server
		 * Send the incoming message to the server
		 * Send the server response back to the client
		 */
		try {
			Socket server = new Socket("localhost",80);
			InputStream clientRequest = client.getInputStream();
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			int data = clientRequest.read();
			while(data != -1) {
				System.out.print(data);
				bos.write(data);
				byte[] streamKeeper = bos.toByteArray();
				ByteArrayInputStream bis = new ByteArrayInputStream(streamKeeper);
				Streamer xyz = new Streamer(bis, server.getOutputStream());
				Streamer pqr = new Streamer(server.getInputStream(), client.getOutputStream());
				Thread x = new Thread(xyz);
				x.setName("ClientToServerStreamThread");
				x.start();
				Thread y = new Thread(pqr);
				y.setName("ServerToClientStreamThread");
				y.start();
				x.join();
				y.join();
				server.close();
			}
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
