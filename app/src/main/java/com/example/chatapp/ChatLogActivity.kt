package com.example.chatapp

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_chat_log.*

class ChatLogActivity : AppCompatActivity() {

    companion object {
        val TAG = "ChatLog"
    }
    val adapter = GroupAdapter<ViewHolder>()

    var toUser: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_log)

        recyclerview_chat_log.adapter = adapter

        toUser = intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)
        supportActionBar?.title = toUser?.username

        listenForMessages()

        send_button_chat_log.setOnClickListener {
            Log.d(TAG,"Attepmt to send Message....")
            performSendMessage()
        }
    }

    private fun listenForMessages() {
        val fromId = FirebaseAuth.getInstance().uid
        val toId = toUser?.uid
        val ref = FirebaseDatabase.getInstance().getReference("Messages/$fromId/$toId")

        ref.addChildEventListener(object : ChildEventListener{

            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                val chatMessage = p0.getValue(ChatMessage::class.java)

                if (chatMessage != null){
                    Log.d(TAG,chatMessage.text)


                    if (chatMessage.formId == FirebaseAuth.getInstance().uid){
                        val currentUser = Dashboard.currentUser
                        adapter.add(ChatFromItem(chatMessage.text,currentUser!!))
                    }else{
                        adapter.add(ChatToItem(chatMessage.text,toUser!!))
                    }
                }
                recyclerview_chat_log.scrollToPosition(adapter.itemCount -1)

            }

            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {

            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {

            }

            override fun onChildRemoved(p0: DataSnapshot) {

            }
        })
    }

    private fun performSendMessage() {
        val text = editText_chat_log.text.toString()

        val fromId = FirebaseAuth.getInstance().uid
        val user = intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)
        val toId = user.uid

        if (fromId == null) return

        val reference = FirebaseDatabase.getInstance().getReference("Messages/$fromId/$toId").push()

        val toreference = FirebaseDatabase.getInstance().getReference("Messages/$toId/$fromId").push()

        val chatMessage = ChatMessage(reference.key!!, text, fromId,toId,System.currentTimeMillis() / 1000)

        reference.setValue(chatMessage)
            .addOnSuccessListener {
                Log.d(TAG,"Save Our Chat Message: ${reference.key}")
                editText_chat_log.text.clear()
                recyclerview_chat_log.scrollToPosition(adapter.itemCount -1)
            }
        toreference.setValue(chatMessage)

        val latestMessageRef = FirebaseDatabase.getInstance().getReference("Latest Messages/$fromId/$toId")
        latestMessageRef.setValue(chatMessage)
        val latestMessageToRef = FirebaseDatabase.getInstance().getReference("Latest Messages/$toId/$fromId")
        latestMessageToRef.setValue(chatMessage)


    }

}
