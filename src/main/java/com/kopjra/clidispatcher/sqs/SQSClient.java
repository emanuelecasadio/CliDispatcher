package com.kopjra.clidispatcher.sqs;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import com.amazonaws.services.sqs.model.SendMessageBatchRequestEntry;
import com.kopjra.clidispatcher.Job;
import com.kopjra.clidispatcher.QueueClient;

public class SQSClient implements QueueClient {
	
	private AmazonSQSClient c;
	private String queueUrl;
	private String dead_letter_queue;
	
	public SQSClient(AWSCredentials credentials, String endpoint){
		c = new AmazonSQSClient(credentials);
		c.setEndpoint(endpoint);
	}
	
	@Override
	public void deleteJob(Job j) {
		c.deleteMessage(queueUrl, ((SQSJob)j).message.getReceiptHandle());
	}

	@Override
	public void buryJob(Job j) {
		if(dead_letter_queue!=null && !dead_letter_queue.isEmpty()){
			c.sendMessage(dead_letter_queue, j.getBody());
			c.deleteMessage(queueUrl, ((SQSJob)j).message.getReceiptHandle());
		}
	}

	@Override
	public void releaseJob(Job j) {
		c.changeMessageVisibility(queueUrl, ((SQSJob)j).message.getReceiptHandle(), 0);
	}

	@Override
	public Job reserveJob() {
		SQSJob job=null;
		ReceiveMessageResult result = c.receiveMessage(queueUrl);
		if(result.getMessages().size()>=1){
			job = new SQSJob(result.getMessages().get(0));
		}
		return job;
	}

	@Override
	public void setAttribute(String key, Object value) {
		switch(key){
			case "deadletterqueueurl":
				dead_letter_queue = (String) value;
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
		queueUrl = queue;
	}
	
	/**
	 * @todo Roba schifosa!
	 */
	public void sendBulkMessage(List<String> messages){
		List<SendMessageBatchRequestEntry> entries = new ArrayList<>(10);
		Integer i=0;
		for(String m : messages){
			entries.add(new SendMessageBatchRequestEntry((i++).toString(), m));
		}
		c.sendMessageBatch(queueUrl, entries);
	}
	
}
