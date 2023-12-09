package me.kodokenshi.kodocore.plugin

import me.kodokenshi.kodocore.extras.enumValueOfOrElse
import org.bukkit.Location
import org.bukkit.block.BlockFace

inline val Location.cardinalDirection: CardinalDirection get() {

    var rot = (yaw - 90f) % 360f
    if (rot < 0) rot += 360f

    return when {
        0 <= rot && rot < 22.5 -> CardinalDirection.WEST
        22.5 <= rot && rot < 67.5 -> CardinalDirection.NORTH_WEST
        67.5 <= rot && rot < 112.5 -> CardinalDirection.NORTH
        112.5 <= rot && rot < 157.5 -> CardinalDirection.NORTH_EAST
        157.5 <= rot && rot < 202.5 -> CardinalDirection.EAST
        202.5 <= rot && rot < 247.5 -> CardinalDirection.SOUTH_EAST
        247.5 <= rot && rot < 292.5 -> CardinalDirection.SOUTH
        292.5 <= rot && rot < 337.5 -> CardinalDirection.SOUTH_WEST
        337.5 <= rot && rot < 360 -> CardinalDirection.WEST
        else -> CardinalDirection.UNKNOWN
    }

}
enum class CardinalDirection {

    NORTH,
    NORTH_EAST,
    EAST,
    SOUTH_EAST,
    SOUTH,
    SOUTH_WEST,
    WEST,
    NORTH_WEST,

    UNKNOWN;

    inline val noDiagonal get() = valueOf(if (name.contains("_")) name.substring(0, name.indexOf("_")) else name)

    inline val blockFace get() = enumValueOfOrElse<BlockFace>(name, BlockFace.NORTH)
    inline val blockFaceNoDiagonal get() = enumValueOfOrElse<BlockFace>(noDiagonal.name, BlockFace.NORTH)

}