package com.example.yemektarifuygulamasi

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.yemektarifuygulamasi.databinding.FragmentRecipeDetailBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class RecipeDetailFragment : Fragment() {
    private lateinit var binding: FragmentRecipeDetailBinding
    private lateinit var userId: String
    private lateinit var database: FirebaseDatabase
    private lateinit var recipeId: String
    private lateinit var databaseReference: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private var isFavorite: Boolean = false
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRecipeDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        databaseReference = FirebaseDatabase.getInstance().reference
        auth = FirebaseAuth.getInstance()
        userId = auth.currentUser?.uid ?: ""
        database = FirebaseDatabase.getInstance()
        val bundle = arguments
        if (bundle != null) {
            recipeId = bundle.getString("recipeId") ?: ""
            val recipeRef = databaseReference.child("tarifler").child(recipeId)
            recipeRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val recipeName = dataSnapshot.child("recipeName").value.toString()
                    val cookingTime = dataSnapshot.child("cookingTime").value.toString()
                    val preparationTime = dataSnapshot.child("preparationTime").value.toString()
                    val servings = dataSnapshot.child("servings").value.toString()
                    val imageURL = dataSnapshot.child("imageUrl").value.toString()
                    val description = dataSnapshot.child("description").value.toString()
                    val ingredients =
                        dataSnapshot.child("ingredients").children.map { it.value.toString() }
                    binding.recipeNameTextView.text = recipeName
                    binding.cookingTimeTextView.text = "Pişirme Süresi: $cookingTime dakika"
                    binding.preparationTimeTextView.text =
                        "Hazırlık Süresi: $preparationTime dakika"
                    binding.servingsTextView.text = "Kaç Kişilik: $servings"
                    binding.recipeDescriptionTextView.text = description
                    binding.ingredientsTextView.text =
                        "Malzemeler:\n${ingredients.joinToString("\n")}"
                    Glide.with(requireContext())
                        .load(imageURL)
                        .placeholder(R.drawable.placeholder_image)
                        .error(R.drawable.error_image)
                        .into(binding.recipeImageView)
                    isFavoriteRecipe(userId, recipeId) { isFavorite ->
                        if (isFavorite) {
                            binding.favoriteImageView.setImageResource(R.drawable.ic_star_filled)
                        } else {
                            binding.favoriteImageView.setImageResource(R.drawable.ic_star)
                        }
                    }
                    binding.favoriteImageView.setOnClickListener {
                        isFavorite = !isFavorite
                        if (isFavorite) {
                            addToFavorites(userId, recipeId)
                            binding.favoriteImageView.setImageResource(R.drawable.ic_star_filled)
                        } else {
                            removeFromFavorites(userId, recipeId)
                            binding.favoriteImageView.setImageResource(R.drawable.ic_star)
                        }
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.e(
                        "RecipeDetailFragment",
                        "Veritabanı okuma hatası",
                        databaseError.toException()
                    )
                }
            })
        }
    }

    private fun isFavoriteRecipe(userId: String, recipeId: String, callback: (Boolean) -> Unit) {
        val favoritesRef = databaseReference.child("Favori Tarifler").child(userId).child(recipeId)
        favoritesRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                callback(dataSnapshot.exists())
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e(
                    "RecipeDetailFragment",
                    "Veritabanı okuma hatası",
                    databaseError.toException()
                )
                callback(false)
            }
        })
    }

    private fun addToFavorites(userId: String, recipeId: String) {
        val favoritesRef = database.reference.child("Favori Tarifler").child(userId).child(recipeId)
        favoritesRef.setValue(true)
            .addOnSuccessListener {
                Log.d("RecipeDetailFragment", "Tarif favorilere eklendi")
            }
            .addOnFailureListener { e ->
                Log.e("RecipeDetailFragment", "Tarif favorilere eklenirken hata oluştu", e)
            }
    }

    private fun removeFromFavorites(userId: String, recipeId: String) {
        val favoritesRef = database.reference.child("Favori Tarifler").child(userId).child(recipeId)
        favoritesRef.removeValue()
            .addOnSuccessListener {
                Log.d("RecipeDetailFragment", "Tarif favorilerden kaldırıldı")
            }
            .addOnFailureListener { e ->
                Log.e("RecipeDetailFragment", "Tarif favorilerden kaldırılırken hata oluştu", e)
            }
    }
}
