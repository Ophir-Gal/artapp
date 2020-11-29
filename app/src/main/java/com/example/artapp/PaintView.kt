package com.example.artapp

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.File.separator
import java.io.FileOutputStream
import java.io.OutputStream


class PaintView : View {

    private val mPath: Path = Path() // drawing path
    private var mCanvasPaint: Paint? = null // defines what paint to draw with
    private val mBrush: Paint = Paint() // defines how to draw
    private var mPaintColor = Color.BLACK //initial color
    private var mCanvas: Canvas? = null // canvas - holds drawings and transfers them to the view
    private var mCanvasBitmap: Bitmap? = null // canvas bitmap
    private var mCurrentBrushSize = 0f // current brush size
    private var mLastBrushSize = 0f // last brush size
    private var mLine: Line? = null // line object that needs to be drawn
    private var mPoint: PointF = PointF() // holds reference to locations of touch

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        mCurrentBrushSize = 8f
        mLastBrushSize = mCurrentBrushSize
        mBrush.color = mPaintColor
        mBrush.isAntiAlias = true
        mBrush.strokeWidth = mCurrentBrushSize
        mBrush.style = Paint.Style.STROKE
        mBrush.strokeJoin = Paint.Join.ROUND
        mBrush.strokeCap = Paint.Cap.ROUND


        mCanvasPaint = Paint(Paint.DITHER_FLAG)
        setSaveEnabled(true)
    }

    class Line() {
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
        mPaintColor = Color.parseColor(color)
        mBrush.color = mPaintColor
    }

    fun changeBrushSize(size: Float) {
        mCurrentBrushSize = size
        mBrush.strokeWidth = mCurrentBrushSize
    }

    // Called when activity loads, it is overridden here to initialize variables
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        //create canvas of certain device size.
        super.onSizeChanged(w, h, oldw, oldh)

        DatabaseProxy.mWidth = w
        DatabaseProxy.mHeight = h
        DatabaseProxy.mPaintView = this

        //create Bitmap of certain w,h
        mCanvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)

        //apply bitmap to graphic to start drawing.
        mCanvas = Canvas(mCanvasBitmap!!)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        var x = event.x
        var y = event.y

        if (event.action == MotionEvent.ACTION_DOWN) {
            mPath.moveTo(x, y)
            mLine = Line()

            // hold ref to starting point
            mPoint.x = x
            mPoint.y = y

            // add it to our line
            mLine!!.setPoint(mPoint.x / width, mPoint.y / height)
        } else if (event.action == MotionEvent.ACTION_MOVE) {

            // set path from starting points to end points
            mPath.quadTo(mPoint.x, mPoint.y, x, y)

            //Log.i("TEST", "start ${mPoint.x} ${mPoint.y} end: ${x} ${y}")
            // get reference to end point
            mPoint.x = x
            mPoint.y = y

            // add it to our line
            mLine!!.setPoint(mPoint.x / width, mPoint.y / height)
        } else if (event.action == MotionEvent.ACTION_UP) {
            // draw the path and send it to the  database
            mPath.lineTo(mPoint.x, mPoint.y)
            mCanvas!!.drawPath(mPath, mBrush);
            mPath.reset()
            mLine!!.brushColor = mPaintColor
            mLine!!.brushSize = mCurrentBrushSize

            DatabaseProxy.sendLineToDatabase(mLine!!)
        } else {
            return false
        }

        postInvalidate()
        return true
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas!!.drawBitmap(mCanvasBitmap!!, 0f, 0f, mCanvasPaint)
        canvas!!.drawPath(mPath, mBrush)
    }

    // sets path object out of the points received from database
    private fun setPath(linePoints: List<PointF>): Path {
        var currPoint = linePoints[0]
        var nextPoint: PointF? = null
        val path = Path()

        path.moveTo(currPoint.x * width, currPoint.y * height)
        if (linePoints.size == 1) {
            path.lineTo(currPoint.x * width, currPoint.y * height)
            invalidate()
        } else {
            for (i in 1 until linePoints.size) {
                nextPoint = linePoints[i]
                path.quadTo(
                    currPoint.x * width,
                    currPoint.y * height,
                    nextPoint.x * width,
                    nextPoint.y * height
                )
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

    //downloads current canvas
    fun downloadCanvas() {
        //creates a new canvas/bitmap with a white background (unable to see drawings on a black background)
        val bitmapToSave = Bitmap.createBitmap(
            mCanvasBitmap!!.width, mCanvasBitmap!!.height,
            Bitmap.Config.ARGB_8888
        )
        val tempCanvas = Canvas(bitmapToSave)
        tempCanvas.drawColor(Color.WHITE) // set white background
        tempCanvas.drawBitmap(mCanvasBitmap!!, 0f, 0f, mCanvasPaint) // overlay the current drawing

        if (android.os.Build.VERSION.SDK_INT >= 29) {
            val values = contentValues()

            values.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/ArtApp")
            values.put(MediaStore.Images.Media.IS_PENDING, true)

            val uri: Uri? =
                context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

            if (uri != null) {
                //success
                saveImageToStream(bitmapToSave, context.contentResolver.openOutputStream(uri))
                values.put(MediaStore.Images.Media.IS_PENDING, false)
                context.contentResolver.update(uri, values, null, null)
                Toast.makeText(context, "Canvas downloaded to Photos", Toast.LENGTH_LONG).show()
            } else
                Toast.makeText(context, "Download failed", Toast.LENGTH_LONG).show()


        } else {
            val directory =
                File(Environment.getExternalStorageDirectory().toString() + separator + "ArtApp")

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

    private fun contentValues(): ContentValues {
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


    //shares canvas via intent
    fun share() {
        val intent = Intent(Intent.ACTION_SEND).setType("image/*")

        val bitmapToShare = Bitmap.createBitmap(
            mCanvasBitmap!!.width, mCanvasBitmap!!.height,
            Bitmap.Config.ARGB_8888
        )
        val tempCanvas = Canvas(bitmapToShare)
        tempCanvas.drawColor(Color.WHITE) // set white background
        tempCanvas.drawBitmap(mCanvasBitmap!!, 0f, 0f, mCanvasPaint) // overlay the current drawing

        bitmapToShare!!.compress(Bitmap.CompressFormat.PNG, 100, ByteArrayOutputStream())

        val path = MediaStore.Images.Media.insertImage(
            context.contentResolver,
            bitmapToShare,
            "tempImage",
            "ArtApp Canvas"
        )

        val uri = Uri.parse(path)

        intent.putExtra(Intent.EXTRA_STREAM, uri)
        context.startActivity(intent)
    }
}