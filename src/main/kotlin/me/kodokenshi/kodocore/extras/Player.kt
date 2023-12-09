package me.kodokenshi.kodocore.extras

import me.kodokenshi.kodocore.modules.ChatInput
import me.kodokenshi.kodocore.plugin.JsonMessage
import me.kodokenshi.kodocore.plugin.KPlugin
import org.bukkit.Location
import org.bukkit.Sound
import org.bukkit.entity.HumanEntity
import org.bukkit.entity.Player

fun Player.pickupSound(volume: Double = .7) = playSound(Sound.ENTITY_CHICKEN_EGG, volume = volume, distortion = ((randomFloat() - randomFloat()) * .7 + 1f) * 2f)
fun Player.playSound(sound: Sound, volume: Double = 1.0, distortion: Double = 1.0) = playSound(location, sound, volume.toFloat(), distortion.toFloat())
fun Player.playTitle(title: String = "", subtitle: String = "", fadeIn: Int = 10, stay: Int = 70, fadeOut: Int = 20) = sendTitle(title, subtitle, fadeIn, stay, fadeOut)
//fun Player.actionBar(text: String = "") = sendMessage(ChatMessageType.ACTION_BAR, *TextComponent.fromLegacyText(text))
inline fun Player.sendJsonMessage(jsonMessage: JsonMessage.() -> Unit) = JsonMessage().apply(jsonMessage).sendTo(this)
fun Player.sendJsonMessage(jsonMessage: JsonMessage) = jsonMessage.sendTo(this)
fun HumanEntity.faceLocation(location: Location) {
    val direction = location.clone().subtract(eyeLocation).toVector()
    val playerLocation = this.location.setDirection(direction)
    teleport(playerLocation)
}
fun HumanEntity.sendColoredMessage(message: String) = sendMessage(message.color())
fun HumanEntity.sendColoredMessage(vararg message: String) = sendMessage(*message.toList().color().toTypedArray())
fun HumanEntity.isWaitingAnyChatInput() = javaPlugin<KPlugin>().modules.chatInput.hasAny(this)
fun HumanEntity.isWaitingChatInput(id: String) = javaPlugin<KPlugin>().modules.chatInput.has("$name.$id")
fun HumanEntity.waitChatInput(
    input: ChatInput.Result.() -> Unit,
    whenQuitWithoutInput: () -> Unit = {},
    timeout: Long = 0L,
    onTimeout: () -> Unit = {},
    persistent: Boolean = false,
    sync: Boolean = false,
    id: String = ""
) = javaPlugin<KPlugin>().modules.chatInput.waitInput(this@waitChatInput, input, whenQuitWithoutInput, timeout, onTimeout, persistent, sync, "$name.$id")
fun HumanEntity.cancelChatInput(id: String) = javaPlugin<KPlugin>().modules.chatInput.cancel("$name.$id")
fun HumanEntity.cancelAllChatInput() = javaPlugin<KPlugin>().modules.chatInput.cancelAll(this)
