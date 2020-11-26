package com.example.artapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity


class MainActivity : AppCompatActivity() {

    private lateinit var mGlobalButton : Button
    private lateinit var mExistingButton: Button
    private lateinit var mNewButton: Button
    private var mUserKey: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar!!.hide()
        setContentView(R.layout.activity_main)
        mGlobalButton = findViewById(R.id.global_bttn)
        mExistingButton = findViewById(R.id.existing_bttn)
        mNewButton = findViewById(R.id.new_bttn)

        if (savedInstanceState != null) {
            mUserKey = savedInstanceState.getString("userKey")
        }
    }

    fun onGlobalRoomButtonClick(view: View) {
        mUserKey = DatabaseProxy.enterGlobalRoom(mUserKey)
        val globalRoomIntent = Intent(this@MainActivity, DrawingActivity::class.java)
        globalRoomIntent.putExtra(BUTTON_CLICKED_EXTRA, GLOBAL_ROOM)
        startActivity(globalRoomIntent)
    }

    fun onExistingRoomButtonClick(view: View) {
        /*
        show dialog to user asking for key
        ask database to enter room
        enter room if access granted
        otherwise show appropriate dialog message to user (leave dialog open if possible)
        */


        /*mUserKey = DatabaseProxy.enterExistingRoom(mUserKey, roomKey)*/
        /*startActivity(Intent(this@MainActivity, DrawingActivity::class.java))*/
    }

    fun onNewRoomButtonClick(view: View) {
        val userKeyRoomKeyPair = DatabaseProxy.enterNewRoom(mUserKey)
        mUserKey = userKeyRoomKeyPair.first
        val roomKey = userKeyRoomKeyPair.second
        val newRoomIntent = Intent(this@MainActivity, DrawingActivity::class.java)
        newRoomIntent.putExtra(BUTTON_CLICKED_EXTRA, NEW_ROOM)
        newRoomIntent.putExtra(ROOM_KEY_EXTRA, roomKey)
        startActivity(newRoomIntent)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        if (mUserKey != null) {
            outState.putString("userKey", mUserKey)
        }
        super.onSaveInstanceState(outState)
    }

    companion object {
        val BUTTON_CLICKED_EXTRA = "button clicked"
        val ROOM_KEY_EXTRA = "room key"
        val GLOBAL_ROOM = "Global Room"
        val EXISTING_ROOM = "Existing Room"
        val NEW_ROOM = "New Room"

    }
}