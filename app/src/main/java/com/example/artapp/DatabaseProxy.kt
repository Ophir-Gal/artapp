 package com.example.artapp

import android.graphics.Bitmap
import android.util.Log
import com.google.firebase.database.*
import java.lang.Exception

// `object` is used here to make the DatabaseProxy a singleton
object DatabaseProxy {

    private val mDBRef : DatabaseReference = FirebaseDatabase.getInstance().reference
    private val GLOBAL_ROOM_PATH = "globalRoom"
    private val PRIVATE_ROOMS_PATH = "privateRooms"
    private val ROOM_KEY_RANGE = 3..7
    private lateinit var mUserKey : String
    private lateinit var mUserRef : DatabaseReference
    public lateinit var mPaintView : PaintView
    public lateinit var mMainActivity: MainActivity
    public var mWidth: Int = 0
    public var mHeight : Int = 0

    init {
        // in case we need to initialize anything
    }

    // sets up the DB ref and returns a new user key if parameter is null
    fun enterGlobalRoom(userKey:String?) : String {
        if (userKey == null) { // user hasn't entered the room yet
            mUserRef = mDBRef.child(GLOBAL_ROOM_PATH).push().ref // create user in global room
            setUpUserEntry()
        } else { // user has already been in the room
            mUserRef = mDBRef.child(GLOBAL_ROOM_PATH).child(userKey).ref // get reference
        }

        mUserKey = mUserRef.key!! // save userKey
        setEventListeners()

        return mUserKey
    }

    // makes an asynchronous check to see if room exists, then calls the handler in MainActivity
    fun requestToEnterExistingRoom(userKey:String?, roomKey : String) {

        mDBRef.child(PRIVATE_ROOMS_PATH).addListenerForSingleValueEvent(object : ValueEventListener {

            override fun onDataChange(privateRoomsSnapshot: DataSnapshot) {
                var privateRoomRef : DatabaseReference? = null
                var responsePair : Pair<String, String>? = null
                // search for a matching room key
                for (privateRoom in privateRoomsSnapshot.children) {
                    if (privateRoom.key!!.substring(ROOM_KEY_RANGE) == roomKey) { // roomKey matched
                        privateRoomRef = privateRoomsSnapshot.ref.child(privateRoom.key!!).ref
                        if (userKey == null) { // user does not have a userKey
                            // create user inside private room
                            mUserRef = privateRoomRef.push().ref
                            setUpUserEntry()
                        } else {               // user already has a userKey
                            mUserRef = privateRoomRef.child(userKey).ref // get reference
                        }
                        mUserKey = mUserRef.key!! // save userKey
                        setEventListeners()

                        responsePair = Pair(mUserKey, roomKey) // prepare response tuple
                    }
                }

                // respond to MainActivity's request
                mMainActivity.finishExistingRoomRequest(responsePair)
            }

            // called when listener failed at server or was removed due to Firebase security rules
            override fun onCancelled(p0: DatabaseError) {}
        })
    }

    // sets up the DB ref and returns a new user key if parameter is null
    fun enterNewRoom(userKey:String?) : Pair<String, String> {
        if (userKey == null) { // user does not have a userKey
            // create private room and user
            mUserRef = mDBRef.child(PRIVATE_ROOMS_PATH).push().push().ref
        } else { // user already has a userKey
            mUserRef = mDBRef.child(PRIVATE_ROOMS_PATH).push().child(userKey).ref // get reference
        }

        mUserKey = mUserRef.key!! // save userKey
        setUpUserEntry()
        setEventListeners()

        val roomKey = mUserRef.parent!!.key!!.substring(ROOM_KEY_RANGE) // a substring is taken
        return Pair(mUserKey, roomKey)
    }

    // Initializes the users entry with an empty line object
    private fun setUpUserEntry() {
        mUserRef.setValue(PaintView.Line())
    }

    // TODO: should probably delete rooms when no users inside (can keep track of a user counter)
    // TODO: should probably write code to remove event listeners when user exits a room
    private fun setEventListeners() {
        mUserRef.parent!!.addChildEventListener(object : ChildEventListener {

            override fun onChildChanged(otherUser: DataSnapshot, previousChildName: String?) {
                if (otherUser.key != mUserKey) {
                    // get the line obj from the database and draw it
                    val line: PaintView.Line? = otherUser.getValue(PaintView.Line::class.java)

                    if (line != null && mPaintView != null) {
                        mPaintView.drawLine(line)
                    }
                }
            }

            override fun onChildAdded(otherUser: DataSnapshot, previousChildName: String?) {
                if (otherUser.key != mUserKey) {
                    // get the line obj from the database and draw it
                    val line: PaintView.Line? = otherUser.getValue(PaintView.Line::class.java)
                    if (line != null && mPaintView != null) {
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