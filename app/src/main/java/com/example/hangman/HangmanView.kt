package com.example.hangman

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import com.google.android.material.color.MaterialColors
import kotlin.math.min

/** Рисует виселицу по этапам (0..7) на Canvas. */
class HangmanView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    var errors: Int = 0
        set(value) {
            field = value.coerceIn(0, 7)
            invalidate()
        }

    private val gallowsPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        color = MaterialColors.getColor(this@HangmanView, com.google.android.material.R.attr.colorOnSurface, Color.BLACK)
    }
    private val personPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        color = MaterialColors.getColor(this@HangmanView, com.google.android.material.R.attr.colorPrimary, Color.BLUE)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val size = min(width, height).toFloat()
        val ox = (width - size) / 2f
        val oy = (height - size) / 2f
        gallowsPaint.strokeWidth = size * 0.025f
        personPaint.strokeWidth = size * 0.025f
        if (errors >= 7) {
            personPaint.color = MaterialColors.getColor(this, com.google.android.material.R.attr.colorError, Color.RED)
        }

        fun line(x1: Float, y1: Float, x2: Float, y2: Float, p: Paint) =
            canvas.drawLine(ox + x1 * size, oy + y1 * size, ox + x2 * size, oy + y2 * size, p)

        // 1 — виселица: основание, стойка, перекладина, подпорка
        if (errors >= 1) {
            line(0.10f, 0.95f, 0.70f, 0.95f, gallowsPaint)
            line(0.25f, 0.95f, 0.25f, 0.05f, gallowsPaint)
            line(0.25f, 0.05f, 0.65f, 0.05f, gallowsPaint)
            line(0.25f, 0.20f, 0.40f, 0.05f, gallowsPaint)
        }
        // 2 — верёвка
        if (errors >= 2) line(0.65f, 0.05f, 0.65f, 0.20f, personPaint)
        // 3 — голова
        if (errors >= 3) canvas.drawCircle(ox + 0.65f * size, oy + 0.28f * size, 0.08f * size, personPaint)
        // 4 — туловище
        if (errors >= 4) line(0.65f, 0.36f, 0.65f, 0.60f, personPaint)
        // 5 — руки
        if (errors >= 5) {
            line(0.65f, 0.43f, 0.52f, 0.54f, personPaint)
            line(0.65f, 0.43f, 0.78f, 0.54f, personPaint)
        }
        // 6 — левая нога
        if (errors >= 6) line(0.65f, 0.60f, 0.53f, 0.78f, personPaint)
        // 7 — правая нога
        if (errors >= 7) line(0.65f, 0.60f, 0.77f, 0.78f, personPaint)
    }
}
