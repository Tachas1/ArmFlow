package com.example.testarmflow.chatFrag

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.testarmflow.CompClassesChat.Chat
import com.example.testarmflow.CompClassesChat.ChatAdapter
import com.example.testarmflow.CompClassesChat.Users
import com.example.testarmflow.R
import com.example.testarmflow.databinding.ActivityChatMessagesBinding
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.squareup.picasso.Picasso
import com.google.android.gms.tasks.Continuation
import com.google.firebase.database.DatabaseReference
import com.google.firebase.storage.ktx.storage

@Suppress("DEPRECATION")
class ChatMessagesActivity : AppCompatActivity() {
    private lateinit var userIdMessage: String
    private lateinit var auth: FirebaseUser
    var chatAdapter: ChatAdapter? = null
    var mChatList: List<Chat>? = null
    lateinit var chatList : RecyclerView
    var seenListener: ValueEventListener? = null
    private lateinit var reference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val sharedPreferences1: SharedPreferences = applicationContext.getSharedPreferences("moje_pref", Context.MODE_PRIVATE)
        val mt = sharedPreferences1.getBoolean("motyw", true)

        val motyw = if(mt){
            R.layout.activity_chat_messages_light
        }else{
            R.layout.activity_chat_messages
        }

        setContentView(motyw)

        intent = intent
        userIdMessage = intent.getStringExtra("id")!!
        auth = FirebaseAuth.getInstance().currentUser!!
        chatList = findViewById(R.id.chatList)
        chatList.setHasFixedSize(true)
        var linearLayoutManager = LinearLayoutManager(applicationContext)
        linearLayoutManager.stackFromEnd = true
        chatList.layoutManager = linearLayoutManager

        reference = FirebaseDatabase.getInstance().reference
            .child("users").child(userIdMessage)
        reference.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val user: Users? = snapshot.getValue(Users::class.java)

                findViewById<TextView>(R.id.userNameChat).text = user!!.getNickName()
                val storage = Firebase.storage
                val gsReference = storage.getReferenceFromUrl(user.getAvatarSrc().toString())
                gsReference.downloadUrl.addOnSuccessListener {
                    Picasso
                        .get()
                        .load(it)
                        .fit()
                        .centerCrop()
                        .into(findViewById<ImageView>(R.id.userImageChat))
                    retrieveMessages(auth!!.uid, userIdMessage, user.getAvatarSrc().toString())
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })

        findViewById<ImageView>(R.id.send_message_button).setOnClickListener {
            val message = findViewById<EditText>(R.id.chat_message).text.toString()
            if (message == "") {
                showDialog("Cannot send a empty message!")
            } else {
                sendMessageToUser(auth.uid, userIdMessage, message)
            }
            findViewById<EditText>(R.id.chat_message).setText("")
        }

        findViewById<ImageView>(R.id.attach_image_file_button).setOnClickListener {
            val intent = Intent()
            intent.action = Intent.ACTION_GET_CONTENT
            intent.type = "image/*"
            startActivityForResult(Intent.createChooser(intent,"Pick Image"), 438)
        }

        seenMessage(userIdMessage)

        findViewById<ImageView>(R.id.chatGoBack).setOnClickListener {
            finish()
        }
    }

    private fun showDialog(s: String){
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show()
    }

    private fun sendMessageToUser(senderID: String, receiverID: String, message: String){
        val reference = FirebaseDatabase.getInstance().reference
        val messageKey = reference.push().key

        val messageObject = Chat(
            senderID,
            message,
            receiverID,
            false,
            "",
            messageKey!!
        )
        reference.child("chats")
            .child(messageKey!!)
            .setValue(messageObject)
            .addOnCompleteListener{ task ->
                if (task.isSuccessful){
                    val chatListReference = FirebaseDatabase.getInstance()
                        .reference
                        .child("chatLists")
                        .child(auth!!.uid)
                        .child(userIdMessage)
                    chatListReference.addListenerForSingleValueEvent(object : ValueEventListener{
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (!snapshot.exists()){
                                chatListReference.child("id").setValue(userIdMessage)
                            }
                            val chatListReceiverReference = FirebaseDatabase.getInstance()
                                .reference
                                .child("chatLists")
                                .child(userIdMessage)
                                .child(auth!!.uid)
                            chatListReceiverReference.child("id").setValue(auth!!.uid)
                        }
                        override fun onCancelled(error: DatabaseError) {
                        }
                    })
                }
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == 438 && resultCode == RESULT_OK && data!=null && data!!.data!=null){
            val loadingBar = ProgressDialog(this)
            loadingBar.setMessage("Uploading image...")
            loadingBar.show()
            val fileUri = data.data
            val storageReference = FirebaseStorage.getInstance().reference.child("Chat Images")
            val ref = FirebaseDatabase.getInstance().reference
            val messageID = ref.push().key
            val filePath = storageReference.child("$messageID.png")

            val uploadTask: StorageTask<*>
            uploadTask = filePath.putFile(fileUri!!)

            uploadTask.continueWithTask<Uri?>(Continuation <UploadTask.TaskSnapshot, Task<Uri>>{ task ->
                if(!task.isSuccessful) {
                    task.exception?.let {
                        throw it
                    }
                }
                return@Continuation filePath.downloadUrl
            }).addOnCompleteListener{task ->
                if (task.isSuccessful){
                    val downloadUrl = task.result
                    val url = downloadUrl.toString()

                    val messageObject = Chat(
                        auth!!.uid,
                        "<Image>",
                        userIdMessage,
                        false,
                        url,
                        messageID!!
                    )

                    ref.child("chats").child(messageID!!).setValue(messageObject)
                        .addOnCompleteListener{ task ->
                            if(task.isSuccessful){
                                loadingBar.dismiss()
                                val reference = FirebaseDatabase.getInstance()
                                    .reference
                                    .child("users")
                                    .child(auth!!.uid)
                            }
                        }
                    loadingBar.dismiss()
                }
            }
        }
    }

    private fun retrieveMessages(senderID: String?, receiverID: String?, receiverImageUrl: String?) {
        mChatList = ArrayList()
        val ref = FirebaseDatabase.getInstance().reference.child("chats")

        ref.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(p0: DataSnapshot) {
                (mChatList as ArrayList<Chat>).clear()
                for (snapshot in p0.children){
                    val chat = snapshot.getValue(Chat::class.java)
                    if(chat!!.getReceiver().equals(senderID) && chat.getSender().equals(receiverID)
                        || chat.getReceiver().equals(receiverID) && chat.getSender().equals(senderID)){
                        (mChatList as ArrayList<Chat>).add(chat)
                    }
                    chatAdapter = ChatAdapter(this@ChatMessagesActivity, (mChatList as ArrayList<Chat>), receiverImageUrl!!)
                    chatList.adapter = chatAdapter
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    private fun seenMessage(userID: String){
        val ref = FirebaseDatabase.getInstance().reference.child("chats")
        seenListener = ref!!.addValueEventListener(object : ValueEventListener{
            override fun onCancelled(error: DatabaseError) {

            }

            override fun onDataChange(snapshot: DataSnapshot) {
                for (dataSnapshot in snapshot.children){
                    val chat = dataSnapshot.getValue(Chat::class.java)
                    if(chat!!.getReceiver().equals(auth!!.uid) && chat!!.getSender().equals(userID)){
                        val hashMap = HashMap<String, Any>()
                        hashMap["isseen"] = true
                        dataSnapshot.ref.updateChildren(hashMap)
                    }
                }
            }
        })
    }

    override fun onPause() {
        super.onPause()
        reference.removeEventListener(seenListener!!)
    }
}
