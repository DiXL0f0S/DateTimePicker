package ru.dixl0f0s.datetimepicker.sample

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*
import ru.dixl0f0s.datetimepicker.R
import java.time.LocalDateTime

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        dateTimePicker.minDate = LocalDateTime.now()
        dateTimePicker.maxDate = LocalDateTime.now().plusDays(7)
    }
}