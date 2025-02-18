package com.example.doctoronline

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import coil.load
import com.example.doctoronline.databinding.ActivityMainBinding
import com.example.doctoronline.databinding.NavigationLayoutBinding
import com.example.doctoronline.utils.Utils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {
    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private lateinit var drawerToggle: ActionBarDrawerToggle
    private lateinit var database: FirebaseDatabase

    private lateinit var auth: FirebaseAuth

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        val user = Utils.getUserData(this)

        binding.tvLogin.text = "Hello " + user?.name

        if (user?.userImage != "") {
            binding.imageEmail.load(user?.userImage)
        } else {
            binding.imageEmail.setImageResource(R.drawable.placeholder)
        }
        
        auth = FirebaseAuth.getInstance()


        database = FirebaseDatabase.getInstance()



        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val window: Window = window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = resources.getColor(R.color.green, theme)
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }

        drawerToggle =
            ActionBarDrawerToggle(this, binding.drawerLayout, R.string.open, R.string.close)
        drawerToggle.syncState()
        binding.drawerLayout.addDrawerListener(drawerToggle)

        val navigation = NavigationLayoutBinding.inflate(layoutInflater)
        binding.navDrawer.addView(navigation.root)

        binding.btMenu.setOnClickListener {
            binding.drawerLayout.open()
        }
        navigation.navLogout.setOnClickListener {
            Firebase.auth.signOut()
            Utils.clearData(this)
            startActivity(
                Intent(this, LoginScreen::class.java)

            )
            finish()
        }
        navigation.navAppointment.setOnClickListener {
            startActivity(Intent(this@MainActivity, ViewAppointmentScreen::class.java))
        }

        navigation.navDoctor.setOnClickListener {
            startActivity(Intent(this@MainActivity, FindDoctosScreen::class.java))
        }

        navigation.navCart.setOnClickListener {
            startActivity(Intent(this@MainActivity, CartScreen::class.java))
        }
        navigation.navProfile.setOnClickListener {
            startActivity(Intent(this@MainActivity, ProfileScreen::class.java))
        }
        navigation.navContactUs.setOnClickListener {
            val doctor = "doctoronline@gmail.com"
            val intent = Intent(Intent.ACTION_SENDTO)
            intent.data = Uri.parse("mailto:")
            intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(doctor))
            intent.putExtra(Intent.EXTRA_SUBJECT, "Doctor Online")
            startActivity(intent)
        }
        if (user?.userImage != "") {
            navigation.getimg.load(user?.userImage)
        } else {
            navigation.getimg.setImageResource(R.drawable.placeholder)
        }

        navigation.name.text = user?.name
        navigation.number.text = "+91 " + user?.phoneNumber
        binding.btAppointWithDoctor.setOnClickListener {
            startActivity(Intent(this@MainActivity, FindDoctosScreen::class.java))
        }

        binding.btBuyMedicine.setOnClickListener {
            startActivity(Intent(this@MainActivity, BuyMedicineScreen::class.java))
        }

        binding.btViewAppointment.setOnClickListener {
            startActivity(Intent(this@MainActivity, ViewAppointmentScreen::class.java))
        }

        binding.btKnowYourDiseases.setOnClickListener {
            startActivity(Intent(this@MainActivity, DiseaseScreen::class.java))
        }
    }


}
