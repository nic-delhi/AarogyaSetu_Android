package nic.goi.aarogyasetu.utility

import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

/**
 * Created by Kshitij Khatri on 21/03/20.
 */
object ExecutorHelper {

    private val numCores = Runtime.getRuntime().availableProcessors()

    val threadPoolExecutor: ThreadPoolExecutor
        get() = ThreadPoolExecutor(
            numCores * 2, numCores * 2,
            60L, TimeUnit.SECONDS, LinkedBlockingQueue<Runnable>()
        )
}
