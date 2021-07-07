package org.example;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author Nicholas Curl
 */
public class StoringRejectedExecutionHandler implements RejectedExecutionHandler {

    /**
     * The instance of the logger
     */
    private static final Logger logger = LogManager.getLogger(StoringRejectedExecutionHandler.class);

    /**
     * Method that may be invoked by a {@link ThreadPoolExecutor} when {@link ThreadPoolExecutor#execute execute} cannot
     * accept a task.  This may occur when no more threads or queue slots are available because their bounds would be
     * exceeded, or upon shutdown of the Executor.
     *
     * <p>In the absence of other alternatives, the method may throw
     * an unchecked {@link RejectedExecutionException}, which will be propagated to the caller of {@code execute}.
     *
     * @param r        the runnable task requested to be executed
     * @param executor the executor attempting to execute this task
     *
     * @throws RejectedExecutionException if there is no remedy
     */
    @Override
    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
        if (!executor.isShutdown()) {
            try {
                executor.getQueue().put(r);
            }
            catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
