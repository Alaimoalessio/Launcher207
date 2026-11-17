package it.peugeot.launcher

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup

/** Mantiene il rapporto 1024:600 su radio e tablet, con bande dove necessario. */
class FitLayout(context: Context, attrs: AttributeSet?) : ViewGroup(context, attrs) {
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val density = resources.displayMetrics.density
        val childWidth = (1024 * density).toInt()
        val childHeight = (600 * density).toInt()
        getChildAt(0)?.measure(
            MeasureSpec.makeMeasureSpec(childWidth, MeasureSpec.EXACTLY),
            MeasureSpec.makeMeasureSpec(childHeight, MeasureSpec.EXACTLY)
        )
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.getSize(heightMeasureSpec))
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        val child = getChildAt(0) ?: return
        val scale = minOf(width.toFloat() / child.measuredWidth, height.toFloat() / child.measuredHeight)
        child.layout(0, 0, child.measuredWidth, child.measuredHeight)
        child.pivotX = 0f
        child.pivotY = 0f
        child.scaleX = scale
        child.scaleY = scale
        child.translationX = (width - child.measuredWidth * scale) / 2
        child.translationY = (height - child.measuredHeight * scale) / 2
    }
}
