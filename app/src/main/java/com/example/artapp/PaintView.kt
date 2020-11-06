package com.example.artapp

import android.view.View
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.Log
import android.view.MotionEvent
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class PaintView : View {

    private val path = Path()
    private val brush = Paint()
    private val mDatabaseAdapter : DatabaseAdapter

    constructor(context: Context) : super(context) {
        brush.isAntiAlias = true
        brush.color = Color.BLACK
        brush.style = Paint.Style.STROKE
        brush.strokeJoin = Paint.Join.ROUND
        brush.strokeWidth = 8f
        mDatabaseAdapter = DatabaseAdapter()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        var x = event.x
        var y = event.y

        if (event.action == MotionEvent.ACTION_DOWN) {
            path.moveTo(x, y)
            return true
        } else if (event.action == MotionEvent.ACTION_MOVE) {
            path.lineTo(x, y)
        } else if (event.action == MotionEvent.ACTION_UP) {
            Log.i("TEST FINGER OFF", "FINGER WAS TAKEN OFF")
            /*
             * Send last drawn path to DatabaseAdapter
             */

            return true
        }

        postInvalidate()
        return true
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas!!.drawPath(path, brush)
    }

    public fun drawPath(path : Array<IntArray>) {

    }


}