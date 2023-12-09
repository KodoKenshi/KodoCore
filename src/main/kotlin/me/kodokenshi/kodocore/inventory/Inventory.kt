package me.kodokenshi.kodocore.inventory

import me.kodokenshi.kodocore.extras.*
import me.kodokenshi.kodocore.inventory.oop.ClickableItem
import me.kodokenshi.kodocore.inventory.oop.InventoryItem
import me.kodokenshi.kodocore.inventory.oop.InventoryProperties
import me.kodokenshi.kodocore.inventory.oop.PageProperties
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.HumanEntity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.HandlerList
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryOpenEvent
import org.bukkit.event.server.PluginDisableEvent
import org.bukkit.inventory.InventoryHolder
import org.bukkit.inventory.ItemStack

open class Inventory(title: String = org.bukkit.event.inventory.InventoryType.CHEST.defaultTitle, lines: Int = 3) : InventoryHolder, InventoryProperties() {

    companion object {

        private val listener by lazy { Listener() }
        private val cachedInventories by lazy { mutableMapOf<String, Inventory>() }

        fun getCachedInventory(id: String) = cachedInventories[id]

    }

    //--------------------------------------------------------------------------------

    var title: String = ""
        set(title) {

            field = title

            for (viewer in viewers)
                viewer.openInventory.title = title

        }
    private var inventory: org.bukkit.inventory.Inventory; override fun getInventory() = inventory

    init {

        require(lines in 1..6) { "Parameter \"lines\" must be from 1 to 6. (value passed was $lines)" }
        also {
            inventory = Bukkit.createInventory(this, lines * 9, title)
            propInventory = this
            mainPage = this
        }

        this.title = title

    }

    init { listener }

    //--------------------------------------------------------------------------------

    val viewers get() = inventory.viewers.toList()

    val items = inventory.contents.withIndex().map { getItem(it.index) }
    val itemsFilterValid get() = inventory.contents.filter { it.isNotNullOrAir() }.withIndex().map { getItem(it.index) }
    val itemsWithIndex get() = buildMap {

        for ((index, itemStack) in inventory.contents.withIndex())
            if (itemStack.isNotNullOrAir())
                this[index] = getItem(index)

    }
    val itemStacks = inventory.contents.toList()
    val itemStacksFilterValid get() = inventory.contents.filter { it.isNotNullOrAir() }
    val itemStacksWithIndex get() = buildMap {

        for ((index, itemStack) in inventory.contents.withIndex())
            if (itemStack.isNotNullOrAir())
                this[index] = itemStack!!

    }

    //--------------------------------------------------------------------------------

    fun getRawSize() = inventory.size
    fun getLines() = inventory.size / 9
    fun setLines(lines: Int) = buildList {

        require(lines in 1..6) { "Parameter \"lines\" must be from 1 to 6. (value passed was $lines)" }

        val oldContent = itemStacks

        inventory = Bukkit.createInventory(this@Inventory, lines * 9, title)
        val newSize = inventory.size

        for ((index, item) in oldContent.withIndex()) {

            if (item.isNullOrAir()) {
                itemHandler.remove(index)
                continue
            }

            if (index >= newSize) {
                itemHandler.remove(index)
                add(item!!)
                continue
            }

            inventory.setItem(index, item)

        }

    }

    //--------------------------------------------------------------------------------

    fun setItemToFirstEmpty(itemStack: ItemStack, block: InventoryItem.() -> Unit = {}) = if (getFirstEmptySlot() == -1) null else setItem(getFirstEmptySlot(), itemStack, block)
    fun setItemToMiddleFirstEmpty(itemStack: ItemStack, block: InventoryItem.() -> Unit = {}) = if (getMiddleFirstEmptySlot() == -1) null else setItem(getMiddleFirstEmptySlot(), itemStack, block)

    fun setItem(line: Int, slot: Int, itemStack: ItemStack, block: InventoryItem.() -> Unit = {}) = getItem(line, slot).apply { this.itemStack = itemStack }.apply(block)
    fun setItem(rawSlot: Int, itemStack: ItemStack, block: InventoryItem.() -> Unit = {}) = getItem(rawSlot).apply { this.itemStack = itemStack }.apply(block)
    fun setItem(vararg rawSlots: Int, itemStack: ItemStack, block: InventoryItem.() -> Unit = {}) { for (rawSlot in rawSlots) setItem(rawSlot, itemStack, block) }

