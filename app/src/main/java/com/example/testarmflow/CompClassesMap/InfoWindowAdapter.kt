package com.example.testarmflow.CompClassesMap

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.example.testarmflow.CompClassesChat.Users
import com.google.android.gms.maps.GoogleMap
import com.example.testarmflow.R
import com.google.android.gms.maps.model.Marker
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.auth.User
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class InfoWindowAdapter(mContext: Context, mEvents: List<Events>, mUser: List<Users>) : GoogleMap.InfoWindowAdapter {
    private val mContext: Context
    private val mEvents: List<Events>
    private val mUser: List<Users>

    init {
        this.mContext = mContext
        this.mEvents = mEvents
        this.mUser = mUser
    }

    override fun getInfoContents(marker: Marker): View? {
        return null
    }

    override fun getInfoWindow(marker: Marker): View? {
        val tag = marker.tag
        val event = mEvents.find { it.getEventID() == tag }
        val user = mUser.find { it.getUID() == event!!.getUID() }
        val infoView: View = LayoutInflater.from(mContext).inflate(R.layout.custom_marker_info, null)
        val headLine = infoView.findViewById<TextView>(R.id.markerHeadLine)
        val description = infoView.findViewById<TextView>(R.id.markerDescription)
        val lat = infoView.findViewById<TextView>(R.id.markerLatitude)
        val long = infoView.findViewById<TextView>(R.id.markerLongitude)
        val addressOne = infoView.findViewById<TextView>(R.id.markerAddressOne)
        val addressTwo = infoView.findViewById<TextView>(R.id.markerAddressTwo)
        val nickName = infoView.findViewById<TextView>(R.id.markerCreator)


        headLine.text = event!!.getHeadLine()
        description.text = event.getDescription()
        lat.text = event.getLatitude().toString()
        long.text = event.getLongitude().toString()
        addressOne.text = event.getAddress()
        addressTwo.text = event.getAddressTwo()
        nickName.text = "By: ${user!!.getNickName()}"
        Log.e("Dupa","${user.getAvatarSrc()}")


        return infoView
    }
}