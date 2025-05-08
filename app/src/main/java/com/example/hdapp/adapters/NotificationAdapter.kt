package com.example.hdapp.adapters

import android.os.Parcel
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.hdapp.databinding.NotificationItemBinding

class NotificationAdapter  (val nName : ArrayList<String>,val nImage : ArrayList<Int>) : RecyclerView.Adapter<NotificationAdapter.ViewHolder>(){

    inner class ViewHolder (val binding : NotificationItemBinding) : RecyclerView.ViewHolder(binding.root){
        fun bind(position: Int) {
            binding.apply {
                notificationText.text=nName[position]
                notificationImage.setImageResource(nImage[position])
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = NotificationItemBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = nName.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(position)

    }
}