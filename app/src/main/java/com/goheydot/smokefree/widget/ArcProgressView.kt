package com.goheydot.smokefree.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.goheydot.smokefree.R

/**
 * 圆形进度条 — 完整 360° 圆环
 *
 * - 起点：0点（顶部），顺时针填充
 * - 未完成 = 浅灰粉轨道，已完成 = 粉色进度弧
 */
class ArcProgressView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    var progress: Int = 0
        set(value) {
            field = value.coerceIn(0, 10000)
            invalidate()
        }

    private val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 12f * resources.displayMetrics.density
        strokeCap = Paint.Cap.ROUND
        color = 0xFFE0D8DC.toInt()    // 浅灰粉轨道
    }

    private val progressPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 12f * resources.displayMetrics.density
        strokeCap = Paint.Cap.ROUND
        color = ContextCompat.getColor(context, R.color.pink_500)
    }

    private val oval = RectF()

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        // 正方形：高度 = 宽度
        val w = MeasureSpec.getSize(widthMeasureSpec)
        setMeasuredDimension(w, w)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        val sw = progressPaint.strokeWidth
        val pad = sw * 0.6f
        oval.set(pad, pad, w.toFloat() - pad, h.toFloat() - pad)
    }

    override fun onDraw(canvas: Canvas) {
        // 背景：完整 360° 圆环轨道，从顶部(270°)开始
        canvas.drawArc(oval, 270f, 360f, false, trackPaint)

        // 前景：从顶部(270°)顺时针填充进度
        val sweep = 360f * progress / 10000f
        if (sweep > 0.5f) {
            canvas.drawArc(oval, 270f, sweep, false, progressPaint)
        }
    }
}
