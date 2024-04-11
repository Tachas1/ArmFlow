package com.example.testarmflow.CompClassesChat

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.testarmflow.chatFrag.ChatMessagesActivity
import com.example.testarmflow.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class UserAdapter(mContext: Context, mUsers: List<Users>, isChatted: Boolean) : RecyclerView.Adapter<UserAdapter.ViewHolder?>()
{
    private val mContext: Context
    private val mUsers: List<Users>
    private val isChatted: Boolean
    private lateinit var lastMassage: String

    init {
        this.mContext = mContext
        this.mUsers = mUsers
        this.isChatted = isChatted
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = LayoutInflater.from(mContext).inflate(R.layout.search_user_layout, parent, false)
        return UserAdapter.ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mUsers.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user: Users = mUsers[position]
        holder.nickNameTxt.text = user!!.getNickName()
        val storage = Firebase.storage
        Picasso.get().load(user.getAvatarSrc()).placeholder(R.drawable.avatar_placeholder).into(holder.avatarSrc)

        if(isChatted){
            retrieveLastMessage(user.getUID(), holder.lastMessageTxt)
            if(user.getStatus() == "online"){
                holder.onlineCircleSrc.visibility = View.VISIBLE
            }
            else{
                holder.onlineCircleSrc.visibility = View.GONE
            }
        }
        else{
            holder.lastMessageTxt.visibility = View.GONE
            holder.onlineCircleSrc.visibility = View.GONE
        }


        holder.itemView.bringToFront()
        holder.itemView.setOnClickListener{
            showDialog("Its work!")
                    val intent = Intent(mContext, ChatMessagesActivity::class.java)
                    intent.putExtra("id", user.getUID())
                    mContext.startActivity(intent)
        }
    }

    private fun retrieveLastMessage(chatUserID: String?, lastMessageTxt: TextView) {
        lastMassage = "default"

        val auth = FirebaseAuth.getInstance().currentUser
        val ref = FirebaseDatabase.getInstance().reference.child("chats")

        ref.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                for( dataSnapshot in snapshot.children){
                    val chat: Chat? = dataSnapshot.getValue(Chat::class.java)

                    if(auth != null && chat != null){
                        if(chat.getReceiver() == auth.uid && chat.getSender() == chatUserID
                            || chat.getReceiver() == chatUserID && chat.getSender() == auth.uid){
                            lastMassage = chat.getMessage()!!
                        }
                    }
                }
                when(lastMassage){
                    "default" -> lastMessageTxt.text = "No message"
                    else -> lastMessageTxt.text = lastMassage
                }
                lastMassage = "default"
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        var nickNameTxt: TextView
        var avatarSrc: CircleImageView
        var onlineCircleSrc: CircleImageView
        var lastMessageTxt: TextView

        init {
            nickNameTxt = itemView.findViewById(R.id.profile_name)
            avatarSrc = itemView.findViewById(R.id.profile_image)
            onlineCircleSrc = itemView.findViewById(R.id.profile_status_online)
            lastMessageTxt = itemView.findViewById(R.id.last_message)
        }
    }

    private fun showDialog(s: String){
        Toast.makeText(mContext, s, Toast.LENGTH_SHORT).show()
    }
}