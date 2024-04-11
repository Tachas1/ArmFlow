package com.example.testarmflow.CompClassesMap

import android.content.Context
import android.content.DialogInterface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.example.testarmflow.R
import com.google.firebase.database.FirebaseDatabase


class EventAdapter(mContext: Context, mEvents: List<Events>) : RecyclerView.Adapter<EventAdapter.ViewHolder?>() {
    private val mContext: Context
    private val mEvents: List<Events>

    init {
        this.mContext = mContext
        this.mEvents = mEvents
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventAdapter.ViewHolder {
        val view: View = LayoutInflater.from(mContext).inflate(R.layout.event_item_layout, parent, false)
        return EventAdapter.ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mEvents.size
    }

    override fun onBindViewHolder(holder: EventAdapter.ViewHolder, position: Int) {
        val event: Events = mEvents[position]
        holder.headLine.text = event.getHeadLine()
        holder.address.text = ("${event.getAddress()}")
        holder.addressTwo.text = ("${event.getAddressTwo()}")
        holder.latitude.text = ("${event.getLatitude()}")
        holder.longitude.text = ("${event.getLongitude()}")

        holder.itemView.bringToFront()
        holder.remove.setOnClickListener{
            val options = arrayOf<CharSequence>(
                "Yes",
                "No"
            )
            var builder: AlertDialog.Builder = AlertDialog.Builder(holder.itemView.context)
            builder.setTitle("Are you sure you want to delete the event?")
            builder.setItems(options, DialogInterface.OnClickListener{
                    _, which ->
                if(which == 0){
                    deleteEvent(position, holder)
                }
            })
            builder.show()
        }
    }

    private fun deleteEvent(position: Int, holder: EventAdapter.ViewHolder){
        val ref = FirebaseDatabase.getInstance().reference.child("events")
            .child(mEvents[position].getEventID()!!)
            .removeValue()
            .addOnCompleteListener { task ->
                if(task.isSuccessful){
                    showDialog("Wydarzenie zostało usunięte!")
                }
                else{
                    showDialog("Błąd podczas usuwania wydarzenia!")
                }
            }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        var headLine: TextView
        var address: TextView
        var addressTwo: TextView
        var latitude: TextView
        var longitude: TextView
        var remove: ImageView

        init {
            headLine = itemView.findViewById(R.id.myEventHeadLine)
            address = itemView.findViewById(R.id.myEventAddress)
            addressTwo = itemView.findViewById(R.id.myEventAddressTwo)
            latitude = itemView.findViewById(R.id.myEventLatitude)
            longitude = itemView.findViewById(R.id.myEventLongitude)
            remove = itemView.findViewById(R.id.myEventRemove)
        }
    }

    private fun showDialog(s: String){
        Toast.makeText(mContext, s, Toast.LENGTH_SHORT).show()
    }
}