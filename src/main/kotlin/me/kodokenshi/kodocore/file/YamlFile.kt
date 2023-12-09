package me.kodokenshi.kodocore.file

import me.kodokenshi.kodocore.extras.*
import me.kodokenshi.kodocore.plugin.KPlugin
import org.bukkit.Location
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.inventory.ItemStack
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets

inline fun yamlFile(filePath: String, fileName: String, block: YamlFile.() -> Unit = {}) = YamlFile(filePath, fileName).apply(block)
inline fun yamlFile(fileName: String, block: YamlFile.() -> Unit = {}) = YamlFile(fileName).apply(block)

@Suppress("UNCHECKED_CAST")
class YamlFile {

    private val fileName: String
    private val filePath: File
    private val file: File
    private var config: YamlConfiguration

    constructor(filePath: String, fileName: String) {

        this.fileName = "$fileName.yml"
        this.filePath = File(filePath)
        this.file = File(this.filePath, this.fileName)

        config = if (init()) YamlConfiguration.loadConfiguration(InputStreamReader(FileInputStream(file), StandardCharsets.UTF_8)) else YamlConfiguration()

    }
    constructor(fileName: String): this("plugins/${javaPlugin<KPlugin>().name}", fileName)

    fun reloadFile() { config.load(InputStreamReader(FileInputStream(file), StandardCharsets.UTF_8)) }
    fun saveFile() { config.save(file) }
    fun delete() { if (!file.delete()) file.deleteOnExit() }

    fun section(path: String, deep: Boolean = false) = config.getConfigurationSection(path)?.getKeys(deep) ?: setOf()
    fun containsPath(key: String) = config.contains(key)
    fun remove(path: String) = config.set(path, null)
    fun setItemStackList(path: String, list: List<ItemStack>) = set(path, list.map { it.encodeToString() ?: "null" })
    operator fun set(path: String, data: Any?) {
        if (data == null) remove(path)
        else config.set(path, when(data) {
            is ItemStack -> data.encodeToString() ?: "null"
            is Location -> data.encodeToString() ?: "null"
            else -> data
        })
    }
    operator fun <T> get(path: String) = config.get(path) as? T
    fun <T> getOrElse(path: String, orElse: T? = null) = get(path) ?: orElse
    fun string(path: String) = get<Any>(path)?.toString()
    fun int(path: String) = string(path)?.toIntOrNull()
    fun long(path: String) = string(path)?.toLongOrNull()
    fun double(path: String) = string(path)?.toDoubleOrNull()
    fun itemStack(path: String) = string(path)?.decodeToItemStack()
    fun location(path: String) = string(path)?.decodeToLocation()
    fun boolean(path: String, otherParsedAsFalse: Array<String> = arrayOf(), otherParsedAsTrue: Array<String> = arrayOf()): Boolean? {

        val string = string(path)?.lowercase() ?: return null

        val bool = string.toBooleanStrictOrNull()
        if (bool != null) return bool

        if (otherParsedAsFalse.any { it.equals(string, true) }) return false
        if (otherParsedAsTrue.any { it.equals(string, true) }) return true

        return null

    }
    fun <T> list(path: String) = get<MutableList<T>>(path)
    fun itemStackList(path: String) = get<MutableList<String>>(path)?.mapNotNull { it.decodeToItemStack() }
    fun locationList(path: String) = get<MutableList<String>>(path)?.mapNotNull { it.decodeToLocation() }

    fun stringOrElse(path: String, orElse: String) = string(path) ?: orElse
    fun intOrElse(path: String, orElse: Int) = int(path) ?: orElse
    fun longOrElse(path: String, orElse: Long) = long(path) ?: orElse
    fun doubleOrElse(path: String, orElse: Double) = double(path) ?: orElse
    fun itemStackOrElse(path: String, orElse: ItemStack) = string(path)?.decodeToItemStack() ?: orElse
    fun locationOrElse(path: String, orElse: Location) = string(path)?.decodeToLocation() ?: orElse
    fun <T> listOrElse(path: String, orElse: MutableList<T>) = list(path) ?: orElse
    fun itemStackListOrElse(path: String, orElse: MutableList<ItemStack>) = itemStackList(path) ?: orElse
    fun locationListOrElse(path: String, orElse: MutableList<Location>) = locationList(path) ?: orElse
    fun booleanOrElse(path: String, orElse: Boolean, otherParsedAsFalse: Array<String> = arrayOf(), otherParsedAsTrue: Array<String> = arrayOf()) = boolean(path, otherParsedAsFalse, otherParsedAsTrue) ?: orElse

    private fun init(): Boolean {

        return try {

            if (!filePath.exists()) filePath.mkdirs()
            if (!file.exists()) {

                try { javaPlugin<KPlugin>().saveResource(fileName, true) } catch (_: Exception) { file.createNewFile() }
                file.createNewFile()

            }

            true

        } catch (e: Exception) {
            "&9${javaPlugin<KPlugin>().name}> &7Couldn't create or load file \"$fileName\" in folder \"${filePath.absoluteFile}\".".log()
            e.printStackTrace()
            false
        }

    }

}