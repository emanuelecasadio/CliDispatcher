package com.emanuelecasadio.beanstalkd.clidispatcher;

import com.dzone.java.ProcessExecutorHandler;

public class DummyProcessExecutorHandler implements ProcessExecutorHandler {

	public void onStandardOutput(String msg) {
		;
	}

	public void onStandardError(String msg) {
		;
	}

}
