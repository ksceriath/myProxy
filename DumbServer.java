package javaMan.core.sockets.proxy;

import java.io.*;
import java.net.*;

/*
 * Reads requests on localhost:80 socket.
 * Prints the request content on console.
 * Replies with a 200 OK response.
 */

public class DumbServer {

	public static boolean isRunning;
	public static char[] ok() {
		String response = "HTTP/1.1 200 OK\n"+
						"Server: DumbServer\n"+
						"Accept-Ranges: bytes\n"+
						"Content-Length: 44\n"+
						"Connection: keep-alive\n"+
						"Content-Type: text/html\n"+  
						"\n<html><body><h1>It LUrks!</h1></body></html>";
//		System.out.println("####"+response.toCharArray()[response.toCharArray().length-2]);
		return response.toCharArray();
	}
	
	public static void serverStart() {
		Thread x = new Thread() {							// This thread is to keep the main thread separate, which can be utilized for server shutdown, etc.
			public void run() {
				try {
					isRunning = true;
					ServerSocket ss = new ServerSocket(80);
					ss.setSoTimeout(1000);
					int timeoutCount=0;
					while(isRunning) {
						try{
//							System.out.println("waiting...!");
//							if(ss==null) {
//								System.out.println(timeoutCount+" whoa...! "
//										+ "serversocket is null here!!");
//							}
							final Socket s = ss.accept();
//							System.out.println("done waiting...!");
//							if(s==null) {
//								System.out.println("whoa...! socket is null here!!");
//							}
							Thread connProc = new Thread(new Runnable() {
								public void run() {
									process(s);
//									System.out.println("finished processing.. starting response.. ");
									respond(s);
//									System.out.println("returned from responding.. closing socket");
									try {
										s.close();
//										System.out.println("and closed...");
									} catch (IOException e) {
										e.printStackTrace();
									}
								}
								
							});
							connProc.setName("Thread#"+timeoutCount);
							connProc.start();
							System.out.println("["+Thread.currentThread().getName()+"] "+"Connection request accepted on count "+timeoutCount
									+" and new thread initiated...");
						} catch(SocketTimeoutException ste) {
							System.out.println("["+Thread.currentThread().getName()+"] "+"#"+ ++timeoutCount+" Socket accept timed out.");
//							ste.printStackTrace();
						}
					}
//					System.out.println("AND HERE???4");
					ss.close();
//					System.out.println("AND HERE???5");
				} catch(SocketException st) {
					System.out.println("["+Thread.currentThread().getName()+"] "+"Socket Exception is getting thrown right now!!");
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		};
		x.setName("ThunderThread");
		x.start();
	}
	
	public static void process(Socket s) {
		try {
//			System.out.println("entered process...");
//			InputStream temp = s.getInputStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(s.getInputStream()));
			String line = null;
//			if(!s.isClosed() || s.isConnected()) {
//				System.out.println("reading the request line...");
				line = br.readLine();			// This is the guy who sits waiting for input
												// and then ends up receiving null...
												// i dont know if there is some sort of
												// timeout defined for this....
//				System.out.println("finished reading... premier line...");
//			}
			System.out.println("["+Thread.currentThread().getName()+"] "+line);
//			System.out.println("[Printing incoming request]");
			while(line != null && !line.equals("")) {
//			while(!line.equals("\r\n")) {
//				System.out.println("and line qualifies...");
				System.out.println("["+Thread.currentThread().getName()+"] "+line);
//				if(s.isConnected()){
//					System.out.println("gonna read...");
					line = br.readLine();
//					System.out.println("read...");
//				}
			}
			System.out.println("["+Thread.currentThread().getName()+"] "+"[Printing completed]");
			
		} catch (IOException e) {
			e.printStackTrace();
		}
//		System.out.println("exiting process...");
	}
	
	public static void respond(Socket s) {
//		System.out.println("enter respond...");
		try {
			OutputStream os = s.getOutputStream();
			char[] res = ok();
			for(char x : res) {
				os.write((int)x);
			}
//			System.out.println("DOES IT COME HERE???");
			os.close();
//			System.out.println("AND HERE???1");
		} catch (IOException e) {
			e.printStackTrace();
		}
//		try {
//			Thread.sleep(10000);
//		} catch (InterruptedException e) {
//			System.out.println("Atleast this shudnt be getting thrown...!");
//			e.printStackTrace();
//		}
//		System.out.println("exit respond...");
	}
	
	public static void serverStop() {
		isRunning = false;
	}
	
	public static void main(String[] args) {
		serverStart();
		System.out.println("["+Thread.currentThread().getName()+"] "+"Server socket started.");
		try {
			System.out.println("["+Thread.currentThread().getName()+"] "+"Sleeping for 30k ms");
			Thread.sleep(30000);
			System.out.println("["+Thread.currentThread().getName()+"] "+"Waking up...");
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("["+Thread.currentThread().getName()+"] "+"Stopping server.");
		serverStop();
		System.out.println("["+Thread.currentThread().getName()+"] "+"Server stop set.");
	}

}
