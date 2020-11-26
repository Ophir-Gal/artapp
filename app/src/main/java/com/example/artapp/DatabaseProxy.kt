 package com.example.artapp

import android.graphics.Bitmap
import android.util.Log
import com.google.firebase.database.*

// `object` is used here to make the DatabaseProxy a singleton
object DatabaseProxy {

    private val mDBRef : DatabaseReference = FirebaseDatabase.getInstance().reference
    private lateinit var mUserKey : String
    private lateinit var mUserRef : DatabaseReference
    public lateinit var mPaintView : PaintView
    public var mWidth: Int = 0
    public var mHeight : Int = 0

    init {
        // in case we need to initialize anything
    }

    // sets up the DB ref and returns a new user key if parameter is null
    fun enterGlobalRoom(userKey:String?) : String {
        if (userKey == null) { // user hasn't entered the room yet
            mUserRef = mDBRef.child("globalRoom").push().ref // create user in global room
            setUpUserEntry()
        } else { // user has already been in the room
            mUserRef = mDBRef.child("globalRoom").child(userKey).ref // get reference
        }

        mUserKey = mUserRef.key!! // save userKey
        setEventListeners()

        return mUserKey
    }

    // sets up the DB ref and returns a new user key if userKey parameter is null
    fun enterExistingRoom(userKey:String?, roomKey : String) : String {
        val privateRoomsRef = mDBRef.child("privateRooms")
        // if (privateRoomsRef.)  // check if key matches any existing room

        return mUserKey
    }

    // sets up the DB ref and returns a new user key if parameter is null
    fun enterNewRoom(userKey:String?) : Pair<String, String> {
        if (userKey == null) { // user does not have a userKey
            // create private room and user
            mUserRef = mDBRef.child("privateRooms").push().push().ref
        } else { // user already has a userKey
            mUserRef = mDBRef.child("privateRooms").push().child(userKey).ref // get reference
        }

        mUserKey = mUserRef.key!! // save userKey
        setUpUserEntry()
        setEventListeners()

        val roomKey = mUserRef.parent!!.key!!.substring(3..7) // notice the indices used here
        return Pair(mUserKey, roomKey)
    }

    private fun setUpUserEntry() {
        mUserRef.setValue("")
    }

    // TODO: should probably delete rooms when no users inside (can keep track of a user counter)
    // TODO: should probably write code to remove event listeners when user exits a room
    private fun setEventListeners() {
        mUserRef.parent!!.addChildEventListener(object : ChildEventListener {

            override fun onChildChanged(otherUser: DataSnapshot, previousChildName: String?) {
                if (otherUser.key != mUserKey) {
                    // get the line obj from the database and draw it
                    val line: PaintView.Line? = otherUser.getValue(PaintView.Line::class.java)

                    if (line != null) {
                        mPaintView.drawLine(line)
                    }
                }
            }

            override fun onChildAdded(otherUser: DataSnapshot, previousChildName: String?) {
                if (otherUser.key != mUserKey) {
                    // get the line obj from the database and draw it
                    val line: PaintView.Line? = otherUser.getValue(PaintView.Line::class.java)
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

    fun sendLineToDatabase(line: PaintView.Line) {
        //val pixels = IntArray(drawing.height * drawing.width) // probably use later
        mUserRef.setValue(line)
                .addOnSuccessListener { Log.i("line", "Line successfully written!") }
                .addOnFailureListener { e -> Log.i("line", "Error writing line :(", e) }

    }
}