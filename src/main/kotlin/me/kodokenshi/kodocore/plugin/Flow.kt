package me.kodokenshi.kodocore.plugin

import me.kodokenshi.kodocore.extras.concurrentMapOf

fun <T> (() -> T).asFlow() = flow { emit(invoke()) }
fun <T> Iterable<T>.asFlow() = flow { forEach { emit(it) } }
fun <T> Sequence<T>.asFlow() = flow { forEach { emit(it) } }
fun <T> flowOf(vararg elements: T) = flow { for (element in elements) emit(element) }
fun <T> flowOf(value: T) = flow { emit(value) }
fun <T> T.toFlow() = flowOf(this)
fun <T> flow(block: FlowCollector<T>.(T) -> Unit) = Flow(block)

open class Flow<T>(block: FlowCollector<T>.(T) -> Unit) {

    internal val collectors = concurrentMapOf<FlowCollector<T>, FlowCollector<T>.(T) -> Unit>()

    fun emit(value: T) { collectors.forEach { it.value(it.key, value) } }
    fun collect(collector: FlowCollector<T>.(T) -> Unit) = FlowCollector(this).apply { collectors[this] = collector }
    fun invalidateCollectors() = collectors.clear()

    init { collect(block) }

}
class FlowCollector<in T>(private inline val flow: Flow<T>) {

    fun emit(value: T) { flow.emit(value) }
    fun invalidate() { flow.collectors.remove(this) }

}