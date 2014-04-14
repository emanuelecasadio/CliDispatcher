package com.kopjra.beanstalkd.clidispatcher;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.concurrent.Future;

import org.apache.commons.exec.CommandLine;

import com.dzone.java.ProcessExecutor;
import com.surftools.BeanstalkClient.Job;
import com.surftools.BeanstalkClient.Client;
import com.surftools.BeanstalkClientImpl.ClientImpl;

/**
 * It keeps listening (asynchronously) to the assigned
 * queues and synchronously dispatches messages to the cli.
 *
 * It can't be asynchronous because of the
 * beanstalkd client.
 *
 * @author Emanuele Casadio
 *
 */
public class QueueListener implements Runnable {

	/*
	 * Maximum blocking time
	 */
	public /*final*/ int POLL_TIME = 15;

	/*
	 * Default values overridden by parameters
	 */
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
				 * Unfortunately I cannot be ready for another
				 * process because I cannot share the same
				 * beanstalkd session, so I wait for the result.
				 */

				String cmd;
				try {
					cmd = new String(job.getData(),"UTF-8");
					/**
					 * @todo Verify if the objid is still valid aka the message is still reserved and
					 * hasn't already been released
					 */
					c.touch(job.getJobId()); // More time, just in case (mostly useless)

					/*
					 *  I don't launch the command: I launch a new bash shell that executes the command
					 */
					/*CommandLine cl = new CommandLine("bash");
					cl.addArgument("-c");
					cl.addArgument(cmd);*/
					CommandLine cl = CommandLine.parse(cmd);
					try {
						DummyProcessExecutorHandler dpeh = new DummyProcessExecutorHandler();
						int watchdog_timer = Integer.parseInt(args[5])*1000; // MILLISECONDS!

						Future<Long> result = ProcessExecutor.runProcess(cl, dpeh, watchdog_timer);
						System.out.println("Executing command "+cl.getExecutable());
						Long lresult = result.get(); // This call is synchronous/blocking
						System.out.println("Result code "+lresult);
						if(lresult!=ProcessExecutor.WATCHDOG_EXIT_VALUE){
							c.delete(job.getJobId());
							System.out.println("Job deleted"); // Everything's good
						} else {
							c.release(job.getJobId(), PRIORITY, DELAY);
							System.out.println("Job released"); // Not good
						}
					} catch (Exception e){
						c.release(job.getJobId(), PRIORITY, DELAY); // Not good
						e.printStackTrace(System.err);
					}

				} catch (UnsupportedEncodingException e) {
					c.release(job.getJobId(), PRIORITY, DELAY); // Not good
					e.printStackTrace(System.err);
				} finally {
					running_number.decrease(); // Eventually release the lock
				}
			} else {
				/* I am not authorized to run this command so
				 * I release this job back into the queue with
				 * priority 2000 (urgent is <1024), after DELAY seconds
				 */
				c.release(job.getJobId(), PRIORITY, DELAY);
			}
		}
	}
}
