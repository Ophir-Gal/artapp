package com.example.artapp

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase


class MainActivity : AppCompatActivity() {

    private val mDBRef : DatabaseReference = FirebaseDatabase.getInstance().getReference("users")
    lateinit var mCanvas: PaintView
    lateinit var mBrushFab: FloatingActionButton
    lateinit var mPaletteFab: FloatingActionButton
    private var smallBrush = true
    private var mColors = ArrayList<String>()
    var index = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        mCanvas = findViewById(R.id.canvas)
        mBrushFab = findViewById(R.id.mainBrushFab)
        mPaletteFab = findViewById(R.id.colorFab)

        mColors.add("black")
        mColors.add("red")
        mColors.add("blue")
        mColors.add("cyan")
        mColors.add("green")
        mColors.add("yellow")


        mBrushFab.setOnClickListener {
            changeBrushSize()
        }

        mPaletteFab.backgroundTintList = ColorStateList.valueOf(Color.BLACK)
        mPaletteFab.setOnClickListener{
            changeBrushColor()
        }
    }

    fun changeBrushSize() {
        if (smallBrush) {
            mCanvas.changeBrushSize(15f)
            smallBrush = false
        } else {
            mCanvas.changeBrushSize(8f)
            smallBrush = true
        }

    }

    fun changeBrushColor() {
        if (index < NUM_COLORS - 1)
            index++
        else
            index = 0

        mCanvas.changeBrushColor(mColors[index])
        mPaletteFab.backgroundTintList = ColorStateList.valueOf(Color.parseColor(mColors[index]))

    }

    companion object {
        private val NUM_COLORS = 6

    }

}