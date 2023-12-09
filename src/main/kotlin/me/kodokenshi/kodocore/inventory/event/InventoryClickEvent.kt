package me.kodokenshi.kodocore.inventory.event

import me.kodokenshi.kodocore.extras.isNotNullOrAir
import me.kodokenshi.kodocore.extras.matches
import me.kodokenshi.kodocore.inventory.Inventory
import me.kodokenshi.kodocore.inventory.oop.InventoryItem
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryAction
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryType

class InventoryClickEvent(
    private inline val event: InventoryClickEvent,
    inline val clickedItem: InventoryItem?,
    inline val inventory: Inventory
) {

    val whoClicked = event.whoClicked as Player
    val hasItem = clickedItem != null
    val hasItemStack get() = event.currentItem.isNotNullOrAir()
    val clickedItemStack get() = event.currentItem
    val hasCursor get() = event.view.cursor.isNotNullOrAir()
    var cursor get() = event.view.cursor; set(cursor) { event.view.cursor = cursor }
    var isCancelled get() = event.isCancelled; set(cancelled) { event.isCancelled = cancelled }
    val isPlayerInventoryClick = event.clickedInventory?.type == InventoryType.PLAYER
    val playerInventory = whoClicked.inventory
    val slot = event.slot

    val isRightClick = event.isRightClick
    val isLeftClick = event.isLeftClick
    val isShiftClick = event.isShiftClick
    val isPickupAll = event.action == InventoryAction.PICKUP_ALL
    val isPickupHalf = event.action == InventoryAction.PICKUP_HALF
    val isPickupOne = event.action == InventoryAction.PICKUP_ONE
    val isPlaceOne = event.action == InventoryAction.PLACE_ONE
    val isPlaceAll = event.action == InventoryAction.PLACE_ALL
    val isDrop = event.action.matches { it == InventoryAction.DROP_ONE_SLOT || it == InventoryAction.DROP_ALL_SLOT }
    val isDropOne = event.action == InventoryAction.DROP_ONE_SLOT
    val isDropAll = event.action == InventoryAction.DROP_ALL_SLOT

    fun reopen() {
        whoClicked.closeInventory()
        whoClicked.openInventory(event.inventory)
    }

}