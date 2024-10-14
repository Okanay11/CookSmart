package com.example.yemektarifuygulamasi

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.SearchView
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import java.util.*

class HomeFragment : Fragment() {
    private lateinit var searchView: SearchView
    private lateinit var recipeRecyclerView: RecyclerView
    private lateinit var categorySpinner: Spinner
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var currentUser: FirebaseUser
    private lateinit var databaseReferenceRecipes: DatabaseReference
    private lateinit var recipeList: MutableList<Recipe>
    private lateinit var adapter: RecipeAdapter
    private lateinit var allRecipes: MutableList<Recipe>
    private val categories = arrayOf(
        "Tümü", "Çorba", "Zeytinyağlılar", "Et Yemekleri", "Tavuk Yemekleri", "Balık Yemekleri",
        "Sebze Yemekleri", "Hamur İşleri", "Pilavlar ve Makarna", "Salatalar", "Tatlılar",
        "Sütlü Tatlılar", "Şerbetli Tatlılar", "Hamur Tatlıları", "Kahvaltılıklar",
        "Aperatifler ve Meze", "İçecekler"
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        searchView = view.findViewById(R.id.searchView)
        categorySpinner = view.findViewById(R.id.categorySpinner)
        recipeRecyclerView = view.findViewById(R.id.recipeRecyclerView)
        firebaseAuth = FirebaseAuth.getInstance()
        currentUser = firebaseAuth.currentUser!!
        databaseReferenceRecipes = FirebaseDatabase.getInstance().getReference("tarifler")
        recipeList = mutableListOf()
        allRecipes = mutableListOf()
        adapter = RecipeAdapter(requireContext(), recipeList)
        recipeRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        recipeRecyclerView.adapter = adapter
        val spinnerAdapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, categories)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categorySpinner.adapter = spinnerAdapter
        databaseReferenceRecipes.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                allRecipes.clear()
                dataSnapshot.children.forEach { recipeSnapshot ->
                    val recipeId = recipeSnapshot.key ?: ""
                    val recipeName = recipeSnapshot.child("recipeName").value.toString()
                    val cookingTime = recipeSnapshot.child("cookingTime").value.toString()
                    val servings = recipeSnapshot.child("servings").value.toString()
                    val imageURL = recipeSnapshot.child("imageUrl").value.toString()
                    val category = recipeSnapshot.child("category").value.toString()
                    val recipe =
                        Recipe(recipeId, recipeName, cookingTime, servings, imageURL, category)
                    allRecipes.add(recipe)
                }
                updateRecipeList()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("FirebaseError", "Failed to read value.", databaseError.toException())
            }
        })
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                Log.d("SearchQuery", "Arama metni: $newText")
                updateRecipeList(newText ?: "")
                return false
            }
        })
        categorySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selectedCategory = categories[position]
                updateRecipeList(searchView.query.toString(), selectedCategory)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
            }
        }
        return view
    }

    data class Recipe(
        val recipeId: String,
        val recipeName: String,
        val cookingTime: String,
        val servings: String,
        val imageURL: String,
        val category: String
    )

    class RecipeAdapter(
        private val context: Context,
        private var recipes: List<Recipe>
    ) : RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder>() {
        inner class RecipeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
            View.OnClickListener {
            val recipeNameTextView: TextView = itemView.findViewById(R.id.recipeNameTextView)
            val cookingTimeTextView: TextView = itemView.findViewById(R.id.cookingTimeTextView)
            val servingsTextView: TextView = itemView.findViewById(R.id.servingsTextView)
            val recipeImageView: ImageView = itemView.findViewById(R.id.recipeImageView)

            init {
                itemView.setOnClickListener(this)
            }

            override fun onClick(view: View?) {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val clickedRecipe = recipes[position]
                    val fragment = RecipeDetailFragment()
                    val bundle = Bundle()
                    bundle.putString("recipeId", clickedRecipe.recipeId)
                    fragment.arguments = bundle
                    (context as AppCompatActivity).supportFragmentManager.beginTransaction()
                        .replace(R.id.frame_container, fragment)
                        .addToBackStack(null)
                        .commit()
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
            val itemView =
                LayoutInflater.from(parent.context).inflate(R.layout.item_recipe, parent, false)
            return RecipeViewHolder(itemView)
        }

        override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
            val recipe = recipes[position]
            holder.recipeNameTextView.text = recipe.recipeName
            holder.cookingTimeTextView.text = "Pişirme Süresi: ${recipe.cookingTime} dakika"
            holder.servingsTextView.text = "Kaç Kişilik: ${recipe.servings}"
            Glide.with(context)
                .load(recipe.imageURL)
                .placeholder(R.drawable.placeholder_image)
                .error(R.drawable.error_image)
                .into(holder.recipeImageView)
        }

        override fun getItemCount(): Int {
            return recipes.size
        }
    }

    private fun updateRecipeList(searchText: String = "", selectedCategory: String = "Tümü") {
        val filteredRecipes = allRecipes.filter { recipe ->
            val matchesSearchText = recipe.recipeName.toLowerCase(Locale.getDefault())
                .contains(searchText.toLowerCase(Locale.getDefault()))
            val matchesCategory = selectedCategory == "Tümü" || recipe.category == selectedCategory
            matchesSearchText && matchesCategory
        }
        recipeList.clear()
        recipeList.addAll(filteredRecipes.shuffled())
        adapter.notifyDataSetChanged()
    }
}
