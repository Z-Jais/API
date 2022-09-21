package fr.ziedelth.utils

import com.sun.management.OperatingSystemMXBean
import java.lang.management.ManagementFactory

object PerformanceMeter {
    private val memoryUsageAverage = mutableListOf<Long>()
    private val cpuUsageAverage = mutableListOf<Double>()
    var request = 0

    fun getMemoryUsage(): Long {
        val runtime = Runtime.getRuntime()
        return runtime.totalMemory() - runtime.freeMemory()
    }

    fun getMemoryTotal(): Long {
        val runtime = Runtime.getRuntime()
        return runtime.totalMemory()
    }

    fun getCPUUsage(): Double {
        val operatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean() as OperatingSystemMXBean
        return operatingSystemMXBean.processCpuLoad
    }

    fun startThread() {
        Thread {
            var i = 0

            while (true) {
                if (i++ >= 60) {
                    i = 0

                    println("----- Performance for $request requests/min -----")
                    println("Memory usage: ${(memoryUsageAverage.average() / (1024.0 * 1024.0)).format()} MiB / ${(getMemoryTotal().toDouble() / (1024.0 * 1024.0)).format()} MiB")
                    println("CPU usage: ${(cpuUsageAverage.average() * 100.0).format()}% with high of ${(cpuUsageAverage.max() * 100.0).format()}%")

                    memoryUsageAverage.clear()
                    cpuUsageAverage.clear()
                    request = 0
                }

                memoryUsageAverage.add(getMemoryUsage())
                cpuUsageAverage.add(getCPUUsage())
                Thread.currentThread().join(1000)
            }
        }.start()
    }

    fun Double.format(digits: Int = 2) = "%.${digits}f".format(this)
}
