package com.kopjra.beanstalkd.clidispatcher.test;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.apache.commons.daemon.DaemonInitException;

import com.kopjra.clidispatcher.DaemonWrapper;

public class DaemonTest {

	public static void main(String[] args) {
		DummyDaemonContext ddc = new DummyDaemonContext(args, null);
		DaemonWrapper dw = new DaemonWrapper();
		try {
			System.out.println("DaemonWrapper is initializing");
			System.out.println("You can stop it by pressing RETURN, any time");
			System.out.println("Starting in 5 seconds...");
			Thread.sleep(5000);
			dw.init(ddc);
			dw.start();
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			String s = br.readLine();
			System.out.println("Stopping DaemonWrapper...");
			dw.stop();
		} catch (DaemonInitException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
