package com.kopjra.beanstalkd.clidispatcher;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Future;

import org.apache.commons.exec.CommandLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	private Logger logger;

	public QueueListener(DaemonWrapper daemon, String ipaddr, int port, List<String> queues, RunningProcessesHelper running_number, String[] args){
		logger = LoggerFactory.getLogger(QueueListener.class);
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
	public void run(){
		Job job = null;
		while(!daemon.isStopped()){
			do{
				try{
					logger.debug("Reserving");
					job = c.reserve(POLL_TIME);
				} catch(Exception e) { logger.error("Error reserving job",e); }
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
					logger.debug("Executing new job");
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
						long watchdog_timer = Long.parseLong(args[5])*1000; // MILLISECONDS!
						Future<Long> result = ProcessExecutor.runProcess(cl, dpeh, watchdog_timer);
						
						logger.debug("Watchgod timeout is: "+watchdog_timer);
						String append_arguments = "";
						try{
							append_arguments = Arrays.toString(cl.getArguments());
						}catch(Exception e){
							append_arguments = "[could not fetch arguments]";
						}
						logger.info("Executing command: "+cl.getExecutable()+" "+append_arguments);
						
						Long lresult = result.get(); // This call is synchronous/blocking
						if(lresult!=ProcessExecutor.WATCHDOG_EXIT_VALUE){
							c.delete(job.getJobId());
							logger.info("result=("+lresult+"), Job "+cl.getExecutable()+" "+append_arguments+" completed, deleted from queue"); // Everything's good
						} else {
							c.release(job.getJobId(), PRIORITY, DELAY);
							logger.error("result=("+lresult+"), Watchdog forced job kill, job released into queue, reinit. pool "); // Not good
							ProcessExecutor.reinitializePool();
						}
					} catch (Exception e){
						c.release(job.getJobId(), PRIORITY, DELAY); // Not good
						logger.error("result=(NULL), Other error happened, job released into queue",e);
					}

				} catch (UnsupportedEncodingException e) {
					c.release(job.getJobId(), PRIORITY, DELAY); // Not good
					logger.error("Unsupported encoding for the job, job released into queue",e);
				} finally {
					running_number.decrease(); // Eventually release the lock
				}
			} else {
				/* I am not authorized to run this command so
				 * I release this job back into the queue with
				 * priority 2000 (urgent is <1024), after DELAY seconds
				 */
				logger.info("Unauthorised to execute the job");
				c.release(job.getJobId(), PRIORITY, DELAY);
			}
		}
	}
}
