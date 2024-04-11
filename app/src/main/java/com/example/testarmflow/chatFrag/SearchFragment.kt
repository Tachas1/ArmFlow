package com.example.testarmflow.chatFrag

import android.content.Context
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.testarmflow.CompClassesChat.UserAdapter
import com.example.testarmflow.CompClassesChat.Users
import com.example.testarmflow.R
import com.example.testarmflow.databinding.FragmentSearchBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class SearchFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var userAdapter: UserAdapter
    private lateinit var mUsers: List<Users>
    private lateinit var searchUserBar: EditText

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val sharedPreferences1: SharedPreferences = requireContext().getSharedPreferences("moje_pref", Context.MODE_PRIVATE)
        val mt = sharedPreferences1.getBoolean("motyw", true)

        val motyw = if(mt){
            R.layout.fragment_search_light
        }else{
            R.layout.fragment_search
        }

        val view = inflater.inflate(motyw, container, false)

        val searchButton: Button = view.findViewById(R.id.search_button)
        val chatsButton: Button = view.findViewById(R.id.chats_button)
        searchUserBar = view.findViewById(R.id.searchUserBar)
        recyclerView = view.findViewById(R.id.searchList)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(context)

        mUsers = ArrayList()
        retrieveAllUsers()

        searchUserBar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                searchUsers(p0.toString().lowercase())
            }

            override fun afterTextChanged(p0: Editable?) {}
        })

        chatsButton.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack(
                "fragment_stack",
                FragmentManager.POP_BACK_STACK_INCLUSIVE
            )
        }

        searchButton.setOnClickListener {
            showDialog("You're right here!")
        }

        return view
    }

    private fun retrieveAllUsers() {
        val userUID = FirebaseAuth.getInstance().currentUser!!.uid
        val refUsers = FirebaseDatabase.getInstance().reference.child("users")

        refUsers.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(param: DataSnapshot) {
                if (isAdded) {
                    (mUsers as ArrayList<Users>).clear()
                    if (searchUserBar.text.toString() == "") {
                        for (snapshot in param.children) {
                            val user: Users? = snapshot.getValue(Users::class.java)
                            if (!user!!.getUID().equals(userUID)) {
                                (mUsers as ArrayList<Users>).add(user)
                            }
                        }
                    }
                    userAdapter = UserAdapter(requireContext(), mUsers, false)
                    recyclerView.adapter = userAdapter
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun showDialog(s: String) {
        Toast.makeText(requireContext(), s, Toast.LENGTH_SHORT).show()
    }

    private fun searchUsers(str: String) {
        val userUID = FirebaseAuth.getInstance().currentUser!!.uid
        val queryUsers = FirebaseDatabase.getInstance().reference
            .child("users")
            .orderByChild("nicknameLC")
            .startAt("$str")
            .endAt("$str\uf8ff")

        queryUsers.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(param: DataSnapshot) {
                (mUsers as ArrayList<Users>).clear()
                for (snapshot in param.children) {
                    val user: Users? = snapshot.getValue(Users::class.java)
                    if (!user!!.getUID().equals(userUID)) {
                        (mUsers as ArrayList<Users>).add(user)
                    }
                }
                userAdapter = UserAdapter(requireContext(), mUsers, false)
                recyclerView.adapter = userAdapter
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }
}
