package me.kodokenshi.kodocore.command

import org.bukkit.command.CommandSender
import org.bukkit.command.ConsoleCommandSender
import org.bukkit.entity.Player

open class CommandProperties {

    internal val _executeRules = mutableListOf<Rule>()
    internal val _tabCompleteRules = mutableListOf<Rule>()
    internal val _subcommands = mutableListOf<SubCommand>()

    val subcommands get() = _subcommands.toList()
    inline val subcommandNames get() = buildList {
        for (subcommand in subcommands) {
            add(subcommand.name)
            addAll(subcommand.alias)
        }
    }
    fun subcommandNamesFor(player: Player) = buildList {
        for (subcommand in subcommands) {

            if (!subcommand.permission.isNullOrBlank() && !player.hasPermission(subcommand.permission!!)) continue

            add(subcommand.name)
            addAll(subcommand.alias)

        }
    }
    val executeRules get() = _executeRules.toList()
    val tabCompleteRules get() = _tabCompleteRules.toList()

    open var alias = listOf<String>(); internal set
    var usage: String? = null
    var permissionMessage: String? = null
    var permission: String? = null
    var description: String? = null

    var executableBy = ExecutableBy.BOTH

    fun getSubCommand(name: String) = _subcommands.toList().find { it.name.equals(name, true) || it.alias.any { a -> a.equals(name, true) } }
    fun addSubCommand(subcommand: SubCommand) {

        if (hasSubCommand(subcommand.name) || subcommand.alias.any { hasSubCommand(it) }) {
            require(!hasExecuteRule(subcommand.name)) { "An subcommand rule by name \"${subcommand.name}\" has already been added." }
            return
        }

        _subcommands.add(subcommand)

    }
    fun hasSubCommand(name: String) = _subcommands.toList().any { it.name.equals(name, true) || it.alias.any { a -> a.equals(name, true) } }
    fun removeSubCommand(name: String) = _subcommands.remove(getSubCommand(name) ?: false)

    fun canExecute(sender: CommandSender, commandAlias: String, args: Array<String>): Boolean {

        if (!permission.isNullOrBlank() && !sender.hasPermission(permission!!)) {
            if (permissionMessage != null) sender.sendMessage(permissionMessage!!)
            return false
        }
        if (executableBy == ExecutableBy.PLAYER && sender !is Player || executableBy == ExecutableBy.CONSOLE && sender !is ConsoleCommandSender) {
            if (!executableBy.cantExecuteMessage.isNullOrBlank()) sender.sendMessage(executableBy.cantExecuteMessage!!)
            return false
        }

        for (priority in Rule.Priority.entries)
            for (rule in _executeRules.toList().filter { it.priority == priority })
                if (!rule.test(sender, commandAlias, args))
                    return false

        return true
    }
    fun canTabComplete(sender: CommandSender, commandAlias: String, args: Array<String>): Boolean {

        if (!permission.isNullOrBlank() && !sender.hasPermission(permission!!)) return false
        if (executableBy == ExecutableBy.PLAYER && sender !is Player || executableBy == ExecutableBy.CONSOLE && sender !is ConsoleCommandSender) return false

        for (priority in Rule.Priority.entries)
            for (rule in _tabCompleteRules.toList().filter { it.priority == priority })
                if (!rule.test(sender, commandAlias, args))
                    return false

        return true
    }

    fun rule(name: String, priority: Rule.Priority, predicate: CommandArguments.() -> Boolean) = Rule(name, priority, predicate)
    fun rule(name: String, predicate: CommandArguments.() -> Boolean) = rule(name, Rule.Priority.MEDIUM, predicate)
    
    fun addExecuteRule(rule: Rule) {
        require(!hasExecuteRule(rule.name)) { "An execute rule by name \"${rule.name}\" has already been added." }
        _executeRules.add(rule)
    }
    fun addExecuteRule(name: String, priority: Rule.Priority, predicate: CommandArguments.() -> Boolean) {
        require(!hasExecuteRule(name)) { "An execute rule by name \"$name\" has already been added." }
        _executeRules.add(Rule(name, priority, predicate))
    }
    fun addExecuteRule(name: String, predicate: CommandArguments.() -> Boolean) = addExecuteRule(name, Rule.Priority.MEDIUM, predicate)

    fun hasExecuteRule(name: String) = _executeRules.toList().any { it.name.equals(name, true) }

    fun removeExecuteRule(name: String) = _executeRules.remove(_executeRules.toList().find { it.name == name } ?: false)

    fun addTabCompleteRule(rule: Rule) {
        require(!hasTabCompleteRule(rule.name)) { "An tab complete rule by name \"${rule.name}\" has already been added." }
        _tabCompleteRules.add(rule)
    }
    fun addTabCompleteRule(name: String, priority: Rule.Priority, predicate: CommandArguments.() -> Boolean) {
        require(!hasTabCompleteRule(name)) { "An tab complete rule by name \"$name\" has already been added." }
        _tabCompleteRules.add(Rule(name, priority, predicate))
    }
    fun addTabCompleteRule(name: String, predicate: CommandArguments.() -> Boolean) = addTabCompleteRule(name, Rule.Priority.MEDIUM, predicate)

    fun hasTabCompleteRule(name: String) = _tabCompleteRules.toList().any { it.name.equals(name, true) }

    fun removeTabCompleteRule(name: String) = _tabCompleteRules.remove(_tabCompleteRules.toList().find { it.name == name } ?: false)
    
    enum class ExecutableBy(inline var cantExecuteMessage: String? = null) {

        CONSOLE, PLAYER, BOTH;

        fun isAssignableFrom(commandSender: CommandSender) = when {
            this == CONSOLE -> commandSender is ConsoleCommandSender
            this == PLAYER -> commandSender is Player
            else -> true
        }

    }

    class CommandArguments(
        inline val sender: CommandSender,
        inline val commandAlias: String,
        inline val args: Array<String>,
    )
    open class Rule(
        inline val name: String,
        inline val priority: Priority,
        inline val predicate: CommandArguments.() -> Boolean
    ) {

        fun test(sender: CommandSender, commandAlias: String, args: Array<String>) = predicate(CommandArguments(sender, commandAlias, args))

        enum class Priority { LOW, MEDIUM, HIGH }

    }

}