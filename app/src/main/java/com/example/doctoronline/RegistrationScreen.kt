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
import com.example.doctoronline.databinding.ActivityRegistrationScreenBinding
import com.example.doctoronline.databinding.DialogProgressBinding
import com.example.doctoronline.model.UserModel
import com.example.doctoronline.utils.Utils
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID

class RegistrationScreen : AppCompatActivity() {
    private val binding by lazy { ActivityRegistrationScreenBinding.inflate(layoutInflater) }
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseDatabase
    private lateinit var dbStorage: FirebaseStorage
    private lateinit var progressDialog: AlertDialog
    private var isLogin: Boolean = false

    private var profileImgUri: Uri? = null

    private val startForProfileImageResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            val resultCode = result.resultCode
            val data = result.data

            when (resultCode) {
                Activity.RESULT_OK -> {

                    val fileUri = data?.data!!

                    profileImgUri = fileUri
                    binding.ivUserImage.setImageURI(profileImgUri)

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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val window: Window = window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = resources.getColor(R.color.white, theme)
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }


        auth = FirebaseAuth.getInstance()
        db = FirebaseDatabase.getInstance()
        dbStorage = FirebaseStorage.getInstance()

        progressDialog = AlertDialog.Builder(this, R.style.CustomAlertDialog).create()
        val progressDialogLay = DialogProgressBinding.inflate(LayoutInflater.from(this))
        progressDialog.setView(progressDialogLay.root)
        progressDialog.setCanceledOnTouchOutside(false)

        binding.ivUserImage.setOnClickListener {
            ImagePicker.with(this)
                .cropSquare()
                .createIntent { intent ->
                    startForProfileImageResult.launch(intent)
                }
        }

        binding.tvLogin.setOnClickListener {
            startActivity(Intent(this@RegistrationScreen, LoginScreen::class.java))
            finish()
        }
        binding.btRegister.setOnClickListener {
            registerNewUser()
        }
        isLogin = intent.getBooleanExtra("google", false)
        if (isLogin) {

            val getName = FirebaseAuth.getInstance().currentUser?.displayName
            val getEmail = FirebaseAuth.getInstance().currentUser?.email
            binding.etEmail.setText(getEmail)
            binding.etName.setText(getName)

            binding.etEmail.isEnabled = false
        } else {
            binding.etEmail.isEnabled = true


        }
    }


    private fun registerNewUser() {
        val name = binding.etName.text.toString().trim()
        val country = binding.etCountry.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val phoneNumber = binding.etPhoneNumber.text.toString().trim()
        val address = binding.etAddress.text.toString().trim()
        val age = binding.etAge.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        validateUserInput(name, country, age, email, phoneNumber, address, password)
    }

    private fun validateUserInput(
        name: String,
        country: String,
        age: String,
        email: String,
        phoneNumber: String,
        address: String,
        password: String
    ) {
        if (name.isEmpty()) {
            binding.etName.requestFocus()
            binding.etName.error = "Empty"
        } else if (country.isEmpty()) {
            binding.etCountry.requestFocus()
            binding.etCountry.error = "Empty"
        } else if (email.isEmpty()) {
            binding.etEmail.requestFocus()
            binding.etEmail.error = "Empty"
        } else if (phoneNumber.isEmpty()) {
            binding.etPhoneNumber.requestFocus()
            binding.etPhoneNumber.error = "Empty"
        } else if (password.isEmpty()) {
            binding.etPassword.requestFocus()
            binding.etPassword.error = "Empty"
        } else if (age.isEmpty()) {
            binding.etAge.requestFocus()
            binding.etAge.error = "Empty"
        } else if (address.isEmpty()) {
            binding.etAddress.requestFocus()
            binding.etAddress.requestFocus()
        } else if (profileImgUri == null) {
            progressDialog.show()
            createUser("", name, phoneNumber, country, address, age, email, password)
        } else {
            progressDialog.show()
            uploadProfilePic(
                profileImgUri,
                name,
                phoneNumber,
                country,
                address,
                age,
                email,
                password
            )

        }
    }


    private fun uploadProfilePic(
        profileImgUri: Uri?,
        name: String,
        phoneNumber: String,
        country: String,
        address: String,
        age: String,
        email: String,
        password: String
    ) {
        val fileName = UUID.randomUUID().toString()
        dbStorage.reference.child("UserProfileImage/$fileName").putFile(profileImgUri!!)
            .addOnSuccessListener {
                it.storage.downloadUrl.addOnSuccessListener { imgUrl ->
                    createUser(
                        imgUrl.toString(),
                        name,
                        phoneNumber,
                        country,
                        address,
                        age,
                        email,
                        password
                    )
                }
            }.addOnFailureListener {
                progressDialog.dismiss()
                Toast.makeText(this, "Something Went Wrong", Toast.LENGTH_SHORT).show()
            }


    }

    private fun createUser(
        profileImage: String,
        name: String,
        phoneNumber: String,
        country: String,
        address: String,
        age: String,
        email: String,
        password: String
    ) {
        if (isLogin) {
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
                    val intent = Intent(this, MainActivity::class.java)
                    Utils.saveUserData(this, user)
                    progressDialog.dismiss()
                    startActivity(intent)
                    finish()
                    Toast.makeText(this, "User Created Successfully", Toast.LENGTH_SHORT)
                        .show()
                }.addOnFailureListener {
                    Toast.makeText(this, "User Creation Failed", Toast.LENGTH_SHORT)
                        .show()
                }
        } else {
            auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener {
                    val userId = it.user!!.uid
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
                            val intent = Intent(this, MainActivity::class.java)
                            Utils.saveUserData(this@RegistrationScreen, user)
                            progressDialog.dismiss()
                            startActivity(intent)
                            finish()
                            Toast.makeText(this, "User Created Successfully", Toast.LENGTH_SHORT)
                                .show()
                        }


                }.addOnFailureListener {
                    progressDialog.dismiss()
                    Toast.makeText(this, "Account Creation Failed", Toast.LENGTH_SHORT).show()
                }

        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

}
