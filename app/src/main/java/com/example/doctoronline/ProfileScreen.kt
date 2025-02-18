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
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import coil.load
import com.example.doctoronline.databinding.ActivityProfileScreenBinding
import com.example.doctoronline.databinding.DialogProgressBinding
import com.example.doctoronline.model.UserModel
import com.example.doctoronline.utils.Utils
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID

class ProfileScreen : AppCompatActivity() {


    private val binding by lazy {
        ActivityProfileScreenBinding.inflate(layoutInflater)
    }

    private lateinit var db: FirebaseDatabase
    private lateinit var dbStorage: FirebaseStorage
    private lateinit var progressDialog: AlertDialog


    private var imageUri: Uri? = null
    private val getImageLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            val resultCode = it.resultCode
            val data = it.data!!
            when (resultCode) {
                Activity.RESULT_OK -> {

                    val fileUri = data.data!!

                    imageUri = fileUri
                    binding.profileImage.setImageURI(imageUri)

                }

                ImagePicker.RESULT_ERROR -> {
                    Toast.makeText(this, ImagePicker.getError(data), Toast.LENGTH_SHORT).show()
                }

                else -> {
                    Toast.makeText(this, "Uploading Image Cancelled", Toast.LENGTH_SHORT).show()
                }
            }
        }


    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(binding.root)


        db = FirebaseDatabase.getInstance()
        dbStorage = FirebaseStorage.getInstance()

        progressDialog = AlertDialog.Builder(this, R.style.CustomAlertDialog).create()
        val progressDialogLay = DialogProgressBinding.inflate(LayoutInflater.from(this))
        progressDialog.setView(progressDialogLay.root)
        progressDialog.setCanceledOnTouchOutside(false)


        val user = Utils.getUserData(this@ProfileScreen)
        binding.name.text = user?.name
        binding.name1.setText(user?.name)
        binding.email.text = user?.email
        binding.email1.setText(user?.email)
        binding.email1.isEnabled = false
        binding.age.setText(user?.age)
        binding.country.setText(user?.country)
        binding.mobilenum.setText(user?.phoneNumber)
        binding.address.setText(user?.address)
        if (user?.userImage != "") {
            binding.profileImage.load(user?.userImage)
        } else {
            binding.profileImage.setImageResource(R.drawable.placeholder)
        }


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val window: Window = window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = resources.getColor(R.color.white, theme)
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }

        binding.profileImage.setOnClickListener {
            ImagePicker.with(this)
                .cropSquare()
                .createIntent { intent ->
                    getImageLauncher.launch(intent)
                }
        }


        binding.updateBtn.setOnClickListener {
            if (imageUri == null) {
                progressDialog.show()
                updateData(
                    binding.name1.text.toString(),
                    binding.mobilenum.text.toString(),
                    binding.country.text.toString(),
                    binding.address.text.toString(),
                    binding.age.text.toString(),
                    binding.email.text.toString()

                )
            } else {
                progressDialog.show()
                updateImageAndData(imageUri!!)
            }
        }

        binding.tvForgetPassword.setOnClickListener {
            val intent = Intent(this@ProfileScreen, forgot_password::class.java)
            finish()
            intent.putExtra("updatePassword", true)
            startActivity(intent)
        }

        binding.backBtn.setOnClickListener {
            onBackPressed()
        }


    }

    private fun updateData(
        name: String,
        mobile: String,
        country: String,
        address: String,
        age: String,
        email: String
    ) {
        val userId = FirebaseAuth.getInstance().currentUser!!.uid
        val user = UserModel(
            userId,
            Utils.getUserData(this)?.userImage,
            name,
            mobile,
            country,
            address,
            age,
            email
        )

        db.reference.child("Users").child(userId).setValue(user)
            .addOnSuccessListener {
                val intent = Intent(this, MainActivity::class.java)
                Utils.saveUserData(this, user)
                progressDialog.dismiss()
                startActivity(intent)
                finish()
                Toast.makeText(this, "Information Updated Successfully", Toast.LENGTH_SHORT)
                    .show()
            }.addOnFailureListener {
                Toast.makeText(this, "User Creation Failed", Toast.LENGTH_SHORT)
                    .show()
            }
    }

    private fun updateImageAndData(imageUri: Uri) {
        val fileName = UUID.randomUUID().toString()
        dbStorage.reference.child("UserProfileImage/$fileName").putFile(imageUri)
            .addOnSuccessListener {
                it.storage.downloadUrl.addOnSuccessListener { imgUrl ->
                    updateUser(
                        imgUrl.toString(),
                        binding.name1.text.toString(),
                        binding.mobilenum.text.toString(),
                        binding.country.text.toString(),
                        binding.address.text.toString(),
                        binding.age.text.toString(),
                        binding.email.text.toString()

                    )
                }
            }.addOnFailureListener {
                progressDialog.dismiss()
                Toast.makeText(this, "Something Went Wrong", Toast.LENGTH_SHORT).show()
            }

    }

    private fun updateUser(
        profileImage: String,
        name: String,
        phoneNumber: String,
        country: String,
        address: String,
        age: String,
        email: String,

        ) {

        val userId = FirebaseAuth.getInstance().currentUser!!.uid
        val user =
            UserModel(
                userId,
                profileImage,
                name,
                phoneNumber,
                country,
                address,
                age,
                email
            )

        db.reference.child("Users").child(userId).setValue(user)
            .addOnSuccessListener {
                Utils.saveUserData(this, user)
                progressDialog.dismiss()
                val intent = Intent(this,MainActivity::class.java)
                startActivity(intent)
                Toast.makeText(this, "Information Updated Successfully", Toast.LENGTH_SHORT)
                    .show()
            }.addOnFailureListener {
                Toast.makeText(this, "User Creation Failed", Toast.LENGTH_SHORT)
                    .show()
            }


    }
}




