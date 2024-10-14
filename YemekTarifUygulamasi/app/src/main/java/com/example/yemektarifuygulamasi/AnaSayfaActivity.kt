package com.example.yemektarifuygulamasi

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.yemektarifuygulamasi.databinding.ActivityAnasayfaBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth

class AnaSayfaActivity : AppCompatActivity() {
    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityAnasayfaBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityAnasayfaBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()

        bottomNavigationView = findViewById(R.id.bottom_navigation)

        bottomNavigationView.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.bottom_home -> {
                    replaceFragment(HomeFragment())
                    true
                }

                R.id.bottom_star -> {
                    replaceFragment(FavoritesFragment())
                    true
                }

                R.id.bottom_add -> {
                    replaceFragment(TarifEkleFragment())
                    true
                }

                R.id.bottom_profile -> {
                    replaceFragment(ProfileFragment())
                    true
                }

                R.id.bottom_ai -> {
                    replaceFragment(aiFragment())
                    true
                }

                else -> false
            }
        }
        replaceFragment(HomeFragment())
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction().replace(R.id.frame_container, fragment).commit()
    }
}