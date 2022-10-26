package FileManager;

import java.io.*;
import java.net.*;


public class FileSender{
	
	DataOutputStream out;
	Socket socket;
	
	//Creates a FileSender object to send files to the specified Socket
	public FileSender(Socket socket) {
		this.socket = socket;
		try {
			out = new DataOutputStream(socket.getOutputStream());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	//Creates a FileSender object and attatches to it a Socket with the specified IP address and port to send files
	public FileSender(int port, InetAddress IP) {
		try {
			this.socket = new Socket(IP, port);
			out = new DataOutputStream(socket.getOutputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	//Sends a file to the socket specified upen creation of the FileSender
	public void sendFile(File file) throws IOException {
		FileInputStream fileIn = new FileInputStream(file.getName());
		
		byte[] fileName = file.getName().getBytes();
		out.writeInt(fileName.length);
		out.write(fileName);
		
		byte[] fileContents = new byte[(int) file.length()];
		
		fileIn.read(fileContents);
		out.writeInt(fileContents.length);
		out.write(fileContents);
		
		fileIn.close();
		
	}
	
	//Closes the OutputStream created with the FileSender
	public void close() {
		try {
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}