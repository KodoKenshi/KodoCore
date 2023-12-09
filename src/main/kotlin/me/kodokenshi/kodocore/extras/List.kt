package me.kodokenshi.kodocore.extras

import me.kodokenshi.kodocore.oop.MutableListIterator

inline fun <reified E> List<E>.mutableListIterator() = MutableListIterator(*this.toTypedArray())

fun <T> Iterable<T>.findWithIndex(predicate: (T) -> Boolean): Pair<T, Int>? {
    for ((index, element) in withIndex()) if (predicate(element)) return element to index
    return null
}

fun coloredList(vararg string: String) = listOf(*string).color()
fun CharArray.containsIgnoreCase(char: Char): Boolean {
    for (c in this)
        if (c.equals(char, true))
            return true
    return false
}
fun List<Char>.containsIgnoreCase(char: Char): Boolean {
    for (c in this)
        if (c.equals(char, true))
            return true
    return false
}
fun List<String>.containsIgnoreCase(string: String): Boolean {
    for (str in this)
        if (str.equals(string, true))
            return true
    return false
}
fun List<String>.color(colorChar: Char = '&') = buildList {
    for (text in this@color)
        add(text.color(colorChar))
}
fun MutableList<String>.addIfAbsent(value: String, ignoreCase: Boolean = false): Boolean {

    if (!any { it.equals(value, ignoreCase) }) {

        add(value)
        return true

    }

    return false
}
fun <T> MutableList<T>.addIfAbsent(value: T): Boolean {

    if (!contains(value)) {

        add(value)
        return true

    }

    return false
}
fun <T> MutableList<T>.addIf(element: T, comparingBy: T.(T) -> Boolean): Boolean {
    for (t in toList())
        if (!comparingBy(element, t)) return false
    add(element)
    return true
}
fun <T> MutableList<T>.removeDuplicates(): MutableList<T> {

    val newList = toMutableList()
    for (t in this)
        if (!newList.contains(t))
            add(t)

    clear()
    addAll(newList)

    return this

}