package com.kopjra.beanstalkd.clidispatcher;

import com.dzone.java.ProcessExecutorHandler;

public class DummyProcessExecutorHandler implements ProcessExecutorHandler {

	public void onStandardOutput(String msg) {
		System.out.println(msg);
	}

	public void onStandardError(String msg) {
		System.err.println(msg);
	}

}
