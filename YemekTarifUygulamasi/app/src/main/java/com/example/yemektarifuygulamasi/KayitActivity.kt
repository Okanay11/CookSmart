package com.example.yemektarifuygulamasi

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.text.TextUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.example.yemektarifuygulamasi.databinding.ActivityKayitBinding
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class KayitActivity : AppCompatActivity() {
    private lateinit var binding: ActivityKayitBinding
    private lateinit var auth: FirebaseAuth
    private var databaseReference: DatabaseReference? = null
    private var database: FirebaseDatabase? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        val intent = Intent(this@KayitActivity, MainActivity::class.java)
        super.onCreate(savedInstanceState)
        binding = ActivityKayitBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        databaseReference = database?.reference!!.child("Users")
        binding.kayitButton.setOnClickListener {
            var kayitKullaniciAd = binding.kayitKullaniciAd.text.toString()
            var kayitSifre = binding.kayitSifre.text.toString()
            var kayitEmail = binding.kayitEmail.text.toString()
            var kayitSifreTekrar = binding.kayitSifreTekrar.text.toString()

            if (TextUtils.isEmpty(kayitKullaniciAd)) {
                binding.kayitKullaniciAd.error = "Kullanıcı adınızı giriniz."
                return@setOnClickListener
            } else if (TextUtils.isEmpty(kayitEmail)) {
                binding.kayitEmail.error = "Email adresinizi giriniz."
                return@setOnClickListener
            } else if (TextUtils.isEmpty(kayitSifre)) {
                binding.kayitSifre.error = "Şifrenizi giriniz."
                return@setOnClickListener
            } else if (TextUtils.isEmpty(kayitSifreTekrar)) {
                binding.kayitSifreTekrar.error = "Şifrenizi tekrar giriniz."
                return@setOnClickListener
            }
            if (kayitSifre == kayitSifreTekrar) {
                auth.createUserWithEmailAndPassword(
                    binding.kayitEmail.text.toString(),
                    binding.kayitSifre.text.toString()
                )
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            var currentuser = auth.currentUser
                            var currentUserDb =
                                currentuser?.let { it1 -> databaseReference?.child(it1.uid) }
                            currentUserDb?.child("kullaniciadi")
                                ?.setValue(binding.kayitKullaniciAd.text.toString())
                            currentUserDb?.child("sifre")
                                ?.setValue(binding.kayitSifre.text.toString())
                            currentUserDb?.child("email")
                                ?.setValue(binding.kayitEmail.text.toString())
                            Toast.makeText(this@KayitActivity, "Kayıt Başarılı!", Toast.LENGTH_LONG)
                                .show()
                            startActivity(intent)
                            finish()
                        } else {
                            Toast.makeText(
                                this@KayitActivity,
                                "Kayıt Başarısız.",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
            } else {
                Toast.makeText(this@KayitActivity, "Şifreler Uyuşmuyor", Toast.LENGTH_LONG).show()
            }

        }
        binding.twGirisEkraniGecis.setOnClickListener {
            Log.d("Tıklama", "TextView'a tıklandı")
            val intent = Intent(applicationContext, MainActivity::class.java)
            try {
                startActivity(intent)
                finish()
            } catch (e: Exception) {
                Log.e("Hata", "Aktivite geçişinde bir hata oluştu: ${e.message}")
            }
        }
    }
}