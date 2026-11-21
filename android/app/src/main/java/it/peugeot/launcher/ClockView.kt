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
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create("sans-serif", Typeface.NORMAL)
    }
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.ITALIAN)
    private val dateFormat = SimpleDateFormat("EEEE d MMMM", Locale.ITALIAN)
    private val calendar = Calendar.getInstance()
    private var timeLabel = ""
    private var dateLabel = ""
    private var minute = 0
    private val tickLines = FloatArray(60 * 4)

    init {
        refresh()
    }

    fun refresh() {
        val zone = TimeZone.getDefault()
        timeFormat.timeZone = zone
        dateFormat.timeZone = zone
        calendar.timeZone = zone
        val now = Date()
        val time = timeFormat.format(now)
        val date = dateFormat.format(now)
        if (time == timeLabel && dateLabel.equals(date, ignoreCase = true)) return
        calendar.time = now
        minute = calendar.get(Calendar.MINUTE)
        timeLabel = time
        dateLabel = date.replaceFirstChar { it.titlecase(Locale.ITALIAN) }
        contentDescription = "$time, $date"
        invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val density = resources.displayMetrics.density
        val outer = 139 * density
        for (tick in 0 until 60) {
            val inner = outer - (if (tick % 5 == 0) 8 else 4) * density
            val x = tickDirections[tick * 2]
            val y = tickDirections[tick * 2 + 1]
            val offset = tick * 4
            tickLines[offset] = w / 2f + x * inner
            tickLines[offset + 1] = h / 2f + y * inner
            tickLines[offset + 2] = w / 2f + x * outer
            tickLines[offset + 3] = h / 2f + y * outer
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
        paint.color = Color.rgb(116, 105, 88)
        val activeTick = minute * 4
        if (activeTick > 0) canvas.drawLines(tickLines, 0, activeTick, paint)
        val nextTick = activeTick + 4
        if (nextTick < tickLines.size) {
            canvas.drawLines(tickLines, nextTick, tickLines.size - nextTick, paint)
        }
        paint.color = Color.rgb(240, 199, 126)
        paint.strokeWidth = 3 * density
        canvas.drawLine(
            cx + tickDirections[minute * 2] * (125 * density),
            cy + tickDirections[minute * 2 + 1] * (125 * density),
            tickLines[activeTick + 2], tickLines[activeTick + 3], paint
        )
        paint.style = Paint.Style.FILL
        paint.color = Color.rgb(244, 239, 228)
        paint.textSize = 76 * density
        canvas.drawText(timeLabel, cx, cy + 5 * density, paint)
        paint.color = Color.rgb(189, 182, 168)
        paint.textSize = 17 * density
        canvas.drawText(dateLabel, cx, cy + 43 * density, paint)
    }

    private companion object {
        val tickDirections = FloatArray(120).apply {
            for (tick in 0 until 60) {
                val angle = Math.toRadians(tick * 6.0 - 90)
                this[tick * 2] = cos(angle).toFloat()
                this[tick * 2 + 1] = sin(angle).toFloat()
            }
        }
    }
}
