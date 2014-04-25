package com.kopjra.beanstalkd.clidispatcher;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.surftools.BeanstalkClient.Client;
import com.surftools.BeanstalkClientImpl.ClientImpl;

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

	private DaemonWrapper daemon;
	private String[] args;
	private Object dumblock;
	private Logger logger;

	public CliDispatcher(DaemonWrapper daemon, String[] args, Object dumblock){
		logger = LoggerFactory.getLogger(CliDispatcher.class);
		this.daemon = daemon;
		this.args = args;
		this.dumblock = dumblock;
		logger.debug("CliDispatcher created");
	}

	@Override
	public void run() {
		logger.debug("Running");
		String ipaddr = args[0];
		int port = Integer.parseInt(args[1]);

		/**
		 * The beanstalkd client cannot be totally synchronous because it has to 
		 * detach quite often in order to poll what's going on on his
		 * master (e.g.: a stop has been requested), so the third
		 * parameter is set to false
		 */
		Client beanstalkclient = new ClientImpl(ipaddr, port, false);
		logger.debug("Created Beanstalkd Client: "+ipaddr+":"+port);
		
		List<String> queues = beanstalkclient.listTubes();
		int maxprocs = Integer.parseInt(args[2]);
		RunningProcessesHelper running_number = new RunningProcessesHelper(0, maxprocs);
		Set<Thread> listeners = new HashSet<Thread>();
		
		/*
		 * Creates maxprocs different listeners to the beanstalkd queue/tube
		 */
		for(int i=0;i<maxprocs;i++){
			logger.debug("Starting QueueListener");
			QueueListener listener = new QueueListener(daemon, ipaddr, port, queues, running_number, args);
			Thread t = new Thread(listener);
			listeners.add(t);
			t.start();
		}

		logger.debug("Closing Beanstalkd connection");
		beanstalkclient.close();
		beanstalkclient = null;

		synchronized (dumblock) {
			while(!daemon.isStopped()){
				try {
					/*
					 *  Locked here until the DaemonWrapper uses the dumblock.notifyAll();
					 *  or something strange on the VM happens.
					 */
					dumblock.wait();
				} catch (InterruptedException e) { }
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
