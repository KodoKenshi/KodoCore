package me.kodokenshi.kodocore.plugin

import me.kodokenshi.kodocore.extras.javaPlugin
import org.bukkit.plugin.java.JavaPlugin

fun runSync(block: Task.() -> Unit) = Task(javaPlugin(), block = block)
fun runSync(later: Long, block: Task.() -> Unit) = Task(javaPlugin(), later = later, block = block)
fun runSync(later: Long, every: Long, block: Task.() -> Unit) = Task(javaPlugin(), true, later, every, block = block)
fun runAsync(block: Task.() -> Unit) = Task(javaPlugin(), sync = false, block = block)
fun runAsync(later: Long, block: Task.() -> Unit) = Task(javaPlugin(), sync = false, later = later, block = block)
fun runAsync(later: Long, every: Long, block: Task.() -> Unit) = Task(javaPlugin(), true, sync = false, later = later, every = every, block = block)

class Task(
    javaPlugin: JavaPlugin,
    timerTask: Boolean = false,
    later: Long = 0,
    every: Long = 0,
    sync: Boolean = true,
    block: Task.() -> Unit
) {

    private val task = if (timerTask) {

        if (sync) javaPlugin.server.scheduler.runTaskTimer(javaPlugin, Runnable { block.invoke(this) }, later, every)
        else javaPlugin.server.scheduler.runTaskTimerAsynchronously(javaPlugin, Runnable { block.invoke(this) }, later, every)

    } else if (later > 0) {

        if (sync) javaPlugin.server.scheduler.runTaskLater(javaPlugin, Runnable { block.invoke(this) }, later)
        else javaPlugin.server.scheduler.runTaskLaterAsynchronously(javaPlugin, Runnable { block.invoke(this) }, later)

    } else {

        if (sync) javaPlugin.server.scheduler.runTask(javaPlugin, Runnable { block.invoke(this) })
        else javaPlugin.server.scheduler.runTaskAsynchronously(javaPlugin, Runnable { block.invoke(this) })

    }

    val taskId = task.taskId
    val owner = task.owner
    val isSync = task.isSync
    val isAsync = !task.isSync
    fun cancel() = task.cancel()
    fun stop() = task.cancel()

}