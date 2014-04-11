package com.emanuelecasadio.beanstalkd.clidispatcher;

import java.io.UnsupportedEncodingException;
import java.util.List;
import com.surftools.BeanstalkClient.Job;

/**
 * It keeps listening to the assigned queue and 
 * asynchronously dispatches messages to the cli.
 * 
 * @author Emanuele Casadio
 *
 */
public class QueueListener implements Runnable {
	
	public /*final*/ int POLL_TIME = 15;
	public /*final*/ int PRIORITY = 2000;
	public /*final*/ int DELAY = 30;

	private SharedClient c;
	private List<String> queues;
	private int port;
	private String ipaddr;	
	private RunningProcessesHelper running_number;
	private DaemonWrapper daemon;
	private String[] args;
	
	public QueueListener(DaemonWrapper daemon, String ipaddr, int port, List<String> queues, RunningProcessesHelper running_number, String[] args){
		this.c = new SharedClient(ipaddr, port, false);
		this.ipaddr = ipaddr;
		this.port = port;
		this.queues = queues;
		this.running_number = running_number;
		this.daemon = daemon;
		this.args = args;
		
		PRIORITY = Integer.parseInt(args[4]);
		DELAY = Integer.parseInt(args[7]);
		
		for (String queue : queues) {
			this.c.watch(queue);
		}
	}
	
	/**
	 * It's silly, but I always have to verify if the daemon received the
	 * stop command, after every possible waiting time.
	 */
	@Override
	public void run(){
		Job job;
		while(!daemon.isStopped()){
			do{
				job = c.reserve(POLL_TIME);
			}while(job==null && !daemon.isStopped());
			
			if(running_number.increase()){
				/* I am authorized to run this command
				 * Maybe I've slept a lot, so I reserve more
				 * time to do this job and I keep track of its
				 * process so I can delete if from the queue
				 * when it's done.
				 * In the meanwhile, I'm ready to get another 
				 * message, yay!
				 */
				
				String cmd;
				try {
					cmd = new String(job.getData(),"UTF-8");
					c.touch(job.getJobId());
					ProcessDelegate delegate = new ProcessDelegate(job.getJobId(), c, cmd, args);
					Thread t = new Thread(delegate);
					t.start();					
				} catch (UnsupportedEncodingException e) {
					c.release(job.getJobId(), PRIORITY, DELAY);
				}
			} else {
				/* I am not authorized to run this command so
				 * I release this job back into the queue with
				 * priority 2000 (urgent is <1024), after 30s
				 */
				c.release(job.getJobId(), PRIORITY, DELAY);
			}
		}
	}
}
