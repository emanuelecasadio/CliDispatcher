package com.kopjra.clidispatcher;

import com.dzone.java.ProcessExecutorHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DummyProcessExecutorHandler implements ProcessExecutorHandler {

	private Logger logger;
	
	public DummyProcessExecutorHandler(){
		logger = LoggerFactory.getLogger(DummyProcessExecutorHandler.class);
	}
	
	public void onStandardOutput(String msg) {
		logger.info(msg);
	}

	public void onStandardError(String msg) {
		logger.error(msg);
	}

}
