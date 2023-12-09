package me.kodokenshi.kodocore.modules

import me.kodokenshi.kodocore.extras.javaPlugin
import me.kodokenshi.kodocore.plugin.*
import org.bukkit.entity.HumanEntity
import org.bukkit.event.HandlerList
import org.bukkit.event.player.AsyncPlayerChatEvent
import org.bukkit.event.player.PlayerQuitEvent

class ChatInput {

    private val chatInputCallback = mutableListOf<Result>()

    private val listener by lazy {

        listener {

            event<AsyncPlayerChatEvent> { isCancelled = chatInput(player, message) }
            event<PlayerQuitEvent> {

                for (result in chatInputCallback.toList()) {

                    if (result.player == player) {

                        result.cancelTimeout()
                        result.whenQuitWithoutInput()
                        chatInputCallback.remove(result)

                    }

                }

            }

        }

    }
    var enabled = false; set(enabled) {

        if (enabled == field) return

        if (enabled) enable()
        else disable()

        field = enabled

    }

    private fun disable() {
        HandlerList.unregisterAll(listener)
        for (value in chatInputCallback.toList()) value.cancelTimeout()
        chatInputCallback.clear()
    }
    private fun enable() { javaPlugin<KPlugin>().apply { server.pluginManager.registerEvents(listener, this) } }

    fun chatInput(player: HumanEntity, message: String): Boolean {

        if (!enabled) return false

        var cancelChatInput = false

        for (result in chatInputCallback.toList()) {

            if (result.player != player) continue

            result.cancelTimeout()
            result.input = message

            if (!result.persistent) chatInputCallback.remove(result)

            if (result.sync) runSync { result.callback(result) }
            else result.callback(result)

            if (result.cancelChatEvent) cancelChatInput = true

        }

        return cancelChatInput

    }
    fun waitInput(
        player: HumanEntity,
        input: Result.() -> Unit,
        whenQuitWithoutInput: () -> Unit,
        timeout: Long = 0L,
        onTimeout: () -> Unit,
        persistent: Boolean = false,
        sync: Boolean = false,
        id: String = player.name
    ) {

        enabled = true

        chatInputCallback.find { it.id == id }?.stopListening()
        chatInputCallback.add(Result(this, player, sync, whenQuitWithoutInput, timeout, onTimeout, input, persistent, id))

    }
    fun has(id: String) = chatInputCallback.firstOrNull { it.id == id } != null
    fun cancel(id: String): Boolean {

        val find = chatInputCallback.firstOrNull { it.id == id } ?: return false
        find.cancelTimeout()
        chatInputCallback.remove(find)
        return true

    }
    fun hasAny(player: HumanEntity) = chatInputCallback.any { it.player == player }
    fun cancelAll(player: HumanEntity) = chatInputCallback.removeIf {
        val eq = it.player == player
        if (eq) it.cancelTimeout()
        eq
    }

    class Result(
        private inline val chatInput: ChatInput,
        internal inline val player: HumanEntity,
        inline val sync: Boolean = false,
        internal inline val whenQuitWithoutInput: () -> Unit,
        private inline val timeout: Long = 0L,
        internal inline val onTimeout: () -> Unit,
        internal inline val callback: Result.() -> Unit,
        internal inline var persistent: Boolean,
        inline val id: String
    ) {

        var input = ""
        var cancelChatEvent = true

        private var timeoutTask: Task? = null
        internal fun cancelTimeout() {
            timeoutTask?.cancel()
            timeoutTask = null
        }

        init { createTask() }

        fun stopListening() {
            cancelTimeout()
            chatInput.chatInputCallback.remove(this)
        }
        fun waitChatInputAgain() {
            cancelTimeout()
            if (!persistent) chatInput.chatInputCallback.add(this)
            createTask()
        }

        private fun createTask() {
            timeoutTask?.cancel()
            timeoutTask = if (timeout <= 0L) null else if (sync) runSync(timeout) { stopListening(); onTimeout() } else runAsync(timeout) { stopListening(); onTimeout() }
        }

    }

}