package me.kodokenshi.kodocore.oop

class MutableIterator<E>: List<E> {

    private var index = -1
    private val list: MutableList<E>

    constructor() { list = mutableListOf() }
    constructor(vararg elements: E) { list = mutableListOf(*elements) }

    fun currentIndex() = index
    fun nextIndex() = if (hasNext()) index + 1 else - 1
    fun previousIndex() = if (hasPrevious()) index - 1 else -1

    fun current(): E {
        if (index == -1) throw NoSuchElementException("There's no current element.")
        return list[index]
    }
    fun next(): E {
        if (!hasNext()) throw NoSuchElementException("There's no next element.")
        index++
        return list[index]
    }
    fun previous(): E {
        if (!hasPrevious()) throw NoSuchElementException("There's no previous element.")
        index--
        return list[index]
    }

    fun hasCurrent() = index != -1
    fun hasNext() = index < (size - 1)
    fun hasPrevious() = index > 0

    fun set(index: Int, element: E) = list.set(index, element)
    fun add(element: E) = list.add(element)
    fun add(index: Int, element: E) = list.add(index, element)
    fun addAll(elements: Collection<E>) = list.addAll(elements)
    fun remove(element: E) = list.remove(element).apply {
        if (isEmpty())
            index = -1
        else if (index >= size) index = size - 1
    }
    fun removeAt(index: Int) = list.removeAt(index).apply {
        if (isEmpty())
            this@MutableIterator.index = -1
        else if (this@MutableIterator.index >= size) this@MutableIterator.index = size - 1
    }
    fun removeAll(elements: Collection<E>) = list.removeAll(elements).apply {
        if (isEmpty())
            index = -1
        else if (index >= size) index = size - 1
    }

    fun clear() {
        index = -1
        list.clear()
    }

    override val size get() = list.size
    override fun contains(element: E) = list.contains(element)
    override fun containsAll(elements: Collection<E>) = list.containsAll(elements)
    override fun get(index: Int) = list[index]
    override fun indexOf(element: E) = list.indexOf(element)
    override fun isEmpty() = list.isEmpty()
    override fun iterator() = list.iterator()
    override fun lastIndexOf(element: E) = list.lastIndexOf(element)
    override fun listIterator() = list.listIterator()
    override fun listIterator(index: Int) = list.listIterator(index)
    override fun subList(fromIndex: Int, toIndex: Int) = list.subList(fromIndex, toIndex)

}