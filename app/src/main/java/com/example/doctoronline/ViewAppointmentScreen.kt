package com.example.doctoronline

import android.app.AlertDialog
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import coil.load
import com.example.doctoronline.adapter.ViewAppointmentAdapter
import com.example.doctoronline.databinding.ActivityViewAppointmentScreenBinding
import com.example.doctoronline.databinding.DialogProgressBinding
import com.example.doctoronline.model.AppointmentModel
import com.example.doctoronline.utils.Utils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject

class ViewAppointmentScreen : AppCompatActivity() {
    private val binding by lazy { ActivityViewAppointmentScreenBinding.inflate(layoutInflater) }
    private lateinit var appointmentList: ArrayList<AppointmentModel>
    private lateinit var db: FirebaseFirestore
    private lateinit var progressDialog: AlertDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        if (Utils.getUserData(this)?.userImage != "") {
            binding.imageEmail.load(Utils.getUserData(this)?.userImage)
        } else {
            binding.imageEmail.setImageResource(R.drawable.placeholder)
        }
        binding.tvLogin.text = "Hello " + Utils.getUserData(this)?.name

        binding.backBtn.setOnClickListener {
            onBackPressed()
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val window: Window = window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = resources.getColor(R.color.green, theme)
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }

        db = FirebaseFirestore.getInstance()

        progressDialog = AlertDialog.Builder(this, R.style.CustomAlertDialog).create()
        val progressDialogLay = DialogProgressBinding.inflate(LayoutInflater.from(this))
        progressDialog.setView(progressDialogLay.root)
        progressDialog.setCanceledOnTouchOutside(false)
        progressDialog.show()

        getAppointments(FirebaseAuth.getInstance().currentUser!!.uid)

    }

    private fun getAppointments(uid: String) {
        appointmentList = ArrayList()
        db.collection("AppointmentList").document(uid).collection("Appointment").get()
            .addOnSuccessListener { appointments ->
                for (appointment in appointments) {
                    val appoint = appointment.toObject<AppointmentModel>()
                    appointmentList.add(appoint)
                }
                binding.appointmentRv.adapter = ViewAppointmentAdapter(this, appointmentList)
                progressDialog.dismiss()

                if (appointmentList.isEmpty()) {
                    binding.appointmentRv.visibility = View.GONE
                    binding.bookingStatus.text = "No Appointment Booked"
                    binding.bookingStatus.visibility = View.VISIBLE
                } else {
                    binding.appointmentRv.visibility = View.VISIBLE
                    binding.bookingStatus.visibility = View.GONE
                }
            }.addOnFailureListener {
                progressDialog.dismiss()
                Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show()
            }
    }


}