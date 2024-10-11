package dev.itsvic.DaysCountdown

import java.time.LocalDate
import java.time.Duration
import java.time.Instant
import java.time.ZoneId

fun daysUntilNow(date: Long): Long {
    val firstTimestamp = LocalDate.now()
    val secondTimestamp = Instant.ofEpochMilli(date).atZone(ZoneId.systemDefault()).toLocalDate()
    return Duration.between(firstTimestamp.atStartOfDay(), secondTimestamp.atStartOfDay()).toDays()
}
