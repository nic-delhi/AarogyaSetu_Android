package nic.goi.aarogyasetu.utility

import android.os.Handler
import android.os.Looper
import java.util.concurrent.Executor

object AppExecutors {

    private val mainThread by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        MainThreadExecutor()
    }

    fun runOnMain(op : () -> Unit) {
        mainThread().execute {
            op()
        }
    }

    private fun mainThread(): Executor {
        return mainThread
    }

    private class MainThreadExecutor : Executor {
        private val mainThreadHandler = Handler(Looper.getMainLooper())
        override fun execute(command: Runnable) {
            mainThreadHandler.post(command)
        }
    }

}