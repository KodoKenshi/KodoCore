package me.kodokenshi.kodocore.inventory.oop

import me.kodokenshi.kodocore.inventory.InventoryType
import me.kodokenshi.kodocore.inventory.event.InventoryClickEvent

open class ClickableItem {

    var ignoreCancelled = false

    var cancelClickEventOn = InventoryType.NONE
    var listenClickEventOn = InventoryType.ANY

    internal var clickEvent: InventoryClickEvent.() -> Unit = {}

    fun onClick(clickEvent: InventoryClickEvent.() -> Unit): ClickableItem {
        this.clickEvent = clickEvent
        return this
    }

}