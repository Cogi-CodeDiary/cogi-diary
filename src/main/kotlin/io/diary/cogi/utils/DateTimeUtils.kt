package io.diary.cogi.utils

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object DateTimeUtils {
    fun LocalDateTime.toIsoString(): String = this.format(DateTimeFormatter.ISO_DATE_TIME)
}