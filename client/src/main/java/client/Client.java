package main.java.client;

import java.io.*;
import java.net.*;
import java.util.logging.*;

public class Client {	
    protected Logger logger;
    private int port;
    private String filePath;
    private InetAddress serverAddr;
    
    public Client(Logger logger, String filePath, InetAddress serverAddr, int port) {
    	this.filePath = filePath;
    	this.serverAddr = serverAddr;
    	this.port = port;
    	this.logger = logger;
    	sendFile();
    } 

    private void sendFile() {
    	
        File file = new File("src/main/resources" + filePath);    	
    	if (!file.isFile()) { 
    	      logger.log(Level.SEVERE, "Can't find the file " + filePath);
              System.exit(1);
    	}

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
