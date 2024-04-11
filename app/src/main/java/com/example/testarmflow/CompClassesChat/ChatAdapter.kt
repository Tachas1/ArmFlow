package com.example.testarmflow.CompClassesChat

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.example.testarmflow.R
import com.example.testarmflow.chatFrag.ViewImageActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class ChatAdapter(
    mContext: Context,
    mChatList: List<Chat>,
    imageUrl: String
): RecyclerView.Adapter<ChatAdapter.ViewHolder?>() {

    private val mContext: Context
    private val mChatList: List<Chat>
    private val imageUrl: String
    var auth: FirebaseUser? = FirebaseAuth.getInstance().currentUser!!

    init{
        this.mContext = mContext
        this.mChatList = mChatList
        this.imageUrl = imageUrl
    }

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        var profile_image: CircleImageView? = null
        var text_message: TextView? = null
        var left_image_view: ImageView? = null
        var message_seen: TextView? = null
        var right_image_view: ImageView? = null

        init {
            profile_image = itemView.findViewById(R.id.profile_image)
            text_message = itemView.findViewById(R.id.text_message)
            left_image_view = itemView.findViewById(R.id.left_image_view)
            message_seen = itemView.findViewById(R.id.message_seen)
            right_image_view = itemView.findViewById(R.id.right_image_view)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, position: Int): ViewHolder {
        return if (position == 1){
            val view: View = LayoutInflater.from(mContext).inflate(R.layout.message_item_right, parent, false)
            ViewHolder(view)
        }
        else {
            val view: View = LayoutInflater.from(mContext).inflate(R.layout.message_item_left, parent, false)
            ViewHolder(view)
        }
    }

    override fun getItemCount(): Int {
        return mChatList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val chat: Chat = mChatList[position]
        Picasso.get().load(imageUrl).into(holder.profile_image)
        // images messages
        if (chat.getMessage().equals("<Image>") && !chat.getUrl().equals("")){
            // right side
            if (chat.getSender().equals(auth!!.uid)){
                holder.text_message!!.visibility = View.GONE
                holder.right_image_view!!.visibility = VISIBLE
                Picasso.get().load(chat.getUrl()).into(holder.right_image_view)

                holder.right_image_view!!.setOnClickListener {
                    val options = arrayOf<CharSequence>(
                        "View full image",
                        "Delete image",
                        "Cancel"
                    )
                    var builder: AlertDialog.Builder = AlertDialog.Builder(holder.itemView.context)
                    builder.setTitle("Select operation:")
                    builder.setItems(options, DialogInterface.OnClickListener{
                        _, which ->
                        if(which == 0){
                            val intent = Intent(mContext, ViewImageActivity::class.java)
                            intent.putExtra("url", chat.getUrl())
                            mContext.startActivity(intent)
                        }
                        else if(which == 1){
                            deleteImageMessage(position, holder)
                        }
                    })
                    builder.show()
                }
            }
            // left side
            else if(!chat.getSender().equals(auth!!.uid)){
                holder.text_message!!.visibility = View.GONE
                holder.left_image_view!!.visibility = VISIBLE
                Picasso.get().load(chat.getUrl()).into(holder.left_image_view)

                holder.left_image_view!!.setOnClickListener {
                    val options = arrayOf<CharSequence>(
                        "View full image",
                        "Cancel"
                    )
                    var builder: AlertDialog.Builder = AlertDialog.Builder(holder.itemView.context)
                    builder.setTitle("Select operation:")
                    builder.setItems(options, DialogInterface.OnClickListener{
                            _, which ->
                        if(which == 0){
                            val intent = Intent(mContext, ViewImageActivity::class.java)
                            intent.putExtra("url", chat.getUrl())
                            mContext.startActivity(intent)
                        }
                    })
                    builder.show()
                }
            }
        }
        // text messages
        else{
            holder.text_message!!.text = chat.getMessage()

            if(auth!!.uid == chat.getSender()){
                holder.text_message!!.setOnClickListener {
                    val options = arrayOf<CharSequence>(
                        "Delete message",
                        "Cancel"
                    )
                    var builder: AlertDialog.Builder = AlertDialog.Builder(holder.itemView.context)
                    builder.setTitle("Select operation:")
                    builder.setItems(options, DialogInterface.OnClickListener{
                            _, which ->
                        if(which == 0){
                            deleteSentMessage(position, holder)
                        }
                    })
                    builder.show()
                }
            }
        }
        // sent and seen features
        if(position == mChatList.size-1){
            if(chat.getIsSeen()){
                holder.message_seen!!.text = "Seen"
                if(chat.getMessage().equals("<Image>") && !chat.getUrl().equals("")){
                    val layParam: RelativeLayout.LayoutParams? = holder.message_seen!!.layoutParams as RelativeLayout.LayoutParams?
                    layParam!!.setMargins(0, 245, 10, 0)
                    holder.message_seen!!.layoutParams = layParam
                }
            }
            else{
                holder.message_seen!!.text = "Sent"
                if(chat.getMessage().equals("<Image>") && !chat.getUrl().equals("")){
                    val layParam: RelativeLayout.LayoutParams? = holder.message_seen!!.layoutParams as RelativeLayout.LayoutParams?
                    layParam!!.setMargins(0, 245, 10, 0)
                    holder.message_seen!!.layoutParams = layParam
                }
            }
        }
        else{
            holder.message_seen!!.visibility = View.GONE
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if(mChatList[position].getSender().equals(auth!!.uid)){
            1
        }
        else{
            0
        }

    }

    private fun deleteSentMessage(position: Int, holder: ChatAdapter.ViewHolder){
        val ref = FirebaseDatabase.getInstance().reference.child("chats")
            .child(mChatList[position].getMessageID()!!)
            .removeValue()
            .addOnCompleteListener { task ->
                if(task.isSuccessful){
                    Toast.makeText(holder.itemView.context, "Message deleted!", Toast.LENGTH_SHORT).show()
                }
                else{
                    Toast.makeText(holder.itemView.context, "Deleting operation failed!", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun deleteImageMessage(position: Int, holder: ChatAdapter.ViewHolder){
        val ref = FirebaseDatabase.getInstance().reference.child("chats")
            .child(mChatList[position].getMessageID()!!)
            .removeValue()

        val storageReference = FirebaseStorage.getInstance().reference.child("Chat Images")
            .child(mChatList[position].getMessageID()!! + ".png")
            .delete()
            .addOnCompleteListener{ task ->
                if(task.isSuccessful){
                    Toast.makeText(holder.itemView.context, "Image deleted!", Toast.LENGTH_SHORT).show()
                }
                else{
                    Toast.makeText(holder.itemView.context, "Deleting operation failed!", Toast.LENGTH_SHORT).show()
                }
            }
    }
}