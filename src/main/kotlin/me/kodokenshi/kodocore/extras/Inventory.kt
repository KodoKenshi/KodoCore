package me.kodokenshi.kodocore.extras

import org.bukkit.inventory.Inventory
import kotlin.math.max
import kotlin.math.min

inline fun createInventory(title: String, lines: Int = 3, block: me.kodokenshi.kodocore.inventory.Inventory.() -> Unit = {}) = object: me.kodokenshi.kodocore.inventory.Inventory(title, lines) {}.apply(block)
fun calculateInventoryLinesFor(itemCount: Int, itemsPerLine: Int = 9, minLines: Int = 1, maxLines: Int = 6): Int {

    require(maxLines in 1..6) { "Parameter \"maxLines\" must be from 1 to 6. (value passed was $maxLines)" }
    require(minLines <= maxLines) { "Parameter \"minLines\" must be lower or equal to parameter \"maxLines\". (value passed was $minLines to $maxLines)" }
    require(minLines > 0) { "Parameter \"minLines\" must be from 1 to 6. (value passed was $minLines)" }
    require(itemsPerLine in 1..9) { "Parameter \"itemsPerLine\" must be from 1 to 10. (value passed was $itemsPerLine)" }

    val cleanCalc = itemCount / itemsPerLine
    val calc = itemCount.toDouble() / itemsPerLine.toDouble()

    return min(max(if (calc > cleanCalc) cleanCalc + 1 else cleanCalc, minLines), maxLines)

}
fun getCachedInventory(id: String) = me.kodokenshi.kodocore.inventory.Inventory.getCachedInventory(id)

fun Inventory.closeToViewers() { for (viewer in viewers.toList()) viewer.closeInventory() }