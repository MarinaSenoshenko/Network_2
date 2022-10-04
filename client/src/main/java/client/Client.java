package main.java.client;

import java.io.*;
import java.net.*;
import java.util.logging.*;

public class Client {	
    private int port;
    private String filePath;
    private InetAddress serverAddr;
    public final static Logger logger = Logger.getLogger(Client.class.getName());
    
    private static final int CLIENT_ARGS_COUNT = 3;	
    
    public Client(String filePath, InetAddress serverAddr, int port) {
    	this.filePath = filePath;
    	this.serverAddr = serverAddr;
    	this.port = port;
    } 
	
    public static void main(String[] args) {
    	
    	 LogManager logManager = LogManager.getLogManager();
         try {
             logManager.readConfiguration(new FileInputStream("src/main/resources/logClient.properties"));
         } catch (IOException ex){
             logger.log(Level.SEVERE, "Cannot get log configuration!" + ex.getMessage());
         }
    	
    	if (args.length < CLIENT_ARGS_COUNT) {
            logger.log(Level.SEVERE, "Not enough arguments\nYou should type:\n1)file path\n2)server address\n3)port number");
            System.exit(1);
        }    	
    	
    	String filePath = args[0];
        int port = Integer.parseInt(args[2]);

        InetAddress serverAddr = null;
        try {
            serverAddr = InetAddress.getByName(args[1]);        	
        }
        catch (UnknownHostException e) {
            logger.log(Level.SEVERE, "Can't recognize host: " + e.getMessage());
            System.exit(1);
        }

        Client client = new Client(filePath, serverAddr, port);
        client.sendFile();
    }
    
    private void sendFile() {
    	
    	File file = new File("src/main/resources" + filePath);    	

        try (Socket socket = new Socket(serverAddr, port);
        	 InputStream filestream = Client.class.getResourceAsStream(filePath);
        		
             OutputStream socketOut = socket.getOutputStream();
        	 InputStream socketIn = socket.getInputStream();
        		
             DataOutputStream socketDataOut = new DataOutputStream(socketOut);
             DataInputStream socketDataIn = new DataInputStream(socketIn))
        {
            String fileName = file.getName();
            
            socketDataOut.writeUTF(fileName);
            socketDataOut.writeLong(file.length());

            byte[] buf = new byte[8192];
            int count;
            while ((count = filestream.read(buf)) > 0) {
                socketOut.write(buf, 0, count);
            } 

            if (socketIn.read() == 100) {
                logger.log(Level.INFO, "Successfully sent " + fileName);
            }

        }
        catch (FileNotFoundException e) {
            logger.log(Level.SEVERE, "Can't find the file: " + e.getMessage());
            System.exit(1);
        }
        catch (IOException e) {
            logger.log(Level.SEVERE, "Can't connect to the server: " + e.getMessage());
            System.exit(1);
        }
    }
}
