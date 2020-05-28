package ru.dixl0f0s.datetimepicker

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DateTimePickerTest {
    lateinit var picker: DateTimePicker

    @Before
    fun setup() {
        picker = DateTimePicker(InstrumentationRegistry.getInstrumentation().targetContext)
    }

    @Test
    fun test() {
        //for (i in 0..100) {
        //    picker.selectNextMinute()
        //}
    }
}