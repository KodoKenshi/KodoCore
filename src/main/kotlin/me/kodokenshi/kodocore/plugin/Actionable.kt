package me.kodokenshi.kodocore.plugin

import java.util.concurrent.ConcurrentHashMap

fun actionable() = Actionable()
fun actionable(block: Trigger.() -> Unit) = Actionable(block)

open class Actionable(block: (Trigger.() -> Unit)? = null) {

    internal val listeners = ConcurrentHashMap<Trigger, Trigger.() -> Unit>()

    fun trigger() { listeners.forEach { it.value(it.key) } }
    fun listen(listener: Trigger.() -> Unit) = Trigger(this).apply { listeners[this] = listener }
    fun invalidateListeners() = listeners.clear()

    init { if (block != null) listen(block) }

}
class Trigger(private inline val actionable: Actionable) {

    fun trigger() { actionable.trigger() }
    fun invalidate() { actionable.listeners.remove(this) }

}