package com.emanuelecasadio.beanstalkd.clidispatcher;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.concurrent.Future;

import org.apache.commons.exec.CommandLine;

import com.dzone.java.ProcessExecutor;
import com.surftools.BeanstalkClient.Job;
import com.surftools.BeanstalkClient.Client;
import com.surftools.BeanstalkClientImpl.ClientImpl;

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

	private Client c;
	//private List<String> queues;
	//private int port;
	//private String ipaddr;	
	private RunningProcessesHelper running_number;
	private DaemonWrapper daemon;
	private String[] args;
	
	public QueueListener(DaemonWrapper daemon, String ipaddr, int port, List<String> queues, RunningProcessesHelper running_number, String[] args){
		this.c = new ClientImpl(ipaddr, port, false);
		//this.ipaddr = ipaddr;
		//this.port = port;
		//this.queues = queues;
		this.running_number = running_number;
		this.daemon = daemon;
		this.args = args;
		
		PRIORITY = Integer.parseInt(args[3]);
		DELAY = Integer.parseInt(args[6]);
		
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
		Job job = null;
		while(!daemon.isStopped()){
			do{
				try{
					job = c.reserve(POLL_TIME);
				} catch(Exception e) { e.printStackTrace(System.err); }
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
					
					CommandLine cl = CommandLine.parse(cmd);
					try {
						Future<Long> result = ProcessExecutor.runProcess(cl, new DummyProcessExecutorHandler(), Integer.parseInt(args[5]));
						System.out.println("Executing command "+cl.getExecutable());
						Long l = result.get();
						System.out.println("Result code "+l);
						if(l!=ProcessExecutor.WATCHDOG_EXIT_VALUE){
							c.delete(job.getJobId());
							System.out.println("Job deleted");
						} else {
							c.release(job.getJobId(), PRIORITY, DELAY);
							System.out.println("Job released");
						}
					} catch (Exception e){
						c.release(job.getJobId(), PRIORITY, DELAY);
						e.printStackTrace(System.err);
					}
					
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
