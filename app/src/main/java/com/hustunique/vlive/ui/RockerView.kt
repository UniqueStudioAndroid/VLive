package com.hustunique.vlive.ui

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import com.hustunique.vlive.R
import kotlin.math.acos
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 *    author : Yuxuan Xiao
 *    e-mail : qpalwo@qq.com
 *    date   : 2021/5/25
 */
class RockerView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    companion object {
        private const val TAG = "Rocker"
    }

    private val bgColor: Int
    private val fgColor: Int
    private val radiusDiff: Float

    init {
        context.obtainStyledAttributes(attrs, R.styleable.RockerView).run {
            bgColor = getColor(R.styleable.RockerView_bg_color, Color.RED)
            fgColor = getColor(R.styleable.RockerView_fg_color, Color.BLUE)
            radiusDiff = getDimension(R.styleable.RockerView_radius_diff, 0f)
            recycle()
        }
    }


    private val bgPaint = Paint().apply {
        isAntiAlias = true
        color = bgColor
    }

    private val fgPaint = Paint().apply {
        isAntiAlias = true
        color = fgColor
    }

    private val centerPos = PointF()
    private val curPos = PointF()
    private var curPosRadius = 0f

    // radians, progress
    var onUpdate: (Float, Float) -> Unit = { _, _ -> }

    var enable = false
        set(value) {
            visibility = if (value) {
                VISIBLE
            } else {
                GONE
            }
            field = value
        }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        centerPos.x = measuredWidth / 2f
        centerPos.y = measuredHeight / 2f
        curPosRadius = centerPos.x - radiusDiff
        curPos.set(centerPos)
    }


    override fun onDraw(canvas: Canvas?) {
        canvas?.run {
            centerPos.drawCircle(canvas, bgPaint)
            curPos.drawCircle(canvas, fgPaint, curPosRadius)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!enable) return super.onTouchEvent(event)

        when (event.action) {
            MotionEvent.ACTION_MOVE -> {
                calculateCurPos(event.x, event.y)
                invalidate()
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                curPos.set(centerPos)
                onUpdate(0f, 0f)
                invalidate()
            }
        }
        return enable
    }

    private fun calculateCurPos(x: Float, y: Float) {
        val p = PointF(x, y) - centerPos
        val radians = getRadians(p) * if (y > centerPos.y) -1 else 1
        val progress = p.distance() / radiusDiff
        if (progress < 1f) {
            curPos.set(x, y)
            onUpdate(radians, progress)
        } else {
            val offset = PointF(radiusDiff * cos(radians), radiusDiff * sin(radians))
            curPos.set(centerPos.x + offset.x, centerPos.y - offset.y)
            onUpdate(radians, 1f)
        }
    }

    private fun PointF.drawCircle(canvas: Canvas, paint: Paint, radius: Float = x.coerceAtMost(y)) {
        canvas.drawCircle(x, y, radius, paint)
    }

    private val zeroVec = PointF(1f, 0f)

    private fun getRadians(p: PointF): Float = acos(p.dot(zeroVec) / p.distance())

    private operator fun PointF.minus(p: PointF): PointF = PointF(x - p.x, y - p.y)

    private operator fun PointF.plus(p: PointF): PointF = PointF(x - p.x, y - p.y)

    private fun PointF.dot(p: PointF): Float = x * p.x + x * p.y

    private fun PointF.distance(): Float = sqrt(x * x + y * y)
}