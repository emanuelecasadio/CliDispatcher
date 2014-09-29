package com.kopjra.clidispatcher.sqs;

import java.util.ArrayList;
import java.util.List;

import com.amazonaws.auth.AWSCredentials;

public class BufferedSQSBulkSender extends SQSClient{

	private int max_parts;
	private int max_messages;
	List<String> parts;
	List<String> messages;
	private String message_heading;
	
	public BufferedSQSBulkSender(AWSCredentials credentials, String endpoint, int maxparts, int maxmessages, String mes_head) {
		super(credentials, endpoint);
		max_parts = maxparts;
		max_messages = maxmessages;
		this.parts = new ArrayList<String>(max_parts);
		this.messages = new ArrayList<String>(max_messages);
		message_heading = mes_head;
	}
	
	public void addMessage(List<String> p){
		String mess = new String();
		if(!message_heading.isEmpty()){
			mess = message_heading;
		}
		
		for(String s : p){
			if(!mess.isEmpty()){
				s = " "+s;
			}
			mess = mess + s;
		}
		messages.add(mess);
		if(messages.size()==max_messages){
			this.sendBulkMessage(messages);
			messages.clear();
		}
	}
	
	public void sendPart(String p){
		parts.add(p);
		if(parts.size()==max_parts){
			addMessage(parts);
			parts.clear();
		}
	}
	
	public void flush(){
		flushParts();
		flushMessages();
	}
	
	public void flushParts(){
		if(parts.size()!=0){
			addMessage(parts);
			parts.clear();
		}
	}
	
	public void flushMessages(){
		if(messages.size()!=0){
			this.sendBulkMessage(messages);
			messages.clear();				
		}
	}
	
}
