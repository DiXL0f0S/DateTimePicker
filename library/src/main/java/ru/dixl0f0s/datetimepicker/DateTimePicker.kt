package ru.dixl0f0s.datetimepicker

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
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
) : FrameLayout(context, attrs, defStyle, defStyleRes) {

    private val adapterDays = Adapter(mutableListOf(), object : ItemClickListener {
        override fun onItemClick(position: Int) {
            onMinuteClick(position)
        }
    })
    private val rvDays: RecyclerView

    private val adapterHours = Adapter(0, 23, object : ItemClickListener {
        override fun onItemClick(position: Int) {
            onMinuteClick(position)
        }
    })
    private val rvHours: RecyclerView

    private val adapterMinutes = Adapter(0, 59, 5, object : ItemClickListener {
        override fun onItemClick(position: Int) {
            onMinuteClick(position)
        }
    })
    private val rvMinutes: RecyclerView

    private val tvColon: TextView

    private val viewFadeTop: View

    private val viewFadeBottom: View

    private var _startDateTime: LocalDateTime? = null
    var startDateTime: LocalDateTime
        get() {
            return if (_startDateTime == null) LocalDateTime.now().withSecond(0).withNano(0) else _startDateTime!!
        }
        set(value) {
            _startDateTime = value
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
            if (value != field) {
                if (value.toLocalTime().isBefore(minTime)) {
                    adjustPickers(minTime)
                    field = field.withHour(minTime.hour).withMinute(minTime.minute)
                } else if (value.toLocalTime().isAfter(maxTime)) {
                    adjustPickers(maxTime)
                    field = field.withHour(maxTime.hour).withMinute(maxTime.minute)
                } else {
                    field = value
                }
                listener?.onDateTimeSelected(field)
            }
        }

    @ColorInt
    var color: Int = Color.BLACK
        set(value) {
            field = value
            adapterDays.textColor = field
            adapterHours.textColor = field
            adapterMinutes.textColor = field
        }

    @ColorInt
    var backgroundColor: Int? = null
        set(value) {
            if (value == null) {
                viewFadeTop.setBackgroundColor(Color.TRANSPARENT)
                viewFadeBottom.setBackgroundColor(Color.TRANSPARENT)
            } else {
                field = value
                viewFadeTop.background = GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, intArrayOf(
                    field!!, Color.TRANSPARENT))
                viewFadeBottom.background = GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, intArrayOf(
                    Color.TRANSPARENT, field!!))
            }
        }

    var showTodayText: Boolean = true

    var listener: DateTimeSelectedListener? = null

    private var isTodayAvailable: Boolean = true

    init {
        val view = LayoutInflater.from(context).inflate(R.layout.widget_picker, this, true)
        rvDays = view.findViewById(R.id.rvDays)
        rvHours = view.findViewById(R.id.rvHours)
        rvMinutes = view.findViewById(R.id.rvMinutes)
        tvColon = view.findViewById(R.id.tvColon)
        viewFadeTop = view.findViewById(R.id.viewFadeTop)
        viewFadeBottom = view.findViewById(R.id.viewFadeBottom)

        val snapHelperDays = object : LinearSnapHelper() {
            override fun findSnapView(layoutManager: RecyclerView.LayoutManager?): View? {
                val snapView = super.findSnapView(layoutManager)
                if (snapView != null) {
                    val str = snapView.findViewById<TextView>(R.id.tvItem).text.toString()
                    var pos = adapterDays.getItemPosition(str) - 1
                    if (!isTodayAvailable)
                        pos += 1
                    selectedDate = selectedDate.withDayOfYear(startDateTime.plusDays(pos.toLong()).dayOfYear)
                }
                return snapView
            }
        }
        snapHelperDays.attachToRecyclerView(rvDays)
        rvDays.layoutManager = CenterLayoutManager(context)
        rvDays.adapter = adapterDays

        val snapHelperHours = object : LinearSnapHelper() {
            override fun findSnapView(layoutManager: RecyclerView.LayoutManager?): View? {
                val snapView = super.findSnapView(layoutManager)
                if (snapView != null) {
                    val str = snapView.findViewById<TextView>(R.id.tvItem).text.toString()
                    selectedDate = selectedDate.withHour(str.toInt())
                }
                return snapView
            }
        }
        snapHelperHours.attachToRecyclerView(rvHours)
        rvHours.layoutManager = CenterLayoutManager(context)
        rvHours.adapter = adapterHours

        val snapHelperMinutes = object : LinearSnapHelper() {
            override fun findSnapView(layoutManager: RecyclerView.LayoutManager?): View? {
                val snapView = super.findSnapView(layoutManager)
                if (snapView != null) {
                    val str = snapView.findViewById<TextView>(R.id.tvItem).text.toString()
                    selectedDate = selectedDate.withMinute(str.toInt())
                }
                return snapView
            }
        }
        snapHelperMinutes.attachToRecyclerView(rvMinutes)
        rvMinutes.layoutManager = CenterLayoutManager(context)
        rvMinutes.adapter = adapterMinutes

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
        updateValues()
    }

    private fun LocalTime.isInRange(startTime: LocalTime, endTime: LocalTime): Boolean =
        this.isAfter(startTime) && this.isBefore(endTime)

    private fun LocalDateTime.isInRange(
        startDateTime: LocalDateTime,
        endDateTime: LocalDateTime
    ): Boolean =
        this.isAfter(startDateTime) && this.isBefore(endDateTime)

    private fun onMinuteClick(pos: Int) {
        rvMinutes.layoutManager!!.smoothScrollToPosition(rvMinutes, null, pos)
    }

    private fun adjustPickers(time: LocalTime) {
        (rvHours.layoutManager as CenterLayoutManager)
            .smoothScrollToPosition(
                rvHours,
                null,
                adapterHours.getItemPosition(time.hour.toString())
            )
        (rvMinutes.layoutManager as CenterLayoutManager)
            .smoothScrollToPosition(
                rvMinutes,
                null,
                adapterMinutes.getItemPosition(roundToStep(time.minute).toString())
            )
    }

    private fun updateValues() {
        val dateTime = startDateTime
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
        for (i in 0..ChronoUnit.DAYS.between(startDateTime, startDateTime.plusDays(6))) {
            val date = startDateTime.plusDays(i)
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