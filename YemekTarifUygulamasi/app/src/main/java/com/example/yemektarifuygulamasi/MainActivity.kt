package com.example.yemektarifuygulamasi

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.text.TextUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.yemektarifuygulamasi.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("MainActivity", "onCreate method called")
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()
        binding.girisButton.setOnClickListener {
            Log.d("MainActivity", "Giris button clicked")
            val girisEmail = binding.girisEmail.text.toString()
            val girisSifre = binding.girisSifre.text.toString()
            if (TextUtils.isEmpty(girisEmail)) {
                binding.girisEmail.error = "Email adresinizi giriniz."
                return@setOnClickListener
            } else if (TextUtils.isEmpty(girisSifre)) {
                binding.girisSifre.error = "Şifrenizi giriniz."
                return@setOnClickListener
            }
            auth.signInWithEmailAndPassword(girisEmail, girisSifre)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        startActivity(Intent(applicationContext, AnaSayfaActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(
                            applicationContext,
                            "Hatalı email veya şifre girdiniz, Lütfen tekrar deneyiniz.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
        }
        binding.twKayitEkraniGecis.setOnClickListener {
            Log.d("MainActivity", "Kayit ekranina gecis button clicked")
            startActivity(Intent(applicationContext, KayitActivity::class.java))
            finish()
        }
    }
}
