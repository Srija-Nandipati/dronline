package com.example.doctoronline

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.doctoronline.databinding.ActivityPaymentScreenBinding
import com.example.doctoronline.databinding.DialogProgressBinding
import com.example.doctoronline.model.AppointmentModel
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID

class PaymentScreen : AppCompatActivity() {
    private val binding by lazy { ActivityPaymentScreenBinding.inflate(layoutInflater) }
    private var paymentScreenshotUri: Uri? = null
    private lateinit var storageRef: FirebaseStorage
    private lateinit var db: FirebaseFirestore
    private lateinit var progressDialog: AlertDialog

    private val startForProfileImageResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            val resultCode = result.resultCode
            val data = result.data

            when (resultCode) {
                Activity.RESULT_OK -> {

                    val fileUri = data?.data!!

                    paymentScreenshotUri = fileUri
                    binding.ivUserPaymentScreenShot.setImageURI(paymentScreenshotUri)

                    binding.paymentQrLayout.visibility = View.GONE
                    binding.userScreenShotLayout.visibility = View.VISIBLE
                    val screenShot = "Upload Screenshot"
                    binding.buttonUpload.text = screenShot
                }

                ImagePicker.RESULT_ERROR -> {
                    Toast.makeText(this, ImagePicker.getError(data), Toast.LENGTH_SHORT).show()
                }

                else -> {
                    Toast.makeText(this, "Task Cancelled", Toast.LENGTH_SHORT).show()
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.backBtn.setOnClickListener { onBackPressed() }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val window: Window = window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = resources.getColor(R.color.white, theme)
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }

        storageRef = FirebaseStorage.getInstance()
        db = FirebaseFirestore.getInstance()


        progressDialog = AlertDialog.Builder(this, R.style.CustomAlertDialog).create()
        val progressDialogLay = DialogProgressBinding.inflate(LayoutInflater.from(this))
        progressDialog.setView(progressDialogLay.root)
        progressDialog.setCanceledOnTouchOutside(false)

        val check = intent.getBooleanExtra("appointment", false)

        if (check) {
            binding.paymentText.text = "Payable: 500"
        } else {
            val cartPrice = intent.getStringExtra("totalPrice")
            binding.paymentText.text = "Payable: $cartPrice"
        }

        binding.buttonClose.setOnClickListener {
            binding.paymentQrLayout.visibility = View.VISIBLE
            binding.userScreenShotLayout.visibility = View.GONE
            paymentScreenshotUri = null
            val screenShot = "Add Screenshot"
            binding.buttonUpload.text = screenShot
        }

        binding.buttonUpload.setOnClickListener {
            if (paymentScreenshotUri == null) {
                ImagePicker.with(this)
                    .crop()
                    .createIntent { intent ->
                        startForProfileImageResult.launch(intent)
                    }

            } else {
                progressDialog.show()
                uploadAndBookAppointment()
            }
        }
    }


    private fun uploadAndBookAppointment() {
        val fileName = UUID.randomUUID().toString() + "jpg"
        storageRef.reference.child("PaymentScreenshot/$fileName").putFile(paymentScreenshotUri!!)
            .addOnSuccessListener {
                it.storage.downloadUrl.addOnSuccessListener { screenshotUrl ->
                    bookAppointment(screenshotUrl.toString())

                }
            }
    }

    private fun bookAppointment(screenshotUrl: String) {
        val userId = FirebaseAuth.getInstance().currentUser!!.uid
        val dbRef = db.collection("AppointmentList")
        val appointmentId = dbRef.document().id

        val doctor = intent.getStringExtra("doctor")
        val date = intent.getStringExtra("date")
        val time = intent.getStringExtra("time")
        val ageGroup = intent.getStringExtra("ageGroup")
        val amount = "500"

        val appointment = AppointmentModel(
            appointmentId,
            screenshotUrl,
            doctor ?: "",
            date ?: "",
            time ?: "",
            ageGroup ?: "",
            amount
        )
        dbRef.document(userId).collection("Appointment").add(appointment).addOnSuccessListener {
            progressDialog.dismiss()
            startActivity(Intent(this, MainActivity::class.java))
            Toast.makeText(this, "Your Appointment Booked", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            progressDialog.dismiss()
            Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show()
        }
    }
}