import java.io.*;
import java.net.*;

//package cpsc441;

public class HTTrack {
	
	public static void main (String[] arg) throws Exception {
		// if no link is entered, prompt for a restart and exit the program
		if (arg.length != 1){
			System.out.println("Incorrect input. Please restart.");
			System.exit(0);
		}
		
		// take the input url and split it into its respective parts
		String url = null;
		url = arg[0];
		System.out.println("\nInput URL: " + url);
		String cutURL = url.replace("http://", "");
		String[] parts = cutURL.split("/");
		String hostName = parts[0];
		String lastPath = parts[parts.length-1];
		String pathName = "/";
		for (int i = 1; i < parts.length-1; i++) {
			pathName += parts[i];
			pathName += "/";
		}
		
		//Open up a socket as well as get the connection.
		Socket sock = new Socket(hostName, 80);
		BufferedReader dIn = new BufferedReader(new InputStreamReader(sock.getInputStream()));
		PrintStream dOut = new PrintStream(sock.getOutputStream());
		dOut.println("GET " + pathName + lastPath + " HTTP/1.0");
		dOut.append("\r\n");
		dOut.append("\r\n");
		dOut.flush();
	   
	   //Create the primary folder based on the hostName, and make all according folders
		//via the method folderBreakupAndMake
		String hostAndPath = hostName + pathName;
		Boolean noSourceDirectory = false;
		
		//Create the main page that was addressed in the input url
		File hostPack = folderBreakupAndMake(hostAndPath, null, noSourceDirectory);
		File mainPage = new File(hostPack, lastPath);
		if (!mainPage.exists()) {
			FileOutputStream is = new FileOutputStream(mainPage);
			OutputStreamWriter osw = new OutputStreamWriter(is);
			Writer w = new BufferedWriter(osw);
		}
		PrintWriter out = new PrintWriter(mainPage);
		String http = "http://";
		String href = "href=";
		String str = " ";
		
		//Read from the buffered reader and process accordingly based on the inputs
		//that are being read. 
		Boolean hasPassedConDetails = false;
		Boolean pageDoesNotExist = false;
		do {
			str = dIn.readLine();		
			if (str != null) {
				if (str.contains(href)) {
					if (str.contains(http)) {
					} else if (str.contains(".gif") || str.contains(".jpg") || str.contains(".png")) {
						processPicture(str, url, hostPack, hostName, pathName, sock);		// calls method to deal with picture files
					} else {
						pageDoesNotExist = innerWebpage(str, hostPack, hostName, pathName, sock);		// calls method that deals with all other webpages
						if (pageDoesNotExist == true) {
							//In order to disable to link, we will remove the reference to the link
							//and return the rest of the str which will be printed.
							str = str.replace("</a>", "");
							String[] splitLink = str.split("\"");
							str = str.replace("<a href=\"" + splitLink[1] + "\">", "");
						}
					}
				}
				if (hasPassedConDetails == true) {
					out.println(str);			//Write to file
				}
				if (str.contains("Content-Type")){
					hasPassedConDetails = true; //this ensures that the header isn't printed into the output file
				}				
			}
		} while (str != null);
		
		out.close(); //close the print writer
		sock.close(); //closes socket
	}
	
	//Processes all internal webpages within the primary hostpage.
	public static boolean innerWebpage (String str, File hostName_File, String hostName, String pathName, Socket sock) throws Exception {
		String[] newSplit = str.split("=");
		String[] split2 = newSplit[1].split(">");
		String newLastPath = split2[0].replace("\"", "");
		String copyLastPath = newLastPath;
		File newDirName = hostName_File;
		String str2 = "";
		Boolean webpageDNE = false;
		
		//If there are new directories that need to be made, it will be processed here
		if (newLastPath.contains("/")) {
			newDirName = folderBreakupAndMake(newLastPath, hostName_File, false);
			String[] splitFromDir = newLastPath.split("/");
			newLastPath = splitFromDir[splitFromDir.length-1];
		}
		
		//Creates a new webpage that exists either inside a new directory or the same
		//directory as the primary hostpage.
		File webception = new File(newDirName, newLastPath);
		if (!webception.exists()) {
			FileOutputStream is2 = new FileOutputStream(webception);
			OutputStreamWriter osw2 = new OutputStreamWriter(is2);
			Writer w2 = new BufferedWriter(osw2);
		}
		pathName += copyLastPath;
		
		//A new socket must be opened, as well as the resulting readers+streams
		Socket sock2 = new Socket(hostName, 80);
		BufferedReader dIn2 = new BufferedReader(new InputStreamReader(sock2.getInputStream()));
		PrintStream dOut2 = new PrintStream(sock2.getOutputStream());
		dOut2.println("GET " + pathName + " HTTP/1.0");
		dOut2.append("\r\n");
		dOut2.append("\r\n");
		dOut2.flush();
		
		//Passes over the header and writes everything else to file. 
		Boolean hasPassedConDetails2 = false;
		PrintWriter innerOut = new PrintWriter(webception);
		do {
			str2 = dIn2.readLine();		
			if (str2 != null) {
				if (hasPassedConDetails2 == true) {
					innerOut.println(str2);			//Write to file
				}
				if (str2.contains("Content-Type")){
					hasPassedConDetails2 = true;
				}
				if (str2.contains("404")) {
					webpageDNE = true;
				}
			}
		} while (str2 != null);		
		innerOut.close(); //closes writer
		sock2.close();	//closes socket
		return webpageDNE; //returns boolean whether or not the webpage exists or not.
	}
	
