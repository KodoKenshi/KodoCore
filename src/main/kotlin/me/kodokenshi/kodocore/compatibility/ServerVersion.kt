package me.kodokenshi.kodocore.compatibility

import org.bukkit.Bukkit

enum class ServerVersion(val version: String, val nmsVersion: String) {

    UNKNOWN("UNKNOWN", "UNKNOWN"),
    MC_1_19("1.19", "v1_19_R1"),
    MC_1_20("1.20", "v1_20_R1");

    fun isOrLess(other: ServerVersion) = ordinal <= other.ordinal
    fun isLessThan(other: ServerVersion) = ordinal < other.ordinal
    fun isAtLeast(other: ServerVersion) = ordinal >= other.ordinal
    fun isGreaterThan(other: ServerVersion) = ordinal > other.ordinal
    fun isIn(min: ServerVersion, max: ServerVersion) = ordinal in min.ordinal..max.ordinal

    companion object {

        val CURRENT by lazy {
            val version = Bukkit.getBukkitVersion().split("-")[0]
            entries.firstOrNull { version.contains(it.version) } ?: UNKNOWN
        }

    }

}