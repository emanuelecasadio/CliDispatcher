package com.kopjra.beanstalkd.cloudwatch;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collection;
import java.util.HashSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.cloudwatch.model.MetricDatum;
import com.amazonaws.services.cloudwatch.model.StandardUnit;

public class HTTPAliveMetricDatumProvider implements MetricDatumProvider {

	private String url;
	private String metricname;
	private int timeout; // MILLISECONDS
	private Logger logger;
	
	/**
	 * 
	 * @param url The HTTP URL to be pinged.
	 * @param timeout The timeout in seconds for both the connection timeout and the response read timeout. Note that
	 * the total timeout is effectively two times the given timeout.
	 * @param metricname
	 */
	public HTTPAliveMetricDatumProvider(String url, int timeout, String metricname){
		this.metricname = metricname;
		this.url = url;
		this.timeout = timeout*1000; //SECONDS TO MILLISECONDS
		logger = LoggerFactory.getLogger(HTTPAliveMetricDatumProvider.class);
	}
	
	@Override
	public Collection<MetricDatum> getMetricDatumCollection() {
	  	  Collection<MetricDatum> cmd = new HashSet<MetricDatum>(2);
	  	  MetricDatum md = new MetricDatum();
	  	  md.setUnit(StandardUnit.Count);
	  	  md.setMetricName(metricname);
	  	  if(ping()){
	  		  md.setValue(1.0);
	  	  } else {
	  		  md.setValue(0.0);
	  	  }
	  	  cmd.add(md);
	  	  return cmd;
	}
	
	/**
	 * Pings a HTTP URL. This effectively sends a HEAD request and returns <code>true</code> if the response code is in 
	 * the 200-399,401 range.
	 * @return <code>true</code> if the given HTTP URL has returned response code in the set stated above on a GET request within the
	 * given timeout, otherwise <code>false</code>.
	 */
	private boolean ping() {
	    String url = this.url.replaceFirst("https", "http"); // Otherwise an exception may be thrown on invalid SSL certificates.
	    
	    try {
	        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
	        connection.setConnectTimeout(timeout);
	        connection.setReadTimeout(timeout);
	        connection.setRequestMethod("GET");
	        int responseCode = connection.getResponseCode();
	        logger.info(metricname+": response code is "+responseCode);
	        return ((200 <= responseCode && responseCode <= 399) || (responseCode == 401));
	    } catch (IOException exception) {
	    	logger.info(metricname+": exception returned");
	        return false;
	    }
	}

}
