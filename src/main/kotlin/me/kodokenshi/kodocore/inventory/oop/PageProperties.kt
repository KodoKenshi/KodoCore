package me.kodokenshi.kodocore.inventory.oop

import me.kodokenshi.kodocore.extras.createItemStack
import me.kodokenshi.kodocore.extras.displayName
import me.kodokenshi.kodocore.extras.isNotNullOrAir
import me.kodokenshi.kodocore.inventory.Inventory
import me.kodokenshi.kodocore.inventory.event.InventoryClickEvent
import org.bukkit.Material

class PageProperties(private inline val inventory: Inventory) {

    var startOnMainPage = false
    var pageItemAlignment = PageItemAlignment.CENTER_UP
    var itemAlignment = ItemAlignment.FIRST_SLOT
    internal var titleBuilder: (Int) -> String = { inventory.title }
    internal var pageBuild: Inventory.() -> Unit = {}
    var nextPageItem = createItemStack(Material.STONE_BUTTON).apply { displayName = "Next page" }
        set(itemStack) {
            require(itemStack.isNotNullOrAir()) { "ItemStack cannot be AIR." }
            field = itemStack
        }
    internal var nextPageItemClick: InventoryClickEvent.() -> Unit = {}
    var previousPageItem = createItemStack(Material.STONE_BUTTON).apply { displayName = "Previous page" }
        set(itemStack) {
            require(itemStack.isNotNullOrAir()) { "ItemStack cannot be AIR." }
            field = itemStack
        }
    internal var previousPageItemClick: InventoryClickEvent.() -> Unit = {}

    fun onTitleRequest(title: (Int) -> String) { this.titleBuilder = title }
    fun onNextPageItemClick(nextPageItemClick: InventoryClickEvent.() -> Unit) { this.nextPageItemClick = nextPageItemClick }
    fun onPreviousPageItemClick(previousPageItemClick: InventoryClickEvent.() -> Unit) { this.previousPageItemClick = previousPageItemClick }

    enum class PageItemAlignment { TOP, CENTER_UP, CENTER_DOWN }
    enum class ItemAlignment { FIRST_SLOT, CENTER_FIRST_SLOT, CENTER_LAST_SLOT, LAST_SLOT }

}