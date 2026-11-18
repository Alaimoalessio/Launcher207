package it.peugeot.launcher

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.View
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.cos
import kotlin.math.sin

class ClockView(context: Context, attrs: AttributeSet?) : View(context, attrs) {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.ITALIAN)
    private val dateFormat = SimpleDateFormat("EEEE d MMMM", Locale.ITALIAN)
    private val calendar = Calendar.getInstance()
    private var now = Date()
    private var lastDescription = ""

    fun refresh() {
        val zone = TimeZone.getDefault()
        timeFormat.timeZone = zone
        dateFormat.timeZone = zone
        calendar.timeZone = zone
        now = Date()
        val description = timeFormat.format(now) + ", " + dateFormat.format(now)
        if (description != lastDescription) {
            contentDescription = description
            lastDescription = description
            invalidate()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val density = resources.displayMetrics.density
        val cx = width / 2f
        val cy = height / 2f
        val radius = 149 * density
        paint.color = Color.rgb(97, 89, 74)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = density
        canvas.drawCircle(cx, cy, radius, paint)
        calendar.time = now
        val minute = calendar.get(Calendar.MINUTE)
        for (tick in 0 until 60) {
            val angle = Math.toRadians(tick * 6.0 - 90)
            val active = tick == minute
            val outer = radius - 10 * density
            val inner = outer - (if (active) 14 else if (tick % 5 == 0) 8 else 4) * density
            paint.color = if (active) Color.rgb(240, 199, 126) else Color.rgb(116, 105, 88)
            paint.strokeWidth = (if (active) 3 else 1) * density
            canvas.drawLine(cx + cos(angle).toFloat() * inner, cy + sin(angle).toFloat() * inner,
                cx + cos(angle).toFloat() * outer, cy + sin(angle).toFloat() * outer, paint)
        }
        paint.style = Paint.Style.FILL
        paint.textAlign = Paint.Align.CENTER
        paint.typeface = Typeface.create("sans-serif", Typeface.NORMAL)
        paint.color = Color.rgb(244, 239, 228)
        paint.textSize = 76 * density
        canvas.drawText(timeFormat.format(now), cx, cy + 5 * density, paint)
        paint.color = Color.rgb(189, 182, 168)
        paint.textSize = 17 * density
        canvas.drawText(dateFormat.format(now).replaceFirstChar { it.titlecase(Locale.ITALIAN) },
            cx, cy + 43 * density, paint)
    }
}