    //--------------------------------------------------------------------------------

    fun getItemStack(line: Int, slot: Int) = inventory.getItem(calcSlot(line, slot))
    fun getItemStack(rawSlot: Int): ItemStack? {

        require(rawSlot >= 0) { "Parameter \"rawSlot\" must be non negative. (value passed was $rawSlot)" }
        require(rawSlot < inventory.size) { "Parameter \"rawSlot\" must be lower than inventory size, which is ${inventory.size}. (value passed was $rawSlot)" }

        return inventory.getItem(rawSlot)

    }
    fun getItem(line: Int, slot: Int) = getItem(calcSlot(line, slot))
    fun getItem(rawSlot: Int) = itemHandler[rawSlot] ?: InventoryItem(this, rawSlot.apply {
        require(rawSlot >= 0) { "Parameter \"rawSlot\" must be non negative. (value passed was $rawSlot)" }
        require(rawSlot < inventory.size) { "Parameter \"rawSlot\" must be lower than inventory size, which is ${inventory.size}. (value passed was $rawSlot)" }
    }, createItemStack(Material.AIR)).apply { itemHandler[rawSlot] = this }

    fun hasEmptySlot() = inventory.firstEmpty() != -1
    fun hasMiddleEmptySlot() = getMiddleFirstEmptySlot() != -1
    fun hasPartialEmptySlot(material: Material) = getFirstPartialSlot(material) != -1

    fun getFirstEmptySlot() = inventory.firstEmpty()
    fun getFirstPartialSlot(material: Material) = inventory.contents.withIndex().firstOrNull { it.value?.type == material && it.value.amount < it.value.type.maxStackSize }?.index ?: -1
    fun getMiddleFirstEmptySlot(): Int {

        val content = inventory.contents

        var slot = 1
        while (slot < inventory.size) {

            when (slot) {
                8 -> slot = 10
                17 -> slot = 19
                26 -> slot = 28
                35 -> slot = 37
                44 -> slot = 46
                53 -> return -1
            }

            if (content[slot].isNullOrAir())
                return slot

            slot++

        }

        return -1

    }

    //--------------------------------------------------------------------------------

    fun fillWith(content: Collection<ItemStack>, replace: Boolean = false) {

        if (content.isEmpty()) return

        var index = 0
        for (item in content) {

            val atSlot = getItem(index)
            if (atSlot.itemStack.isNullOrAir() || replace) atSlot.itemStack = item

            index++
            if (index >= inventory.size) break

        }

    }

    //--------------------------------------------------------------------------------

    fun setSlotClickHandler(line: Int, slot: Int, block: me.kodokenshi.kodocore.inventory.event.InventoryClickEvent.() -> Unit) = setSlotClickHandler(calcSlot(line, slot), block)
    fun setSlotClickHandler(vararg rawSlots: Int, block: me.kodokenshi.kodocore.inventory.event.InventoryClickEvent.() -> Unit) { for (rawSlot in rawSlots) setSlotClickHandler(rawSlot, block) }
    fun setSlotClickHandler(rawSlot: Int, block: me.kodokenshi.kodocore.inventory.event.InventoryClickEvent.() -> Unit) {

        require(rawSlot >= 0) { "Parameter \"rawSlot\" must be non negative. (value passed was $rawSlot)" }
        require(rawSlot < inventory.size) { "Parameter \"rawSlot\" must be lower than inventory size, which is ${inventory.size}. (value passed was $rawSlot)" }

        slotHandler[rawSlot] = block

    }

    fun setCancelClickOnSlot(vararg rawSlots: Int) { for (rawSlot in rawSlots) setCancelClickOnSlot(rawSlot) }
    fun setCancelClickOnSlot(rawSlot: Int) = setSlotClickHandler(rawSlot) { isCancelled = true }

    fun clearSlotClickHandler(line: Int, slot: Int) = clearSlotClickHandler(calcSlot(line, slot))
    fun clearSlotClickHandler(rawSlot: Int) {

        require(rawSlot >= 0) { "Parameter \"rawSlot\" must be non negative. (value passed was $rawSlot)" }
        require(rawSlot < inventory.size) { "Parameter \"rawSlot\" must be lower than inventory size, which is ${inventory.size}. (value passed was $rawSlot)" }

        slotHandler.remove(rawSlot)

    }
    fun clearSlotClickHandlers() { slotHandler.clear() }

    //--------------------------------------------------------------------------------

