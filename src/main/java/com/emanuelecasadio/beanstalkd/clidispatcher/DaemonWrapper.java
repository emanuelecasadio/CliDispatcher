package com.emanuelecasadio.beanstalkd.clidispatcher;

import org.apache.commons.daemon.Daemon;
import org.apache.commons.daemon.DaemonContext;
import org.apache.commons.daemon.DaemonInitException;

/**
 * OS daemon wrapper class
 * 
 * Thanks to Sheldon Neilson (za.co.neilson) for the original skeleton
 * 
 * @author Emanuele Casadio
 */
public class DaemonWrapper implements Daemon {

    private Thread serverThread; 
    private boolean stopped;
    private Object dumblock;
    private final long MAX_WAIT = 60000; // Maximum waiting time in milliseconds
    
    public synchronized boolean isStopped(){
    	return stopped;
    }
    
    private synchronized void setStopped(boolean s){
    	stopped = s;
    }
   
    public void init(DaemonContext daemonContext) throws DaemonInitException, Exception {
        String[] args = daemonContext.getArguments();
        dumblock = new Object();
        stopped = false;
        serverThread = new CliDispatcher(this,args,dumblock);
    }

    public void start() throws Exception {
    	this.setStopped(false);
        serverThread.start();
    }

    public void stop() throws Exception {
    	this.setStopped(true);
    	synchronized (dumblock) {
    		dumblock.notifyAll();
		}
        try{
            serverThread.join(MAX_WAIT);
        }catch(InterruptedException e){
            System.err.println(e.getMessage());
            throw e;
        }
    }
   
    public void destroy() {
        serverThread = null;
    }
}