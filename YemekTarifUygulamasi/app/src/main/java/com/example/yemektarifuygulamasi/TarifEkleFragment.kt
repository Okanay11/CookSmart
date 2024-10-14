package com.example.yemektarifuygulamasi

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream
import java.util.*

class TarifEkleFragment : Fragment() {

    private lateinit var storage: FirebaseStorage
    private lateinit var database: FirebaseDatabase
    private lateinit var selectedImageUri: Uri
    private lateinit var auth: FirebaseAuth
    private lateinit var categorySpinner: Spinner
    private lateinit var ingredientsLayout: LinearLayout
    private val ingredientsList = mutableListOf<EditText>()

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_tarif_ekle, container, false)
        storage = FirebaseStorage.getInstance()
        database = FirebaseDatabase.getInstance()
        auth = FirebaseAuth.getInstance()
        categorySpinner = view.findViewById(R.id.category_spinner)
        ingredientsLayout = view.findViewById(R.id.ingredients_layout)
        val categories = arrayOf(
            "Çorba", "Zeytinyağlılar", "Et Yemekleri", "Tavuk Yemekleri", "Balık Yemekleri",
            "Sebze Yemekleri", "Hamur İşleri", "Pilavlar ve Makarna", "Salatalar", "Tatlılar",
            "Sütlü Tatlılar", "Şerbetli Tatlılar", "Hamur Tatlıları", "Kahvaltılıklar",
            "Aperatifler ve Meze", "İçecekler"
        )
        val adapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categorySpinner.adapter = adapter
        val uploadFromCameraButton: Button = view.findViewById(R.id.upload_from_camera_button)
        val uploadFromGalleryButton: Button = view.findViewById(R.id.upload_from_gallery_button)
        val addIngredientButton: Button = view.findViewById(R.id.add_ingredient_button)
        val saveRecipeButton: Button = view.findViewById(R.id.save_recipe_button)
        uploadFromCameraButton.setOnClickListener {
            if (checkCameraPermission()) {
                openCamera()
            } else {
                requestCameraPermission()
            }
        }
        uploadFromGalleryButton.setOnClickListener {
            if (checkGalleryPermission()) {
                openGallery()
            } else {
                requestGalleryPermission()
            }
        }
        addIngredientButton.setOnClickListener {
            addIngredientField()
        }
        saveRecipeButton.setOnClickListener {
            saveRecipe()
        }
        selectedImageUri = Uri.EMPTY
        return view
    }

    private fun checkCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(Manifest.permission.CAMERA),
            CAMERA_PERMISSION_CODE
        )
    }

    private fun checkGalleryPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestGalleryPermission() {
        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
            GALLERY_PERMISSION_CODE
        )
    }

    private fun openCamera() {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE)
    }

    private fun openGallery() {
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(galleryIntent, GALLERY_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CAMERA_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val photo = data?.extras?.get("data") as Bitmap
            selectedImageUri = getImageUri(requireContext(), photo)
            showSelectedImage(selectedImageUri)
        } else if (requestCode == GALLERY_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                val selectedImage: Uri? = data.data
                if (selectedImage != null) {
                    selectedImageUri = selectedImage
                    showSelectedImage(selectedImageUri)
                }
            }
        }
    }

    private fun getImageUri(context: Context, photo: Bitmap): Uri {
        val bytes = ByteArrayOutputStream()
        photo.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path =
            MediaStore.Images.Media.insertImage(context.contentResolver, photo, "Title", null)
        return Uri.parse(path)
    }

    private fun showSelectedImage(imageUri: Uri?) {
        val imageView: ImageView = requireView().findViewById(R.id.recipe_image_view)
        if (imageUri != null) {
            imageView.setImageURI(imageUri)
            imageView.visibility = View.VISIBLE
        } else {
            Toast.makeText(requireContext(), "Resim yüklenemedi", Toast.LENGTH_SHORT).show()
        }
    }

    private fun addIngredientField() {
        val ingredientEditText = EditText(requireContext())
        ingredientEditText.hint = "Malzeme"
        ingredientEditText.inputType = android.text.InputType.TYPE_CLASS_TEXT
        ingredientsLayout.addView(ingredientEditText)
        ingredientsList.add(ingredientEditText)
    }

    private fun saveRecipe() {
        val userId = auth.currentUser?.uid
        val recipeRef = database.reference.child("tarifler").push()
        val recipeId = recipeRef.key
        val servingsEditText: EditText = requireView().findViewById(R.id.servings_edit_text)
        val cookingTimeEditText: EditText = requireView().findViewById(R.id.cooking_time_edit_text)
        val preparationTimeEditText: EditText =
            requireView().findViewById(R.id.preparation_time_edit_text)
        val descriptionEditText: EditText = requireView().findViewById(R.id.description_edit_text)
        val recipeNameEditText: EditText = requireView().findViewById(R.id.recipe_name_edit_text)
        val category = categorySpinner.selectedItem.toString()
        val servings = servingsEditText.text.toString().toInt()
        val cookingTime = cookingTimeEditText.text.toString().toInt()
        val preparationTime = preparationTimeEditText.text.toString().toInt()
        val description = descriptionEditText.text.toString()
        val recipeName = recipeNameEditText.text.toString()
        val ingredients = ingredientsList.map { it.text.toString() }
        if (recipeName.isEmpty()) {
            Toast.makeText(requireContext(), "Lütfen tarifin adını girin", Toast.LENGTH_SHORT)
                .show()
            return
        }
        val recipeMap = HashMap<String, Any>()
        recipeMap["userId"] = userId!!
        recipeMap["recipeName"] = recipeName
        recipeMap["servings"] = servings
        recipeMap["cookingTime"] = cookingTime
        recipeMap["preparationTime"] = preparationTime
        recipeMap["description"] = description
        recipeMap["category"] = category
        recipeMap["ingredients"] = ingredients
        val imageRef = storage.reference.child("images/$recipeId.jpg")
        val uploadTask = imageRef.putFile(selectedImageUri)
        uploadTask.continueWithTask { task ->
            if (!task.isSuccessful) {
                task.exception?.let {
                    throw it
                }
            }
            imageRef.downloadUrl
        }.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val downloadUri = task.result
                val imageUrl = downloadUri.toString()
                recipeMap["imageUrl"] = imageUrl

                recipeRef.setValue(recipeMap)
                    .addOnSuccessListener {
                        Toast.makeText(
                            requireContext(),
                            "Tarif başarıyla eklendi",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(
                            requireContext(),
                            "Tarif eklenirken bir hata oluştu",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            } else {
                Toast.makeText(
                    requireContext(),
                    "Fotoğraf yüklenirken bir hata oluştu",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    companion object {
        private const val CAMERA_PERMISSION_CODE = 101
        private const val GALLERY_PERMISSION_CODE = 102
        private const val CAMERA_REQUEST_CODE = 103
        private const val GALLERY_REQUEST_CODE = 104
    }
}