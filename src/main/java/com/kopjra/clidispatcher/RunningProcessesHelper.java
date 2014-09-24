/**
 * 
 */
package com.kopjra.clidispatcher;

/**
 * Useful in order to share in a thread-safe manner, small
 * pieces of information between concurrent threads. 
 * 
 * It's very similar to a Monitor, by concurrent programming pattern.
 * 
 * @author Emanuele Casadio
 *
 */
class RunningProcessesHelper {
	private int max;
	private int count;
	private boolean stop;
	
	/**
	 * Creates a new RunningProcessHelper.
	 * For obvious reasons, the constructor cannot be thread-safe
	 * @param i The initialization value
	 * @param max The maximum number of concurrent clis
	 */
	public RunningProcessesHelper(int init, int max){
		count = init;
		this.max = max;
		stop = false;
	}
	
	/**
	 * Increments the counter or running processes
	 * It's also a waiting queue on maximum number of processes
	 * @return true if the caller is authorized to perform the operation, false otherwise
	 */
	public synchronized boolean increase(){
		while(count>=max && !stop){
			try{
				wait();
			} catch(InterruptedException e) {}
		}
		if(!stop){
			count++;
		}
		return !stop;
	}
	
	/**
	 * Decrements the counter of running processes
	 * It also notifies that there is a new slot available
	 */
	public synchronized void decrease(){
		count--;
		notify();
	}
	
	/**
	 * Returns the number of concurrent running processes
	 * @return Counter number
	 */
	public synchronized int get(){
		return count;
	}
	
	/**
	 * Blocks the concurrent process queue
	 * Useful when the daemon wants to terminate, so that every process
	 * can unlock itself, realize it and dismiss
	 */
	public synchronized void block(){
		stop = true;
		notifyAll();
	}
	
	/**
	 * Unlocks the concurrent process queue
	 * Don't know if it's useful but, hey, it's here! 
	 */
	public synchronized void deblock(){
		stop = false;
	}	

}
