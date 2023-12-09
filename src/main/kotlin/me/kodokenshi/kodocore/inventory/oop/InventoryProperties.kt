package me.kodokenshi.kodocore.inventory.oop

import me.kodokenshi.kodocore.extras.createInventory
import me.kodokenshi.kodocore.inventory.Inventory
import me.kodokenshi.kodocore.inventory.InventoryType
import me.kodokenshi.kodocore.inventory.event.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryOpenEvent

open class InventoryProperties {

    var id = ""
    var userData: Any? = null

    protected lateinit var propInventory: Inventory
    lateinit var mainPage: Inventory; protected set

    var cancelClickEventOn = InventoryType.NONE
    var listenClickEventOn = InventoryType.ANY

    var clickEvent: InventoryClickEvent.() -> Unit = {}
    var openEvent: InventoryOpenEvent.() -> Unit = {}
    var closeEvent: InventoryCloseEvent.() -> Unit = {}

    fun onClick(clickEvent: InventoryClickEvent.() -> Unit) { this.clickEvent = clickEvent }
    fun onOpen(openEvent: InventoryOpenEvent.() -> Unit) { this.openEvent = openEvent }
    fun onClose(closeEvent: InventoryCloseEvent.() -> Unit) { this.closeEvent = closeEvent }

    protected val itemHandler = mutableMapOf<Int, InventoryItem>()
    protected val slotHandler = mutableMapOf<Int, InventoryClickEvent.() -> Unit>()
    private val pages = mutableListOf<Inventory>()

    //------------------------------------------------------------------------------------------------------

    fun addPage(title: String, lines: Int = 3, copyMainPageBehaviour: Boolean = false, block: Inventory.() -> Unit = {}): Inventory {

        if (isPage()) return mainPage.addPage(title, lines, copyMainPageBehaviour, block)

        val inventory = createInventory(title, lines)
        inventory.mainPage = propInventory

        copyBehaviourTo(inventory)
        pages.add(inventory)

        block(inventory)

        return inventory

    }
    fun addPage(page: Inventory, copyMainPageBehaviour: Boolean = false) {

        if (isPage()) mainPage.addPage(page, copyMainPageBehaviour)
        else {

            page.mainPage = propInventory
            pages.add(page)

            if (copyMainPageBehaviour) copyBehaviourTo(page)

        }

    }

    fun removePage(index: Int): Inventory? {

        if (isPage()) return mainPage.removePage(index)

        val page = getPage(index) ?: return null

        pages.remove(page)
        page.mainPage = page.propInventory

        return page

    }
    fun removePage(id: String): Inventory? {

        if (isPage()) return mainPage.removePage(id)

        val page = getPage(id) ?: return null

        pages.remove(page)
        page.mainPage = page.propInventory

        return page

    }
    fun removePage(page: Inventory) {

        if (isPage()) mainPage.removePage(page)
        else {

            pages.remove(page)
            page.mainPage = page.propInventory

        }

    }

    //------------------------------------------------------------------------------------------------------

    fun isPage() = mainPage != propInventory

    fun hasPage(index: Int) = getPage(index) != null
    fun hasPage(id: String) = getPage(id) != null

    fun hasNextPage(): Boolean {

        val index = getPageIndex()
        return index <= pages.size - 1

    }
    fun hasPreviousPage() = mainPage != propInventory

    fun clearPages() = pages.clear()

    //------------------------------------------------------------------------------------------------------

    fun getPageIndex() = if (!isPage()) 0 else pages.withIndex().firstOrNull { it.value.propInventory == propInventory }?.index?.plus(1) ?: -1
    fun getLastPageIndex() = pages.size

    fun getPage(index: Int): Inventory? {

        if (index == 0) return mainPage
        if (isPage()) return mainPage.getPage(index)

        val realIndex = index - 1
        if (realIndex < 0 || realIndex > pages.size) return null
        return pages[realIndex]

    }
    fun getPage(id: String, ignoreCase: Boolean = false): Inventory? {

        if (id.equals(mainPage.id, ignoreCase)) return mainPage
        return pages.find { it.id.equals(id, ignoreCase) }

    }

    fun getNextPage() = getPage(getPageIndex() + 1)
    fun getPreviousPage() = getPage(getPageIndex() - 1)

    fun getPages() = pages.toList()

    //------------------------------------------------------------------------------------------------------

    fun copyBehaviourTo(inventory: Inventory) {
        inventory.cancelClickEventOn = cancelClickEventOn
        inventory.listenClickEventOn = listenClickEventOn
        inventory.clickEvent = clickEvent
        inventory.closeEvent = closeEvent
        inventory.openEvent = openEvent
        inventory.slotHandler.putAll(slotHandler)
    }

}