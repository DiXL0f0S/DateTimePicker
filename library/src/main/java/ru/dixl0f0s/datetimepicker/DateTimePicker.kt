package ru.dixl0f0s.datetimepicker

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.NumberPicker
import android.widget.TextView
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.chrono.ChronoLocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import kotlin.math.max


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

    private var currentMinDate: LocalDateTime = LocalDateTime.now()
        set(value) {
            field = value
            //updateValues()
        }

    private var currentMaxDate: LocalDateTime = LocalDateTime.now()
        set(value) {
            field = value
            updateValues()
        }

    var minDayTime: LocalTime = LocalTime.of(10, 10)
        set(value) {
            field = value
            updateValues()
        }

    var maxDayTime: LocalTime = LocalTime.of(23, 30)
        set(value) {
            field = value
            updateValues()
        }

    var selectedDate: LocalDateTime = LocalDateTime.now()

    init {
        orientation = HORIZONTAL
        gravity = Gravity.CENTER

        addView(numberPickerDay)
        val dayLayoutParams = numberPickerDay.layoutParams as MarginLayoutParams
        dayLayoutParams.setMargins(10, 0, 30, 0)
        numberPickerDay.layoutParams = dayLayoutParams
        numberPickerDay.setOnValueChangedListener { picker, oldVal, newVal ->
            onDayChanged(numberPickerHour.value)
        }

        addView(numberPickerHour)
        val hourLayoutParams = numberPickerHour.layoutParams as MarginLayoutParams
        hourLayoutParams.setMargins(10, 0, 10, 0)
        numberPickerHour.layoutParams = hourLayoutParams
        numberPickerHour.setOnValueChangedListener { picker, oldVal, newVal ->
            onHourChanged(getSelectedHour())
        }

        addView(tvColon)
        val colonLayoutParams = tvColon.layoutParams as MarginLayoutParams
        colonLayoutParams.setMargins(10, 0, 10, 0)
        tvColon.layoutParams = colonLayoutParams

        addView(numberPickerMinute)
        val minuteLayoutParams = numberPickerMinute.layoutParams as MarginLayoutParams
        minuteLayoutParams.setMargins(10, 0, 10, 0)
        numberPickerMinute.layoutParams = minuteLayoutParams
        numberPickerMinute.setOnValueChangedListener { picker, oldVal, newVal ->
            onMinuteChanged(getSelectedMinute())
        }
    }

    private fun updateValues() {
        setDayPicker()
        setHourPicker()
        setMinutePicker()
    }

    private fun setDayPicker() {
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
        numberPickerDay.valuesList = daysStrings
    }

    private fun setHourPicker() {
        val hours: MutableList<LocalTime> = mutableListOf()
        val hoursStrings: MutableList<String> = mutableListOf()

        val min: LocalTime
        if (isFirstDaySelected()) {
            min = getMaxTime(minDayTime, LocalTime.now())
        } else {
            min = minDayTime
        }
        val max = maxDayTime
        LongRange(0, ChronoUnit.HOURS.between(min, max)).forEach {
            val time = min.plusHours(it)
            hours.add(time)
            val str = (if (time.hour.toString().length == 1) "0" else "") + time.hour.toString()
            hoursStrings.add(str)
        }
        numberPickerHour.valuesList = hoursStrings
    }

    private fun setMinutePicker() {
        val minutes: MutableList<LocalTime> = mutableListOf()
        val minutesStrings: MutableList<String> = mutableListOf()

        val min: LocalTime
        if (isFirstDaySelected() && isFirstHourSelected()) {
            min = getMaxTime(minDayTime.withHour(getSelectedHour()), LocalTime.now())
        } else {
            min = minDayTime.withHour(getSelectedHour())
        }

        val max: LocalTime
        if (isLastHourSelected()) {
            max = maxDayTime.withHour(getSelectedHour())
        } else {
            max = maxDayTime.withHour(getSelectedHour()).withMinute(59)
        }
        LongRange(0, ChronoUnit.MINUTES.between(min, max)).forEach {
            val time = min.plusMinutes(it)
            minutes.add(time)
            val str = (if (time.minute.toString().length == 1) "0" else "") + time.minute.toString()
            minutesStrings.add(str)
        }
        numberPickerMinute.valuesList = minutesStrings
    }

    private fun onDayChanged(index: Int) {
        selectedDate = selectedDate.withDayOfYear(LocalDate.now().plusDays(index.toLong()).dayOfYear)
        setHourPicker()
    }

    private fun onHourChanged(hour: Int) {
        selectedDate = selectedDate.withHour(hour)
        val min = if (selectedDate.isToday() && selectedDate.isCurrentHour())
            LocalTime.of(hour, LocalTime.now().minute) else
            LocalTime.of(hour, minDayTime.minute)
        val max = LocalTime.of(hour, 59)
        setMinutePicker()
    }

    private fun onMinuteChanged(minute: Int) {
        selectedDate = selectedDate.withMinute(minute)
    }

    fun getSelectedHour(): Int = numberPickerHour.displayedValues[numberPickerHour.value].toInt()

    fun getSelectedMinute(): Int = numberPickerMinute.displayedValues[numberPickerMinute.value].toInt()

    fun isFirstDaySelected(): Boolean = numberPickerDay.value == 0

    fun isLastDaySelected(): Boolean = numberPickerDay.value == (numberPickerDay.displayedValues.size - 1)

    fun isFirstHourSelected(): Boolean = numberPickerHour.value == 0

    fun isLastHourSelected(): Boolean = numberPickerHour.value == (numberPickerHour.displayedValues.size - 1)

    fun isFirstMinuteSelected(): Boolean = numberPickerMinute.value == 0

    fun isLastMinuteSelected(): Boolean = numberPickerMinute.value == (numberPickerMinute.displayedValues.size - 1)

    private fun getMaxTime(localTimeA: LocalTime, localTimeB: LocalTime): LocalTime {
        if (localTimeA.isAfter(localTimeB))
            return localTimeA
        return localTimeB
    }

    private fun getMaxDate(localDateA: LocalDate, localDateB: LocalDate): LocalDate {
        if (localDateA.isAfter(localDateB))
            return localDateA
        return localDateB
    }

    private fun getMaxDateTime(localDateTimeA: LocalDateTime, localDateTimeB: LocalDateTime): LocalDateTime {
        if (localDateTimeA.isAfter(localDateTimeB))
            return localDateTimeA
        return localDateTimeB
    }

    private var NumberPicker.valuesList: Collection<String>
        get() = displayedValues.toList()
        set(value) {
            displayedValues = null
            minValue = 0
            maxValue = value.size - 1
            wrapSelectorWheel = false
            displayedValues = value.toTypedArray()
        }

    private fun LocalDate.isToday() = LocalDate.now().isEqual(this)

    private fun LocalDateTime.isToday() = LocalDate.now().isEqual(this.toLocalDate())

    private fun LocalTime.isCurrentHour() = this.hour == LocalTime.now().hour

    private fun LocalDateTime.isCurrentHour() = this.hour == LocalTime.now().hour
}