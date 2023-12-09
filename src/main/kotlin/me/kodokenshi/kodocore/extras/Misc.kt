package me.kodokenshi.kodocore.extras

import org.bukkit.plugin.java.JavaPlugin
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets

inline val currentTimeMillis get() = System.currentTimeMillis()
inline fun <reified T : JavaPlugin> javaPlugin() = JavaPlugin.getPlugin(T::class.java)
fun readJarTextFile(pathInsideJar: String) = buildList {

    try {

        val url = object {}.javaClass.getResource(pathInsideJar)

        if (url == null) {
            add("not found")
            return@buildList
        }

        val inputStreamReader = InputStreamReader(url.openConnection().apply { useCaches = false }.getInputStream(), StandardCharsets.UTF_8)

        addAll(inputStreamReader.readLines())

        inputStreamReader.close()

    } catch (_: Exception) { add("error") }

}