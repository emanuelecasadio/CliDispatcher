package com.emanuelecasadio.beanstalkd.clidispatcher;

import java.util.List;

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
	
	public CliDispatcher(DaemonWrapper daemon, String[] args){
		this.daemon = daemon;
		this.args = args;
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
		RunningProcessesHelper running_number = new RunningProcessesHelper(0, Integer.parseInt(args[2]));
		QueueListener listener = new QueueListener(daemon, ipaddr, port, queues, running_number, args);
		Thread t = new Thread(listener);
		t.start();
		
		bc.close();
		bc = null;
	}
	
}
