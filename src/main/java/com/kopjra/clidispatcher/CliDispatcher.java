package com.kopjra.clidispatcher;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.auth.PropertiesCredentials;
import com.kopjra.cloudwatch.CloudwatchInformer;
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
	private Properties properties;
	/**
	 * @todo Uniformare il poll time
	 */
	private int POLL_TIME = 20;

	private void commonConstructor(String[] args){
		logger = LoggerFactory.getLogger(CliDispatcher.class);
		this.args = args;
		
		// Retrieve arguments from parameter file
		InputStream inputStream = 
			    CloudwatchInformer.class.getResourceAsStream("/CliDispatcher.properties");
		Logger logger = LoggerFactory.getLogger(CloudwatchInformer.class);
		
		properties = new Properties();
	    try {
	    	properties.load(inputStream);
		} catch (IOException e) {
			logger.error("Unable to load CliDispatcher properties");
		}
	}
	
	public CliDispatcher(String[] args){
		// Non-daemonized CliDispatcher
		commonConstructor(args);
		this.dumblock = new Object();
		this.daemon = new SimpleStoppable();
		logger.debug("Non-daemonized CliDispatcher created");
	}
	
	public CliDispatcher(DaemonWrapper daemon, String[] args, Object dumblock){
		commonConstructor(args);
		// Daemonized CliDispatcher
		this.daemon = daemon;
		this.dumblock = dumblock;
		logger.debug("Daemonized CliDispatcher created");
	}

	public static void main(String[] args){
		CliDispatcher clidisp = new CliDispatcher(args);
		clidisp.start();
		
		// DA TESTARE BENE, PER ORA IGNORIAMO
		/*
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			String s;
			do{
				s=br.readLine();
			}while(!s.equals("exit"));
		} catch (IOException e) {
			;
		} finally {
			SimpleStoppable ss = (SimpleStoppable) clidisp.daemon;
			ss.setStopped(true);
			try {
				clidisp.join(60);
			} catch (InterruptedException e) {
				clidisp.stop();
			}
		}
		*/
		
	}
	
	@Override
	public void run() {
		logger.debug("Running");
		
		HashMap<String,Object> map = new HashMap<>(5);
		String type = properties.getProperty("type");
		map.put("host", properties.getProperty("host"));
		map.put("port", Integer.parseInt(properties.getProperty("port")));
		String queue = properties.getProperty("queue");
		map.put("deadletterqueue", properties.getProperty("deadletterqueue"));
		int maxprocs = Integer.parseInt(properties.getProperty("maxprocs"));
		map.put("priority", Integer.parseInt(properties.getProperty("priority")));
		map.put("delay", Integer.parseInt(properties.getProperty("delay")));
		int ttr = Integer.parseInt(properties.getProperty("ttr"));
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
