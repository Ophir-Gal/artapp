package com.example.artapp

import android.view.View
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.view.MotionEvent

class PaintView : View {

    private val path = Path()
    private val brush = Paint()


    constructor(context: Context) : super(context) {
        brush.isAntiAlias = true
        brush.color = Color.BLACK
        brush.style = Paint.Style.STROKE
        brush.strokeJoin = Paint.Join.ROUND

        brush.strokeWidth = 8f

    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        var x = event.x
        var y = event.y

        if (event.action == MotionEvent.ACTION_DOWN) {
            path.moveTo(x, y)
            return true
        } else if (event.action == MotionEvent.ACTION_MOVE)
            path.lineTo(x, y)


        postInvalidate()
        return false
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas!!.drawPath(path, brush)
    }


}