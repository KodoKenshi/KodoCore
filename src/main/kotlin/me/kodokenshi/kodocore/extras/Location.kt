package me.kodokenshi.kodocore.extras

import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.block.Block
import org.bukkit.util.io.BukkitObjectOutputStream
import java.io.ByteArrayOutputStream
import java.util.*
import java.util.zip.GZIPOutputStream
import kotlin.math.max
import kotlin.math.min

fun Location.getHighestBlock(
    minY: Int = -64,
    maxY: Int = world.maxHeight,
    ignore: List<Material?> = listOf(),
): Block {

    val x = x.toInt()
    var y = maxY
    val z = z.toInt()

    var highestBlock = world.getBlockAt(x, minY, z)

    while (y > minY) {

        val loop = world.getBlockAt(x, y, z)
        val type = loop.type

        if (type.isSolid && !ignore.contains(type)) {

            highestBlock = loop
            break

        }

        y--

    }

    return highestBlock

}
fun Location.equalsBlockXYZ(other: Location) = world == other.world && blockX == other.blockX && blockY == other.blockY && blockZ == other.blockZ
fun Location.equalsIgnoreYawPitch(other: Location) = world == other.world && x == other.x && y == other.y && z == other.z
fun Location.center() = clone().add(.5, .0, .5)
fun Location.pickupSound(volume: Double = .7) = sound(Sound.ENTITY_CHICKEN_EGG, volume = volume, distortion = ((randomFloat() - randomFloat()) * .7 + 1f) * 2f)
fun Location.sound(sound: Sound, volume: Double = 1.0, distortion: Double = 1.0) { world?.playSound(this, sound, volume.toFloat(), distortion.toFloat()) }
fun Location.getBlocksInRadius(radius: Int) = buildList {

    val world = world ?: return@buildList
    val cx = blockX
    val cy = blockY
    val cz = blockZ

    for (x in cx - radius..cx + radius)
        for (y in cy - radius..cy + radius)
            for (z in cz - radius..cz + radius) {
                val loc = Location(world, x.toDouble(), y.toDouble(), z.toDouble())
                if (distance(loc) <= radius)
                    add(loc.block)
            }

}
fun Location.getSquare(pointB: Location) = mutableListOf<Location>().apply {

    if (world == null || pointB.world == null || world != pointB.world) return@apply

    val topX = max(blockX, pointB.blockX)
    val bottomX = min(blockX, pointB.blockX)

    val topY = max(blockY, pointB.blockY)
    val bottomY = min(blockY, pointB.blockY)

    val topZ = max(blockZ, pointB.blockZ)
    val bottomZ = min(blockZ, pointB.blockZ)

    for (loopX in bottomX..topX)
        for (loopZ in bottomZ..topZ)
            for (loopY in bottomY..topY)
                addIf(Location(world, loopX.toDouble(), loopY.toDouble(), loopZ.toDouble())) { it != this }

}
fun Location.getSquareBorders(pointB: Location) = mutableListOf<Location>().apply {

    if (world == null || pointB.world == null || world != pointB.world) return@apply

    val topX = max(blockX, pointB.blockX)
    val bottomX = min(blockX, pointB.blockX)

    val topY = max(blockY, pointB.blockY)
    val bottomY = min(blockY, pointB.blockY)

    val topZ = max(blockZ, pointB.blockZ)
    val bottomZ = min(blockZ, pointB.blockZ)

    for (loopX in bottomX..topX) {

        addIf(Location(world, loopX.toDouble(), bottomY.toDouble(), bottomZ.toDouble())) { it != this }
        addIf(Location(world, loopX.toDouble(), bottomY.toDouble(), topZ.toDouble())) { it != this }
        if (bottomY != topY) {
            addIf(Location(world, loopX.toDouble(), topY.toDouble(), bottomZ.toDouble())) { it != this }
            addIf(Location(world, loopX.toDouble(), topY.toDouble(), topZ.toDouble())) { it != this }
        }

    }
    for (loopZ in bottomZ..topZ) {

        addIf(Location(world, bottomX.toDouble(), bottomY.toDouble(), loopZ.toDouble())) { it != this }
        addIf(Location(world, topX.toDouble(), bottomY.toDouble(), loopZ.toDouble())) { it != this }
        if (bottomY != topY) {
            addIf(Location(world, bottomX.toDouble(), topY.toDouble(), loopZ.toDouble())) { it != this }
            addIf(Location(world, topX.toDouble(), topY.toDouble(), loopZ.toDouble())) { it != this }
        }

    }

    if (bottomY != topY) {

        for (loopY in bottomY..topY) {

            addIf(Location(world, bottomX.toDouble(), loopY.toDouble(), bottomZ.toDouble())) { it != this }
            addIf(Location(world, topX.toDouble(), loopY.toDouble(), topZ.toDouble())) { it != this }
            addIf(Location(world, bottomX.toDouble(), loopY.toDouble(), topZ.toDouble())) { it != this }
            addIf(Location(world, topX.toDouble(), loopY.toDouble(), bottomZ.toDouble())) { it != this }

        }

    }

}
fun Location.getSquareCorners(pointB: Location) = mutableListOf<Location>().apply {

    if (world == null || pointB.world == null || world != pointB.world) return@apply

    val topX = max(blockX, pointB.blockX).toDouble()
    val bottomX = min(blockX, pointB.blockX).toDouble()

    val topY = max(blockY, pointB.blockY).toDouble()
    val bottomY = min(blockY, pointB.blockY).toDouble()

    val topZ = max(blockZ, pointB.blockZ).toDouble()
    val bottomZ = min(blockZ, pointB.blockZ).toDouble()

    addIf(Location(world, bottomX, bottomY, bottomZ)) { it != this }
    addIf(Location(world, topX, bottomY, topZ)) { it != this }
    addIf(Location(world, bottomX, bottomY, topZ)) { it != this }
    addIf(Location(world, topX, bottomY, bottomZ)) { it != this }

    if (bottomY != topY) {

        addIf(Location(world, bottomX, topY, bottomZ)) { it != this }
        addIf(Location(world, topX, topY, topZ)) { it != this }
        addIf(Location(world, bottomX, topY, topZ)) { it != this }
        addIf(Location(world, topX, topY, bottomZ)) { it != this }

    }

}

fun Location.encodeToString(): String? {

    try {

        val bos = ByteArrayOutputStream()
        BukkitObjectOutputStream(bos).use { it.writeObject(serialize()) }
        val compressedBytes = ByteArrayOutputStream().apply {
            GZIPOutputStream(this).use { it.write(bos.toByteArray()) }
        }.toByteArray()
        return Base64.getEncoder().encodeToString(compressedBytes)

    } catch (_: Exception) {}

    return null

}