package javaMan.core.sockets.proxy;

import java.io.*;
import java.net.*;

/* This is a simple (reverse?) proxy server, that will take incoming requests on some port 
 * and pass them on to a DumbServer instance running on some other port.
 */

public class SimpleProxy {
		
	public static void main(String[] args) {
		
		try {
			Socket y = new Socket("localhost",80);				// connection to DumbServer
			InputStream fromServer = y.getInputStream();
			OutputStream toServer = y.getOutputStream();
			
			ServerSocket proxy = new ServerSocket(8080);		// server socket
			Socket x = proxy.accept();
			InputStream ins = x.getInputStream();
			OutputStream outs = x.getOutputStream();
			
			Thread inputProcessing = new Thread(new Streamer(ins, toServer));
			Thread outputProcessing = new Thread(new Streamer(fromServer, outs));

			inputProcessing.start();
			outputProcessing.start();

			try {
				inputProcessing.join();
				outputProcessing.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} finally {
				proxy.close();
				y.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}

class Streamer implements Runnable {

	private InputStream ins;
	private OutputStream outs;

	public Streamer(InputStream i, OutputStream o) {
		ins = i;
		outs = o;
	}
	
	public void run() {
		streamMapper(ins, outs);
	}
	
	public void streamMapper(InputStream in, OutputStream out) {
		try {
			int x = 0;
			while(x != -1) {
				x = in.read();
				out.write(x);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
