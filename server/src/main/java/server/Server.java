package main.java.server;

import java.io.*;
import java.util.logging.*;
import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;


public class Server implements Runnable {
	private int thisThreadNumber; 
    private Socket socket;
    private EveryThreeSecondTimer timer;
    private static AtomicInteger globalNumber = new AtomicInteger(-1); 
    private static final Logger logger = Logger.getLogger(Server.class.getName());
    
    private ScheduledExecutorService sceduledThreadPool = Executors.newScheduledThreadPool(1);
    private static ExecutorService threadPool = Executors.newCachedThreadPool();
    
    private static final int SERVER_ARGS_COUNT = 1;
    private static final long REPEAT_LOG_TIME = 3000;   
    private static final long MILLS_TO_SEC = 1000;   
    
    
    public Server(Socket socket) {        
        this.socket = socket;
        this.thisThreadNumber = globalNumber.incrementAndGet();
        this.timer = new EveryThreeSecondTimer(logger, thisThreadNumber);
    }

    
    public static void main(String[] args) {
    	
    	LogManager logManager = LogManager.getLogManager();
        try {
            logManager.readConfiguration(new FileInputStream("src/main/resources/logServer.properties"));
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "Cannot get log configuration!" + ex.getMessage());
        }
    	
    	if (args.length < SERVER_ARGS_COUNT) {
            logger.log(Level.SEVERE, "Not enough arguments\\nYou should type port numbert");
            System.exit(1);
        }
    	
        int port = Integer.parseInt(args[0]);
        
        try {
        	try (ServerSocket serverSocket = new ServerSocket(port)) {
				logger.log(Level.INFO, "The server started working");
      
				while (!serverSocket.isClosed()) {
				     Server myServer = new Server(serverSocket.accept());                                     
				     threadPool.submit(() -> myServer.run());  
				}
			}
        }
        catch (Exception e) {
            logger.log(Level.SEVERE, "Can't receive connections: " + e.getMessage());
		}    	       
    }
    
    private long getFileAndSaveTime(InputStream socketIn, FileOutputStream fileStream, long fileSize) throws SocketException, IOException {
    	long periodStart = System.currentTimeMillis(), start = periodStart ;
        long allCount = 0, speedCount = 0;
        byte[] buf = new byte[8192];
        int count;
        
        while (allCount < fileSize) {
            count = socketIn.read(buf);
            fileStream.write(buf, 0, count);

            allCount += count;
            speedCount += count;            
        }
        
        long finish = System.currentTimeMillis();
        timer.setSeedCount(speedCount);
        
        return finish - start;
    }


    @Override
    public void run() {
    	sceduledThreadPool.scheduleAtFixedRate(timer, 2, 3, TimeUnit.SECONDS);
    	
        File downloadedFile = null;
        FileOutputStream fileStream = null;
        try (OutputStream socketOut = socket.getOutputStream();
            InputStream socketIn = socket.getInputStream();
        		
            DataOutputStream socketDataOut = new DataOutputStream(socketOut);
            DataInputStream socketDataIn = new DataInputStream(socketIn))
        {
           
            String fileName = socketDataIn.readUTF();
            long fileSize = socketDataIn.readLong();

            downloadedFile = new File("uploads/" + fileName);
            downloadedFile.getParentFile().mkdirs();

            downloadedFile.createNewFile(); 
            fileStream = new FileOutputStream(downloadedFile, false);

            long time = getFileAndSaveTime(socketIn, fileStream, fileSize);
            
            byte msg = 100;
       
    		socketOut.write(msg);
    		fileStream.close();
            logger.log(Level.INFO, "Server Thread №" + thisThreadNumber + ": Successfully downloaded " + fileName + " with average speed " + MILLS_TO_SEC * fileSize / time + " bytes per second in " + time / 1000.0 + " seconds");
            
            timer.setLocalTime(time);
            if (time < REPEAT_LOG_TIME) {
            	timer.run();
            }
            sceduledThreadPool.shutdown();
        }
        catch (SocketException e) {
        	sceduledThreadPool.shutdown();
        	
            if (fileStream != null) {
                try {
                    fileStream.close();
                    if (downloadedFile.exists()) {
                        downloadedFile.delete();                        
                    }
                }
                catch (IOException e2)
                {
                    logger.log(Level.SEVERE, "Server Thread №" + thisThreadNumber + ": Can't close file output stream: " + e2.getMessage());
                }
            }
            logger.log(Level.SEVERE, "Server Thread №" + thisThreadNumber + ": Socket error: " + e.getMessage());
            
        }
        catch (IOException e) {
        	sceduledThreadPool.shutdown();
            logger.log(Level.SEVERE, "Server Thread №" + thisThreadNumber + ": Connection error: " + e.getMessage());
        }
        finally {
        	globalNumber.decrementAndGet();
        }
    }
}
