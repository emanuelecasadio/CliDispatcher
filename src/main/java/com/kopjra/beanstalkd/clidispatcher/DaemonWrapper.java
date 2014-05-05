package com.kopjra.beanstalkd.clidispatcher;

import org.apache.commons.daemon.Daemon;
import org.apache.commons.daemon.DaemonContext;
import org.apache.commons.daemon.DaemonInitException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dzone.java.ProcessExecutor;

/**
 * OS daemon wrapper class
 *
 * Thanks to Sheldon Neilson (za.co.neilson) for the original skeleton
 *
 * @todo Override MAX_WAIT with the proper parameter
 * @author Emanuele Casadio
 */
public class DaemonWrapper implements Daemon {

	private Thread serverThread;
	private boolean stopped;
	private Object dumblock;
	private final long MAX_WAIT = 120000; // Maximum waiting time in milliseconds
	private Logger logger;

	public synchronized boolean isStopped(){
		return stopped;
	}

	private synchronized void setStopped(boolean s){
		stopped = s;
	}

	public void init(DaemonContext daemonContext) throws DaemonInitException, Exception {
		System.setProperty(org.slf4j.impl.SimpleLogger.DEFAULT_LOG_LEVEL_KEY, "INFO");
		logger = LoggerFactory.getLogger(DaemonWrapper.class);
		logger.debug("DaemonWrapper initializing");
		String[] args = daemonContext.getArguments();
		dumblock = new Object();
		stopped = false;
		serverThread = new CliDispatcher(this,args,dumblock);
		logger.debug("DaemonWrapper initialized");
	}

	public void start() throws Exception {
		logger.debug("DaemonWrapper starting");
		this.setStopped(false);
		serverThread.start();
		logger.debug("DaemonWrapper started");
	}

	public void stop() throws Exception {
		logger.debug("DaemonWrapper stopping");
		this.setStopped(true);
		synchronized (dumblock) {
			dumblock.notifyAll();
		}
		try{
			ProcessExecutor.shutdownExecutor();
			serverThread.join(MAX_WAIT);
		}catch(InterruptedException e){
			logger.error(e.getMessage(),e);
			throw e;
		}
		logger.debug("DaemonWrapper stopped");
	}

	public void destroy() {
		serverThread = null;
	}
}