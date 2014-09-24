package com.kopjra.clidispatcher.sqs;

import com.amazonaws.services.sqs.model.Message;
import com.kopjra.clidispatcher.Job;

public class SQSJob implements Job {

	Message message;
	
	public SQSJob(Message m){
		message = m;
	}

	@Override
	public String getBody() {
		return message.getBody();
	}

}
