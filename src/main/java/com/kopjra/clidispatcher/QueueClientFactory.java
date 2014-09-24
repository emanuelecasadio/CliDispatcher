package com.kopjra.clidispatcher;

import java.util.Map;

import com.kopjra.clidispatcher.beanstalkd.BDClient;
import com.kopjra.clidispatcher.sqs.SQSClient;

public class QueueClientFactory {
	public enum QueueClientType {
		BD, SQS
	}
	
	public static QueueClient newQueueClient(QueueClientType type, Map<String,Object> construction_parameters){
		switch(type){
		case BD:
			return new BDClient();
		case SQS:
			return new SQSClient((com.amazonaws.auth.AWSCredentials) construction_parameters.get("awscredentials"),(String) construction_parameters.get("host"));
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
