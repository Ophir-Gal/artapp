package com.example.artapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
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
        DatabaseProxy.mMainActivity = this

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
        // Shows dialog to user asking for key, and asks DatabaseAdapter to enter the room
        // TODO: leave dialog open if possible

        var roomKey = "1Q4cg"

        DatabaseProxy.requestToEnterExistingRoom(mUserKey, roomKey)
    }

    // Enter room if access granted, otherwise show appropriate message to user
    fun finishExistingRoomRequest(userKeyRoomKeyPair : Pair<String, String>?) {

        if (userKeyRoomKeyPair != null) {
            mUserKey = userKeyRoomKeyPair.first
            val roomKey = userKeyRoomKeyPair.second
            val existingRoomIntent = Intent(this@MainActivity, DrawingActivity::class.java)
            existingRoomIntent.putExtra(BUTTON_CLICKED_EXTRA, EXISTING_ROOM)
            existingRoomIntent.putExtra(ROOM_KEY_EXTRA, roomKey)
            startActivity(existingRoomIntent)
        } else {
            Toast.makeText(this@MainActivity,
                    "Could not find a matching room.",
                    Toast.LENGTH_LONG).show()
        }
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