	//Processes any picture files (.jpg, .png, .gif) within the primary hostpage
	public static void processPicture (String str, String url, File hostName_File, String hostName, String pathName, Socket sock) throws Exception {
		String[] newSplit = str.split("=");
		String[] split2 = newSplit[1].split(">");
		String newLastPath = split2[0].replace("\"", "");
		String copyLastPath = newLastPath;
		File newDirName = hostName_File;
		String str2 = "";
		int pictureByte = 0;
		
		//If there are new directories that need to be made, it will be processed here
		if (newLastPath.contains("/")) {
			newDirName = folderBreakupAndMake(newLastPath, hostName_File, false);
			String[] splitFromDir = newLastPath.split("/");
			newLastPath = splitFromDir[splitFromDir.length-1];
		}
		
		//Creates a new picture file should one not already exist. 
		File pictureFile = new File(newDirName, newLastPath);
		if (!pictureFile.exists()) {
			FileOutputStream is_picture = new FileOutputStream(pictureFile);
			OutputStreamWriter osw_picture = new OutputStreamWriter(is_picture);
			Writer w_picture = new BufferedWriter(osw_picture);
		}
		pathName += copyLastPath;
		
		//Creates a new socket as well as its resulting readers + streams
		Socket sockpic = new Socket(hostName, 80);
		BufferedReader readerIn = new BufferedReader(new InputStreamReader(sockpic.getInputStream()));
		PrintStream dOut2 = new PrintStream(sockpic.getOutputStream());
		dOut2.println("GET " + pathName + " HTTP/1.0");
		dOut2.append("\r\n");
		dOut2.append("\r\n");
		dOut2.flush();

		//After skipping over the header, this processes the picture into
		//an array of bytes. 
		Boolean hasPassedConDetails2 = false;
		int arraySize = 0;
		while (true) {
			str2 = readerIn.readLine();		
			if (str2 != null) {
				if (str2.contains("Content-Length")) {
					String[] arraySize_str = str2.split(" ");
					arraySize = Integer.parseInt(arraySize_str[1]);
				}
				if (str2.contains("Content-Type")){
					hasPassedConDetails2 = true;
				}
				if (hasPassedConDetails2 == true) {
					break;
				}
			}
		}
		BufferedInputStream dIn2 = new BufferedInputStream(sockpic.getInputStream());
		ByteArrayOutputStream byteOutput = new ByteArrayOutputStream();
		FileOutputStream outStream = new FileOutputStream(pictureFile);
		byte[] byteArray = new byte[arraySize];
		int bytesRead = 0;
		while ((bytesRead = dIn2.read(byteArray)) != -1) {
			byteOutput.write(byteArray, 0, bytesRead);	
		}
		byteArray = byteOutput.toByteArray();
		outStream.write(byteArray);
		outStream.flush();
		outStream.close();
		sockpic.close();
	}
	
	//Separates the path such that the necessary folders can be created
	public static File folderBreakupAndMake (String newLastPath, File oldDirectoryName, Boolean noSourceDirectory) {
		//Separates such that it knows how many subdirectories need to be created
		newLastPath = newLastPath.replace("\"", "");
		String[] splitAgain = newLastPath.split("/");
		String newDirectoryName = "";
		
		//runs if the last element does NOT contain "." in it
		if (!splitAgain[splitAgain.length-1].contains(".")) {
			for (int i = 0; i < splitAgain.length; i++) {
				newDirectoryName = splitAgain[i];
				//This assumes that we are making the folder in the primary directory
				if (noSourceDirectory == true) {
					File newDir = new File(newDirectoryName);
					if (!newDir.exists()) {
						newDir.mkdir();
					}
					noSourceDirectory = false;
					oldDirectoryName = newDir;
				} else {
					//Creates a new directory given that it is within another newly-created directory
					File newDir = new File(oldDirectoryName, newDirectoryName);
					newDir.mkdir();
					oldDirectoryName = newDir;
				}
			}
		//runs in all other cases
		} else {
			for (int i = 0; i < splitAgain.length-1; i++) {
				newDirectoryName = splitAgain[i];
				if (noSourceDirectory == true) {
					File newDir = new File(newDirectoryName);
					if (!newDir.exists()) {
						newDir.mkdir();
					}
					noSourceDirectory = false;
					oldDirectoryName = newDir;
				} else {
					//Creates a new directory given that it is within another newly-created directory
					File newDir = new File(oldDirectoryName, newDirectoryName);
					newDir.mkdir();
					oldDirectoryName = newDir;
				}
			}
		}
		newLastPath = splitAgain[splitAgain.length-1];
		newDirectoryName = newDirectoryName.replace("\\", "\\\\");
		return oldDirectoryName;
	}

}
