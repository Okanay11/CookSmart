package com.example.yemektarifuygulamasi

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*

class ProfileFragment : Fragment() {

    private lateinit var userListView: ListView
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var currentUser: FirebaseUser
    private lateinit var databaseReferenceUsers: DatabaseReference
    private lateinit var databaseReferenceRecipes: DatabaseReference
    private lateinit var userNameTextView: TextView
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)
        userNameTextView = view.findViewById(R.id.userNameTextView)
        userListView = view.findViewById(R.id.userListView)
        firebaseAuth = FirebaseAuth.getInstance()
        currentUser = firebaseAuth.currentUser!!
        databaseReferenceUsers =
            FirebaseDatabase.getInstance().getReference("Users").child(currentUser.uid)
        databaseReferenceUsers.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val userName = dataSnapshot.child("kullaniciadi").value.toString()
                userNameTextView.text = userName
            }

            override fun onCancelled(databaseError: DatabaseError) {

            }
        })
        val recipeList: MutableList<Recipe> = mutableListOf()
        databaseReferenceRecipes = FirebaseDatabase.getInstance().getReference("tarifler")
        databaseReferenceRecipes.orderByChild("userId").equalTo(currentUser.uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for (recipeSnapshot in dataSnapshot.children) {
                        val recipeName = recipeSnapshot.child("recipeName").value.toString()
                        val imageURL = recipeSnapshot.child("imageUrl").value.toString()
                        val recipe = Recipe(recipeName, imageURL)
                        recipeList.add(recipe)
                    }

                    val adapter = RecipeAdapter(requireContext(), R.layout.item_recipe, recipeList)
                    userListView.adapter = adapter
                }

                override fun onCancelled(databaseError: DatabaseError) {
                }
            })

        return view
    }
}

data class Recipe(val recipeName: String, val imageURL: String)
class RecipeAdapter(
    private val context: Context,
    private val resource: Int,
    private val recipes: List<Recipe>
) : ArrayAdapter<Recipe>(context, resource, recipes) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(resource, null)
        val recipeImageView: ImageView = view.findViewById(R.id.recipeImageView)
        val recipeNameTextView: TextView = view.findViewById(R.id.recipeNameTextView)
        val recipe = recipes[position]
        Glide.with(context)
            .load(recipe.imageURL)
            .placeholder(R.drawable.placeholder_image)
            .error(R.drawable.error_image)
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    e?.let {
                        Log.e("Glide", "Error loading image: ${e.message}")
                        Toast.makeText(context, "Error loading image", Toast.LENGTH_SHORT).show()
                    }
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    return false
                }
            })
            .into(recipeImageView)
        recipeNameTextView.text = recipe.recipeName
        return view
    }
}
