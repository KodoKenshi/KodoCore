package me.kodokenshi.kodocore.file

import me.kodokenshi.kodocore.extras.javaPlugin
import me.kodokenshi.kodocore.extras.log
import me.kodokenshi.kodocore.plugin.Json
import me.kodokenshi.kodocore.plugin.KPlugin
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardOpenOption
import java.util.*

inline fun jsonFile(fileName: String, block: JsonFile.() -> Unit) = JsonFile("plugins/${javaPlugin<KPlugin>().name}", fileName).apply(block)
inline fun jsonFile(filePath: String, fileName: String, block: JsonFile.() -> Unit) = JsonFile(filePath, fileName).apply(block)

class JsonFile(inline val filePath: String, fileName: String): Json() {

    val name = "$fileName.json"

    private val fileFilePath = File(filePath)
    private val file = File(fileFilePath, this.name)

    init {

        try {

            if (!fileFilePath.exists()) fileFilePath.mkdirs()
            if (!file.exists()) file.createNewFile()

            reloadFile()

        } catch (e: Exception) {
            "&9${javaPlugin<KPlugin>().name}> &7Couldn't create or load file \"$fileName\" in folder \"${fileFilePath.absoluteFile}\".".log()
            e.printStackTrace()
        }

    }

    fun reloadFile() {

        try {

            data.clear()

            val update = GSON.fromJson(Files.newBufferedReader(file.toPath()), data.javaClass)
            if (update != null && update.isNotEmpty()) data.putAll(update)

        } catch (e: Exception) {
            "&9${javaPlugin<KPlugin>().name}> &7Couldn't load file \"$name\" in folder \"${fileFilePath.absoluteFile}\".".log()
            e.printStackTrace()
        }

    }
    fun saveFile() {

        try {

            Files.write(file.toPath(), Collections.singletonList(build()), StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE)

        } catch (e: Exception) {
            "&9${javaPlugin<KPlugin>().name}> &7Couldn't save file \"$name\" in folder \"${fileFilePath.absoluteFile}\".".log()
            e.printStackTrace()
        }

    }
    fun deleteFile() { if (!file.delete()) file.deleteOnExit() }

}