package ru.dixl0f0s.datetimepicker

import java.time.LocalDateTime

interface DateTimeSelectedListener {
    fun onDateTimeSelected(dateTime: LocalDateTime)
}