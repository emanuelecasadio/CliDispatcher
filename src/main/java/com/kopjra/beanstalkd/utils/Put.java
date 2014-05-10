package com.kopjra.beanstalkd.utils;

import com.surftools.BeanstalkClient.Client;
import com.surftools.BeanstalkClientImpl.ClientImpl;

public class Put {

	/**
	 * 
	 * @param args (BEANSTALKD_IP | BEANSTALKD_HOST) BEANSTALKD_PORT PRIORITY DELAY TTR DATA
	 */
	public static void main(String[] args) {
		try{
			String beanstalkd_ip = args[0];
			int beanstalkd_port = Integer.parseInt(args[1]);
			int beanstalkd_priority = Integer.parseInt(args[2]);
			int beanstalkd_delay = Integer.parseInt(args[3]);
			int beanstalkd_ttr = Integer.parseInt(args[4]);
			Client beanstalkd_client = new ClientImpl(beanstalkd_ip, beanstalkd_port, false);
			
			String data_string = "";
			byte[] data_byte;
			
			for(int i=5;i<args.length;i++){
				data_string = data_string + " " + args[i];
			}
			data_byte = data_string.getBytes("UTF-8");
			
			beanstalkd_client.useTube("default");
			beanstalkd_client.put(beanstalkd_priority, beanstalkd_delay, beanstalkd_ttr, data_byte);
			beanstalkd_client.close();
		} catch (Exception e){
			e.printStackTrace(System.err);
		}

	}

}
