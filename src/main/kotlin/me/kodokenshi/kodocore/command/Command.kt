package me.kodokenshi.kodocore.command

import me.kodokenshi.kodocore.extras.javaPlugin
import me.kodokenshi.kodocore.extras.log
import me.kodokenshi.kodocore.plugin.KPlugin
import org.bukkit.command.CommandSender
import org.bukkit.command.ConsoleCommandSender
import org.bukkit.entity.Player

fun command(
    name: String,
    properties: (CommandPropertiesBuilder.() -> Unit)? = null,
    onAnyExecute: (Command.(sender: CommandSender, commandAlias: String, args: Array<String>) -> Unit)? = null,
    onPlayerExecute: (Command.(player: Player, commandAlias: String, args: Array<String>) -> Unit)? = null,
    onConsoleExecute: (Command.(console: ConsoleCommandSender, commandAlias: String, args: Array<String>) -> Unit)? = null,
    onAnyTabComplete: (Command.(sender: CommandSender, commandAlias: String, args: Array<String>) -> List<String>?)? = null,
    onPlayerTabComplete: (Command.(player: Player, commandAlias: String, args: Array<String>) -> List<String>?)? = null,
    onConsoleTabComplete: (Command.(console: ConsoleCommandSender, commandAlias: String, args: Array<String>) -> List<String>?)? = null,
) = object: Command(name, properties) {

    override fun onAnyExecute(sender: CommandSender, commandAlias: String, args: Array<String>) { onAnyExecute?.invoke(this, sender, commandAlias, args) }
    override fun onPlayerExecute(player: Player, commandAlias: String, args: Array<String>) { onPlayerExecute?.invoke(this, player, commandAlias, args) }
    override fun onConsoleExecute(console: ConsoleCommandSender, commandAlias: String, args: Array<String>) { onConsoleExecute?.invoke(this, console, commandAlias, args) }
    override fun onAnyTabComplete(sender: CommandSender, commandAlias: String, args: Array<String>): List<String>? { return onAnyTabComplete?.invoke(this, sender, commandAlias, args) }
    override fun onPlayerTabComplete(player: Player, commandAlias: String, args: Array<String>): List<String>? { return onPlayerTabComplete?.invoke(this, player, commandAlias, args) }
    override fun onConsoleTabComplete(console: ConsoleCommandSender, commandAlias: String, args: Array<String>): List<String>? { return onConsoleTabComplete?.invoke(this, console, commandAlias, args) }

}

abstract class Command(
    name: String,
    properties: (CommandPropertiesBuilder.() -> Unit)? = null
): SubCommand(name, {

    if (properties != null) {

        val builder = CommandPropertiesBuilder().apply(properties)

        _executeRules.addAll(builder._executeRules)
        _tabCompleteRules.addAll(builder._tabCompleteRules)
        _subcommands.addAll(builder._subcommands)
        usage = builder.usage
        permissionMessage = builder.permissionMessage
        permission = builder.permission
        description = builder.description
        executableBy = builder.executableBy

    }

}) {

    init {

        val main = javaPlugin<KPlugin>()
        val pluginCommand = main.getCommand(name)
        if (pluginCommand == null) { "&9${main.name}> &7Command \"${name}\" not declared in plugin.yml.".log() } else {

            if (usage.isNullOrEmpty()) usage = pluginCommand.usage
            if (permission.isNullOrEmpty()) permission = pluginCommand.permission
            if (permissionMessage.isNullOrEmpty()) permissionMessage = pluginCommand.permissionMessage
            if (description.isNullOrEmpty()) description = pluginCommand.description

            also {

                alias = pluginCommand.aliases
                pluginCommand.setExecutor(it)
                pluginCommand.tabCompleter = it

            }

        }
        
    }

}