package me.kodokenshi.kodocore.plugin

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import me.kodokenshi.kodocore.extras.addIfAbsent
import me.kodokenshi.kodocore.extras.decodeToItemStack
import me.kodokenshi.kodocore.extras.decodeToLocation
import me.kodokenshi.kodocore.extras.encodeToString
import org.bukkit.Location
import org.bukkit.inventory.ItemStack
import java.io.Serializable

inline fun json(block: Json.() -> Unit) = Json().apply(block).build()
fun String.toJson() = Json(this)
fun String.toJsonEmptyIfError() = try { Json(this) } catch (_: Exception) { Json() }
fun MutableMap<String, Any>.toJson() = Json(this)

open class Json: Serializable {

    companion object {
        val GSON: Gson = GsonBuilder().setPrettyPrinting().create()
    }

    internal var data = mutableMapOf<String, Any>()

    constructor()
    constructor(jsonString: String) {
        if (jsonString.isNotBlank())
            data = GSON.fromJson<MutableMap<String, Any>>(jsonString, MutableMap::class.java)
    }
    constructor(map: MutableMap<String, Any>) { data = map }

    fun contains(key: String) = data.containsKey(key)
    val keys get() = data.keys.toList()
    fun section(path: String): MutableList<String> {

        val ret = mutableListOf<String>()

        data.keys.filter { it.startsWith("$path.") }.forEach {

            val sp = it.substring(path.length + 1)
            if (sp.contains(".")) ret.addIfAbsent(sp.substring(0, sp.indexOf(".")))
            else ret.addIfAbsent(sp)

        }

        return ret

    }

    fun containsPath(path: String) = data.containsKey(path)
    fun remove(path: String) = data.remove(path)
    fun removeAll(path: String) = buildMap {
        for (datum in data.toMap().filter { it.key.startsWith(path) })
            this[datum.key] = data.remove(datum.key)
    }
    fun setItemStackList(path: String, list: List<ItemStack>) = set(path, list.map { it.encodeToString() ?: "null" })
    fun setLocationList(path: String, list: List<Location>) = set(path, list.map { it.encodeToString() ?: "null" })
    operator fun set(path: String, data: Any?) {
        if (data == null) remove(path)
        else this.data[path] = when (data) {
            is ItemStack -> data.encodeToString() ?: "null"
            is Location -> data.encodeToString() ?: "null"
            else -> data
        }
    }
    @Suppress("UNCHECKED_CAST") operator fun <T> get(path: String) = data[path] as? T
    fun <T> getOrElse(path: String, orElse: T) = get(path) ?: orElse

    fun string(path: String) = get<Any>(path)?.toString()
    fun int(path: String) = double(path)?.toInt()
    fun short(path: String) = int(path)?.toShort()
    fun long(path: String) = double(path)?.toLong()
    fun double(path: String) = string(path)?.toDoubleOrNull()
    fun itemStack(path: String) = string(path)?.decodeToItemStack()
    fun location(path: String) = string(path)?.decodeToLocation()
    fun <T> list(path: String) = get<MutableList<T>>(path)
    fun itemStackList(path: String) = get<MutableList<String>>(path)?.mapNotNull { it.decodeToItemStack() }?.toMutableList()
    fun locationList(path: String) = get<MutableList<String>>(path)?.mapNotNull { it.decodeToLocation() }?.toMutableList()
    fun boolean(path: String, otherParsedAsFalse: Array<String> = arrayOf(), otherParsedAsTrue: Array<String> = arrayOf()): Boolean? {

        val string = string(path)?.lowercase() ?: return null

        val bool = string.toBooleanStrictOrNull()
        if (bool != null) return bool

        if (otherParsedAsFalse.any { it.equals(string, true) }) return false
        if (otherParsedAsTrue.any { it.equals(string, true) }) return true

        return null

    }

    fun stringOrElse(path: String, orElse: String) = string(path) ?: orElse
    fun intOrElse(path: String, orElse: Int) = int(path) ?: orElse
    fun longOrElse(path: String, orElse: Long) = long(path) ?: orElse
    fun shortOrElse(path: String, orElse: Short) = short(path) ?: orElse
    fun itemStackOrElse(path: String, orElse: ItemStack) = string(path)?.decodeToItemStack() ?: orElse
    fun locationOrElse(path: String, orElse: Location) = string(path)?.decodeToLocation() ?: orElse
    fun <T> listOrElse(path: String, orElse: List<T>) = list(path) ?: orElse.toMutableList()
    fun itemStackListOrElse(path: String, orElse: List<ItemStack>) = itemStackList(path) ?: orElse.toMutableList()
    fun locationListOrElse(path: String, orElse: List<Location>) = locationList(path) ?: orElse.toMutableList()
    fun booleanOrElse(path: String, orElse: Boolean, otherParsedAsFalse: Array<String> = arrayOf(), otherParsedAsTrue: Array<String> = arrayOf()) = boolean(path, otherParsedAsFalse, otherParsedAsTrue) ?: orElse

    fun build(): String = GSON.toJson(data)

}