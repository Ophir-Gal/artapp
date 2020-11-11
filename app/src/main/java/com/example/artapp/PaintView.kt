package com.example.artapp

import android.content.Context
import android.graphics.*
import android.util.Log
import android.view.MotionEvent
import android.view.View


class PaintView : View {

    private val path: Path = Path() // drawing path
    private var canvasPaint: Paint? = null // defines what paint to draw with
    private val brush: Paint = Paint() // defines how to draw
    private val paintColor = Color.BLACK //initial color
    private var mCanvas: Canvas? = null // canvas - holds drawings and transfers them to the view
    private var canvasBitmap: Bitmap? = null // canvas bitmap
    private var currentBrushSize = 0f // current brush size
    private var lastBrushSize = 0f // last brush size
    private var mLine: Line? = null // line object that needs to be drawn
    private lateinit var databaseAdapter: DatabaseAdapter
    private var mPoint : PointF = PointF() // holds reference to locations of touch
    private var mScale : Int = 0 // need for later

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

    class Line() {
        val points = ArrayList<PointF>()

        val size = Point()
            get() = field

        fun setPoint(x: Float, y: Float) {
            points.add(PointF(x, y))
        }

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
            mLine = Line()

            // hold ref to starting point
            mPoint.x = x
            mPoint.y = y

            // add it to our line
            mLine!!.setPoint(mPoint.x, mPoint.y)
        } else if (event.action == MotionEvent.ACTION_MOVE) {

            // set path from starting points to end points
            path.quadTo(mPoint.x, mPoint.y, x, y)

            //Log.i("TEST", "start ${mPoint.x} ${mPoint.y} end: ${x} ${y}")
            // get reference to end point
            mPoint.x = x
            mPoint.y = y

            // add it to our line
            mLine!!.setPoint(mPoint.x, mPoint.y)
        } else if (event.action == MotionEvent.ACTION_UP) {
            // draw the path and send it to the  database
            mCanvas!!.drawPath(path, brush);
            path.reset()
            databaseAdapter.sendLineToDatabase(mLine!!, canvasBitmap!!)
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

    // sets path object out of the points received from database
    private fun setPath(linePoints: List<PointF>): Path {
        var currPoint = linePoints[0]
        var nextPoint: PointF? = null
        val path = Path()

        path.moveTo(currPoint.x, currPoint.y)
        if (linePoints.size == 1) {
            Log.i("PATH","ONE POINT")
            path.lineTo(currPoint.x, currPoint.y)
            invalidate()
        } else {
            Log.i("PATH", "NOT ONE POINT")
            for (i in 1 until linePoints.size) {
                nextPoint = linePoints[i]
                path.quadTo(currPoint.x, currPoint.y, nextPoint.x, nextPoint.y)
                invalidate()
                currPoint = nextPoint
            }
        }
        return path
    }

    // Draws the line
    fun drawLine(line: Line) {
        mCanvas!!.drawPath(setPath(line.points as List<PointF>), brush)
    }
}