    fun <T> buildPages(
        content: List<T>,
        pageProperties: PageProperties.() -> Unit = {},
        itemBuilder: ClickableItem.(T) -> ItemStack,
    ) {

        val builder = PageProperties(this).apply(pageProperties)

        val pageItemAlignment = builder.pageItemAlignment
        val itemAlignment = builder.itemAlignment

        val pageItemOnTop = pageItemAlignment == PageProperties.PageItemAlignment.TOP
        val pageItemOnCenterUp = pageItemAlignment == PageProperties.PageItemAlignment.CENTER_UP
        val pageItemOnCenterDown = pageItemAlignment == PageProperties.PageItemAlignment.CENTER_DOWN
        val itemAlignOnFirstSlot = itemAlignment == PageProperties.ItemAlignment.FIRST_SLOT
        val itemAlignOnLastSlot = itemAlignment == PageProperties.ItemAlignment.LAST_SLOT
        val itemAlignOnCenterFirstSlot = itemAlignment == PageProperties.ItemAlignment.CENTER_FIRST_SLOT
        val itemAlignOnCenterLastSlot = itemAlignment == PageProperties.ItemAlignment.CENTER_LAST_SLOT
        val alignOnCenter = itemAlignOnCenterFirstSlot || itemAlignOnCenterLastSlot

        val maxContentIndex = content.size - 1

        var pageIndex = 1
        var currentPage = if (builder.startOnMainPage) this else createInventory(
            builder.titleBuilder(pageIndex),
            calculateInventoryLinesFor(maxContentIndex, if (alignOnCenter) 7 else 9, if (alignOnCenter) 3 else 1)
        )
        var pageSize = currentPage.inventory.size
        var pageItemSlot = when {
            pageItemOnTop -> 0
            pageItemOnCenterUp -> (when (pageSize) {
                54, 45 -> 18
                36, 27 -> 9
                else -> 0
            })
            pageItemOnCenterDown -> (when (pageSize) {
                54 -> 27
                45, 36 -> 18
                27, 18 -> 9
                else -> 0
            })
            else -> pageSize - 9
        }
        var pageItemSlot2 = pageItemSlot + 8

        val decreasing = itemAlignOnLastSlot || itemAlignOnCenterLastSlot
        var slot = when {
            itemAlignOnFirstSlot -> 0
            itemAlignOnCenterFirstSlot -> 10
            itemAlignOnCenterLastSlot -> pageSize - 11
            else -> pageSize - 1
        }

        for (contentIndex in 0..maxContentIndex) {

            if (slot == pageItemSlot || slot == pageItemSlot2) {
                if (decreasing) slot--
                else slot++
            }

            if (decreasing && slot == (if (alignOnCenter) 9 else -1) || slot == 54 || alignOnCenter && slot == 44) {

                builder.pageBuild(currentPage)

                currentPage.getItem(pageItemSlot2).apply {
                    this.itemStack = builder.nextPageItem
                }.onClick {

                    inventory.getNextPage()?.openTo(whoClicked)
                    builder.nextPageItemClick(this)

                }
                currentPage = currentPage.addPage(builder.titleBuilder(pageIndex), calculateInventoryLinesFor(
                    maxContentIndex - contentIndex,
                    if (alignOnCenter) 7 else 9,
                    if (alignOnCenter) 3 else 1
                ))

                pageSize = currentPage.inventory.size
                pageItemSlot = when {
                    pageItemOnTop -> 0
                    pageItemOnCenterUp -> (when (pageSize) {
                        54, 45 -> 18
                        36, 27 -> 9
                        else -> 0
                    })
                    pageItemOnCenterDown -> (when (pageSize) {
                        54 -> 27
                        45, 36 -> 18
                        27, 18 -> 9
                        else -> 0
                    })
                    else -> pageSize - 9
                }
                pageItemSlot2 = pageItemSlot + 8
                slot = when {
                    itemAlignOnFirstSlot -> 0
                    itemAlignOnCenterFirstSlot -> 10
                    itemAlignOnCenterLastSlot -> pageSize - 11
                    else -> pageSize - 1
                }
                pageIndex++

                currentPage.setItem(pageItemSlot, builder.previousPageItem).onClick {

                    inventory.getPreviousPage()?.openTo(whoClicked)
                    builder.previousPageItemClick(this)

                }

            }

            val clickableItem = ClickableItem()
            val item = itemBuilder(clickableItem, content[contentIndex])
            if (item.isNotNullOrAir()) currentPage.setItem(slot, item).apply {
                ignoreCancelled = clickableItem.ignoreCancelled
                cancelClickEventOn = clickableItem.cancelClickEventOn
                listenClickEventOn = clickableItem.listenClickEventOn
                clickEvent = clickableItem.clickEvent
            }

            if (decreasing) slot--
            else slot++

            if (alignOnCenter) {

                if (decreasing) when (slot) {
                    36 -> slot = 34
                    27 -> slot = 25
                    18 -> slot = 16
                } else when (slot) {
                    17 -> slot = 19
                    26 -> slot = 28
                    35 -> slot = 37
                }

            }

        }

        builder.pageBuild(currentPage)

    }

