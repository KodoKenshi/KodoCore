package me.kodokenshi.kodocore.extras

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*
import kotlin.random.Random
import kotlin.time.Duration.Companion.seconds

inline val Int.seconds get() = seconds.inWholeSeconds * 1000L
inline val Int.secondsInTicks get() = this * 20L
inline val Int.minutesInTicks get() = secondsInTicks * 60L
inline val Long.seconds get() = seconds.inWholeSeconds * 1000L
inline val Long.secondsInTicks get() = this * 20L
inline val Long.minutesInTicks get() = secondsInTicks * 60L
inline val Double.seconds get() = seconds.inWholeSeconds * 1000L
inline val Double.secondsInTicks get() = this.toLong() * 20L
inline val Double.minutesInTicks get() = secondsInTicks * 60L

fun Number.toRoman(): String {

    val values = listOf(
        1000 to "M",
        900 to "CM",
        500 to "D",
        400 to "CD",
        100 to "C",
        90 to "XC",
        50 to "L",
        40 to "XL",
        10 to "X",
        9 to "IX",
        5 to "V",
        4 to "IV",
        1 to "I"
    )

    var remaining = this.toDouble()

    return buildString {

        for (i in values) {

            val value = i.first
            val symbol = i.second

            while (remaining >= value) {

                append(symbol)
                remaining -= value

            }

        }

    }

}

fun random0to100() = Random.nextDouble() * 100.0
fun randomFloat() = Random.nextFloat()
fun randomDouble() = Random.nextDouble()
inline val Number.isEven get() = this.toDouble() % 2.0 == .0
inline val Number.idOdd get() = !isEven

private val decimalFormat = DecimalFormat("#,###.##", DecimalFormatSymbols(Locale("pt", "BR")))
fun String.formatNumber(
    integralChar: Char = '.',
    decimalChar: Char = ',',
    minIntegerDigits: Int = 1,
    maxIntegerDigits: Int = 40,
    minDecimalDigits: Int = 0,
    maxDecimalDigits: Int = 3,
    includeDecimal: Boolean = true,
) = (toLongOrNull() ?: toDoubleOrNull())?.format(integralChar, decimalChar, minIntegerDigits, maxIntegerDigits, minDecimalDigits, maxDecimalDigits, includeDecimal) ?: this
fun Number.format(
    integralChar: Char = '.',
    decimalChar: Char = ',',
    minIntegerDigits: Int = 1,
    maxIntegerDigits: Int = 40,
    minDecimalDigits: Int = 0,
    maxDecimalDigits: Int = 3,
    includeDecimal: Boolean = true,
): String {

    decimalFormat.decimalFormatSymbols.groupingSeparator = integralChar
    decimalFormat.decimalFormatSymbols.decimalSeparator = decimalChar
    decimalFormat.minimumIntegerDigits = minIntegerDigits
    decimalFormat.maximumIntegerDigits = maxIntegerDigits
    decimalFormat.minimumFractionDigits = minDecimalDigits
    decimalFormat.maximumFractionDigits = maxDecimalDigits

    var string = decimalFormat.format(this)

    if (string.contains(decimalChar) && !includeDecimal)
        string = string.substring(0, string.indexOf(decimalChar))

    return string

}
fun String.twoDigits(
    integralChar: Char = '.',
    decimalChar: Char = ',',
    includeDecimal: Boolean = true
) = toLongOrNull()?.twoDigits(integralChar, decimalChar, includeDecimal) ?: toDoubleOrNull()?.twoDigits(integralChar, decimalChar, includeDecimal) ?: this
fun Number.twoDigits(
    integralChar: Char = '.',
    decimalChar: Char = ',',
    includeDecimal: Boolean = true
) = format(
    integralChar = integralChar,
    decimalChar = decimalChar,
    minIntegerDigits = 2,
    minDecimalDigits = if (includeDecimal) 2 else 0,
    includeDecimal = includeDecimal
)