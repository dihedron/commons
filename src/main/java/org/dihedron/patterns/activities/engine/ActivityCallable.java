/**
 * Copyright (c) 2012-2014, Andrea Funto'. All rights reserved. See LICENSE for details.
 */ 

package org.dihedron.patterns.activities.engine;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;

import org.dihedron.core.License;
import org.dihedron.patterns.activities.exceptions.ActivityException;
import org.dihedron.patterns.activities.types.ActivityData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Andrea Funto'
 */
@License
public class ActivityCallable implements Callable<ActivityData> {
	
	/**
	 * The logger.
	 */
	private final static Logger logger = LoggerFactory.getLogger(ActivityCallable.class);

	/**
	 * The asynchronous task index in the asynchronous pool.
	 */
	private int index;
	
	/**
	 * The queue used to synchronise task execution.
	 */
	private BlockingQueue<Integer> queue;	
	
	/**
	 * The activity and the element of data it will run on.
	 */
	private ActivityInfo info;
	
	/**
	 * Constructor.
	 *
	 * @param index
	 *   the task index in the asynchronous pool.
	 * @param queue
	 *   the queue used internally to synchronise on asynchronous task execution.  
	 * @param info
	 *   info about the activity to be executed asynchronously and the data it 
	 *   will run against, all in a single object.
	 */
	public ActivityCallable(int index, BlockingQueue<Integer> queue, ActivityInfo info) {
		this.index = index;
		this.queue = queue;
		this.info = info;
	}
	
	/**
	 * Actual thread's workhorse method; it implements a "code around" pattern, 
	 * and delegates actual business logic to the {@code Activity}, while retaining 
	 * the logic necessary to signal completion to the caller.
	 * 
	 * @see java.util.concurrent.Callable#call()
	 */
	@Override
	public ActivityData call() throws ActivityException {
				
		try {
			// do the real work asynchronously...
			logger.trace("activity '{}' at index {} starting", info.getActivity().getId(), index); 
			return info.getActivity().perform(info.getContext(), info.getData());
		} catch(Exception e) {
			logger.error("thread interrupted while executing task '{}' at index {}", info.getActivity().getId(), index);
			throw new ActivityException("Thread interrupted while executing task '" + info.getActivity().getId() + "' at index " + index, e);
		} finally {
			// signal that the task is complete
			logger.trace("task '{}' at index {} is complete (queue size: {})", info.getActivity().getId(), index, queue.size());
			queue.offer(index);
		}
	}
}
