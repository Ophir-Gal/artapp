 package com.example.artapp

import android.graphics.Bitmap
import android.util.Log
import com.google.firebase.database.*

class DatabaseAdapter {

    private val mDBRef : DatabaseReference = FirebaseDatabase.getInstance().getReference("users")
    private val mPaintView : PaintView
    private val mUserKey : String
    private val mUserRef : DatabaseReference
    private val mWidth : Int
    private val mHeight : Int

    constructor(paintView: PaintView, width: Int, height: Int) {
        mWidth = width
        mHeight = height
        mPaintView = paintView
        mUserRef = mDBRef.push() // create the current user's database entry
        mUserKey = mUserRef.key!! // save userKey
        setUpUserEntry()
        setEventListeners()
    }

    fun sendLineToDatabase(line: PaintView.Line, drawing: Bitmap) {
        //val pixels = IntArray(drawing.height * drawing.width) // probably use later
        mUserRef.setValue(line)
            .addOnSuccessListener { Log.i("pixels", "pixels successfully written!") }
            .addOnFailureListener { e -> Log.i("pixels", "Error writing pixels :(", e) }
    }


    private fun setUpUserEntry(){
        //mUserRef.child("w").setValue(mWidth)
        //mUserRef.child("h").setValue(mHeight)
        mUserRef.child("points").setValue(null)
        //mUserRef.child("path").setValue(null)
    }


    private fun setEventListeners() {
        mDBRef.addChildEventListener(object : ChildEventListener {

            // TODO:
            // 1. Scale the line
            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val otherUser = snapshot
                if (otherUser.key != mUserKey) {
                    // get the line obj from the database and draw it
                    val line: PaintView.Line? = snapshot.getValue(PaintView.Line::class.java)
                    if (line != null) {
                        mPaintView.drawLine(line)
                    }
                }
            }

            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val otherUser = snapshot
                if (otherUser.key != mUserKey) {
                    // get the line obj from the database and draw it
                    val line: PaintView.Line? = snapshot.getValue(PaintView.Line::class.java)
                    if (line != null) {
                        mPaintView.drawLine(line)
                    }
                }
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                // probably don't need to use this method
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                // probably don't need to use this method
            }

            override fun onCancelled(error: DatabaseError) {
                // probably don't need to use this method
            }
        })
    }
}