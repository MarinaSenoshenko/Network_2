package main.java.client;

import java.io.*;
import java.net.*;
import java.util.logging.*;

public class Main {	
    private final static Logger logger = Logger.getLogger(Main.class.getName());
    
    private static final int CLIENT_ARGS_COUNT = 3;	
    
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
    	
    	int port = 0;
    	
    	try {
            port = Integer.parseInt(args[2]);
    	} catch (NumberFormatException ex) {
    		logger.log(Level.SEVERE, ex.getMessage() + ". It can't be port number, please write integer");
    		System.exit(1);
    	}
    	
    	String filePath = args[0];

        InetAddress serverAddr = null;
        try {
        	serverAddr = InetAddress.getByName(args[1]);
        	
        }
        catch (UnknownHostException e) {
            logger.log(Level.SEVERE, "Can't recognize host: " + e.getMessage());
            System.exit(1);
        }

        new Client(logger, filePath, serverAddr, port);
    }
   
}
