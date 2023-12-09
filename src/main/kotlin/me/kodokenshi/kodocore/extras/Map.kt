package me.kodokenshi.kodocore.extras

import java.util.concurrent.ConcurrentHashMap

fun <K, V> concurrentMapOf() = ConcurrentHashMap<K, V>()
fun <K, V> concurrentMapOf(vararg pairs: Pair<K, V>) = ConcurrentHashMap<K, V>().apply { putAll(pairs) }
fun <T> MutableCollection<T>.addIfAbsent(value: T): Boolean {
    return if (!contains(value)) { add(value); true }
    else false
}