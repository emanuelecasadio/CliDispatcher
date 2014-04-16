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
public class AmazonInformer {

  public static void main( String[] args ) throws IOException {
	  final String env = "staging";
    AWSCredentials credentials =
      new PropertiesCredentials(
          AmazonInformer.class.getResourceAsStream( "AwsCredentials.properties" )
    );
    
      try {
    	  // Publish Queue Length
    	  AmazonCloudWatch cw = new AmazonCloudWatchClient(credentials);
    	  cw.setEndpoint("monitoring.eu-west-1.amazonaws.com");
    	  com.surftools.BeanstalkClient.Client bc = new com.surftools.BeanstalkClientImpl.ClientImpl("127.0.0.1", 11300, true);
    	  List<String> lt = bc.listTubes();
    	  double tot = 0.0;
    	  for (String tube : lt) {
			Map<String,String> st = bc.statsTube(tube);
			tot += Integer.parseInt(st.get("current-jobs-ready"));
			tot += Integer.parseInt(st.get("current-jobs-reserved"));
			tot += Integer.parseInt(st.get("current-jobs-delayed"));
			tot += Integer.parseInt(st.get("current-jobs-buried"));
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

          // Get workers' private ips
          AmazonEC2 ec2 = new AmazonEC2Client(credentials);
          ec2.setEndpoint("ec2.eu-west-1.amazonaws.com");
          DescribeInstancesRequest req = new DescribeInstancesRequest();
          Collection<Filter> filters = new HashSet<Filter>();
          List<String> values = new ArrayList<String>();
          values.add("worker");
          filters.add(new Filter("tag-key",values));
          req.setFilters(filters);
          DescribeInstancesResult ec2res = ec2.describeInstances(req);
          List<Reservation> reservations = ec2res.getReservations();
          for (Reservation reservation : reservations) {
			List<Instance> instances = reservation.getInstances();
			for(Instance instance : instances){
				InstanceState s = instance.getState();
				if(s.getName().equals("running")){
					System.out.println(instance.getPrivateIpAddress());
				}
			}
		}
                    
      } catch (AmazonServiceException ase) {
          System.err.println( "AmazonServiceException" );
      } catch (AmazonClientException ace) {
          System.err.println( "AmazonClientException" );
      } catch (Exception e){
    	  System.err.println( "OtherException" );
      }
  }

}