package com.example.artapp

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.File.separator
import java.io.FileOutputStream
import java.io.OutputStream


class PaintView : View {

    private val path: Path = Path() // drawing path
    private var canvasPaint: Paint? = null // defines what paint to draw with
    private val brush: Paint = Paint() // defines how to draw
    private var paintColor = Color.BLACK //initial color
    private var mCanvas: Canvas? = null // canvas - holds drawings and transfers them to the view
    private var canvasBitmap: Bitmap? = null // canvas bitmap
    private var currentBrushSize = 0f // current brush size
    private var lastBrushSize = 0f // last brush size
    private var mLine: Line? = null // line object that needs to be drawn
    private var mPoint : PointF = PointF() // holds reference to locations of touch

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
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

    class Line {
        val points = ArrayList<PointF>()

        fun setPoint(x: Float, y: Float) {
            points.add(PointF(x, y))
        }

        var brushSize = 0f
        var brushColor = Color.BLACK

    }

    //color must be in format #RRGGBB #AARRGGBB 'red',
    // 'blue', 'green', 'black', 'white', 'gray', 'cyan',
    // 'magenta', 'yellow', 'lightgray', 'darkgray'
    fun changeBrushColor(color: String) {
        paintColor = Color.parseColor(color)
        brush.color = paintColor
    }

    fun changeBrushSize(size: Float) {
        currentBrushSize = size
        brush.strokeWidth = currentBrushSize
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        //create canvas of certain device size.
        super.onSizeChanged(w, h, oldw, oldh)

        DatabaseProxy.mWidth = w
        DatabaseProxy.mHeight = h
        DatabaseProxy.mPaintView = this

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
            mLine!!.setPoint(mPoint.x / width, mPoint.y / height)
        } else if (event.action == MotionEvent.ACTION_MOVE) {

            // set path from starting points to end points
            path.quadTo(mPoint.x, mPoint.y, x, y)

            //Log.i("TEST", "start ${mPoint.x} ${mPoint.y} end: ${x} ${y}")
            // get reference to end point
            mPoint.x = x
            mPoint.y = y

            // add it to our line
            mLine!!.setPoint(mPoint.x / width, mPoint.y / height)
        } else if (event.action == MotionEvent.ACTION_UP) {
            // draw the path and send it to the  database
            path.lineTo(mPoint.x, mPoint.y)
            mCanvas!!.drawPath(path, brush);
            path.reset()
            mLine!!.brushColor = paintColor
            mLine!!.brushSize = currentBrushSize


            DatabaseProxy.sendLineToDatabase(mLine!!)
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

        path.moveTo(currPoint.x * width, currPoint.y * height)
        if (linePoints.size == 1) {
            Log.i("PATH","ONE POINT")
            path.lineTo(currPoint.x * width, currPoint.y * height)
            invalidate()
        } else {
            Log.i("PATH", "NOT ONE POINT")
            for (i in 1 until linePoints.size) {
                nextPoint = linePoints[i]
                path.quadTo(currPoint.x * width, currPoint.y * height, nextPoint.x * width, nextPoint.y * height)
                invalidate()
                currPoint = nextPoint
            }
        }
        return path
    }

    // Draws the line
    fun drawLine(line: Line) {
        if (line.points.isEmpty()) {
            return // do nothing if there are no points to draw
        }

        //create a new brush based off properties in Line
        val otherBrush = Paint()
        otherBrush.color = line.brushColor
        otherBrush.strokeWidth = line.brushSize
        otherBrush.isAntiAlias = true
        otherBrush.style = Paint.Style.STROKE
        otherBrush.strokeJoin = Paint.Join.ROUND
        otherBrush.strokeCap = Paint.Cap.ROUND

        mCanvas!!.drawPath(setPath(line.points as List<PointF>), otherBrush)
    }

    fun downloadCanvas() {
        val bitmapToSave = Bitmap.createBitmap(canvasBitmap!!.width, canvasBitmap!!.height,
                Bitmap.Config.ARGB_8888)
        val tempCanvas = Canvas(bitmapToSave)
        tempCanvas.drawColor(Color.WHITE) // set white background
        tempCanvas.drawBitmap(canvasBitmap!!, 0f, 0f, canvasPaint) // overlay the current drawing

        if (android.os.Build.VERSION.SDK_INT >= 29) {
            val values = contentValues()

            values.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/ArtApp")
            values.put(MediaStore.Images.Media.IS_PENDING, true)

            val uri: Uri? = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

            if (uri != null) {
                //success
                saveImageToStream(bitmapToSave, context.contentResolver.openOutputStream(uri))
                values.put(MediaStore.Images.Media.IS_PENDING, false)
                context.contentResolver.update(uri, values, null, null)
                Toast.makeText(context, "Canvas downloaded to Photos", Toast.LENGTH_LONG).show()
            } else
                Toast.makeText(context, "Download failed", Toast.LENGTH_LONG).show()

        } else {
            val directory = File(Environment.getExternalStorageDirectory().toString() + separator + "ArtApp")

            if (!directory.exists())
                directory.mkdirs()

            val fileName = System.currentTimeMillis().toString() + ".png"
            val file = File(directory, fileName)
            saveImageToStream(bitmapToSave, FileOutputStream(file))
            if (file.absolutePath != null) {
                val values = contentValues()
                values.put(MediaStore.Images.Media.DATA, file.absolutePath)
                context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            }
        }
    }

    private fun contentValues() : ContentValues {
        val values = ContentValues()
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/png")
        values.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis() / 1000)
        values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis())
        return values
    }

    private fun saveImageToStream(bitmap: Bitmap, outputStream: OutputStream?) {
        if (outputStream != null) {
            try {
                bitmap.setHasAlpha(true);
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                outputStream.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun share() {
        val intent = Intent(Intent.ACTION_SEND).setType("image/*")

        canvasBitmap!!.compress(Bitmap.CompressFormat.PNG, 100, ByteArrayOutputStream())

        val path = MediaStore.Images.Media.insertImage(context.contentResolver,canvasBitmap, "tempImage", "ArtApp Canvas" )

        val uri = Uri.parse(path)

        intent.putExtra(Intent.EXTRA_STREAM, uri)
        context.startActivity(intent)
    }
}