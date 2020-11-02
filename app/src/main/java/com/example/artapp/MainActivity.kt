package com.example.artapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class MainActivity : AppCompatActivity() {

    private val mDBRef : DatabaseReference = FirebaseDatabase.getInstance().getReference("canvas")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        var paintView = PaintView(this)
        setContentView(paintView)

    }
}