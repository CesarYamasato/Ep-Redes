package socketApplication;

import Encryption.Asymmetric.RSA.RSAEncryption;
import Encryption.EncryptionException.NoKeyException;
import Encryption.Symmetric.RC6.RC6Decryptor;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import FileManager.FileReceiver;

public class Client extends SocketApplication{
	private FileReceiver fileReceiver;
	private Directory otherPeersDirectory;
	private RSAEncryption rsaEncryptor;
	private RC6Decryptor rc6Decryptor;

	//Connects to another peer's serverSocket
	public Client(Socket clientSocket) throws UnknownHostException, IOException, NoSuchAlgorithmException{
		super(clientSocket);
		otherPeersDirectory = new Directory();
		rsaEncryptor = new RSAEncryption();
		rc6Decryptor = new RC6Decryptor();
		this.fileReceiver = new FileReceiver(clientSocket, rc6Decryptor);
		receiveListDirectory();
	}

	//TODO: remove test messages add comments
	public void sendRSAKey() throws IOException{
		byte[] keyBytes = rsaEncryptor.getPublicKey().getEncoded();
		out.writeInt(keyBytes.length);
		out.write(keyBytes);
	}

	//TODO: remove test messages add comments
	public void receiveRC6Key() throws IOException, NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, NoKeyException{
		int keySize = in.readInt();
		byte [] keyBytes = in.readNBytes(keySize);
		byte [] decryptedKey = rsaEncryptor.decrypt(keyBytes);
		rc6Decryptor.set_key(decryptedKey);
	}
	
	//TODO: decrypt the names on arrival 
	//Receives the List of items the other peer can send over
	private void receiveListDirectory() throws IOException {
		otherPeersDirectory = new Directory();
		int numberOfFiles = in.readInt();
		
		for(int i=0; i<numberOfFiles; i++) {
			/*
			all of the directory contents are sent in the order of <size of name>, <isFolder> ,<name>
			it is necessary to retrieve the contents in such order. the name size is used to retrieve 
			<sizeOfName> bytes from the input stream.
			*/
			int size = in.readInt();
			boolean isFolder = in.readBoolean();
			String directory_name = new String(in.readNBytes(size));
			otherPeersDirectory.add_item(directory_name, isFolder);
		}
		otherPeersDirectory.printDirectory();
	}
	
	//TODO: decrypt the files on arrival
	//Receives all files that are on the in
	private void receiveFiles() throws IOException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, NoKeyException{
		fileReceiver.receiveFile();
	}
	
	//Selects a file to be downloaded from the server Peer
	private void selectFile() throws IOException {
		otherPeersDirectory.printDirectory();
		int requestedFile = systemIn.nextInt();
		out.writeInt(requestedFile);
	}
	
	//Selects a folder to navigate to on the other peer's directory
	private void selectFolder() throws IOException {
		otherPeersDirectory.printDirectory();
		int requestFolder = systemIn.nextInt();
		out.writeInt(requestFolder);
		//the other peer will send "false" in case the selected folder was up the parent folder from which the programbegan execution
		if(!in.readBoolean()) System.out.println("Access not permitted." + System.lineSeparator());
	}
		
//============================================== Public Side =========================================================//

	//Sends handshake
	public void sendAwake(int port) throws IOException {
		out.writeInt(port);
	}
	
	//Sends a request to the other peer
	public void sendRequest() throws IOException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, NoKeyException {
		System.out.println(
				"/////////////////////////////////////////"+ System.lineSeparator() +
				"// 1: Update directory list            //"+ System.lineSeparator() +
				"// 2: Select a file/folder to download //"+ System.lineSeparator() +
				"// 3: Navigate to folder               //"+ System.lineSeparator() +
				"// 4: End connection                   //"+ System.lineSeparator() +
				"/////////////////////////////////////////"
				);
		int request = systemIn.nextInt();
		out.writeInt(request);
		while (in.available() == 0);
		int answer= in.readInt();
		//Requests: 
		if(answer == 9999) {
			switch (request) {
			case 1:
				receiveListDirectory();
				break;
			case 2:
				selectFile();
				receiveFiles();
				break;
			case 3:
				selectFolder();
				receiveListDirectory();
				break;
			case 4:
				this.close();
				break;
			default:
				System.out.println("Invalid Option.");
			}
		}
	}
}