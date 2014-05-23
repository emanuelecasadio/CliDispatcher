package com.kopjra.beanstalkd.cloudwatch;
import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import com.amazonaws.auth.*;
import com.amazonaws.services.cloudwatch.*;
import com.amazonaws.services.cloudwatch.model.*;
import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.ec2.*;
import com.amazonaws.services.ec2.model.*;

/**
 * CloudWatch metric updater and EC2 instances monitoring tool
 *
 * It updates a custom CloudWatch metric based on the length of the
 * queues and retrieves the list of worker IPs available at the moment
 *
 * @author Emanuele Casadio
 *
 */
public class CloudwatchInformer {

  public static void main( String[] args ) throws IOException {
	  final String env = "staging";
    AWSCredentials credentials =
      new PropertiesCredentials(
          CloudwatchInformer.class.getResourceAsStream( "/AwsCredentials.properties" )
    );

    String beanstalkdip = args[0];
    int beanstalkdport = Integer.parseInt(args[1]);
    
      try {
    	  // Publish Queue Length
    	  AmazonCloudWatch cw = new AmazonCloudWatchClient(credentials);
    	  cw.setEndpoint("monitoring.eu-west-1.amazonaws.com");
    	  com.surftools.BeanstalkClient.Client bc = new com.surftools.BeanstalkClientImpl.ClientImpl(beanstalkdip, beanstalkdport, true);
    	  List<String> lt = bc.listTubes();
    	  double tot = 0.0;
    	  for (String tube : lt) {
			Map<String,String> st = bc.statsTube(tube);
			tot += Integer.parseInt(st.get("current-jobs-ready"));
			tot += Integer.parseInt(st.get("current-jobs-reserved"));
    	  }

    	  PutMetricDataRequest data = new PutMetricDataRequest();
    	  MetricDatum md = new MetricDatum();
    	  md.setUnit(StandardUnit.Count);
    	  md.setValue(tot);
    	  md.setMetricName("metric-"+env+"-totalqueueslength");
    	  Collection<MetricDatum> cmd = new ArrayList<MetricDatum>();
    	  cmd.add(md);
    	  data.setNamespace("queues-"+env);
    	  data.setMetricData(cmd);
          cw.putMetricData(data);
      } catch (AmazonServiceException ase) {
          System.err.println( "AmazonServiceException" );
      } catch (AmazonClientException ace) {
          System.err.println( "AmazonClientException" );
      } catch (Exception e){
    	  System.err.println( "OtherException" );
      }
  }

}
