package me.kodokenshi.kodocore.plugin

import me.kodokenshi.kodocore.compatibility.ServerVersion
import me.kodokenshi.kodocore.extras.log
import me.kodokenshi.kodocore.extras.removeFirst
import me.kodokenshi.kodocore.file.JsonFile
import me.kodokenshi.kodocore.modules.Modules
import org.bukkit.NamespacedKey
import org.bukkit.event.HandlerList
import org.bukkit.event.player.PlayerCommandPreprocessEvent
import org.bukkit.persistence.PersistentDataType
import org.bukkit.plugin.java.JavaPlugin

abstract class KPlugin(
    val minServerVersion: ServerVersion = ServerVersion.MC_1_19,
    val maxServerVersion: ServerVersion = ServerVersion.UNKNOWN
): JavaPlugin() {

    val modules by lazy { Modules() }
    val jsonDataFile by lazy { JsonFile("plugins/${description.name}", "data") }

    final override fun onLoad() {

        var minVersion = minServerVersion
        if (minVersion == ServerVersion.UNKNOWN) {
            minVersion = ServerVersion.MC_1_19
            log("&9$name with KodoCore> &cMinimum server version cannot be UNKNOWN. &7Changed to MC_1_19.")
        }

        if (minVersion.isGreaterThan(ServerVersion.CURRENT)) {
            log("&9$name> &cThis server version is not supported. &7Minimum version: ${ServerVersion.CURRENT.version}")
            isEnabled = false
            return
        }

        onPluginLoad()

    }
    final override fun onEnable() {

        val namespace = NamespacedKey(this, "kodocore-hologram")
        for (world in server.worlds.toList())
            for (entity in world.entities.toList())
                if (entity.persistentDataContainer.has(namespace, PersistentDataType.BOOLEAN))
                    entity.remove()

        listener {

            event<PlayerCommandPreprocessEvent> {

                if (message.startsWith("/$name#jsonmessage#chat ", true)) {
                    isCancelled = true
                    player.chat(message.removeFirst("/$name#jsonmessage#chat ", true))
                } else if (message.startsWith("/$name#jsonmessage#chatinput ", true)) {
                    isCancelled = true
                    modules.chatInput.chatInput(player, message.removeFirst("/$name#jsonmessage#chatinput ", true))
                }

            }

        }

        onPluginEnable()

    }
    final override fun onDisable() {

        HandlerList.unregisterAll(this)
        server.scheduler.cancelTasks(this)

        onPluginDisable()

    }

    open fun onPluginLoad() {}
    abstract fun onPluginEnable()
    open fun onPluginDisable() {}

}