package com.kopjra.clidispatcher.utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.PropertiesCredentials;
import com.kopjra.clidispatcher.sqs.BufferedSQSBulkSender;

public class BulkImport {
	
	/**
	 * @param args
	 */
	public static final int MAX_MESSAGES = 10; // Fixed in SQS API
	
	public static void main(String[] args) {
		try {
			Logger logger = LoggerFactory.getLogger(BulkImport.class);
			AWSCredentials credentials = new PropertiesCredentials(
					  BulkImport.class.getResourceAsStream( "/AwsCredentials.properties" )
					);
			Properties properties = new Properties();
			properties.load(BulkImport.class.getResourceAsStream("/CliDispatcher.properties"));
			int max_parts = Integer.parseInt(args[0]);
			String prepend = "";
			for(int i=1;i<args.length;i++){
				if(i!=1){
					prepend = prepend + " ";
				}
				prepend = prepend + args[i];
			}
			
			BufferedSQSBulkSender sqscli = new BufferedSQSBulkSender(credentials, properties.getProperty("host"), max_parts, MAX_MESSAGES, prepend);
			sqscli.setQueue(properties.getProperty("queue"));
			
			BufferedReader br = new BufferedReader(new FileReader(args[0]));
			String line;
			int i = 0;
			while ((line = br.readLine()) != null) {
				logger.debug("Read line "+i);
				sqscli.sendPart(line);
			}
			br.close();
			sqscli.flush();
			
		} catch (IOException e1) {
			e1.printStackTrace();
		}		
	}

}
