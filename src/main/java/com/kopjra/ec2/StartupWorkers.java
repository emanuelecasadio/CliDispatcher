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
public class StartupWorkers {	
	
  public static void main( String[] args ) throws IOException {
	  final String env = "prod";
    AWSCredentials credentials =
      new PropertiesCredentials(
          StartupWorkers.class.getResourceAsStream( "/AwsCredentials.properties" )
    );
    
      try {          
    	  // Scalo a zero
    	  int min = 1;
    	  int desired = 1;
    	  int max = 10;
    	  try{
        	  min = Integer.parseInt(args[0]);
        	  max = Integer.parseInt(args[1]);
        	  desired = Integer.parseInt(args[2]);
    	  } catch(Exception e){
    		  System.out.println("Error happened during parameters parsing: defaulted to 1 1 10. Are you sure (y/n)?");
    		  BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
              String ask = br.readLine();
              if(!(ask.equals("y")||ask.equals("Y")||ask.equals("yes")||ask.equals("Yes")||ask.equals("YES"))){
            	 System.out.println("Aborted");
            	 System.exit(1);
              }
    	  }

    	  AmazonAutoScaling as = new AmazonAutoScalingClient(credentials);
    	  as.setEndpoint("autoscaling.eu-west-1.amazonaws.com");
          UpdateAutoScalingGroupRequest uasgr = new UpdateAutoScalingGroupRequest();
          uasgr.setAutoScalingGroupName("asg-kopjra-prod-workers");
          uasgr.setDesiredCapacity(desired);
          uasgr.setMaxSize(max);
          uasgr.setMinSize(min);
          as.updateAutoScalingGroup(uasgr);
          
          System.out.println("Done: workers starting up!");
          
      } catch (AmazonServiceException ase) {
          System.err.println( "AmazonServiceException" );
      } catch (AmazonClientException ace) {
          System.err.println( "AmazonClientException" );
      } catch (Exception e){
    	  System.err.println( "OtherException" );
      }
  }

}