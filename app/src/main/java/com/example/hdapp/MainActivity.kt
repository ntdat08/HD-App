package com.example.hdapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.hdapp.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigationView2)

        val navController=findNavController(R.id.fragmentContainerView3)

        bottomNav.setupWithNavController(navController)

        binding.imageView8.setOnClickListener {
            val bottomSheetDialog = NotificationFragment()
            bottomSheetDialog.show(supportFragmentManager,"Test")
        }

    }
}