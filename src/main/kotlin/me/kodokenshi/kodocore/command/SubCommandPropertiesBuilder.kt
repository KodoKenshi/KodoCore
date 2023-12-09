package me.kodokenshi.kodocore.command

class SubCommandPropertiesBuilder: CommandProperties() {

    override var alias get() = super.alias; public set(alias) { super.alias = alias }

    fun setAlias(vararg alias: String) { this.alias = alias.toList() }

}