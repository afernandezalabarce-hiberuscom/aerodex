package dev.afalabarce.aircrafts.core.common.extensions


import kotlinx.datetime.*
import platform.Foundation.*
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

actual fun Int.format(format: String): String = try {
    val formatter = NSNumberFormatter()
    formatter.positiveFormat = format
    formatter.negativeFormat = "-$format"

    formatter.stringFromNumber(NSNumber.numberWithInt(this)) ?: ""
} catch (_: Exception) {
    ""
}

actual fun Long.format(format: String): String = try {
    val formatter = NSNumberFormatter()
    formatter.positiveFormat = format
    formatter.negativeFormat = "-$format"

    formatter.stringFromNumber(NSNumber.numberWithLong(this)) ?: ""
} catch (_: Exception) {
    ""
}

actual fun Double.format(format: String): String = try {
    val formatter = NSNumberFormatter()
    formatter.positiveFormat = format
    formatter.negativeFormat = "-$format"

    formatter.stringFromNumber(NSNumber.numberWithDouble(this)) ?: ""
} catch (_: Exception) {
    ""
}

actual fun Float.format(format: String): String = try {
    val formatter = NSNumberFormatter()
    formatter.positiveFormat = format
    formatter.negativeFormat = "-$format"

    formatter.stringFromNumber(NSNumber.numberWithFloat(this)) ?: ""
} catch (_: Exception) {
    ""
}

private const val SECONDS_PER_DAY = 86_400L

@ExperimentalTime
actual fun LocalDate?.format(format: String): String = try {
    val dateFormatter = NSDateFormatter()
    dateFormatter.dateFormat = format
    dateFormatter.stringFromDate(this!!.toInstant().toNSDate())
} catch (_: Exception) {
    ""
}

@ExperimentalTime
actual fun LocalDateTime?.format(format: String): String = try {
    val dateFormatter = NSDateFormatter()
    dateFormatter.dateFormat = format
    dateFormatter.stringFromDate(this!!.toInstant(UtcOffset.ZERO).toNSDate())
} catch (_: Exception) {
    ""
}

actual fun String?.toLocalDate(format: String): LocalDate? = try {
    NSDateFormatter().let { formatter ->
        formatter.dateFormat = format
        val result: Long = formatter.dateFromString(string = this!!)?.timeIntervalSince1970?.toLong() ?: 0L

        LocalDate.fromEpochDays((result / SECONDS_PER_DAY).toInt())
    }
} catch (_: Exception) {
    null
}

@ExperimentalTime
actual fun String?.toLocalDateTime(format: String): LocalDateTime? = try {
    NSDateFormatter().let { formatter ->
        formatter.dateFormat = format
        val result = formatter.dateFromString(string = this!!)!!.toKotlinInstant()

        result.toLocalDateTime(TimeZone.UTC)
    }
} catch (_: Exception) {
    null
}

@ExperimentalTime
private fun LocalDate.toInstant(): Instant = Instant.fromEpochSeconds(this.toEpochDays() * SECONDS_PER_DAY)
