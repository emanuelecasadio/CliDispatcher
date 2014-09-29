package com.kopjra.clidispatcher.beanstalkd;

import java.util.Map;
import java.util.Set;

import com.kopjra.clidispatcher.Job;
import com.kopjra.clidispatcher.QueueClient;
import com.surftools.BeanstalkClient.Client;
import com.surftools.BeanstalkClientImpl.ClientImpl;

public class BDClient implements QueueClient {
	
	private Client c;
	private String host;
	private int port;
	private int priority = 2000;
	private int delay = 0;
	private int poll_time = 20;
	
	public BDClient(String host, int port){
		c = new ClientImpl(host, port, false);
	}
	
	@Override
	public void deleteJob(Job j) {
		c.delete(((BDJob)j).innerjob.getJobId());
	}

	@Override
	public void buryJob(Job j) {
		c.bury(((BDJob)j).innerjob.getJobId(), priority);
	}

	@Override
	public void releaseJob(Job j) {
		c.release(((BDJob)j).innerjob.getJobId(), priority, delay);
	}

	@Override
	public Job reserveJob() {
		BDJob job = new BDJob();
		job.innerjob = c.reserve(poll_time);
		return job;
	}

	@Override
	public void setAttribute(String key, Object value) {
		switch(key){
			case "priority":
				priority = (Integer) value;
				break;
			case "delay":
				delay = (Integer) value;
				break;
			case "poll_time":
				poll_time = (Integer) value;
				break;				
		}
	}

	@Override
	public void setAttributes(Map<String, Object> map) {
		for(String key : map.keySet()){
			this.setAttribute(key, map.get(key));
		}
	}
	
	@Override
	public Set<String> getAttributes() {
		// TODO Auto-generated method stub
		return null;
	}	
	
	@Override
	public Object getAttribute(String key) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setQueue(String queue) {
		c.watch(queue);
	}	

}
