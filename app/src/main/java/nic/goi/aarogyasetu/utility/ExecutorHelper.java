package nic.goi.aarogyasetu.utility;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by Kshitij Khatri on 21/03/20.
 */
public class ExecutorHelper {

    private static int numCores = Runtime.getRuntime().availableProcessors();

    public static ThreadPoolExecutor getThreadPoolExecutor() {
        return new ThreadPoolExecutor(numCores * 2, numCores * 2,
                60L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
    }
}
