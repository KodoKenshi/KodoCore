package me.kodokenshi.kodocore.extras

import me.kodokenshi.kodocore.plugin.KPlugin
import org.bukkit.Location
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.metadata.FixedMetadataValue
import org.bukkit.plugin.java.JavaPlugin

fun LivingEntity.dropEquipments(location: Location = getLocation(), ignoreDropChance: Boolean = false) {

    equipment?.apply {

        val world = location.world ?: return

        if (itemInMainHand.isNotNullOrAir() && (ignoreDropChance || randomFloat() <= itemInMainHandDropChance)) world.dropItemNaturally(location, itemInMainHand)
        if (itemInOffHand.isNotNullOrAir() && (ignoreDropChance || randomFloat() <= itemInOffHandDropChance)) world.dropItemNaturally(location, itemInOffHand)

        if (helmet.isNotNullOrAir() && (ignoreDropChance || randomFloat() <= helmetDropChance)) world.dropItemNaturally(location, helmet!!)
        if (chestplate.isNotNullOrAir() && (ignoreDropChance || randomFloat() <= chestplateDropChance)) world.dropItemNaturally(location, chestplate!!)
        if (leggings.isNotNullOrAir() && (ignoreDropChance || randomFloat() <= leggingsDropChance)) world.dropItemNaturally(location, leggings!!)
        if (boots.isNotNullOrAir() && (ignoreDropChance || randomFloat() <= bootsDropChance)) world.dropItemNaturally(location, boots!!)

    }

}
fun LivingEntity.removeEquipments(silent: Boolean = true) {

    equipment?.apply {

        setItemInMainHand(null, silent)
        setItemInOffHand(null, silent)
        setHelmet(null, silent)
        setChestplate(null, silent)
        setLeggings(null, silent)
        setBoots(null, silent)

    }

}
fun LivingEntity.equipmentsWontDrop() {

    equipment?.apply {

        itemInMainHandDropChance = 0f
        itemInOffHandDropChance = 0f
        helmetDropChance = 0f
        chestplateDropChance = 0f
        leggingsDropChance = 0f
        bootsDropChance = 0f

    }

}

fun <T> Entity.removeTemporaryData(key: String) = removeTemporaryData<T?>(javaPlugin(), key)
fun Entity.removeTemporaryData(key: String) = removeTemporaryData(javaPlugin(), key)
fun Entity.removeTemporaryData(plugin: JavaPlugin, key: String) = removeMetadata(key, plugin)
fun <T> Entity.removeTemporaryData(plugin: JavaPlugin, key: String): T? {
    val removed = getTemporaryData<T?>(plugin, key)
    removeMetadata(key, plugin)
    return removed
}
fun <T> Entity.setTemporaryData(key: String, data: T) = setMetadata(key, FixedMetadataValue(javaPlugin(), data))
fun <T> Entity.setTemporaryData(plugin: JavaPlugin, key: String, data: T) = setMetadata(key, FixedMetadataValue(plugin, data))
fun <T> Entity.getTemporaryData(key: String): T? {
    val main = javaPlugin<KPlugin>()
    return getMetadata(key).find { it.owningPlugin == main }?.value() as? T?
}
fun <T> Entity.getTemporaryDataOrElse(key: String, orElse: T) = getTemporaryData(key) ?: orElse
fun <T> Entity.getTemporaryDataOrElse(plugin: JavaPlugin, key: String, orElse: T) = getTemporaryData(plugin, key) ?: orElse
fun <T> Entity.getTemporaryData(plugin: JavaPlugin, key: String) = getMetadata(key).find { it.owningPlugin == plugin }?.value() as? T?
fun Entity.hasTemporaryData(key: String) = hasTemporaryData(javaPlugin(), key)
fun Entity.hasTemporaryData(plugin: JavaPlugin, key: String) = getTemporaryData<Any?>(plugin, key) != null