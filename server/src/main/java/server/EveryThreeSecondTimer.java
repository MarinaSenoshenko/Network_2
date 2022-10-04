package main.java.server;

import java.util.logging.*;

public class EveryThreeSecondTimer implements Runnable {
    protected Logger logger;
    private long speedCount;
    private long localTime;
    private int thisThreadNumber; 
	
    private static final long REPEAT_LOG_TIME = 3000;   
    private static final long MILLS_TO_SEC =1000;   
    
    public EveryThreeSecondTimer(Logger logger, int thisThreadNumber) {
	this.speedCount = 0;
	this.logger = logger;
	this.localTime = REPEAT_LOG_TIME;
	this.thisThreadNumber = thisThreadNumber;
    }

    @Override
    public void run() {
        logger.log(Level.INFO, "Server Thread №" + thisThreadNumber + ": Speed right now is " + MILLS_TO_SEC * speedCount / localTime + " bytes per second (" + speedCount + " bytes)");	
    }
	
    public void setSeedCount(long speedCount) {
	this.speedCount = speedCount;
    }

    public void setLocalTime(long localTime) {
	this.localTime = localTime;		
    }
}
