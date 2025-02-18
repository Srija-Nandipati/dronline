package com.example.doctoronline.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.doctoronline.BuyMedicineScreen
import com.example.doctoronline.FindDoctosScreen
import com.example.doctoronline.R
import com.example.doctoronline.databinding.ItemDiseaseBinding
import com.example.doctoronline.model.DiseaseModel

class DiseaseAdopter(val context: Context, private val diseaseList: ArrayList<DiseaseModel>):
    RecyclerView.Adapter<DiseaseAdopter.DiseaseVH>() {
        inner class DiseaseVH(val binding: ItemDiseaseBinding):RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DiseaseVH {
        val binding = ItemDiseaseBinding.inflate(LayoutInflater.from(context), parent, false)
        return DiseaseVH(binding)
    }

    override fun getItemCount(): Int {
        return diseaseList.size
    }

    override fun onBindViewHolder(holder: DiseaseVH, position: Int) {
        val item = diseaseList[position]
        holder.binding.tvDisease.text = item.diseaseTitle
        holder.binding.tvCausesExpandText.text = item.diseaseCauses
        holder.binding.tvSymptomsExpandText.text = item.diseaseSymptoms
        holder.binding.tvTreatmentsExpandText.text = item.diseaseTreatments

        holder.binding.ivDiseaseExpand.setOnClickListener {
            if(holder.binding.diseaseExpandLayout.visibility == View.GONE) {
                holder.binding.diseaseExpandLayout.visibility = View.VISIBLE
                holder.binding.ivdropDown.setImageResource(R.drawable.up_arrow)
            } else {
                holder.binding.diseaseExpandLayout.visibility = View.GONE
                holder.binding.ivdropDown.setImageResource(R.drawable.icon_dropdown)
            }
        }

        holder.binding.ivSymptomsExpand.setOnClickListener {
            if(holder.binding.tvSymptomsExpandText.visibility == View.GONE) {
                holder.binding.tvSymptomsExpandText.visibility = View.VISIBLE
                holder.binding.ivSymptomsExpand.setImageResource(R.drawable.up_arrow)
            } else {
                holder.binding.tvSymptomsExpandText.visibility = View.GONE
                holder.binding.ivSymptomsExpand.setImageResource(R.drawable.icon_dropdown)
            }
        }

        holder.binding.ivCausesExpand.setOnClickListener {
            if(holder.binding.tvCausesExpandText.visibility == View.GONE) {
                holder.binding.tvCausesExpandText.visibility = View.VISIBLE
                holder.binding.ivCausesExpand.setImageResource(R.drawable.up_arrow)
            } else {
                holder.binding.tvCausesExpandText.visibility = View.GONE
                holder.binding.ivCausesExpand.setImageResource(R.drawable.icon_dropdown)
            }
        }

        holder.binding.ivTreatmentsExpand.setOnClickListener {
            if(holder.binding.tvTreatmentsExpandText.visibility == View.GONE) {
                holder.binding.tvTreatmentsExpandText.visibility = View.VISIBLE
                holder.binding.ivTreatmentsExpand.setImageResource(R.drawable.up_arrow)
            } else {
                holder.binding.tvTreatmentsExpandText.visibility = View.GONE
                holder.binding.ivTreatmentsExpand.setImageResource(R.drawable.icon_dropdown)
            }
        }

        holder.binding.buttonAppointDoctor.setOnClickListener {
            context.startActivity(Intent(context, FindDoctosScreen::class.java))
        }

        holder.binding.buttonBuyMedicine.setOnClickListener {
            context.startActivity(Intent(context, BuyMedicineScreen::class.java))
        }
    }
}