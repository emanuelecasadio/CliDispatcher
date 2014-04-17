package com.kopjra.ec2;
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
public class EC2Informer {

  public static void main( String[] args ) throws IOException {
	  final String env = "staging";
    AWSCredentials credentials =
      new PropertiesCredentials(
          EC2Informer.class.getResourceAsStream( "AwsCredentials.properties" )
    );
    
      try {
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