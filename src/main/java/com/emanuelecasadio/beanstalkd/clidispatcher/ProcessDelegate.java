/**
 * 
 */
package com.emanuelecasadio.beanstalkd.clidispatcher;

import java.io.IOException;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.apache.commons.exec.CommandLine;

import com.dzone.java.ProcessExecutor;
import com.dzone.java.ProcessExecutorHandler;

/**
 * @author Emanuele Casadio
 *
 */
public class ProcessDelegate implements Runnable {

	public /*final*/ long TIMEOUT = 30;
	public /*final*/ int PRIORITY = 2000;
	public /*final*/ int DELAY_SECONDS = 30;
	
	private SharedClient sc;
	private String command;
	private ProcessExecutorHandler peh;
	private long jobid;
	
	public ProcessDelegate(long jobid, SharedClient sc, String command, String[] args){
		this.sc = sc;
		this.jobid = jobid;
		this.command = command;
		
		TIMEOUT = Integer.parseInt(args[6]);
		PRIORITY = Integer.parseInt(args[3]);
		DELAY_SECONDS = Integer.parseInt(args[7]);
		
		peh = new DummyProcessExecutorHandler();
	}
	
	@Override
	public void run(){
		CommandLine cl = new CommandLine(command);
		try {
			Future<Long> result = ProcessExecutor.runProcess(cl, peh, TIMEOUT);
			Long l = result.get();
			if(l!=ProcessExecutor.WATCHDOG_EXIT_VALUE){
				sc.delete(jobid);
			} else {
				sc.release(jobid, PRIORITY, DELAY_SECONDS);
			}
		} catch (IOException | CancellationException | InterruptedException | ExecutionException e) {
			sc.release(jobid, PRIORITY, DELAY_SECONDS);
		}
	}

}
