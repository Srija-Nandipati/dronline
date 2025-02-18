package com.example.doctoronline

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.DatePicker
import android.widget.ImageView
import android.widget.Toast
import com.example.doctoronline.databinding.ActivityFindDoctosScreenBinding
import com.example.doctoronline.databinding.DialogProgressBinding
import com.example.doctoronline.databinding.PaymentBottomsheetBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.toObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class FindDoctosScreen : AppCompatActivity() {
    private val binding by lazy { ActivityFindDoctosScreenBinding.inflate(layoutInflater) }
    private lateinit var fireStore: FirebaseFirestore
    private lateinit var doctorList: ArrayList<String>
    private lateinit var timeSlot: ArrayList<String>
    private lateinit var progressDialog: AlertDialog
    private val calendar = Calendar.getInstance()
    private var selectedButton: Button? = null
    private var selectedAge: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.backBtn.setOnClickListener{
            onBackPressed()
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val window: Window = window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = resources.getColor(R.color.white, theme)
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }

        fireStore = FirebaseFirestore.getInstance()

        progressDialog = AlertDialog.Builder(this, R.style.CustomAlertDialog).create()
        val progressDialogLay = DialogProgressBinding.inflate(LayoutInflater.from(this))
        progressDialog.setView(progressDialogLay.root)
        progressDialog.setCanceledOnTouchOutside(false)
        progressDialog.show()

        getDoctors()
        getTimeSlot()

        binding.spinnerSelectDoctor.setOnClickListener {
            showSpinnerList(doctorList, binding.spinnerSelectDoctor)
        }

        binding.spinnerTimeSlot.setOnClickListener {
            showSpinnerList(timeSlot, binding.spinnerTimeSlot)
        }

        binding.spinnerDatePicker.setOnClickListener {
            showDatePicker { selectedCalendar ->
                updateSelectedDate(selectedCalendar)
            }
        }



        binding.btn010.setOnClickListener {
            onAgeButtonClicked(it as Button)
        }

        binding.btn010.setOnClickListener {
            onAgeButtonClicked(it as Button)
        }

        binding.btn010.setOnClickListener {
            onAgeButtonClicked(it as Button)
        }

        binding.btn1030.setOnClickListener {
            onAgeButtonClicked(it as Button)
        }

        binding.btn3050.setOnClickListener {
            onAgeButtonClicked(it as Button)
        }

        binding.btn75.setOnClickListener {
            onAgeButtonClicked(it as Button)
        }


        binding.buttonPayment.setOnClickListener {
            if(binding.spinnerSelectDoctor.text.isEmpty()) {
                binding.spinnerSelectDoctor.requestFocus()
                binding.spinnerSelectDoctor.error = "Empty"
            } else if(binding.spinnerDatePicker.text.isNullOrEmpty()) {
                binding.spinnerDatePicker.requestFocus()
                binding.spinnerDatePicker.error = "Empty"
            } else if(binding.spinnerTimeSlot.text.isEmpty()) {
                binding.spinnerTimeSlot.requestFocus()
                binding.spinnerTimeSlot.error = "Empty"
            } else if(selectedAge == null) {
                Toast.makeText(this, "Please Select Age Group", Toast.LENGTH_SHORT).show()
            } else {
                showPaymentSheet()
            }
        }
    }


    private fun onAgeButtonClicked(clickedButton: Button) {

        if (clickedButton == selectedButton) {
            selectedButton?.setBackgroundResource(R.drawable.edit_bg_small)
            selectedButton?.setTextColor(getColor(R.color.black))
            selectedButton = null
        } else {
            selectedButton?.setBackgroundResource(R.drawable.edit_bg_small)
            selectedButton?.setTextColor(getColor(R.color.black))
            clickedButton.setBackgroundResource(R.drawable.selected_bg)
            clickedButton.setTextColor(getColor(R.color.green))
            selectedButton = clickedButton
            selectedAge = clickedButton.text.toString()
        }
    }

    private fun showPaymentSheet() {
        val dialog = BottomSheetDialog(this)

        val bottomSheetLayout = PaymentBottomsheetBinding.inflate(layoutInflater)
        bottomSheetLayout.buttonClose.setOnClickListener {
            dialog.dismiss()
        }

        bottomSheetLayout.googlepayLay.setOnClickListener {
            goPayment()
        }

        bottomSheetLayout.phonepeLay.setOnClickListener {
            goPayment()
        }

        bottomSheetLayout.paytmLay.setOnClickListener {
            goPayment()
        }

        dialog.setCancelable(true)
        dialog.setContentView(bottomSheetLayout.root)
        dialog.show()
    }

    private fun goPayment() {
        val intent = Intent(this, PaymentScreen::class.java)
        intent.putExtra("doctor", binding.spinnerSelectDoctor.text.toString())
        intent.putExtra("date", binding.spinnerDatePicker.text.toString())
        intent.putExtra("time", binding.spinnerTimeSlot.text.toString())
        intent.putExtra("ageGroup", selectedAge)
        intent.putExtra("appointment", true)
        startActivity(intent)
    }
    private fun showDatePicker(callback: (Calendar) -> Unit) {
        val datePickerDialog = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                val selectedCalendar = Calendar.getInstance()
                selectedCalendar.set(Calendar.YEAR, year)
                selectedCalendar.set(Calendar.MONTH, month)
                selectedCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                if (selectedCalendar.timeInMillis >= calendar.timeInMillis) {
                    callback.invoke(selectedCalendar)
                } else {

                    Toast.makeText(this, "Select valid date", Toast.LENGTH_SHORT).show()
                }
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        datePickerDialog.datePicker.minDate = System.currentTimeMillis()

        datePickerDialog.show()
    }

    private fun showSpinnerList(list: List<String>, autoCompleteTextView: AutoCompleteTextView) {
        val arrayAdopter = ArrayAdapter(this, R.layout.dropdown_item, list)
        autoCompleteTextView.setAdapter(arrayAdopter)

    }


    private fun getDoctors() {
        doctorList = ArrayList()
        fireStore.collection("DoctorList").get().addOnSuccessListener {
            doctorList.clear()
            for(document in it.documents) {
                val doctor = document.getString("doctor")
                doctorList.add(doctor!!)

            }
            val arrayAdopter = ArrayAdapter(this, R.layout.dropdown_item, doctorList)
            binding.spinnerSelectDoctor.setAdapter(arrayAdopter)

        }.addOnFailureListener {
            progressDialog.dismiss()
            Toast.makeText(this, it.localizedMessage, Toast.LENGTH_SHORT).show()
        }

    }

    private fun updateSelectedDate(calendar: Calendar) {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val selectedDate = dateFormat.format(calendar.time)
        binding.spinnerDatePicker.setText(selectedDate)

    }

    private fun getTimeSlot() {
        timeSlot = ArrayList()
        fireStore.collection("TimeSlot").orderBy("time",Query.Direction.ASCENDING).get().addOnSuccessListener {
            timeSlot.clear()
            for(doc in it.documents) {
                val time = doc.getString("time")
                timeSlot.add(time!!)
            }
            val arrayAdopter = ArrayAdapter(this, R.layout.dropdown_item, timeSlot)
            binding.spinnerTimeSlot.setAdapter(arrayAdopter)
            progressDialog.dismiss()
        }.addOnFailureListener {
            progressDialog.dismiss()
            Toast.makeText(this, it.localizedMessage, Toast.LENGTH_SHORT).show()
        }


    }



}