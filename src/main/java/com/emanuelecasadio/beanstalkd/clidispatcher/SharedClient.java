package com.emanuelecasadio.beanstalkd.clidispatcher;

import java.util.List;
import java.util.Map;

import com.surftools.BeanstalkClient.Client;
import com.surftools.BeanstalkClientImpl.ClientImpl;
import com.surftools.BeanstalkClient.Job;

public class SharedClient extends ClientImpl implements Client {

	private Client client;
	
	public SharedClient(String ipaddr, int port, boolean iolock){
		client = new ClientImpl(ipaddr,port,iolock);
	}
	
	// Synchronized
	
	// ****************************************************************
	// Producer methods
	// ****************************************************************

	@Override
	public synchronized long put(long priority, int delaySeconds, int timeToRun, byte[] data) {
		return client.put(priority, delaySeconds, timeToRun, data);
	}

	@Override
	public synchronized void useTube(String tubeName) {
		client.useTube(tubeName);
	}

	// ****************************************************************
	// Consumer methods
	// job-related
	// ****************************************************************
	@Override
	public synchronized Job reserve(Integer timeoutSeconds) {
		return client.reserve(timeoutSeconds);
	}

	@Override
	public synchronized boolean delete(long jobId) {
		return client.delete(jobId);
	}

	@Override
	public synchronized boolean release(long jobId, long priority, int delaySeconds) {
		return client.release(jobId, priority, delaySeconds);
	}

	@Override
	public synchronized boolean bury(long jobId, long priority) {
		return client.bury(jobId, priority);
	}

	@Override
	public synchronized boolean touch(long jobId) {
		return client.touch(jobId);
	}

	// ****************************************************************
	// Consumer methods
	// tube-related
	// ****************************************************************
	@Override
	public synchronized int watch(String tubeName) {
		return client.watch(tubeName);
	}

	@Override
	public synchronized int ignore(String tubeName) {
		return client.ignore(tubeName);
	}

	// ****************************************************************
	// Consumer methods
	// peek-related
	// ****************************************************************
	@Override
	public synchronized Job peek(long jobId) {
		return client.peek(jobId);
	}

	@Override
	public synchronized Job peekBuried() {
		return client.peekBuried();
	}

	@Override
	public synchronized Job peekDelayed() {
		return client.peekDelayed();
	}

	@Override
	public synchronized Job peekReady() {
		return client.peekReady();
	}

	@Override
	public synchronized int kick(int count) {
		return client.kick(count);
	}

	/** Binary different from source
	@Override
	public synchronized boolean kickJob(long jobId) {
		return client.kickJob(jobId);
	}
	**/

	// ****************************************************************
	// Consumer methods
	// stats-related
	// ****************************************************************
	@Override
	//@SuppressWarnings("unchecked")
	public synchronized Map<String, String> statsJob(long jobId) {
		return client.statsJob(jobId);
	}

	@Override
	//@SuppressWarnings("unchecked")
	public synchronized Map<String, String> statsTube(String tubeName) {
		return client.statsTube(tubeName);
	}

	@Override
	//@SuppressWarnings("unchecked")
	public synchronized Map<String, String> stats() {
		return client.stats();
	}

	@Override
	//@SuppressWarnings("unchecked")
	public synchronized List<String> listTubes() {
		return client.listTubes();
	}

	@Override
	public synchronized String listTubeUsed() {
		return client.listTubeUsed();
	}

	@Override
	//@SuppressWarnings("unchecked")
	public synchronized List<String> listTubesWatched() {
		return client.listTubesWatched();
	}

	@Override
	public synchronized String getClientVersion() {
		return client.getClientVersion();
	}

	@Override
	public synchronized void close() {
		client.close();
	}

	@Override
	public synchronized boolean isUniqueConnectionPerThread() {
		return client.isUniqueConnectionPerThread();
	}

	@Override
	public synchronized void setUniqueConnectionPerThread(boolean uniqueConnectionPerThread) {
		client.setUniqueConnectionPerThread(uniqueConnectionPerThread);
	}

	/** Binary different from source???
	@Override
	public synchronized boolean pauseTube(String tubeName, int pauseSeconds) {
		return client.pauseTube(tubeName, pauseSeconds);
	}
	**/

	@Override
	public synchronized String getServerVersion() {
		return client.getServerVersion();
	}	
	
	
}
