package com.dzone.java;

import org.apache.commons.exec.*;
import org.apache.commons.exec.Executor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kopjra.beanstalkd.clidispatcher.QueueListener;

import java.io.IOException;
import java.util.concurrent.*;

/**
 * A wrapper for the the Apache Commons Exe library.
 * 
 * It provides the following features:
 * <ol>
 * <li>Execute the process asynchronously;</li>
 * <li>Ability to abort the process execution;</li>
 * <li>Ability to wait for process completion;</li>
 * <li>On process output notifications;</li>
 * <li>Ability to kill the process in case it hung;</li>
 * <li>Get the process exit code.</li>
 * </ol>
 * 
 * @author      Nadav Azaria
 */
public class ProcessExecutor {
	private static ExecutorService executor = null;
	private static Logger logger = LoggerFactory.getLogger(ProcessExecutor.class);
	
    public static final Long  WATCHDOG_EXIT_VALUE = -999L;

    public static Future<Long> runProcess(final CommandLine commandline, final ProcessExecutorHandler handler, final long watchdogTimeout) throws IOException, RejectedExecutionException{
    	if(executor==null){
    		logger.debug("Initializing new cached thread pool");
    		executor = Executors.newCachedThreadPool();
    		logger.debug("Cached thread pool created");
    	}
    	
        logger.debug("Submitting new callable to the process pool");
        Future<Long> result =  executor.submit(new ProcessCallable(watchdogTimeout, handler, commandline));
        logger.debug("Callable submitted to the process pool");
        return result; 
  }
    
   /**
    * Shuts down the executor so a new pool can (maybe) be prepared 
    */
   public static void shutdownExecutor(){
	   if(executor!=null){
		   logger.debug("Shutting down the current pool");
		   executor.shutdown();
		   executor = null;		   
	   }
   }
    
   private static class ProcessCallable implements Callable<Long>{

        private long watchdogTimeout;
        private ProcessExecutorHandler handler;
        private CommandLine commandline;

        private ProcessCallable(long watchdogTimeout, ProcessExecutorHandler handler, CommandLine commandline) {
            this.watchdogTimeout = watchdogTimeout;
            this.handler = handler;
            this.commandline = commandline;
            logger.debug("ProcessCallable created");
        }

        //@Override
        public Long call() throws Exception {
            Executor executor = new DefaultExecutor();
            executor.setProcessDestroyer(new ShutdownHookProcessDestroyer());
            ExecuteWatchdog watchDog = new ExecuteWatchdog(watchdogTimeout);
            executor.setWatchdog(watchDog);
            executor.setStreamHandler(new PumpStreamHandler(new MyLogOutputStream(handler, true),new MyLogOutputStream(handler, false)));
            Long exitValue;
            try {
                exitValue =  new Long(executor.execute(commandline));

            } catch (ExecuteException e) {
                exitValue =  new Long(e.getExitValue());
            }
            if(watchDog.killedProcess()){
                exitValue =WATCHDOG_EXIT_VALUE;
            }

            return exitValue;
        }
    }

    private static class MyLogOutputStream extends  LogOutputStream{

        private ProcessExecutorHandler handler;
        private boolean forewordToStandardOutput;

        private MyLogOutputStream(ProcessExecutorHandler handler, boolean forewordToStandardOutput) {
            this.handler = handler;
            this.forewordToStandardOutput = forewordToStandardOutput;
        }

        @Override
        protected void processLine(String line, int level) {
            if (forewordToStandardOutput){
                handler.onStandardOutput(line);
            }
            else{
                handler.onStandardError(line);
            }
        }
    }


}