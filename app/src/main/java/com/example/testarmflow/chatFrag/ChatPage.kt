package com.example.testarmflow.chatFrag

import android.content.Context
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.testarmflow.CompClassesChat.Chat
import com.example.testarmflow.CompClassesChat.ChatList
import com.example.testarmflow.CompClassesChat.UserAdapter
import com.example.testarmflow.CompClassesChat.Users
import com.example.testarmflow.R
import com.example.testarmflow.databinding.FragmentChatPageBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ChatPage : Fragment() {
    private lateinit var chatsButton: Button
    private lateinit var searchButton: Button
    private lateinit var chatListView: RecyclerView
    private var userAdapter: UserAdapter? = null
    private var mUsers: List<Users>? = null
    private var userChatList: List<ChatList>? = null
    private var auth: FirebaseUser? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val sharedPreferences1: SharedPreferences = requireContext().getSharedPreferences("moje_pref", Context.MODE_PRIVATE)
        val mt = sharedPreferences1.getBoolean("motyw", true)

        val motyw = if(mt){
            R.layout.fragment_chat_page_light
        }else{
            R.layout.fragment_chat_page
        }

        val view = inflater.inflate(motyw, container, false)

        chatsButton = view.findViewById(R.id.chats_button)
        searchButton = view.findViewById(R.id.search_button)
        chatListView = view.findViewById(R.id.chatListView)
        chatListView.setHasFixedSize(true)
        chatListView.layoutManager = LinearLayoutManager(context)

        chatsButton.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.crimson))

        chatsButton.setOnClickListener {
            showDialog("You're right here!")
        }

        searchButton.setOnClickListener {
            val fragmentSearch = SearchFragment()
            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            transaction.replace(R.id.chat_page_admin, fragmentSearch)
            transaction.setReorderingAllowed(true)
            transaction.addToBackStack("fragment_stack")
            transaction.commit()
        }

        auth = FirebaseAuth.getInstance().currentUser
        userChatList = ArrayList()
        val ref = FirebaseDatabase.getInstance().reference.child("chatLists").child(auth!!.uid)
        ref.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                (userChatList as ArrayList).clear()

                for (snapshot in dataSnapshot.children){
                    val chatList = snapshot.getValue(ChatList::class.java)

                    (userChatList as ArrayList).add(chatList!!)
                }
                retrieveChatList()
            }

            override fun onCancelled(error: DatabaseError) {
                // Obsługa błędu
            }
        })

        val refChats = FirebaseDatabase.getInstance().reference.child("chats")
        refChats.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                var countUnreadMessages = 0

                for(dataSnapshot in snapshot.children){
                    val chat = dataSnapshot.getValue(Chat::class.java)
                    if(chat!!.getReceiver().equals(auth!!.uid) && !chat.getIsSeen()){
                        countUnreadMessages += 1
                    }
                }
                if(countUnreadMessages == 0){
                    chatsButton.text = "Chats"
                }
                else {
                    chatsButton.text = "Chats ($countUnreadMessages)"
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Obsługa błędu
            }
        })

        return view
    }

    private fun showDialog(s: String){
        Toast.makeText(activity, s, Toast.LENGTH_SHORT).show()
    }

    private fun retrieveChatList(){
        mUsers = ArrayList()
        val ref = FirebaseDatabase.getInstance().reference.child("users")
        ref.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                (mUsers as ArrayList).clear()

                for (snapshot in dataSnapshot.children){
                    val user = snapshot.getValue(Users::class.java)

                    for (eachChatList in userChatList!!){
                        if(user!!.getUID().equals(eachChatList.getId())){
                            (mUsers as ArrayList).add(user!!)
                        }
                    }
                }
                if (isAdded) {
                    userAdapter = UserAdapter(requireContext(), (mUsers as ArrayList<Users>), true)
                    chatListView.adapter = userAdapter
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Obsługa błędu
            }
        })
    }
}

