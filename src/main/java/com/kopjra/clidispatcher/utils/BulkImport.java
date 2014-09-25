package com.kopjra.clidispatcher.utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.PropertiesCredentials;
import com.kopjra.clidispatcher.sqs.SQSClient;

public class BulkImport {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			AWSCredentials credentials = new PropertiesCredentials(
					  BulkImport.class.getResourceAsStream( "/AwsCredentials.properties" )
					);
			Properties properties = new Properties();
			properties.load(BulkImport.class.getResourceAsStream("/CliDispatcher.properties"));
			SQSClient sqscli = new SQSClient(credentials, properties.getProperty("host"));
			sqscli.setQueue(properties.getProperty("queue"));
			
			BufferedReader br = new BufferedReader(new FileReader(args[0]));
			String line;
			List<String> lines = new ArrayList<>(10);
			while ((line = br.readLine()) != null) {
				lines.add(line);
				if(lines.size()==10){
					sqscli.sendBulkMessage(lines);
					lines.clear();
				}
			}
			br.close();
			if(lines.size()>0){
				sqscli.sendBulkMessage(lines);
			}
			
		} catch (IOException e1) {
			e1.printStackTrace();
		}		
	}

}
