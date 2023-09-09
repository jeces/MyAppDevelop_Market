package com.example.applicationjeces.product

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.applicationjeces.R

class NotificationAdapter(private var notifications: List<Notification>) : RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>() {

    inner class NotificationViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val notificationText: TextView = view.findViewById(R.id.notificationtext)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.notification_item, parent, false)
        return NotificationViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        val notification = notifications[position]
        holder.notificationText.text = notification.message
        Log.d("adad111", notification.message)
    }

    override fun getItemCount(): Int {
        return return if (notifications.size > 10) 10 else notifications.size
    }

    fun setNotifications(newNotifications: List<Notification>) {
        this.notifications = newNotifications
        notifyDataSetChanged()
    }
}
