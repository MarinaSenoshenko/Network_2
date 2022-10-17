package main.java.server;

import java.io.*;
import java.util.logging.*;
import java.net.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;


public class Server implements Runnable {
    protected Logger logger;
    private int thisThreadNumber; 
    private Socket socket;
    private EveryThreeSecondTimer timer;
    private static AtomicInteger globalNumber = new AtomicInteger(-1); 
    private ScheduledExecutorService sceduledThreadPool = Executors.newScheduledThreadPool(1);

    private static final long REPEAT_LOG_TIME = 3000;   
    private static final long MILLS_TO_SEC = 1000;   
    
    
    public Server(Logger logger, Socket socket) {   
    	this.logger = logger;
        this.socket = socket;
        this.thisThreadNumber = globalNumber.incrementAndGet();
        this.timer = new EveryThreeSecondTimer(logger, thisThreadNumber);
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
            
            String fileCounter = "";
            int fileNumber = 1;
            
            while (new File("uploads/" + fileName + fileCounter).isFile()) {
            	fileCounter = "(" + fileNumber + ")";
            	fileNumber++;
            }
            
            fileName += fileCounter;

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
