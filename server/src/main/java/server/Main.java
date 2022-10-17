package main.java.server;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class Main {
	private final static Logger logger = Logger.getLogger(Main.class.getName());
	private static final int SERVER_ARGS_COUNT = 1;
	private static ExecutorService threadPool = Executors.newCachedThreadPool();
	
	
	 public static void main(String[] args) {
	    	
	    	LogManager logManager = LogManager.getLogManager();
	        try {
	            logManager.readConfiguration(new FileInputStream("src/main/resources/logServer.properties"));
	        } catch (IOException ex) {
	            logger.log(Level.SEVERE, "Cannot get log configuration!" + ex.getMessage());
	        }
	    	
	    	if (args.length < SERVER_ARGS_COUNT) {
	            logger.log(Level.SEVERE, "Not enough arguments\nYou should type port number");
	            System.exit(1);
	        }    	
	    	
	    	int port = 0;
	    	
	    	try {
	            port = Integer.parseInt(args[0]);
	    	} catch (NumberFormatException ex) {
	    		logger.log(Level.SEVERE, ex.getMessage() + ". It can't be port number, please write integer");
	    		System.exit(1);
	    	}
	    	
			try (ServerSocket serverSocket = new ServerSocket(port)) {
				logger.log(Level.INFO, "The server started working");
	      
				while (!serverSocket.isClosed()) {
					Server myServer = new Server(logger, serverSocket.accept());                                     
					   threadPool.submit(() -> myServer.run());  
				}
				
	        }
	        catch (Exception e) {
	            logger.log(Level.SEVERE, "Can't receive connections: " + e.getMessage());
			}    	       
	    }
}
