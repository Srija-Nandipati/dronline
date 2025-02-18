package com.example.doctoronline

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.doctoronline.databinding.ActivityWelcomeScreenBinding

class WelcomeScreen : AppCompatActivity() {
    private val binding by lazy { ActivityWelcomeScreenBinding.inflate(layoutInflater) }

    private val PERMISSION_REQUEST = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val window: Window = window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = resources.getColor(R.color.white, theme)
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }

        checkAndRequestPermission()

        binding.btnLogin.setOnClickListener {
            startActivity(Intent(this@WelcomeScreen, LoginScreen::class.java))
            finish()
        }

        binding.btnRegister.setOnClickListener {
            startActivity(Intent(this@WelcomeScreen, RegistrationScreen::class.java))
            finish()
        }


    }

    private fun checkAndRequestPermission() {
        val fineLocationPermission = Manifest.permission.ACCESS_FINE_LOCATION
        val cameraLocationPermission = Manifest.permission.CAMERA


        if (ContextCompat.checkSelfPermission(
                this,
                fineLocationPermission
            ) != PackageManager.PERMISSION_GRANTED
            || ContextCompat.checkSelfPermission(
                this,
                cameraLocationPermission
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(fineLocationPermission, cameraLocationPermission),
                PERMISSION_REQUEST
            )
        } else {


        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == PERMISSION_REQUEST) {
            if (areAllPermissionsGranted(grantResults)) {

                Toast.makeText(this, "Granted", Toast.LENGTH_SHORT).show()
            } else {

                showPermissionDeniedDialog()

            }
        }
    }

    private fun areAllPermissionsGranted(grantResults: IntArray): Boolean {
        return grantResults.all { it == PackageManager.PERMISSION_GRANTED }
    }


    private fun showPermissionDeniedDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("The main app feature is unavailable because it requires  permissions that you have denied. Please allow  permissions from settings to proceed further.")
            .setTitle("Permission Required")
            .setCancelable(false)
            .setNegativeButton("Cancel") { dialog, which -> dialog.dismiss() }
            .setPositiveButton("Settings") { dialog, which ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri = Uri.fromParts("package", packageName, null)
                intent.data = uri
                startActivity(intent)
                dialog.dismiss()
            }
        builder.show()

    }
}