package com.kopjra.clidispatcher;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.auth.PropertiesCredentials;
import com.kopjra.ec2.EC2Informer;

/**
 * Command line dispatcher for beanstalkd
 *
 * Listens to every queue available on the beanstalkd server
 * and runs every available message as a shell command on the
 * provided location, with an upper bound on the number of
 * concurrent running processes.
 *
 * @author Emanuele Casadio
 *
 */
public class CliDispatcher extends Thread {

	class SimpleStoppable implements Stoppable {
		private boolean stopped = false;
		public synchronized boolean isStopped(){
			return stopped;
		}
		synchronized void setStopped(boolean s){
			stopped = s;
		}
	};
	
	private Stoppable daemon;
	private String[] args;
	private Object dumblock;
	private Logger logger;
	private final int POLL_TIME = 20;

	public CliDispatcher(String[] args){
		// Non-daemonized CliDispatcher
		logger = LoggerFactory.getLogger(CliDispatcher.class);
		this.args = args;
		this.dumblock = new Object();
		this.daemon = new SimpleStoppable();
		logger.debug("Non-daemonized CliDispatcher created");
	}
	
	public CliDispatcher(DaemonWrapper daemon, String[] args, Object dumblock){
		// Daemonized CliDispatcher
		logger = LoggerFactory.getLogger(CliDispatcher.class);
		this.daemon = daemon;
		this.args = args;
		this.dumblock = dumblock;
		logger.debug("Daemonized CliDispatcher created");
	}

	public static void main(String[] args){
		new CliDispatcher(args).start();
	}
	
	@Override
	public void run() {
		logger.debug("Running");
		
		HashMap<String,Object> map = new HashMap<>(5);
		String type = args[0];
		map.put("host", args[1]);
		map.put("port", Integer.parseInt(args[2]));
		String queue = args[3];
		map.put("deadletterqueueurl", args[4]);
		int maxprocs = Integer.parseInt(args[5]);
		map.put("priority", Integer.parseInt(args[6]));
		map.put("delay", Integer.parseInt(args[7]));
		int ttr = Integer.parseInt(args[8]);
		map.put("ttr", ttr);
		try {
			map.put("awscredentials", new PropertiesCredentials(
			  EC2Informer.class.getResourceAsStream( "/AwsCredentials.properties" )
			));
		} catch (IOException e1) {
			logger.error("Cannot get AWS credentials from file");
		}
		
		QueueClient client = QueueClientFactory.newQueueClient(type,map);
		client.setAttributes(map);
		
		RunningProcessesHelper running_number = new RunningProcessesHelper(0, maxprocs);
		Set<Thread> listeners = new HashSet<Thread>();
		
		/*
		 * Creates maxprocs different listeners to the beanstalkd queue/tube
		 */
		for(int i=0;i<maxprocs;i++){
			logger.debug("Starting QueueListener");
			QueueListener listener = new QueueListener(daemon, queue, running_number, client, ttr-1);
			Thread t = new Thread(listener);
			listeners.add(t);
			t.start();
		}

		synchronized (dumblock) {
			while(!daemon.isStopped()&&!this.isInterrupted()){
				try {
					/*
					 *  Locked here until the DaemonWrapper uses the dumblock.notifyAll();
					 *  or something strange on the VM happens.
					 */
					dumblock.wait();
				} catch (InterruptedException e) {
					if(daemon instanceof SimpleStoppable){
						((SimpleStoppable)daemon).setStopped(true);
					}
				}
			}
			for(Thread t : listeners){
				try {
					t.join(POLL_TIME+1);
				} catch (InterruptedException e) {
					t.stop();
				}
			}
		}
		/*
		 * Misleading name: blocks the process queue but in reality
		 * unlocks all the threads that are still waiting in the queue.
		 */
		logger.debug("Blocking the process queue");
		running_number.block();
		logger.debug("Terminated");
	}

}
