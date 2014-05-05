package com.kopjra.beanstalkd.clidispatcher.test;

import org.apache.commons.daemon.DaemonInitException;

import com.kopjra.beanstalkd.clidispatcher.DaemonWrapper;

public class DaemonTest {

	public static void main(String[] args) {
		DummyDaemonContext ddc = new DummyDaemonContext(args, null);
		DaemonWrapper dw = new DaemonWrapper();
		try {
			dw.init(ddc);
			dw.start();
		} catch (DaemonInitException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
