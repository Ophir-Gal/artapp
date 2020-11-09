package com.example.artapp

import android.content.Context
import android.graphics.*
import android.util.Log
import android.view.MotionEvent
import android.view.View


class PaintView : View {

    private val path : Path = Path() // drawing path
    private var canvasPaint : Paint? = null // defines what paint to draw with
    private val brush : Paint = Paint() // defines how to draw
    private val paintColor = Color.BLACK //initial color
    private var mCanvas: Canvas? = null // canvas - holds drawings and transfers them to the view
    private var canvasBitmap: Bitmap? = null // canvas bitmap
    private var currentBrushSize = 0f // current brush size
    private var lastBrushSize = 0f // last brush size
    private lateinit var databaseAdapter : DatabaseAdapter

    constructor(context: Context) : super(context) {
        currentBrushSize = 8f
        lastBrushSize = currentBrushSize
        brush.color = paintColor
        brush.isAntiAlias = true
        brush.strokeWidth = currentBrushSize
        brush.style = Paint.Style.STROKE
        brush.strokeJoin = Paint.Join.ROUND
        brush.strokeCap = Paint.Cap.ROUND

        canvasPaint = Paint(Paint.DITHER_FLAG)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        //create canvas of certain device size.
        super.onSizeChanged(w, h, oldw, oldh)

        databaseAdapter = DatabaseAdapter(this, w, h)

        //create Bitmap of certain w,h
        canvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)

        //apply bitmap to graphic to start drawing.
        mCanvas = Canvas(canvasBitmap!!)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        var x = event.x
        var y = event.y

        if (event.action == MotionEvent.ACTION_DOWN) {
            path.moveTo(x, y)
        } else if (event.action == MotionEvent.ACTION_MOVE) {
            path.lineTo(x, y)
        } else if (event.action == MotionEvent.ACTION_UP) {
            path.lineTo(x, y)
            mCanvas!!.drawPath(path, brush);
            path.reset()
            databaseAdapter.sendDrawingToDatabase(canvasBitmap!!)
        } else {
            return false
        }

        postInvalidate()
        return true
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas!!.drawBitmap(canvasBitmap!!, 0f, 0f, canvasPaint)
        canvas!!.drawPath(path, brush)
    }

    fun addToCanvas(otherPixels: IntArray, otherWidth: Int, otherHeight: Int) {
        // Create Bitmap from pixels array
        val newBitmap = Bitmap.createBitmap(otherPixels,
            otherWidth, otherHeight, Bitmap.Config.ARGB_8888)
            //.copy(Bitmap.Config.ARGB_8888,true) // I believe we don't need this line (doesn't need to be mutable)
        // scale that cropped version to fit screen
        val scaledBitmap = Bitmap.createScaledBitmap(newBitmap,
            canvasBitmap!!.width, canvasBitmap!!.height,false
        ) // I believe this returns a mutable bitmap

        // draw incoming bitmap on auxiliary canvas
        mCanvas!!.drawBitmap(scaledBitmap, 0f, 0f, canvasPaint)

        postInvalidate()
    }
}