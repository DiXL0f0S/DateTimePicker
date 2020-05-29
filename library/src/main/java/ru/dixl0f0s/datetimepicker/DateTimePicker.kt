package ru.dixl0f0s.datetimepicker

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import kotlin.math.abs
import kotlin.math.ceil

class DateTimePicker @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
    defStyleRes: Int = 0
) : LinearLayout(context, attrs, defStyle, defStyleRes) {

    private val adapterDays = Adapter(mutableListOf(), object : ItemClickListener {
        override fun onItemClick(position: Int) {
            onMinuteClick(position)
        }
    })
    private val rvDays = RecyclerView(context).apply {
        layoutManager = CenterLayoutManager(context)
        overScrollMode = View.OVER_SCROLL_NEVER
        adapter = adapterDays
    }

    private val adapterHours = Adapter(0, 23, object : ItemClickListener {
        override fun onItemClick(position: Int) {
            onMinuteClick(position)
        }
    })
    private val rvHours = RecyclerView(context).apply {
        layoutManager = CenterLayoutManager(context)
        overScrollMode = View.OVER_SCROLL_NEVER
        adapter = adapterHours
    }

    private val adapterMinutes = Adapter(0, 59, 5, object : ItemClickListener {
        override fun onItemClick(position: Int) {
            onMinuteClick(position)
        }
    })
    private val rvMinutes = RecyclerView(context).apply {
        layoutManager = CenterLayoutManager(context)
        overScrollMode = View.OVER_SCROLL_NEVER
        adapter = adapterMinutes
    }

    private val tvColon = TextView(context).apply {
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
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

    var maxTime: LocalTime = LocalTime.of(20, 0)
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
            if (value != field) {
                field = value
                listener?.onDateTimeSelected(value)
            }
        }

    @ColorInt
    var color: Int? = null
        set(value) {
            if (value == null) {
                invalidate()
            } else {
                field = value
                //setNumberPickerTextColor(numberPickerDay, value)
                //setDividerColor(numberPickerDay, value)
                //setNumberPickerTextColor(numberPickerHour, value)
                //setDividerColor(numberPickerHour, value)
                //setNumberPickerTextColor(numberPickerMinute, value)
                //setDividerColor(numberPickerMinute, value)
            }
        }

    var showTodayText: Boolean = true

    var listener: DateTimeSelectedListener? = null

    private var isTodayAvailable: Boolean = true

    init {
        orientation = HORIZONTAL
        gravity = Gravity.CENTER
        weightSum = 3f

        val snapHelperDays = object : LinearSnapHelper() {
            override fun findSnapView(layoutManager: RecyclerView.LayoutManager?): View? {
                val view = super.findSnapView(layoutManager)
                if (view != null) {
                    val str = view.findViewById<TextView>(R.id.tvItem).text.toString()
                    var pos = adapterDays.getItemPosition(str) + 1
                    if (!isTodayAvailable)
                        pos -= 1
                    selectedDate = selectedDate.withDayOfYear(startDateTime.plusDays(pos.toLong()).dayOfYear)
                }
                return view
            }
        }
        snapHelperDays.attachToRecyclerView(rvDays)
        addView(rvDays)
        val rvDaysLayoutParams = rvDays.layoutParams as LayoutParams
        rvDaysLayoutParams.setMargins(10, 0, 30, 0)
        rvDaysLayoutParams.weight = 1f
        rvDaysLayoutParams.height = 400
        rvDays.layoutParams = rvDaysLayoutParams

        val snapHelperHours = object : LinearSnapHelper() {
            override fun findSnapView(layoutManager: RecyclerView.LayoutManager?): View? {
                val view = super.findSnapView(layoutManager)
                if (view != null) {
                    val str = view.findViewById<TextView>(R.id.tvItem).text.toString()
                    selectedDate = selectedDate.withHour(str.toInt())
                }
                return view
            }
        }
        snapHelperHours.attachToRecyclerView(rvHours)
        addView(rvHours)
        val rvHoursLayoutParams = rvHours.layoutParams as LayoutParams
        rvHoursLayoutParams.setMargins(10, 0, 10, 0)
        rvHoursLayoutParams.weight = 1f
        rvHoursLayoutParams.height = 400
        rvHours.layoutParams = rvHoursLayoutParams

        addView(tvColon)
        val colonLayoutParams = tvColon.layoutParams as LayoutParams
        colonLayoutParams.setMargins(10, 0, 10, 0)
        tvColon.layoutParams = colonLayoutParams

        val snapHelperMinutes = object : LinearSnapHelper() {
            override fun findSnapView(layoutManager: RecyclerView.LayoutManager?): View? {
                val view = super.findSnapView(layoutManager)
                if (view != null) {
                    val str = view.findViewById<TextView>(R.id.tvItem).text.toString()
                    selectedDate = selectedDate.withMinute(str.toInt())
                }
                return view
            }
        }
        snapHelperMinutes.attachToRecyclerView(rvMinutes)
        addView(rvMinutes)
        val rvMinutesLayoutParams = rvMinutes.layoutParams as LayoutParams
        rvMinutesLayoutParams.setMargins(10, 0, 10, 0)
        rvMinutesLayoutParams.weight = 1f
        rvMinutesLayoutParams.height = 400
        rvMinutes.layoutParams = rvMinutesLayoutParams

        // Snap to start position
        rvDays.post {
            rvDays.scrollBy(0, 1)
        }
        rvHours.post {
            rvHours.scrollBy(0, 1)
        }
        rvMinutes.post {
            rvMinutes.scrollBy(0, 1)
        }
        rvDays.layoutManager!!.smoothScrollToPosition(rvDays, null, 0)
        rvHours.layoutManager!!.smoothScrollToPosition(rvHours, null, 0)
        rvMinutes.layoutManager!!.smoothScrollToPosition(rvMinutes, null, 0)
        setDateTime(startDateTime)
    }

    private fun updateValues() {
        setDayPicker()
        setHourPicker()
        setMinutePicker()
    }

    private fun setDayPicker() {
        adapterDays.updateData(getDaysList())
    }

    private fun setHourPicker() {
/*        val hours: MutableList<LocalTime> = mutableListOf()
        val hoursStrings: MutableList<String> = mutableListOf()

        val min: LocalTime
        //if (isFirstDaySelected()) {
        //     min = getMaxTime(minTime.withMinute(0), startDateTime.toLocalTime().withMinute(0))
        // } else {
        min = minTime
        // }
        val max = maxTime
        for (i in 0..ChronoUnit.HOURS.between(min, max)) {
            val time = min.plusHours(i)
            if (i == 0L) {
                if (startDateTime.minute + stepMinutes < 60) {
                    hours.add(time)
                    val str =
                        (if (time.hour.toString().length == 1) "0" else "") + time.hour.toString()
                    hoursStrings.add(str)
                }
            } else {
                hours.add(time)
                val str = (if (time.hour.toString().length == 1) "0" else "") + time.hour.toString()
                hoursStrings.add(str)
            }
        }
        numberPickerHour.valuesList = hoursStrings
        selectedDate = selectedDate.withHour(getSelectedHour())*/
    }

    private fun setMinutePicker() {
/*        val minutes: MutableList<LocalTime> = mutableListOf()
        val minutesStrings: MutableList<String> = mutableListOf()

        val min: LocalTime
        //if (isFirstDaySelected() && isFirstHourSelected()) {
        //    min = getMaxTime(minTime.withHour(getSelectedHour()), startDateTime.toLocalTime())
        // } else {
        min = minTime.withHour(getSelectedHour())
        //}

        val max: LocalTime
        if (isLastHourSelected()) {
            max = maxTime.withHour(getSelectedHour())
        } else {
            max = maxTime.withHour(getSelectedHour()).withMinute(59)
        }
        for (i in 0..ChronoUnit.MINUTES.between(min, max)) {
            val time = min.plusMinutes(i)
            if (time.minute % stepMinutes == 0) {
                minutes.add(time)
                val str =
                    (if (time.minute.toString().length == 1) "0" else "") + time.minute.toString()
                minutesStrings.add(str)
            }
        }*/
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

    /*fun getSelectedHour(): Int = numberPickerHour.displayedValues[numberPickerHour.value].toInt()*/

/*    fun getSelectedMinute(): Int =
        numberPickerMinute.displayedValues[numberPickerMinute.value].toInt()*/

    /*fun isFirstDaySelected(): Boolean = numberPickerDay.value == 0*/

    /*fun isLastDaySelected(): Boolean =
        numberPickerDay.value == (numberPickerDay.displayedValues.size - 1)*/

    /*fun isFirstHourSelected(): Boolean = numberPickerHour.value == 0*/

/*    fun isLastHourSelected(): Boolean =
        numberPickerHour.value == (numberPickerHour.displayedValues.size - 1)*/

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

    private fun LocalTime.isInRange(startTime: LocalTime, endTime: LocalTime): Boolean =
        this.isAfter(startTime) && this.isBefore(endTime)

    private fun LocalDateTime.isInRange(
        startDateTime: LocalDateTime,
        endDateTime: LocalDateTime
    ): Boolean =
        this.isAfter(startDateTime) && this.isBefore(endDateTime)

    private fun LocalDate.isToday() = startDateTime.toLocalDate().isEqual(this)

    private fun LocalDateTime.isToday() = startDateTime.toLocalDate().isEqual(this.toLocalDate())

    private fun LocalTime.isCurrentHour() = this.hour == startDateTime.hour

    private fun LocalDateTime.isCurrentHour() = this.hour == startDateTime.hour

    private fun onMinuteClick(pos: Int) {
        rvMinutes.layoutManager!!.smoothScrollToPosition(rvMinutes, null, pos)
    }

    // TODO: Works with start date only
    private fun setDateTime(dateTime: LocalDateTime) {
        if (dateTime.toLocalTime().isInRange(minTime, maxTime)) {
            isTodayAvailable = true
            adapterDays.updateData(getDaysList())
            (rvHours.layoutManager as CenterLayoutManager)
                .smoothScrollToPosition(
                    rvHours,
                    null,
                    adapterHours.getItemPosition(dateTime.hour.toString())
                )
            (rvMinutes.layoutManager as CenterLayoutManager)
                .smoothScrollToPosition(
                    rvMinutes,
                    null,
                    adapterMinutes.getItemPosition(roundToStep(dateTime.minute).toString())
                )
        } else {
            isTodayAvailable = false
            adapterDays.updateData(getDaysList())
            (rvHours.layoutManager as CenterLayoutManager)
                .smoothScrollToPosition(
                    rvHours,
                    null,
                    adapterHours.getItemPosition(minTime.hour.toString())
                )
            (rvMinutes.layoutManager as CenterLayoutManager)
                .smoothScrollToPosition(
                    rvMinutes,
                    null,
                    adapterMinutes.getItemPosition(roundToStep(minTime.minute).toString())
                )
        }
    }

    private fun getDaysList(): MutableList<String> {
        val daysStrings: MutableList<String> = mutableListOf()
        val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd MMMM")
        for (i in 0..ChronoUnit.DAYS.between(minDate, maxDate)) {
            val date = minDate.plusDays(i)
            if (i == 0L && isTodayAvailable) {
                if (showTodayText)
                    daysStrings.add(context.getString(R.string.today))
                else
                    daysStrings.add(date.format(formatter))
            } else if (i != 0L) {
                daysStrings.add(date.format(formatter))
            }
        }
        return daysStrings
    }

    private fun roundToStep(number: Int): Int {
        return (stepMinutes * (ceil(abs(number / stepMinutes.toFloat())))).toInt()
    }
}