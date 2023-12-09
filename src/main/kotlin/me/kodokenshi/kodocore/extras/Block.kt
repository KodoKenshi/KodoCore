package me.kodokenshi.kodocore.extras

import me.kodokenshi.kodocore.plugin.KPlugin
import org.bukkit.block.Block
import org.bukkit.metadata.FixedMetadataValue
import org.bukkit.plugin.java.JavaPlugin

fun <T> Block.removeTemporaryData(key: String) = removeTemporaryData<T?>(javaPlugin(), key)
fun Block.removeTemporaryData(key: String) = removeTemporaryData(javaPlugin(), key)
fun <T> Block.removeTemporaryData(plugin: JavaPlugin, key: String): T? {
    val removed = getTemporaryData<T?>(plugin, key)
    removeMetadata(key, plugin)
    return removed
}
fun Block.removeTemporaryData(plugin: JavaPlugin, key: String) = removeMetadata(key, plugin)
fun <T> Block.setTemporaryData(key: String, data: T) = setMetadata(key, FixedMetadataValue(javaPlugin(), data))
fun <T> Block.setTemporaryData(plugin: JavaPlugin, key: String, data: T) = setMetadata(key, FixedMetadataValue(plugin, data))
fun <T> Block.getTemporaryData(key: String): T? {
    val main = javaPlugin<KPlugin>()
    return getMetadata(key).find { it.owningPlugin == main }?.value() as? T?
}
fun <T> Block.getTemporaryDataOrElse(key: String, orElse: T) = getTemporaryData(key) ?: orElse
fun <T> Block.getTemporaryDataOrElse(plugin: JavaPlugin, key: String, orElse: T) = getTemporaryData(plugin, key) ?: orElse
fun <T> Block.getTemporaryData(plugin: JavaPlugin, key: String) = getMetadata(key).find { it.owningPlugin == plugin }?.value() as? T?
fun Block.hasTemporaryData(key: String) = hasTemporaryData(javaPlugin(), key)
fun Block.hasTemporaryData(plugin: JavaPlugin, key: String) = getTemporaryData<Any?>(plugin, key) != null