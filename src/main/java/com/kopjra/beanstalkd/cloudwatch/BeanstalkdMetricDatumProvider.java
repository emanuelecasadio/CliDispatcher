package com.kopjra.beanstalkd.cloudwatch;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import com.amazonaws.services.cloudwatch.model.MetricDatum;
import com.amazonaws.services.cloudwatch.model.StandardUnit;
import com.surftools.BeanstalkClient.Client;
import com.surftools.BeanstalkClientImpl.ClientImpl;

public class BeanstalkdMetricDatumProvider implements MetricDatumProvider {

	private String beanstalkdip;
	private int beanstalkdport;
	
	public BeanstalkdMetricDatumProvider(String beanstalkdip, int beanstalkdport) {
		this.beanstalkdip = beanstalkdip;
		this.beanstalkdport = beanstalkdport;
	}
	
	@Override
	public Collection<MetricDatum> getMetricDatumCollection() {
  	  Collection<MetricDatum> cmd = new HashSet<MetricDatum>(2);
  	  MetricDatum md2 = new MetricDatum();
  	  md2.setUnit(StandardUnit.Count);
  	  md2.setMetricName("qm-servers-functional");  	  
  	  
	  try{
	  Client bc = new ClientImpl(beanstalkdip, beanstalkdport, true);
  	  List<String> lt = bc.listTubes();
  	  double tot = 0.0;
  	  for (String tube : lt) {
			Map<String,String> st = bc.statsTube(tube);
			tot += Integer.parseInt(st.get("current-jobs-ready"));
  	  }
  	  MetricDatum md1 = new MetricDatum();
  	  md1.setUnit(StandardUnit.Count);
  	  md1.setValue(tot);
  	  md1.setMetricName("queues-all-jobs-ready");
  	  
  	  md2.setValue(1.0);
  	  
  	  cmd.add(md1);
  	  } catch (Exception e){
  	  	  md2.setValue(0.0);
  	  }
	  cmd.add(md2);
	  
	  return cmd;
	}

}
