package ru.dixl0f0s.datetimepicker.sample

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*
import ru.dixl0f0s.datetimepicker.DateTimeSelectedListener
import ru.dixl0f0s.datetimepicker.R
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

class MainActivity : AppCompatActivity(), DateTimeSelectedListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        dateTimePicker.minDate = LocalDateTime.now()
        dateTimePicker.maxDate = LocalDateTime.now().plusDays(7)
        dateTimePicker.listener = this
    }

    override fun onDateTimeSelected(dateTime: LocalDateTime) {
        tvSelectedDate.text =
            dateTime.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))
    }
}