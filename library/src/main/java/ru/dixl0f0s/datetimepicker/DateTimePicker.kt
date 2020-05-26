package ru.dixl0f0s.datetimepicker

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.NumberPicker
import android.widget.TextView
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit


class DateTimePicker @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
    defStyleRes: Int = 0
) : LinearLayout(context, attrs, defStyle, defStyleRes) {
    private val numberPickerDay = NumberPicker(context)
    private val numberPickerHour = NumberPicker(context)
    private val numberPickerMinute = NumberPicker(context)
    private val tvColon = TextView(context).apply {
        text = ":"
        gravity = Gravity.CENTER
    }

    var minDate: LocalDateTime = LocalDateTime.now()
        set(value) {
            field = value
            updateValues()
        }

    var maxDate: LocalDateTime = LocalDateTime.now()
        set(value) {
            field = value
            updateValues()
        }

    init {
        orientation = HORIZONTAL
        gravity = Gravity.CENTER

        addView(numberPickerDay)
        val dayLayoutParams = numberPickerDay.layoutParams as MarginLayoutParams
        dayLayoutParams.setMargins(10, 0, 30, 0)
        numberPickerDay.layoutParams = dayLayoutParams

        addView(numberPickerHour)
        val hourLayoutParams = numberPickerHour.layoutParams as MarginLayoutParams
        hourLayoutParams.setMargins(10, 0, 10, 0)
        numberPickerHour.layoutParams = hourLayoutParams

        addView(tvColon)
        val colonLayoutParams = tvColon.layoutParams as MarginLayoutParams
        colonLayoutParams.setMargins(10, 0, 10, 0)
        tvColon.layoutParams = colonLayoutParams

        addView(numberPickerMinute)
        val minuteLayoutParams = numberPickerMinute.layoutParams as MarginLayoutParams
        minuteLayoutParams.setMargins(10, 0, 10, 0)
        numberPickerMinute.layoutParams = minuteLayoutParams
    }

    private fun updateValues() {
        for (i in 0..ChronoUnit.DAYS.between(minDate, maxDate)) {
            println(i)
        }

        val days: MutableList<LocalDateTime> = mutableListOf()
        val daysStrings: MutableList<String> = mutableListOf()
        val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd MMMM")
        LongRange(0, ChronoUnit.DAYS.between(minDate, maxDate)).forEach {
            val date = minDate.plusDays(it)
            days.add(date)
            if (date.toLocalDate().isEqual(LocalDate.now()))
                daysStrings.add(context.getString(R.string.today))
            else
                daysStrings.add(date.format(formatter))
        }
        numberPickerDay.displayedValues = daysStrings.toTypedArray()
        numberPickerDay.maxValue = daysStrings.size - 1
        numberPickerDay.wrapSelectorWheel = false
    }
}