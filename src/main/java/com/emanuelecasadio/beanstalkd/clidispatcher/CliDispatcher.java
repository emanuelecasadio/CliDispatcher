package com.emanuelecasadio.beanstalkd.clidispatcher;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
	private Object dl;
	
	public CliDispatcher(DaemonWrapper daemon, String[] args, Object dumblock){
		this.daemon = daemon;
		this.args = args;
		this.dl = dumblock;
	}
	
	@Override
	public void run() {
		String ipaddr = args[0];
		int port = Integer.parseInt(args[1]);
		
		/**
		 * The beanstalkd client cannot be totally synchronous because it has to 
		 * detach quite often in order to poll what's going on on his
		 * master (e.g.: a stop has been requested), so the third
		 * parameter is set to false
		 */
		Client bc = new ClientImpl(ipaddr, port, false);
		
		List<String> queues = bc.listTubes();
		int maxprocs = Integer.parseInt(args[2]);
		RunningProcessesHelper running_number = new RunningProcessesHelper(0, maxprocs);
		Set<Thread> listeners = new HashSet<Thread>();
		for(int i=0;i<maxprocs;i++){
			QueueListener listener = new QueueListener(daemon, ipaddr, port, queues, running_number, args);
			Thread t = new Thread(listener);
			listeners.add(t);
			t.start();			
		}
		
		bc.close();
		bc = null;
		
		synchronized (dl) {
			while(!daemon.isStopped()){
				try {
					dl.wait();
				} catch (InterruptedException e) { }
			}
		}
		running_number.block();
		
	}
	
}
