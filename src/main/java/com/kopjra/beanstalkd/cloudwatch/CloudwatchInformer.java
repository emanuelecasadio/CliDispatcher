package com.kopjra.beanstalkd.cloudwatch;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.auth.*;
import com.amazonaws.services.cloudwatch.*;
import com.amazonaws.services.cloudwatch.model.*;
import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;

/**
 * CloudWatch metric updater and EC2 instances monitoring tool
 * 
 * It updates a custom CloudWatch metric based on the length of the queues and
 * retrieves the list of worker IPs available at the moment
 * 
 * @author Emanuele Casadio
 * 
 */
public class CloudwatchInformer {

	private static Set<MetricDatumProvider> mdps;

	public static void main(String[] args) throws IOException {
		mdps = new HashSet<MetricDatumProvider>(10);
		AWSCredentials credentials = new PropertiesCredentials(
				CloudwatchInformer.class
						.getResourceAsStream("/AwsCredentials.properties"));
		InputStream inputStream = 
			    CloudwatchInformer.class.getResourceAsStream("/CloudwatchInformer.properties");
		Logger logger = LoggerFactory.getLogger(CloudwatchInformer.class);
		
		Properties props = new Properties();
	    props.load(inputStream);
	    
	    final String env = props.getProperty("env");

		// Initializes the providers: Beanstalkd
		String beanstalkdip = props.getProperty("beanstalkdip");
		int beanstalkdport = Integer.parseInt(props.getProperty("beanstalkdport"));
		MetricDatumProvider bdmdp = new BeanstalkdMetricDatumProvider(beanstalkdip, beanstalkdport); 
		mdps.add(bdmdp);
		
		// Initializes the provider: Elastic Search
		String elasticsearchip = props.getProperty("elasticsearchip");
		int elasticsearchhttpport = Integer.parseInt(props.getProperty("elasticsearchport"));
		MetricDatumProvider esmdp = new HTTPAliveMetricDatumProvider("http://"+elasticsearchip+":"+elasticsearchhttpport, 30, "es-servers-functional"); 
		mdps.add(esmdp);

		// Initializes the provider: Proxy
		String proxyip = props.getProperty("proxyip");
		int proxyport = Integer.parseInt(props.getProperty("proxyport"));
		MetricDatumProvider proxymdp = new HTTPAliveMetricDatumProvider("http://"+proxyip+":"+proxyport, 30, "proxy-servers-functional"); 
		mdps.add(proxymdp);
		
		// Initializes the provider: Web
		String webip = props.getProperty("webip");
		int webport = Integer.parseInt(props.getProperty("webport"));
		MetricDatumProvider webmdp = new HTTPAliveMetricDatumProvider("http://"+webip+":"+webport, 30, "web-servers-functional"); 
		mdps.add(webmdp);

		// Initializes the provider: BitTorrent Client
		String btclient1ip = props.getProperty("btclient1ip");
		int btclient1port = Integer.parseInt(props.getProperty("btclient1port"));
		MetricDatumProvider btcli1mdp = new HTTPAliveMetricDatumProvider("http://"+btclient1ip+":"+btclient1port, 30, "btclient-servers-functional"); 
		mdps.add(btcli1mdp);
		
		try {
			// Sets up a connection to AWS
			AmazonCloudWatch cw = new AmazonCloudWatchClient(credentials);
			cw.setEndpoint("monitoring.eu-west-1.amazonaws.com");

			// Creates the envelope for the request
			PutMetricDataRequest data = new PutMetricDataRequest();
			Collection<MetricDatum> cmd = new ArrayList<MetricDatum>();
			
			// Aggregates the metrics from the various providers
			for (MetricDatumProvider mdp : mdps) {
				for (MetricDatum metricDatum : mdp.getMetricDatumCollection()) {
					cmd.add(metricDatum);
				}
			}
			
			// Pushes them to Cloudwatch
			data.setNamespace("services_status_enquiry-" + env);
			data.setMetricData(cmd);
			cw.putMetricData(data);
		} catch (AmazonServiceException ase) {
			System.err.println("AmazonServiceException");
		} catch (AmazonClientException ace) {
			System.err.println("AmazonClientException");
		} catch (Exception e) {
			System.err.println("OtherException");
		}
	}

}
