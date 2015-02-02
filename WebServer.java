import java.io.*;
import java.net.*;

public class WebServer implements Runnable{
	Socket csocket;

  WebServer(Socket csocket) {
    this.csocket = csocket;
  }

  public static void main(String args[]) throws Exception {
	//Takes in the input port, and prepares to listen for a response from the client
	int port = Integer.parseInt(args[0]);
	ServerSocket ssock = new ServerSocket(port);
    System.out.println("Listening...");
    while (true) {
		//When the client connects, a message will be printed via command prompt to alert
		//that a connection has been made. The multithreading begins. 
		Socket newSocket = ssock.accept();
		new Thread(new WebServer(newSocket)).start();
    }
  }

  public void run() {
    try {
		//initialize the necessary buffers to read
		BufferedReader read_in = new BufferedReader(new InputStreamReader(csocket.getInputStream()));
		DataOutputStream dos = new DataOutputStream(csocket.getOutputStream());
		PrintStream print_out = new PrintStream(dos);
		String source = read_in.readLine();
		String defaultPage = "index.html";
		
		//if its not a GET request, treat as an invalid request
		if (!source.contains("GET")){
			print_out.println("HTTP:/1.1 400 Bad request");
		}
		source = source.replace("GET ", "");
		source = source.replace(" HTTP/1.1", "");
		if (source.endsWith("/")) {				//Applies primarily for the initial opening. However this can be broken by e.g. /fjsd/
			source = defaultPage;
		} 
		if (source.startsWith("/")) {
			source = source.substring(1, source.length());		//removes the '/' at the beginning of the source so it can be processes properly
		}
		System.out.println("Current source: " + source);
		File newFile = new File(source);
		String filePath = newFile.getAbsolutePath();
		
		//Attempts to open the file and display it. If the file does not exist, 
		//the 404 File Not Found will be thrown
		try {
			File file = new File(source);
			print_out.println("HTTP:/1.1 200 OK\r\n");			//If the file is valid this will run
			FileInputStream fis = new FileInputStream(file);
			byte[] byteArray = new byte[fis.available()];
			int content;
			while ((content = fis.read(byteArray)) != -1) {		//processes and prints out the content of the file
				print_out.write(byteArray);
			}
			print_out.close();				//closes the printWriter
			csocket.close();				//closes the socket
		} catch (FileNotFoundException e) {
			print_out.println("HTTP/1.1 404 Not Found\r\n");	//catches should a file not exist
			print_out.close();
		}
	} catch (IOException e) {
		System.out.println(e);
    }
  }
}
