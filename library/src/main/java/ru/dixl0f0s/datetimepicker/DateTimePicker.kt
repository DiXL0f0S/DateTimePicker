package ru.dixl0f0s.datetimepicker

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.graphics.Paint
import android.graphics.drawable.ColorDrawable
import android.util.AttributeSet
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.NumberPicker
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.lang.reflect.Field
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
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
    private val rvMinutes = RecyclerView(context).apply {
        layoutManager = CenterLayoutManager(context)
        overScrollMode = View.OVER_SCROLL_NEVER
        adapter = Adapter(listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20"), object : ItemClickListener {
            override fun onItemClick(position: Int) {
                onMinuteClick(position)
            }
        })
    }
    private val tvColon = TextView(context).apply {
        text = ":"
        gravity = Gravity.CENTER
    }

    private var _startDateTime: LocalDateTime? = null
    var startDateTime: LocalDateTime
        get() {
            return if (_startDateTime == null) LocalDateTime.now() else _startDateTime!!
        }
        set(value) {
            _startDateTime = value
            updateValues()
        }

    var minDate: LocalDateTime = startDateTime
        set(value) {
            field = value
            updateValues()
        }

    var maxDate: LocalDateTime = startDateTime
        set(value) {
            field = value
            updateValues()
        }

    var minTime: LocalTime = LocalTime.of(0, 0)
        set(value) {
            field = value
            updateValues()
        }

    var maxTime: LocalTime = LocalTime.of(23, 59)
        set(value) {
            field = value
            updateValues()
        }

    var stepMinutes: Int = 5
        set(value) {
            field = value
            updateValues()
        }

    var selectedDate: LocalDateTime = startDateTime
        set(value) {
            field = value
            listener?.onDateTimeSelected(value)
        }

    @ColorInt
    var color: Int? = null
        set(value) {
            if (value == null) {
                invalidate()
            } else {
                field = value
                setNumberPickerTextColor(numberPickerDay, value)
                setDividerColor(numberPickerDay, value)
                setNumberPickerTextColor(numberPickerHour, value)
                setDividerColor(numberPickerHour, value)
                //setNumberPickerTextColor(numberPickerMinute, value)
                //setDividerColor(numberPickerMinute, value)
            }
        }

    var showTodayText: Boolean = true

    var listener: DateTimeSelectedListener? = null

    init {
        orientation = HORIZONTAL
        gravity = Gravity.CENTER
        weightSum = 3f

        addView(numberPickerDay)
        val dayLayoutParams = numberPickerDay.layoutParams as LayoutParams
        dayLayoutParams.setMargins(10, 0, 30, 0)
        dayLayoutParams.weight = 1f
        numberPickerDay.layoutParams = dayLayoutParams
        numberPickerDay.setOnValueChangedListener { picker, oldVal, newVal ->
            onDayChanged(newVal)
        }

        addView(numberPickerHour)
        val hourLayoutParams = numberPickerHour.layoutParams as LayoutParams
        hourLayoutParams.setMargins(10, 0, 10, 0)
        hourLayoutParams.weight = 1f
        numberPickerHour.layoutParams = hourLayoutParams
        numberPickerHour.setOnValueChangedListener { picker, oldVal, newVal ->
            onHourChanged(getSelectedHour())
        }

        addView(tvColon)
        val colonLayoutParams = tvColon.layoutParams as LayoutParams
        colonLayoutParams.setMargins(10, 0, 10, 0)
        tvColon.layoutParams = colonLayoutParams

/*        addView(numberPickerMinute)
        val minuteLayoutParams = numberPickerMinute.layoutParams as LayoutParams
        minuteLayoutParams.setMargins(10, 0, 10, 0)
        minuteLayoutParams.weight = 1f
        numberPickerMinute.layoutParams = minuteLayoutParams
        numberPickerMinute.setOnValueChangedListener { picker, oldVal, newVal ->
            onMinuteChanged(getSelectedMinute())
        }*/

        addView(rvMinutes)
        val rvMinutesLayoutParams = numberPickerHour.layoutParams as LayoutParams
        rvMinutesLayoutParams.setMargins(10, 0, 10, 0)
        rvMinutesLayoutParams.weight = 1f
        rvMinutes.layoutParams = rvMinutesLayoutParams
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
            if (showTodayText && date.toLocalDate().isEqual(LocalDate.now()))
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
            min = getMaxTime(minTime.withMinute(0), startDateTime.toLocalTime().withMinute(0))
        } else {
            min = minTime
        }
        val max = maxTime
        LongRange(0, ChronoUnit.HOURS.between(min, max)).forEach {
            val time = min.plusHours(it)
            hours.add(time)
            val str = (if (time.hour.toString().length == 1) "0" else "") + time.hour.toString()
            hoursStrings.add(str)
        }
        numberPickerHour.valuesList = hoursStrings
        selectedDate = selectedDate.withHour(getSelectedHour())
    }

    private fun setMinutePicker() {
        val minutes: MutableList<LocalTime> = mutableListOf()
        val minutesStrings: MutableList<String> = mutableListOf()

        val min: LocalTime
        if (isFirstDaySelected() && isFirstHourSelected()) {
            min = getMaxTime(minTime.withHour(getSelectedHour()), startDateTime.toLocalTime())
        } else {
            min = minTime.withHour(getSelectedHour())
        }

        val max: LocalTime
        if (isLastHourSelected()) {
            max = maxTime.withHour(getSelectedHour())
        } else {
            max = maxTime.withHour(getSelectedHour()).withMinute(59)
        }
        LongRange(0, ChronoUnit.MINUTES.between(min, max)).forEach {
            val time = min.plusMinutes(it)
            if (time.minute % stepMinutes == 0) {
                minutes.add(time)
                val str =
                    (if (time.minute.toString().length == 1) "0" else "") + time.minute.toString()
                minutesStrings.add(str)
            }
        }
        //numberPickerMinute.valuesList = minutesStrings
        //selectedDate = selectedDate.withMinute(getSelectedMinute())
    }

    private fun onDayChanged(index: Int) {
        selectedDate =
            selectedDate.withDayOfYear(
                startDateTime.toLocalDate().plusDays(index.toLong()).dayOfYear
            )
        setHourPicker()
        setMinutePicker()
    }

    private fun onHourChanged(hour: Int) {
        selectedDate = selectedDate.withHour(hour)
        setMinutePicker()
    }

    private fun onMinuteChanged(minute: Int) {
        selectedDate = selectedDate.withMinute(minute)
    }

    fun getSelectedHour(): Int = numberPickerHour.displayedValues[numberPickerHour.value].toInt()

