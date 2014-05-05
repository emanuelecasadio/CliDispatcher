package com.kopjra.beanstalkd.clidispatcher.test;

import org.apache.commons.daemon.DaemonController;

public class DummyDaemonContext implements
		org.apache.commons.daemon.DaemonContext {

	private String[] args;
	private DaemonController dc;
	
	public DummyDaemonContext(String[] args, DaemonController dc){
		this.args = args;
		this.dc = dc;
	}
	
	public String[] getArguments() {
		return args;
	}

	public DaemonController getController() {
		return dc;
	}

}
