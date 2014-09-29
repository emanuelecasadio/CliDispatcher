package com.kopjra.clidispatcher;

import java.util.Arrays;
import java.util.concurrent.Future;
import org.apache.commons.exec.CommandLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.dzone.java.ProcessExecutor;

public class QueueListener implements Runnable {

	protected Logger logger;
	private RunningProcessesHelper running_process_helper;
	private Stoppable daemon;
	private int ttr;
	private QueueClient client;
	
	public QueueListener(Stoppable d, String q, RunningProcessesHelper r, QueueClient c, int t){
		logger = LoggerFactory.getLogger(QueueListener.class);
		this.daemon = d;
		this.running_process_helper = r;
		this.client = c;
		c.setQueue(q);
		this.ttr = t;
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
					job = client.reserveJob();
				} catch(Exception e) {
					logger.error("Error reserving job",e);
				}
			}while(job==null && !daemon.isStopped());

			if(running_process_helper.increase()){
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
					cmd = job.getBody();
					/**
					 * @todo Verify if the objid is still valid aka the message is still reserved and
					 * hasn't already been released
					 */
					//c.touch(job.getJobId())
					
					CommandLine cl = CommandLine.parse(cmd);
					try {
						DummyProcessExecutorHandler dpeh = new DummyProcessExecutorHandler();
						long watchdog_timer = ttr*1000; // MILLISECONDS!
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
						
						if(lresult==ProcessExecutor.WATCHDOG_EXIT_VALUE){
							client.buryJob(job);							
							logger.error("result=("+lresult+"),job=("+cl.getExecutable()+" "+append_arguments+") watchdog forced to kill, buried"); // Not good							
						} else if(lresult!=0){
							client.buryJob(job);							
							logger.error("result=("+lresult+"),job=("+cl.getExecutable()+" "+append_arguments+") error occured, buried"); // Not good														
						} else {
							client.deleteJob(job);
							logger.info("result=("+lresult+"),job=("+cl.getExecutable()+" "+append_arguments+") completed, deleted"); // Everything's good							
						}
						//+cl.getExecutable()+" "+append_arguments+
						
					} catch (Exception e){
						// Release
						client.releaseJob(job);
						logger.error("result=(NULL), Generic error happened, job released into queue",e);
					}

				} finally {
					running_process_helper.decrease(); // Eventually release the lock
				}
			} else {
				/* I am not authorized to run this command so
				 * I release this job back into the queue with
				 * priority 2000 (urgent is <1024), after DELAY seconds
				 */
				logger.info("Unauthorised to execute the job");
				if(job!=null){
					client.releaseJob(job);
				}
			}
		}
	}

}
