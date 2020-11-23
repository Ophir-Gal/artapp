package com.example.artapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Button

class MainActivity : AppCompatActivity() {

    private lateinit var globalButton : Button
    private lateinit var existingButton: Button
    private lateinit var newButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar!!.hide()
        setContentView(R.layout.activity_main)
        globalButton = findViewById(R.id.global_bttn)
        existingButton = findViewById(R.id.existing_bttn)
        newButton = findViewById(R.id.new_bttn)
    }

    fun onGlobalButtonClick(view: View) {
        startActivity(Intent(this@MainActivity, DrawingActivity::class.java))
    }
}