package com.example.artapp

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton


class DrawingActivity : AppCompatActivity() {

    private lateinit var mCanvas: PaintView
    private lateinit var mBrushFabSmall: FloatingActionButton
    private lateinit var mBrushFabLarge: FloatingActionButton
    private lateinit var mPaletteFab: FloatingActionButton
    private lateinit var mTopLayout : ConstraintLayout
    private var mSmallBrush = true
    private var mColors = ArrayList<String>()
    private var mIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_drawing)
        mCanvas = findViewById(R.id.canvas)
        mBrushFabSmall = findViewById(R.id.mainBrushFabSmall)
        mBrushFabLarge = findViewById(R.id.mainBrushFabSmall)
        mPaletteFab = findViewById(R.id.colorFab)
        mTopLayout = findViewById(R.id.drawingActivityTopLayout)

        mColors.add("black")
        mColors.add("white")
        mColors.add("red")
        mColors.add("blue")
        mColors.add("cyan")
        mColors.add("green")
        mColors.add("yellow")


        mPaletteFab.backgroundTintList = ColorStateList.valueOf(Color.BLACK)
        mPaletteFab.setOnClickListener{
            changeBrushColor()
        }

        // Set action bar title
        val prefix = "ArtApp"
        when (intent.getStringExtra(MainActivity.BUTTON_CLICKED_EXTRA)) {
            MainActivity.GLOBAL_ROOM -> supportActionBar!!.title = "$prefix (\uD83C\uDF10)"
            else -> { // existing or new room
                val roomKey = intent.getStringExtra(MainActivity.ROOM_KEY_EXTRA)
                supportActionBar!!.title = "$prefix (\uD83D\uDD12) \uD83D\uDD11 = ${roomKey}"
            }
        }
        var x = PaintView.Line()
        x.setPoint(0.0001f, 0.0001f)
        x.brushSize = 8f
        x.brushColor = Color.BLACK
        DatabaseProxy.sendLineToDatabase(x)
    }

    fun changeBrushSize(view: View) {
        if (mSmallBrush) {
            mCanvas.changeBrushSize(LARGE_BRUSH_SIZE)
            mSmallBrush = false
            reverseOrderOfLastTwoChildrenViews()
        } else {
            mCanvas.changeBrushSize(SMALL_BRUSH_SIZE)
            mSmallBrush = true
            reverseOrderOfLastTwoChildrenViews()
        }
    }

    private fun reverseOrderOfLastTwoChildrenViews() {
        val childView1 = mTopLayout.getChildAt(mTopLayout.childCount - 1)
        val childView2 = mTopLayout.getChildAt(mTopLayout.childCount - 2)
        mTopLayout.removeViews(mTopLayout.childCount - 2, 2)
        mTopLayout.addView(childView1)
        mTopLayout.addView(childView2)
    }

    fun changeBrushColor() {
        if (mIndex < mColors.size - 1)
            mIndex++
        else
            mIndex = 0


        if(mColors[mIndex] == "white") { // set icon color to gray if button color turns white
            val drawable = getDrawable(R.drawable.ic_palette)
            drawable!!.colorFilter = PorterDuffColorFilter(getColor(R.color.verylightgray),
                    PorterDuff.Mode.SRC_IN)
            mPaletteFab.setImageDrawable(drawable)
        } else {
            val drawable = getDrawable(R.drawable.ic_palette)
            mPaletteFab.setImageDrawable(drawable)
        }

        mCanvas.changeBrushColor(mColors[mIndex])
        mPaletteFab.backgroundTintList = ColorStateList.valueOf(Color.parseColor(mColors[mIndex]))

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem) =  when(item.itemId) {
        R.id.action_download -> {
            mCanvas.downloadCanvas()
            true
        }

        R.id.action_share -> {
            mCanvas.share()
            true
        }

        else ->
            super.onOptionsItemSelected(item)
    }

    companion object {
        val LARGE_BRUSH_SIZE = 50f
        val SMALL_BRUSH_SIZE = 10f
    }

    override fun onResume() {
        super.onResume()
        DatabaseProxy.updateView()
    }

    override fun onDestroy() {
        super.onDestroy()
        DatabaseProxy.removeListeners()
    }
}