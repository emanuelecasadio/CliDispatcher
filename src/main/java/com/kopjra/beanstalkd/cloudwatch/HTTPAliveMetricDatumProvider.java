package com.kopjra.beanstalkd.cloudwatch;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collection;
import java.util.HashSet;

import com.amazonaws.services.cloudwatch.model.MetricDatum;
import com.amazonaws.services.cloudwatch.model.StandardUnit;

public class HTTPAliveMetricDatumProvider implements MetricDatumProvider {

	private String url;
	private String metricname;
	private int timeout;
	
	public HTTPAliveMetricDatumProvider(String url, int timeout, String metricname){
		this.metricname = metricname;
		this.url = url;
		this.timeout = timeout;
	}
	
	@Override
	public Collection<MetricDatum> getMetricDatumCollection() {
	  	  Collection<MetricDatum> cmd = new HashSet<MetricDatum>(2);
	  	  MetricDatum md = new MetricDatum();
	  	  md.setUnit(StandardUnit.Count);
	  	  md.setMetricName(metricname);
	  	  if(ping(url,timeout)){
	  		  md.setValue(1.0);
	  	  } else {
	  		  md.setValue(0.0);
	  	  }
	  	  cmd.add(md);
	  	  return cmd;
	}
	
	/**
	 * Pings a HTTP URL. This effectively sends a HEAD request and returns <code>true</code> if the response code is in 
	 * the 200-399 range.
	 * @param url The HTTP URL to be pinged.
	 * @param timeout The timeout in millis for both the connection timeout and the response read timeout. Note that
	 * the total timeout is effectively two times the given timeout.
	 * @return <code>true</code> if the given HTTP URL has returned response code 200-399 on a GET request within the
	 * given timeout, otherwise <code>false</code>.
	 */
	public static boolean ping(String url, int timeout) {
	    url = url.replaceFirst("https", "http"); // Otherwise an exception may be thrown on invalid SSL certificates.

	    try {
	        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
	        connection.setConnectTimeout(timeout);
	        connection.setReadTimeout(timeout);
	        connection.setRequestMethod("GET");
	        int responseCode = connection.getResponseCode();
	        return ((200 <= responseCode && responseCode <= 399) || (responseCode == 401));
	    } catch (IOException exception) {
	        return false;
	    }
	}

}