    //--------------------------------------------------------------------------------

    fun cacheInventory() { cachedInventories[id] = this }
    fun uncacheInventory() { cachedInventories.remove(id) }

    fun clear() {

        for (value in itemHandler.values.toList()) value.slot = -1

        itemHandler.clear()
        inventory.clear()

    }
    fun clear(line: Int, slot: Int) = clear(calcSlot(line, slot))
    fun clear(rawSlot: Int): InventoryItem? {

        require(rawSlot >= 0) { "Parameter \"rawSlot\" must be non negative. (value passed was $rawSlot)" }
        require(rawSlot < inventory.size) { "Parameter \"rawSlot\" must be lower than inventory size, which is ${inventory.size}. (value passed was $rawSlot)" }

        inventory.clear(rawSlot)
        return itemHandler.remove(rawSlot)?.apply { slot = -1 }

    }

    fun close() { for (viewer in viewers) viewer.closeInventory() }
    fun closePages() { for (page in getPages()) page.close() }
    fun closeAll() { close(); closePages() }

    fun openTo(player: HumanEntity) { player.openInventory(inventory) }

    //--------------------------------------------------------------------------------

    private fun calcSlot(line: Int, slot: Int): Int {

        require(line in 1..(inventory.size / 9)) { "Parameter \"line\" must be from 1 to ${inventory.size / 9}. (value passed was $line)" }
        require(slot in 1..9) { "Parameter \"slot\" must be from 1 to 9. (value passed was $slot)" }

        val end = line * 9
        val start = end - 9

        return (start + slot) - 1

    }

    private fun handle(e: InventoryClickEvent) {

        val isPlayerInventory = e.clickedInventory!!.type == org.bukkit.event.inventory.InventoryType.PLAYER
        val callClick = listenClickEventOn == InventoryType.PLAYER && isPlayerInventory || listenClickEventOn == InventoryType.CHEST && !isPlayerInventory || listenClickEventOn == InventoryType.ANY

        e.isCancelled = cancelClickEventOn == InventoryType.ANY || cancelClickEventOn == InventoryType.PLAYER && isPlayerInventory || cancelClickEventOn == InventoryType.CHEST && !isPlayerInventory

        val item = if (isPlayerInventory) null else getItem(e.slot)
        val event = me.kodokenshi.kodocore.inventory.event.InventoryClickEvent(e, item, this)

        if (callClick) clickEvent(event)
        if (item == null) return
        if (callClick) slotHandler[e.slot]?.invoke(event)

        if (e.isCancelled && item.ignoreCancelled) return

        item.clickEvent(event)

    }
    private fun handle(e: InventoryCloseEvent) { closeEvent(e) }
    private fun handle(e: InventoryOpenEvent) {

        e.view.title = title
        openEvent(e)

    }

    private class Listener: me.kodokenshi.kodocore.plugin.Listener(javaPlugin()) {

        @EventHandler fun onEvent(e: InventoryClickEvent) {

            if (e.clickedInventory == null || e.whoClicked !is Player) return
            (e.inventory.holder as? Inventory)?.handle(e)

        }
        @EventHandler fun onEvent(e: InventoryCloseEvent) { (e.inventory.holder as? Inventory)?.handle(e) }
        @EventHandler fun onEvent(e: InventoryOpenEvent) { (e.inventory.holder as? Inventory)?.handle(e) }
        @EventHandler fun onEvent(e: PluginDisableEvent) {

            if (e.plugin != javaPlugin) return

            HandlerList.unregisterAll(this)

            for (onlinePlayer in javaPlugin.server.onlinePlayers)
                (onlinePlayer.openInventory.topInventory.holder as? Inventory)?.close()

        }

    }

}