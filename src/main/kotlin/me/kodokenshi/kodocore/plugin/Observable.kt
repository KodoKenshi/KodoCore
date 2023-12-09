package me.kodokenshi.kodocore.plugin

import me.kodokenshi.kodocore.extras.concurrentMapOf
import java.io.Serializable

fun <T> observableOf(value: T, block: (Observer<T>.(T, T) -> Unit)? = null) = value.observe(block)
fun <T> T.observe(block: (Observer<T>.(T, T) -> Unit)? = null) = Observable(this, block)

open class Observable<T>(inline val initialValue: T, block: (Observer<T>.(T, T) -> Unit)? = null): Serializable {

    var value = initialValue; set(value) {
        val oldValue = field
        field = value
        observers.forEach { it.value(it.key, oldValue, value) }
    }

    internal val observers = concurrentMapOf<Observer<T>, Observer<T>.(T, T) -> Unit>()

    infix fun emit(value: T) { this.value = value }
    fun emitCurrentValue() { emit(value) }
    fun observe(observer: Observer<T>.(oldValue: T, newValue: T) -> Unit) = Observer(this).apply { observers[this] = observer }
    fun observeNewValue(observer: Observer<T>.(newValue: T) -> Unit) = observe { _, newValue -> observer(this, newValue) }
    fun invalidateObservers() { observers.clear() }

    init { if (block != null) observe(block) }

    override fun toString() = value.toString()

}
class Observer<in T>(private inline val observable: Observable<T>) {

    fun emit(value: T) { observable.value = value }
    fun invalidate() { observable.observers.remove(this) }

}