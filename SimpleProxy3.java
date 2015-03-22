package javaMan.core.sockets.proxy;

import java.io.*;
import java.net.*;

public class SimpleProxy3 {
	
	public static String HOST = "localhost";
	public static int PORT = 80;
	
	public static int PROXYPORT = 8080;
	
	public static void threadLogn(String message) {
		System.out.println("["+Thread.currentThread().getName()+"] "+message);
	}
	
	public static void threadLog(String message) {
		System.out.print("["+Thread.currentThread().getName()+"] "+message);
	}
	
	public static void log(String message) {
		System.out.print(message);
	}
	
	public static void main(String[] args) {
		int port = PROXYPORT;
		Server server = new Server(port);
		
		Thread serverThread = new Thread(server);
		serverThread.setName("SERVER_Thread");
		serverThread.start();
		
		try {
			if(System.in.read() != -1)
				server.setServerState(false);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}


/* Class that opens a server socket for the Proxy server */
class Server implements Runnable {
	
	boolean serverOn;
	int port;
	ServerSocket serverSocket;		// proxy server
	
	Server(int port) {
		this.port = port;
		serverOn = true;
	}
	
	public void setServerState(boolean on) {
		serverOn = on;
	}
	
	public void run() {
		try {
			serverSocket = new ServerSocket(port);
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			serverSocket.setSoTimeout(1000);
		} catch (SocketException e) {
			e.printStackTrace();
		}
		Socket clientSocket = null;
		
		while(serverOn) {
			try {
				clientSocket = serverSocket.accept();			// everything throws an exception..!
				SimpleProxy3.threadLogn("Connected to "+clientSocket.getInetAddress()+".");
				Client client = new Client(clientSocket);
				Thread clientThread = new Thread(client);
				clientThread.setName("CLIENT_Thread");
				clientThread.start();
			} catch (SocketTimeoutException e) {
				// to do something additional if needed; otherwise this
				// would anyway get caught as IOException
//				e.printStackTrace();
//				SimpleProxy3.threadLogn("Socket accept timed out.");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		try {
			serverSocket.close();								// yup.. everything!
		} catch (IOException e) {
			SimpleProxy3.threadLogn("v");
			e.printStackTrace();
			SimpleProxy3.threadLogn("^");
		}
	}
}

/* Class that manages and processes the socket connections to 
 * the Main Server and each connected client */
class Client implements Runnable {
	
	Socket clientSocket;				// requesting client
	Socket mainServerSocket;			// responding server
	
	Client(Socket clientSocket) {
		this.clientSocket = clientSocket;
	}
	
	public void run() {
		try {
			mainServerSocket = new Socket(SimpleProxy3.HOST, SimpleProxy3.PORT);
		} catch (UnknownHostException e) {
			SimpleProxy3.threadLogn("Main server could not be located.");
			e.printStackTrace();
		} catch (IOException e) {
			SimpleProxy3.threadLogn("Couldn't connect to Main Server.");
			e.printStackTrace();
			SimpleProxy3.threadLogn("");
		}
		
		X x1 = null, x2 = null;
		try {
			x1 = new X(clientSocket.getInputStream(), mainServerSocket.getOutputStream()) {
				
				public String process(String message) {
					//do something with message here...
					return message;
				}
			};
		} catch (IOException e) {
			SimpleProxy3.threadLogn("");
			e.printStackTrace();
			SimpleProxy3.threadLogn("");
		}
		try {
			x2 = new X(mainServerSocket.getInputStream(), clientSocket.getOutputStream()) {
				
				public String process(String message) {
					// do something with message here...
					return message;
				}
			};
		} catch (IOException e) {
			SimpleProxy3.threadLogn("");
			e.printStackTrace();
			SimpleProxy3.threadLogn("");
		}
		
		Thread mainServerThread = new Thread(x2);
		mainServerThread.setName("MAINSERVER_Thread");
		mainServerThread.start();
		
		x1.run();
		try {
			mainServerThread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		try {
			clientSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			mainServerSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

class X implements Runnable {
	
	InputStream inputStream;
	OutputStream outputStream;
	
	X(InputStream inputStream, OutputStream outputStream) {
		this.inputStream = inputStream;
		this.outputStream = outputStream;
	}
	
	public void run() {
		String message = read();
		message = process(message);
		write(message);
	}
	
	private String read() {
		BufferedReader br = new BufferedReader(new InputStreamReader(
				inputStream));
		int contentLength=0;
		String request="";
		String line = null;
		try {
			line = br.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
		while(line != null && !line.equals("")) {
			SimpleProxy3.threadLogn("reading: "+line);
			request += line + "\n";
			
			if(line.length() > 16 && line.substring(0,16).equals("Content-Length: ")) {
				contentLength = Integer.parseInt(line.substring(16));
			}
			
			try {
				line = br.readLine();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		request += "\n";
		SimpleProxy3.threadLog("reading: ");
		for(int i=0; i<contentLength; i++) {
			char ch = 0;
			try {
				ch = (char)br.read();
				SimpleProxy3.log(""+ch);
			} catch (IOException e) {
				e.printStackTrace();
			}
			request += ch;
		}
		SimpleProxy3.log("\n");
		return request;
	}
	
	public String process(String message) {
		return message;
	}
	
	private void write(String message) {
		SimpleProxy3.threadLogn("writing:\n"+message);
		PrintWriter outputter = new PrintWriter(outputStream);
		outputter.print(message);
		outputter.flush();
		SimpleProxy3.threadLogn("Written and flushed.");
	}
}
