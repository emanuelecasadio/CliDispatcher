package com.kopjra.clidispatcher;

public interface QueueClient extends AcceptsAttributes {
	public void deleteJob(Job j);
	public void buryJob(Job j);
	public void releaseJob(Job j);
	public Job reserveJob();
	public void setQueue(String queue);
}
