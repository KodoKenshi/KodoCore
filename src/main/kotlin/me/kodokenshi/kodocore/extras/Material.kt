package me.kodokenshi.kodocore.extras

import org.bukkit.Material

val Material.isTool get() = name.endsWithAny("_PICKAXE", "_AXE", "_SHOVEL", "_HOE", "_SWORD", "BOW")
val Material.isArmor get() = name.endsWithAny("_HELMET", "_CHESTPLATE", "_LEGGINGS", "_BOOTS")