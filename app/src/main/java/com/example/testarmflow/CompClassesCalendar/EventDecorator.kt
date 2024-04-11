package com.example.testarmflow.CompClassesCalendar

import android.annotation.SuppressLint
import android.graphics.Color
import android.text.style.ForegroundColorSpan
import androidx.core.content.ContextCompat
import com.example.testarmflow.R
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade
import com.prolificinteractive.materialcalendarview.spans.DotSpan
import kotlin.coroutines.coroutineContext

class EventDecorator(private val date: CalendarDay, private val color: Any, private val color2: Int) : DayViewDecorator {

    override fun shouldDecorate(day: CalendarDay?): Boolean {
        return day == date
    }

    @SuppressLint("ResourceAsColor")
    override fun decorate(view: DayViewFacade?) {
        if (color is String) {
            view?.addSpan(ForegroundColorSpan(Color.BLACK))
        } else if (color is Int) {
            view?.addSpan(DotSpan(5F, color))
            view?.addSpan(ForegroundColorSpan(color2))
        }
    }
}