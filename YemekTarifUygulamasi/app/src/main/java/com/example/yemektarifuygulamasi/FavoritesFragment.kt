package com.example.yemektarifuygulamasi

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*

class FavoritesFragment : Fragment() {
    private lateinit var recipeRecyclerView: RecyclerView
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var currentUser: FirebaseUser
    private lateinit var databaseReferenceFavorites: DatabaseReference
    private lateinit var favoriteRecipes: MutableList<String>
    private lateinit var allRecipes: MutableList<HomeFragment.Recipe>
    private lateinit var adapter: HomeFragment.RecipeAdapter
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_favorites, container, false)
        recipeRecyclerView = view.findViewById(R.id.favoritesRecyclerView)
        firebaseAuth = FirebaseAuth.getInstance()
        currentUser = firebaseAuth.currentUser!!
        databaseReferenceFavorites = FirebaseDatabase.getInstance().getReference("Favori Tarifler")
        favoriteRecipes = mutableListOf()
        allRecipes = mutableListOf()
        adapter = HomeFragment.RecipeAdapter(requireContext(), allRecipes)
        recipeRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        recipeRecyclerView.adapter = adapter
        databaseReferenceFavorites.child(currentUser.uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {

                    favoriteRecipes.clear()
                    dataSnapshot.children.forEach { recipeSnapshot ->
                        val recipeId = recipeSnapshot.key ?: ""
                        if (recipeSnapshot.getValue(Boolean::class.java) == true) {
                            favoriteRecipes.add(recipeId)
                        }
                    }
                    fetchFavoriteRecipes()
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.e("FirebaseError", "Failed to read value.", databaseError.toException())
                }
            })
        return view
    }

    private fun fetchFavoriteRecipes() {
        val databaseReferenceRecipes = FirebaseDatabase.getInstance().getReference("tarifler")
        databaseReferenceRecipes.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                allRecipes.clear()
                dataSnapshot.children.forEach { recipeSnapshot ->
                    val recipeId = recipeSnapshot.key ?: ""
                    val recipeName = recipeSnapshot.child("recipeName").value.toString()
                    val cookingTime = recipeSnapshot.child("cookingTime").value.toString()
                    val servings = recipeSnapshot.child("servings").value.toString()
                    val imageURL = recipeSnapshot.child("imageUrl").value.toString()
                    val userId = recipeSnapshot.child("userId").value.toString()
                    val category =
                        recipeSnapshot.child("category").value.toString() // Kategori ekleyin

                    if (favoriteRecipes.contains(recipeId)) {
                        val recipe = HomeFragment.Recipe(
                            recipeId,
                            recipeName,
                            cookingTime,
                            servings,
                            imageURL,
                            category
                        )
                        allRecipes.add(recipe)
                    }
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("FirebaseError", "Failed to read value.", databaseError.toException())
            }
        })
    }
}