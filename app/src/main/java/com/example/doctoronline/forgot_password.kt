package com.example.doctoronline

import android.app.ProgressDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.doctoronline.databinding.ActivityForgotPasswordBinding
import com.example.doctoronline.utils.Utils
import com.google.firebase.auth.FirebaseAuth

class forgot_password : AppCompatActivity() {
    private val binding by lazy { ActivityForgotPasswordBinding.inflate(layoutInflater) }
    private lateinit var auth: FirebaseAuth
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
        val email = Utils.getUserData(this)?.email
        binding.etForgetEmail.setText(email)
        binding.etForgetEmail.isEnabled = binding.etForgetEmail.text.toString().isEmpty()
        binding.btSend.setOnClickListener {
            resetPassword()
        }
        binding.btForgetBack.setOnClickListener { onBackPressed() }
    }

    private fun resetPassword() {
        val email = binding.etForgetEmail.text.toString()
        if (validateForm(email)) {
            auth.sendPasswordResetEmail(email)
                .addOnSuccessListener {
                    val progressDialog = ProgressDialog(this).apply {
                        setMessage("Verifying your email....")
                        show()
                    }

                    Handler().postDelayed({
                        startActivity(Intent(this, WelcomeScreen::class.java))
                        finish()
                        Toast.makeText(this, "Link Sent Successfully", Toast.LENGTH_SHORT).show()

                    }, 2000)
                }
                .addOnFailureListener {

                }
        }
    }

    private fun validateForm(email: String): Boolean {
        return when {
            email.isEmpty() -> {
                binding.etForgetEmail.error = "empty"
                binding.etForgetEmail.requestFocus()
                false
            }

            else -> {
                true
            }
        }
    }
}