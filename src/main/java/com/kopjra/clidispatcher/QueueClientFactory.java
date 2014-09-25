package com.kopjra.clidispatcher;

import java.util.Map;

import com.kopjra.clidispatcher.beanstalkd.BDClient;
import com.kopjra.clidispatcher.sqs.SQSClient;

public class QueueClientFactory {
	public enum QueueClientType {
		BD, SQS
	}
	
	public static QueueClient newQueueClient(QueueClientType type, Map<String,Object> cpars){
		switch(type){
		case BD:
			return new BDClient((String)cpars.get("host"), (Integer)cpars.get("port"));
		case SQS:
			return new SQSClient((com.amazonaws.auth.AWSCredentials) cpars.get("awscredentials"),(String) cpars.get("host"));
		default:
			return null;
		}
	}
	
	public static QueueClient newQueueClient(String type, Map<String,Object> construction_parameters){
		switch(type){
		case "BD":
			return QueueClientFactory.newQueueClient(QueueClientType.BD, construction_parameters);
		case "SQS":
			return QueueClientFactory.newQueueClient(QueueClientType.SQS, construction_parameters);
		default:
			return null;
		}
	}
}
