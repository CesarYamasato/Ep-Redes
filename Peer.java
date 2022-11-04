import java.io.*;
import java.net.*;

import socketApplication.*;

public class Peer extends Thread {

    private Server server;
    private Client client;

    // ====================================== Server Side =============================================================//

    private String clientAddress;
    private int port;
    
    public void createServer(int port) throws UnknownHostException, IOException {
        ServerSocket serverSocket = new ServerSocket(port);
        Socket clientSocket = serverSocket.accept();
        server = new Server(clientSocket);
        clientAddress = clientSocket.getInetAddress().toString();
        clientAddress = clientAddress.substring(1);
        serverSocket.close();
    }

    public void receiveRequest() throws IOException {
        server.receiveRequest();
    }

    public int waitAwake() throws IOException {
        return port = server.waitAwake();
    }

    public String getAddress() {
    	return clientAddress;
    }
    public int getPort() {
    	return port;
    }
    
    public void run() {
        try {
            while (true)
                receiveRequest();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    // ====================================== Client Side ============================================================//

    public void connect(String IP, int port) throws UnknownHostException, IOException {
        Socket clientSocket = new Socket(IP, port);
        client = new Client(clientSocket);
    }

    public void sendRequest() throws IOException {
        client.sendRequest();
    }

    public void sendAwake(int port) throws IOException {
        client.sendAwake(port);
    }

    public static void main(String[] args) throws IOException {
        Peer peer = new Peer();
        if (args[0].equals("-s")) {
            peer.createServer(Integer.parseInt(args[1]));
            peer.waitAwake();
            peer.connect(peer.getAddress(),peer.getPort());
        } else if (args[0].equals("-c")) {
            peer.connect(args[2], Integer.parseInt(args[3]));
            peer.sendAwake(Integer.parseInt(args[1]));
            peer.createServer(Integer.parseInt(args[1]));
        }
        peer.start();
        while (true)
            peer.sendRequest();
    }
}
