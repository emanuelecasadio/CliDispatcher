package com.kopjra.ec2;
import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import com.amazonaws.auth.*;
import com.amazonaws.services.autoscaling.AmazonAutoScaling;
import com.amazonaws.services.autoscaling.AmazonAutoScalingAsyncClient;
import com.amazonaws.services.autoscaling.AmazonAutoScalingClient;
import com.amazonaws.services.autoscaling.model.AutoScalingGroup;
import com.amazonaws.services.autoscaling.model.DescribeAutoScalingGroupsRequest;
import com.amazonaws.services.autoscaling.model.DescribeAutoScalingGroupsResult;
import com.amazonaws.services.autoscaling.model.UpdateAutoScalingGroupRequest;
import com.amazonaws.services.cloudwatch.*;
import com.amazonaws.services.cloudwatch.model.*;
import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.ec2.*;
import com.amazonaws.services.ec2.model.*;

/**
 *
 * @author Emanuele Casadio
 *
 */
public class ShutdownWorkers {	
	
  public static void main( String[] args ) throws IOException {
	  final String env = "prod";
	    AWSCredentials credentials =
	    	      new PropertiesCredentials(
	    	          ShutdownWorkers.class.getResourceAsStream( "AwsCredentials.properties" )
	    	    );
    
      try {          
    	  // Scalo a zero
    	  int min = 0;
    	  int desired = 0;
          int max = 0;
          
    	  AmazonAutoScaling as = new AmazonAutoScalingClient(credentials);
    	  as.setEndpoint("autoscaling.eu-west-1.amazonaws.com");
          DescribeAutoScalingGroupsResult dasgres = as.describeAutoScalingGroups();
          List<AutoScalingGroup> lasg = dasgres.getAutoScalingGroups();
          for (AutoScalingGroup asg : lasg) {
			if(asg.getAutoScalingGroupName().equals("asg-kopjra-prod-workers")){
				desired = asg.getDesiredCapacity();
				max = asg.getMaxSize();
				min = asg.getMinSize();
				break;
			}
          }
          
          UpdateAutoScalingGroupRequest uasgr = new UpdateAutoScalingGroupRequest();
          uasgr.setAutoScalingGroupName("asg-kopjra-prod-workers");
          uasgr.setDesiredCapacity(0);
          uasgr.setMaxSize(0);
          uasgr.setMinSize(0);
          as.updateAutoScalingGroup(uasgr);
          
          System.out.println("Done: workers shutting down!");
          System.out.println("When you run the StartupWorkers, remember to use the following parameters: "+min+" "+max+" "+desired);
          
      } catch (AmazonServiceException ase) {
          System.err.println( "AmazonServiceException" );
      } catch (AmazonClientException ace) {
          System.err.println( "AmazonClientException" );
      } catch (Exception e){
    	  System.err.println( "OtherException" );
      }
  }

}