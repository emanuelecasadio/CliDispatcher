package com.kopjra.clidispatcher.beanstalkd;

import java.io.UnsupportedEncodingException;

import org.slf4j.Logger;

import com.kopjra.clidispatcher.Job;

public class BDJob implements Job {

	com.surftools.BeanstalkClient.Job innerjob;
	private Logger logger;
	
	public BDJob(com.surftools.BeanstalkClient.Job i){
		innerjob = i;
	}

	public BDJob(){
		
	}
	
	@Override
	public String getBody() {
		try {
			return new String(innerjob.getData(),"UTF-8");
		} catch (UnsupportedEncodingException e) {
			logger.error("Unsupported encoding", e);
		}
		return null;
	}

}
