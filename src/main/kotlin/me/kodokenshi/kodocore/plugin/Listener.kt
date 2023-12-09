package me.kodokenshi.kodocore.plugin

import me.kodokenshi.kodocore.extras.javaPlugin
import me.kodokenshi.kodocore.extras.log
import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin

inline fun listener(block: me.kodokenshi.kodocore.plugin.Listener.() -> Unit) = Listener(javaPlugin()).apply(block)

open class Listener(val javaPlugin: JavaPlugin): Listener {

    inline fun <reified T: Event> event(
        priority: EventPriority = EventPriority.NORMAL,
        ignoreCancelled: Boolean = false,
        crossinline block: T.() -> Unit
    ) {

        val type = T::class
        javaPlugin.server.pluginManager.registerEvent(
            type.java, this, priority,
            { _, e -> if (type.isInstance(e)) (e as T).block() },
            javaPlugin, ignoreCancelled
        )

    }

    init {

        val name = javaPlugin.name

        for (method in this::class.java.declaredMethods)
            if (method.isAnnotationPresent(EventHandler::class.java)) {

                val annotation = method.getAnnotation(EventHandler::class.java)

                if (method.parameterCount != 1) {
                    log(
                        "&9$name> &cCould not register event listener for method \"${method.name}\" at class \"${this::class.java.name}\"",
                        "&9$name> &cMethod must have only one parameter and it must be assignable from \"org.bukkit.Event\"."
                    )
                    continue
                }

                val type = method.parameters[0].type

                if (type.isAssignableFrom(Event::class.java)) {
                    log(
                        "&9$name> &cCould not register event listener for method \"${method.name}\" at class \"${this::class.java.name}\"",
                        "&9$name> &cMethod parameter must be assignable from \"org.bukkit.Event\"."
                    )
                    continue
                }

                also {

                    javaPlugin.server.pluginManager.registerEvent(
                        type.asSubclass(Event::class.java), this, annotation.priority,
                        { _, e -> if (type.isInstance(e)) method.invoke(this, e) },
                        javaPlugin, annotation.ignoreCancelled
                    )

                }

            }

    }

}