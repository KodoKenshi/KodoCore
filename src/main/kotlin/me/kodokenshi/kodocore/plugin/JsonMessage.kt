package me.kodokenshi.kodocore.plugin

import me.kodokenshi.kodocore.extras.color
import me.kodokenshi.kodocore.extras.javaPlugin
import me.kodokenshi.kodocore.extras.replaceFirstCharAsString
import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.HoverEvent
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.api.chat.hover.content.Text
import org.bukkit.entity.Player

inline fun jsonMessage(block: JsonMessage.() -> Unit) = JsonMessage().apply(block)

class JsonMessage {

    private val mainComponent = TextComponent()
    private var lastComponent = mainComponent

    fun text(text: String) = apply { lastComponent = TextComponent(text); mainComponent.addExtra(lastComponent) }
    fun coloredText(text: String) = text(text.color())
    fun chat(text: String) = runCommand("${javaPlugin<KPlugin>().name}#jsonmessage#chat $text")
    fun chatInput(text: String) = runCommand("${javaPlugin<KPlugin>().name}#jsonmessage#chatinput $text")
    fun openURL(url: String) = apply { lastComponent.clickEvent = ClickEvent(ClickEvent.Action.OPEN_URL, url) }
    fun openFile(file: String) = apply { lastComponent.clickEvent = ClickEvent(ClickEvent.Action.OPEN_FILE, file) }
    fun changePage(page: Int) = apply { lastComponent.clickEvent = ClickEvent(ClickEvent.Action.CHANGE_PAGE, "$page") }
    fun suggestCommand(command: String) = apply { lastComponent.clickEvent = ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/${command.replaceFirstCharAsString { if (it == "/") "" else it }}") }
    fun runCommand(command: String) = apply { lastComponent.clickEvent = ClickEvent(ClickEvent.Action.RUN_COMMAND, "/${command.replaceFirstCharAsString { if (it == "/") "" else it }}") }
    fun copyToClipboard(text: String) = apply { lastComponent.clickEvent = ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, text) }
    fun showText(text: String) = apply { lastComponent.hoverEvent = HoverEvent(HoverEvent.Action.SHOW_TEXT, Text(text)) }
    fun showColoredText(text: String) = showText(text.color())
    //todo
//    fun showItem(itemStack: ItemStack): JsonMessage { lastComponent.hoverEvent = HoverEvent(HoverEvent.Action.SHOW_ITEM, ); return this }
//    fun showEntity(entity: Entity): JsonMessage { lastComponent.hoverEvent = HoverEvent(HoverEvent.Action.SHOW_ENTITY, ); return this }

    fun sendTo(player: Player) = player.spigot().sendMessage(mainComponent)

}