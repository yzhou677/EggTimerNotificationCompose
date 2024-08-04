package com.example.android.eggtimernotificationcompose.manager

import android.util.Log
import com.example.android.eggtimernotificationcompose.model.Product
import com.example.android.eggtimernotificationcompose.model.Recipe
import com.example.android.eggtimernotificationcompose.util.isSpanishLocale
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.remoteconfig.FirebaseRemoteConfig.TAG
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FireBaseManager @Inject constructor(
    private val db: FirebaseFirestore
) {
    /**
     * Loads products list ordered by price from Firestore
     *
     * @param onResult, callback function to return a list of products
     */
    fun loadSortedProducts(onResult: (List<Product>) -> Unit, sortOrder: Boolean = true) {
        val query = if (sortOrder) {
            db.collection("products").orderBy("price", Query.Direction.ASCENDING)
        } else {
            db.collection("products").orderBy("price", Query.Direction.DESCENDING)
        }

        query.get().addOnSuccessListener { result ->
            val products = mutableListOf<Product>()
            for (document in result) {
                // Choose the field name based on the device's locale: use "translations.es" for Spanish locales or "name" for default english locale
                val nameField = if (isSpanishLocale()) "translations.es" else "name"

                val product = Product(
                    name = document.getString(nameField) ?: document.getString("name") ?: "",
                    price = document.getDouble("price") ?: 0.0,
                    imageUrl = document.getString("imageUrl") ?: "",
                    link = document.getString("link") ?: ""
                )

                products.add(product)
            }
            onResult(products)
        }.addOnFailureListener { exception ->
            Log.w(TAG, "Error getting products documents.", exception)
        }
    }

    /**
     * Loads recipes list from Firestore
     *
     * @param onResult, callback function to return a list of recipes
     */
    fun loadRecipes(onResult: (List<Recipe>) -> Unit) {
        val query = db.collection("recipes").orderBy("name", Query.Direction.ASCENDING)
        query.get().addOnSuccessListener { result ->
            val recipes = mutableListOf<Recipe>()
            for (document in result) {
                // Choose the field name based on the device's locale: use "translations.es" for Spanish locales or "name" for default english locale
                val nameField = if (isSpanishLocale()) "translations.es" else "name"

                val recipe = Recipe(
                    name = document.getString(nameField) ?: document.getString("name") ?: "",
                    calories = document.getDouble("calories") ?: 0,
                    imageUrl = document.getString("imageUrl") ?: "",
                    link = document.getString("link") ?: ""
                )

                recipes.add(recipe)
            }
            onResult(recipes)
        }.addOnFailureListener { exception ->
            Log.w(TAG, "Error getting recipes documents.", exception)
        }
    }
}

