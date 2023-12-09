package me.kodokenshi.kodocore.command

import org.bukkit.command.*
import org.bukkit.command.Command
import org.bukkit.entity.Player

fun subCommand(
    name: String,
    properties: (SubCommandPropertiesBuilder.() -> Unit)? = null,
    onAnyExecute: (SubCommand.(sender: CommandSender, commandAlias: String, args: Array<String>) -> Unit)? = null,
    onPlayerExecute: (SubCommand.(player: Player, commandAlias: String, args: Array<String>) -> Unit)? = null,
    onConsoleExecute: (SubCommand.(console: ConsoleCommandSender, commandAlias: String, args: Array<String>) -> Unit)? = null,
    onAnyTabComplete: (SubCommand.(sender: CommandSender, commandAlias: String, args: Array<String>) -> List<String>?)? = null,
    onPlayerTabComplete: (SubCommand.(player: Player, commandAlias: String, args: Array<String>) -> List<String>?)? = null,
    onConsoleTabComplete: (SubCommand.(console: ConsoleCommandSender, commandAlias: String, args: Array<String>) -> List<String>?)? = null,
) = object: SubCommand(name, properties) {

    override fun onAnyExecute(sender: CommandSender, commandAlias: String, args: Array<String>) { onAnyExecute?.invoke(this, sender, commandAlias, args) }
    override fun onPlayerExecute(player: Player, commandAlias: String, args: Array<String>) { onPlayerExecute?.invoke(this, player, commandAlias, args) }
    override fun onConsoleExecute(console: ConsoleCommandSender, commandAlias: String, args: Array<String>) { onConsoleExecute?.invoke(this, console, commandAlias, args) }
    override fun onAnyTabComplete(sender: CommandSender, commandAlias: String, args: Array<String>): List<String>? { return onAnyTabComplete?.invoke(this, sender, commandAlias, args) }
    override fun onPlayerTabComplete(player: Player, commandAlias: String, args: Array<String>): List<String>? { return onPlayerTabComplete?.invoke(this, player, commandAlias, args) }
    override fun onConsoleTabComplete(console: ConsoleCommandSender, commandAlias: String, args: Array<String>): List<String>? { return onConsoleTabComplete?.invoke(this, console, commandAlias, args) }

}

open class SubCommand(
    inline val name: String,
    properties: (SubCommandPropertiesBuilder.() -> Unit)? = null
): CommandProperties(), CommandExecutor, TabCompleter {

    init {

        if (properties != null) {

            val builder = SubCommandPropertiesBuilder()

            properties.invoke(builder)

            _executeRules.addAll(builder._executeRules)
            _tabCompleteRules.addAll(builder._tabCompleteRules)
            _subcommands.addAll(builder._subcommands)
            usage = builder.usage
            permissionMessage = builder.permissionMessage
            permission = builder.permission
            description = builder.description
            executableBy = builder.executableBy

            also { alias = builder.alias }

        }

    }

    open fun onAnyExecute(sender: CommandSender, commandAlias: String, args: Array<String>) {}
    open fun onPlayerExecute(player: Player, commandAlias: String, args: Array<String>) {}
    open fun onConsoleExecute(console: ConsoleCommandSender, commandAlias: String, args: Array<String>) {}

    open fun onAnyTabComplete(sender: CommandSender, commandAlias: String, args: Array<String>): List<String>? = null
    open fun onPlayerTabComplete(player: Player, commandAlias: String, args: Array<String>): List<String>? = null
    open fun onConsoleTabComplete(console: ConsoleCommandSender, commandAlias: String, args: Array<String>): List<String>? = null

    final override fun onCommand(sender: CommandSender, cmd: Command, alias: String, args: Array<String>): Boolean {

        if (canExecute(sender, alias, args)) {

            val firstArg = args.firstOrNull()

            if (firstArg != null) {

                val subCommand = getSubCommand(firstArg)
                if (subCommand != null)
                    return subCommand.onCommand(sender, cmd, firstArg, args.toMutableList().apply { removeFirst() }.toTypedArray())

            }

            if (sender is Player) onPlayerExecute(sender, alias, args)
            else if (sender is ConsoleCommandSender) onConsoleExecute(sender, alias, args)

            onAnyExecute(sender, alias, args)

        }

        return true
    }

    override fun onTabComplete(sender: CommandSender, cmd: Command, alias: String, args: Array<String>): List<String>? {

        if (canTabComplete(sender, alias, args)) {

            val firstArg = args.firstOrNull()

            if (firstArg != null) {

                val subCommand = getSubCommand(firstArg)
                if (subCommand != null)
                    return subCommand.onTabComplete(sender, cmd, firstArg, args.toMutableList().apply { removeFirst() }.toTypedArray())

            }

            return when (sender) {
                is Player -> onPlayerTabComplete(sender, alias, args)?.toMutableList()?.apply { removeIf { it.isBlank() } }
                is ConsoleCommandSender -> onConsoleTabComplete(sender, alias, args)?.toMutableList()?.apply { removeIf { it.isBlank() } }
                else -> onAnyTabComplete(sender, alias, args)?.toMutableList()?.apply { removeIf { it.isBlank() } }
            }

        }

        return null

    }

}