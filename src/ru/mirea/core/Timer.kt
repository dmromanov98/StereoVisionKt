package ru.mirea.core

import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

class Timer(initializer: Runnable) {
    private var timer: ScheduledExecutorService? = null

    companion object {
        private var staffUpdatePeriod: Long = 33
        private var delay: Long = 0
    }

    fun withStaffUpdatePeriod(staffUpdatePeriod: Long): Timer {
        Timer.staffUpdatePeriod = staffUpdatePeriod
        return this
    }

    fun withDelay(delay: Long): Timer {
        Timer.delay = delay
        return this
    }

    init {
        timer = Executors.newSingleThreadScheduledExecutor()
        timer!!.scheduleAtFixedRate(
            initializer,
            delay,
            staffUpdatePeriod,
            TimeUnit.MILLISECONDS
        )
    }

    fun stop() {
        if (timer != null && !timer!!.isShutdown) {
            try {
                timer!!.shutdown()
                timer!!.awaitTermination(staffUpdatePeriod, TimeUnit.MILLISECONDS)
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }
    }

}