/*    fun getSelectedMinute(): Int =
        numberPickerMinute.displayedValues[numberPickerMinute.value].toInt()*/

    fun isFirstDaySelected(): Boolean = numberPickerDay.value == 0

    fun isLastDaySelected(): Boolean =
        numberPickerDay.value == (numberPickerDay.displayedValues.size - 1)

    fun isFirstHourSelected(): Boolean = numberPickerHour.value == 0

    fun isLastHourSelected(): Boolean =
        numberPickerHour.value == (numberPickerHour.displayedValues.size - 1)

/*    fun isFirstMinuteSelected(): Boolean = numberPickerMinute.value == 0*/

/*    fun isLastMinuteSelected(): Boolean =
        numberPickerMinute.value == (numberPickerMinute.displayedValues.size - 1)*/

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

    private fun getMaxDateTime(
        localDateTimeA: LocalDateTime,
        localDateTimeB: LocalDateTime
    ): LocalDateTime {
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

    private fun LocalDate.isToday() = startDateTime.toLocalDate().isEqual(this)

    private fun LocalDateTime.isToday() = startDateTime.toLocalDate().isEqual(this.toLocalDate())

    private fun LocalTime.isCurrentHour() = this.hour == startDateTime.hour

    private fun LocalDateTime.isCurrentHour() = this.hour == startDateTime.hour

    private fun setNumberPickerTextColor(numberPicker: NumberPicker, color: Int) {
        try {
            val selectorWheelPaintField: Field = numberPicker.javaClass
                .getDeclaredField("mSelectorWheelPaint")
            selectorWheelPaintField.setAccessible(true)
            (selectorWheelPaintField.get(numberPicker) as Paint).setColor(color)
        } catch (e: NoSuchFieldException) {
            Log.w("NumberPickerTextColor", e)
        } catch (e: IllegalAccessException) {
            Log.w("NumberPickerTextColor", e)
        } catch (e: IllegalArgumentException) {
            Log.w("NumberPickerTextColor", e)
        }
        val count = numberPicker.childCount
        for (i in 0 until count) {
            val child: View = numberPicker.getChildAt(i)
            if (child is EditText)
                child.setTextColor(color)
        }
        numberPicker.invalidate()
    }

    private fun setDividerColor(picker: NumberPicker, color: Int) {
        val pickerFields =
            NumberPicker::class.java.declaredFields
        for (pf in pickerFields) {
            if (pf.name == "mSelectionDivider") {
                pf.isAccessible = true
                try {
                    val colorDrawable = ColorDrawable(color)
                    pf[picker] = colorDrawable
                } catch (e: java.lang.IllegalArgumentException) {
                    e.printStackTrace()
                } catch (e: Resources.NotFoundException) {
                    e.printStackTrace()
                } catch (e: IllegalAccessException) {
                    e.printStackTrace()
                }
                break
            }
        }
    }

    private fun onMinuteClick(pos: Int) {
        rvMinutes.layoutManager!!.smoothScrollToPosition(rvMinutes, null, pos)
    }

    @SuppressLint("DiscouragedPrivateApi")
    private fun changeValueByOne(higherPicker: NumberPicker, increment: Boolean) {
        val method: Method
        try {
            method = higherPicker.javaClass.getDeclaredMethod(
                "changeValueByOne",
                Boolean::class.javaPrimitiveType
            )
            method.setAccessible(true)
            method.invoke(higherPicker, increment)
        } catch (e: NoSuchMethodException) {
            e.printStackTrace()
        } catch (e: java.lang.IllegalArgumentException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        } catch (e: InvocationTargetException) {
            e.printStackTrace()
        }
    }
}