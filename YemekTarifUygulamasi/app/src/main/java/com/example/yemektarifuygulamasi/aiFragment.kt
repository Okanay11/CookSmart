package com.example.yemektarifuygulamasi

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject

class aiFragment : Fragment() {

    private lateinit var editTextIngredients: EditText
    private lateinit var buttonGenerateRecipe: Button
    private lateinit var textViewRecipe: TextView
    private val gpt3ApiService = GPT3ApiService.create()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_ai, container, false)
        editTextIngredients = view.findViewById(R.id.editTextIngredients)
        buttonGenerateRecipe = view.findViewById(R.id.buttonGenerateRecipe)
        textViewRecipe = view.findViewById(R.id.textViewRecipe)
        buttonGenerateRecipe.setOnClickListener {
            val ingredients = editTextIngredients.text.toString()
            if (ingredients.isNotEmpty()) {
                generateRecipe(ingredients)
            }
        }
        return view
    }

    private fun generateRecipe(ingredients: String) {
        val jsonObject = JSONObject()
        jsonObject.put("model", "gpt-z3.5-turbo")
        jsonObject.put("messages", JSONArray().apply {
            put(JSONObject().apply {
                put("role", "system")

                put("content", "Sen bir tarif oluşturucusun.")
            })
            put(JSONObject().apply {
                put("role", "user")
                put(
                    "content",
                    "Elimdeki malzemeler: $ingredients. Bu malzemelerle bir yemek tarifi oluştur."
                )
            })
        })
        jsonObject.put("max_tokens", 300)
        jsonObject.put("temperature", 0.7)
        val requestBody = jsonObject.toString()
            .toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = gpt3ApiService.generateRecipe(requestBody).execute()
                if (response.isSuccessful) {
                    val recipe = response.body()?.string()?.let { cleanResponse(it) }
                    withContext(Dispatchers.Main) {
                        textViewRecipe.text = recipe
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    withContext(Dispatchers.Main) {
                        textViewRecipe.text =
                            "Tarif oluşturulamadı, lütfen tekrar deneyin. Hata kodu: ${response.code()}. Hata mesajı: $errorBody"
                    }
                    Log.e("API_ERROR", "Hata kodu: ${response.code()}, Hata mesajı: $errorBody")
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    textViewRecipe.text = "Bir hata oluştu: ${e.message}"
                }
                Log.e("API_ERROR", "Exception: ${e.message}", e)
            }
        }
    }

    private fun cleanResponse(response: String): String {
        val jsonObject = JSONObject(response)
        val choicesArray = jsonObject.getJSONArray("choices")
        val messageObject = choicesArray.getJSONObject(0).getJSONObject("message")
        return messageObject.getString("content").trim()
    }
}