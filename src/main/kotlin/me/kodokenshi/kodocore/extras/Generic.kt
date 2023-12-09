package me.kodokenshi.kodocore.extras

import org.bukkit.Bukkit
import java.io.ByteArrayOutputStream
import java.io.ObjectOutputStream
import java.io.Serializable
import java.util.*
import java.util.zip.GZIPOutputStream

inline fun <reified T : Enum<T>> enumValueOfOrElse(value: String?, orElse: T) = enumValues<T>().find { it.name == value } ?: orElse

fun <T> log(vararg any: T, applyColor: Boolean = true, colorChar: Char = '&') = Bukkit.getConsoleSender().sendMessage(*(
        if (applyColor) any.map { it.toString().color(colorChar) }
        else any.map { it.toString() }
        ).toTypedArray())
inline fun <T> T.matches(op: (T) -> Boolean) = op(this)

fun <T: Serializable> T.encodeToString(): String? {

    try {

        val bos = ByteArrayOutputStream()
        ObjectOutputStream(bos).use { it.writeObject(this) }
        val compressedBytes = ByteArrayOutputStream().apply {
            GZIPOutputStream(this).use { it.write(bos.toByteArray()) }
        }.toByteArray()
        return Base64.getEncoder().encodeToString(compressedBytes)

    } catch (_: Exception) { }

    return null

}