package org.example;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.concurrent.*;

/**
 * @author Nicholas Curl
 */
public class CustomThreadPoolExecutor extends ThreadPoolExecutor {

    /**
     * The instance of the logger
     */
    private static final Logger    logger             = LogManager.getLogger(CustomThreadPoolExecutor.class);
    private              Exception executionException = null;
    private              boolean   interrupted        = false;

    /**
     * Creates a new {@code ThreadPoolExecutor} with the given initial parameters, the default thread factory and the
     * default rejected execution handler.
     *
     * <p>It may be more convenient to use one of the {@link Executors}
     * factory methods instead of this general purpose constructor.
     *
     * @param corePoolSize    the number of threads to keep in the pool, even if they are idle, unless {@code
     *                        allowCoreThreadTimeOut} is set
     * @param maximumPoolSize the maximum number of threads to allow in the pool
     * @param keepAliveTime   when the number of threads is greater than the core, this is the maximum time that excess
     *                        idle threads will wait for new tasks before terminating.
     * @param unit            the time unit for the {@code keepAliveTime} argument
     * @param workQueue       the queue to use for holding tasks before they are executed.  This queue will hold only
     *                        the {@code Runnable} tasks submitted by the {@code execute} method.
     *
     * @throws IllegalArgumentException if one of the following holds:<br> {@code corePoolSize < 0}<br> {@code
     *                                  keepAliveTime < 0}<br> {@code maximumPoolSize <= 0}<br> {@code maximumPoolSize <
     *                                  corePoolSize}
     * @throws NullPointerException     if {@code workQueue} is null
     */
    public CustomThreadPoolExecutor(int corePoolSize,
                                    int maximumPoolSize,
                                    long keepAliveTime,
                                    TimeUnit unit,
                                    BlockingQueue<Runnable> workQueue
    ) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
    }

    /**
     * Creates a new {@code ThreadPoolExecutor} with the given initial parameters and {@linkplain AbortPolicy default
     * rejected execution handler}.
     *
     * @param corePoolSize    the number of threads to keep in the pool, even if they are idle, unless {@code
     *                        allowCoreThreadTimeOut} is set
     * @param maximumPoolSize the maximum number of threads to allow in the pool
     * @param keepAliveTime   when the number of threads is greater than the core, this is the maximum time that excess
     *                        idle threads will wait for new tasks before terminating.
     * @param unit            the time unit for the {@code keepAliveTime} argument
     * @param workQueue       the queue to use for holding tasks before they are executed.  This queue will hold only
     *                        the {@code Runnable} tasks submitted by the {@code execute} method.
     * @param threadFactory   the factory to use when the executor creates a new thread
     *
     * @throws IllegalArgumentException if one of the following holds:<br> {@code corePoolSize < 0}<br> {@code
     *                                  keepAliveTime < 0}<br> {@code maximumPoolSize <= 0}<br> {@code maximumPoolSize <
     *                                  corePoolSize}
     * @throws NullPointerException     if {@code workQueue} or {@code threadFactory} is null
     */
    public CustomThreadPoolExecutor(int corePoolSize,
                                    int maximumPoolSize,
                                    long keepAliveTime,
                                    TimeUnit unit,
                                    BlockingQueue<Runnable> workQueue,
                                    ThreadFactory threadFactory
    ) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory);
    }

    /**
     * Creates a new {@code ThreadPoolExecutor} with the given initial parameters and {@linkplain
     * Executors#defaultThreadFactory default thread factory}.
     *
     * @param corePoolSize    the number of threads to keep in the pool, even if they are idle, unless {@code
     *                        allowCoreThreadTimeOut} is set
     * @param maximumPoolSize the maximum number of threads to allow in the pool
     * @param keepAliveTime   when the number of threads is greater than the core, this is the maximum time that excess
     *                        idle threads will wait for new tasks before terminating.
     * @param unit            the time unit for the {@code keepAliveTime} argument
     * @param workQueue       the queue to use for holding tasks before they are executed.  This queue will hold only
     *                        the {@code Runnable} tasks submitted by the {@code execute} method.
     * @param handler         the handler to use when execution is blocked because the thread bounds and queue
     *                        capacities are reached
     *
     * @throws IllegalArgumentException if one of the following holds:<br> {@code corePoolSize < 0}<br> {@code
     *                                  keepAliveTime < 0}<br> {@code maximumPoolSize <= 0}<br> {@code maximumPoolSize <
     *                                  corePoolSize}
     * @throws NullPointerException     if {@code workQueue} or {@code handler} is null
     */
    public CustomThreadPoolExecutor(int corePoolSize,
                                    int maximumPoolSize,
                                    long keepAliveTime,
                                    TimeUnit unit,
                                    BlockingQueue<Runnable> workQueue,
                                    RejectedExecutionHandler handler
    ) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, handler);
    }

    /**
     * Creates a new {@code ThreadPoolExecutor} with the given initial parameters.
     *
     * @param corePoolSize    the number of threads to keep in the pool, even if they are idle, unless {@code
     *                        allowCoreThreadTimeOut} is set
     * @param maximumPoolSize the maximum number of threads to allow in the pool
     * @param keepAliveTime   when the number of threads is greater than the core, this is the maximum time that excess
     *                        idle threads will wait for new tasks before terminating.
     * @param unit            the time unit for the {@code keepAliveTime} argument
     * @param workQueue       the queue to use for holding tasks before they are executed.  This queue will hold only
     *                        the {@code Runnable} tasks submitted by the {@code execute} method.
     * @param threadFactory   the factory to use when the executor creates a new thread
     * @param handler         the handler to use when execution is blocked because the thread bounds and queue
     *                        capacities are reached
     *
     * @throws IllegalArgumentException if one of the following holds:<br> {@code corePoolSize < 0}<br> {@code
     *                                  keepAliveTime < 0}<br> {@code maximumPoolSize <= 0}<br> {@code maximumPoolSize <
     *                                  corePoolSize}
     * @throws NullPointerException     if {@code workQueue} or {@code threadFactory} or {@code handler} is null
     */
    public CustomThreadPoolExecutor(int corePoolSize,
                                    int maximumPoolSize,
                                    long keepAliveTime,
                                    TimeUnit unit,
                                    BlockingQueue<Runnable> workQueue,
                                    ThreadFactory threadFactory,
                                    RejectedExecutionHandler handler
    ) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);
    }

    @Override
    public List<Runnable> shutdownNow() {
        interrupted = true;
        return super.shutdownNow();
    }

    @Override
    protected void afterExecute(Runnable r, Throwable t) {
        super.afterExecute(r, t);
        if (t == null && r instanceof Future<?> && ((Future<?>) r).isDone()) {
            try {
                Object result = ((Future<?>) r).get();
            }
            catch (CancellationException e) {
                t = e;
            }
            catch (ExecutionException e) {
                t = e.getCause();
            }
            catch (InterruptedException e) {
                interrupted = true;
                Thread.currentThread().interrupt();
            }
        }
        if (t != null) {
            shutdownNow();
            if(t instanceof Exception) {
                executionException = (Exception) t;
            }
        }
    }


    @Override
    protected void terminated() {
        if (executionException != null) {
            logger.fatal("Error occurred during execution", executionException);
            System.exit(1);
        }
        else if (interrupted) {
            logger.fatal("Threads have been interrupted");
            System.exit(1);
        }
        super.terminated();
    }

    public Exception getExecutionException() {
        return executionException;
    }

    public boolean isInterrupted() {
        return interrupted;
    }
}
