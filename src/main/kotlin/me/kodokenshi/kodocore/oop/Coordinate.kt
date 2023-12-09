package me.kodokenshi.kodocore.oop

import org.bukkit.Location
import org.bukkit.World

fun Location.toCoordinate() = Coordinate(x, y, z)
class Coordinate(var x: Double, var y: Double, var z: Double) {

    fun toLocation(world: World) = Location(world, x, y, z)

    override fun equals(other: Any?) = other is Coordinate && other.x == x && other.y == y && other.z == z
    override fun hashCode(): Int {
        var result = x.hashCode()
        result = 31 * result + y.hashCode()
        result = 31 * result + z.hashCode()
        return result
    }

}