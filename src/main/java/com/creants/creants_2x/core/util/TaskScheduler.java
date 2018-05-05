package com.creants.creants_2x.core.util;

import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.creants.creants_2x.core.service.IService;

/**
 * @author LamHM
 *
 */
public class TaskScheduler implements IService {
	private static AtomicInteger schedulerId;
	private final ScheduledThreadPoolExecutor taskScheduler;
	private final String serviceName;

	static {
		TaskScheduler.schedulerId = new AtomicInteger(0);
	}


	public TaskScheduler(final int threadPoolSize) {
		this.serviceName = "TaskScheduler-" + TaskScheduler.schedulerId.getAndIncrement();
		this.taskScheduler = new ScheduledThreadPoolExecutor(threadPoolSize);
	}


	public void init(final Object o) {
		QAntTracer.info(this.getClass(), String.valueOf(this.serviceName) + " started.");
	}


	public void destroy(final Object o) {
		final List<Runnable> awaitingExecution = this.taskScheduler.shutdownNow();
		QAntTracer.info(this.getClass(),
				String.valueOf(this.serviceName) + " stopping. Tasks awaiting execution: " + awaitingExecution.size());
	}


	public String getName() {
		return this.serviceName;
	}


	public void handleMessage(final Object arg0) {
	}


	public void setName(final String arg0) {
	}


	public ScheduledFuture<?> schedule(final Runnable task, final int delay, final TimeUnit unit) {
		return this.taskScheduler.schedule(task, delay, unit);
	}


	public ScheduledFuture<?> scheduleAtFixedRate(final Runnable task, final int initialDelay, final int period,
			final TimeUnit unit) {
		return this.taskScheduler.scheduleAtFixedRate(task, initialDelay, period, unit);
	}


	public void resizeThreadPool(final int threadPoolSize) {
		this.taskScheduler.setCorePoolSize(threadPoolSize);
	}


	public int getThreadPoolSize() {
		return this.taskScheduler.getCorePoolSize();
	}
}
