package com.example.doctoronline.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.doctoronline.databinding.ItemAppointmentBookedBinding
import com.example.doctoronline.model.AppointmentModel

class ViewAppointmentAdapter(val context: Context, private val appointmentList: ArrayList<AppointmentModel>):
RecyclerView.Adapter<ViewAppointmentAdapter.ViewAppointmentVH>(){
    inner class ViewAppointmentVH(val binding: ItemAppointmentBookedBinding):RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewAppointmentVH {
       val binding = ItemAppointmentBookedBinding.inflate(LayoutInflater.from(context), parent, false)
        return ViewAppointmentVH(binding)
    }

    override fun getItemCount(): Int {
        return appointmentList.size
    }

    override fun onBindViewHolder(holder: ViewAppointmentVH, position: Int) {
        val item = appointmentList[position]
        holder.binding.tvDate.text = item.date
        holder.binding.tvDoctor.text = item.doctorType
        holder.binding.tvTime.text = item.time
    }
}