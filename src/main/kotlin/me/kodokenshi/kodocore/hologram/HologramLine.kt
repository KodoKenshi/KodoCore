package me.kodokenshi.kodocore.hologram

import me.kodokenshi.kodocore.extras.javaPlugin
import me.kodokenshi.kodocore.plugin.KPlugin
import org.bukkit.Location
import org.bukkit.NamespacedKey
import org.bukkit.entity.Display
import org.bukkit.entity.EntityType
import org.bukkit.entity.TextDisplay
import org.bukkit.persistence.PersistentDataType

@Deprecated("Shouldn't be used in production yet, not polished.")
class HologramLine(
    private inline var location: Location,
    private inline var text: String
) {

    private var display: TextDisplay? = null

    init {

        display = location.world?.getNearbyEntities(location, 1.0, 1.0, 1.0)?.firstOrNull { it.type == EntityType.TEXT_DISPLAY && it.persistentDataContainer.has(NamespacedKey(javaPlugin<KPlugin>().name.lowercase(), "kodocore-hologram"), PersistentDataType.BOOLEAN) } as? TextDisplay

    }

    fun setText(text: String) {
        this.text = text
        if (display == null || display!!.isDead) spawn()
        else display?.text = text
    }
    fun teleport(location: Location) {
        this.location = location
        display?.teleport(location)
    }

    fun spawn() {

        if (display != null && !display!!.isDead) return

        display = location.world?.spawn(location, TextDisplay::class.java) ?: return

        display!!.persistentDataContainer.set(NamespacedKey(javaPlugin<KPlugin>(), "kodocore-hologram"), PersistentDataType.BOOLEAN, true)

        display!!.text = text
        display!!.alignment = TextDisplay.TextAlignment.CENTER
        display!!.billboard = Display.Billboard.CENTER
        display!!.isSeeThrough = false
        display!!.isPersistent = true

    }
    fun despawn() { display?.remove() }

    fun getUniqueId() = display?.uniqueId
    fun getLocation() = location.clone()

}