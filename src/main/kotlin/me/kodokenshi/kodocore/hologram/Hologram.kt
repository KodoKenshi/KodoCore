package me.kodokenshi.kodocore.hologram

import org.bukkit.Location

@Deprecated("Shouldn't be used in production yet, not polished.")
class Hologram(
    private inline val location: Location,
    vararg lines: String,
    private inline val lineSpacing: Double = .3,
) {

    private val lines = mutableListOf<HologramLine>()

    init {

        val clone = location.clone().add(.0, -lineSpacing, .0)
        for (line in lines)
            this.lines.add(HologramLine(clone.add(.0, lineSpacing, .0).clone(), line))

    }

    fun spawn() { for (line in lines) line.spawn() }
    fun despawn() { for (line in lines) line.despawn() }

    fun setLine(index: Int, text: String) { lines.getOrNull(index)?.setText(text) }
    fun setLines(vararg text: String) {

        val it = text.iterator()
        val lIt = lines.toList().iterator()

        while (it.hasNext()) {

            val next = it.next()

            if (lIt.hasNext()) lIt.next().setText(next)
            else lines.add(HologramLine(
                (lines.lastOrNull()?.getLocation() ?: location.clone().add(.0, -lineSpacing, .0)).add(.0, lineSpacing, .0),
                next
            ).apply { spawn() })

        }

        while (lIt.hasNext()) lines.remove(lIt.next().apply { despawn() })

    }

}