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

    fun sendDrawingToDatabase(drawing: Bitmap) {
        val pixels = IntArray(drawing.height * drawing.width)
        drawing.getPixels(pixels, 0, drawing.width, 0, 0, drawing.width, drawing.height)
        mUserRef.child("pixels").setValue(pixels.toList().toString())
            .addOnSuccessListener { Log.i("pixels", "pixels successfully written!") }
            .addOnFailureListener { e -> Log.i("pixels", "Error writing pixels :(", e) }
        Log.i("pixels", "pixels")
    }

    private fun setUpUserEntry(){
        mUserRef.child("w").setValue(mWidth)
        mUserRef.child("h").setValue(mHeight)
        mUserRef.child("pixels").setValue(null)
    }

    private fun setEventListeners() {
        mDBRef.addChildEventListener(object : ChildEventListener {

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                // TODO: NEED TO IMPLEMENT THIS
                // if child changed is some other user
                //      get child's pixels and width and height
                //      update display by calling:
                //      mPaintView.addToCanvas(pixels, otherWidth, otherHeight)
                val otherUser = snapshot
                if (otherUser.key != mUserKey) {
                    val pix = otherUser.child("pixels").getValue() as String
                    val otherPixels = pix.removeSurrounding("[", "]")
                                         .replace("\\s".toRegex(), "")
                                         .split(",").map { it.toInt() }.toIntArray()
                    val otherWidth = (otherUser.child("w").getValue() as Long).toInt()
                    val otherHeight = (otherUser.child("h").getValue() as Long).toInt()
                    mPaintView.addToCanvas(
                        otherPixels,
                        otherWidth,
                        otherHeight
                    )
                }
            }

            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                // probably don't need to use this method
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