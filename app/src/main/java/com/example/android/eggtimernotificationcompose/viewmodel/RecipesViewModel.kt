package com.example.android.eggtimernotificationcompose.viewmodel

import androidx.lifecycle.ViewModel
import com.example.android.eggtimernotificationcompose.manager.FireBaseManager
import com.example.android.eggtimernotificationcompose.model.Recipe
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class RecipesViewModel : ViewModel() {
    private val _recipes = MutableStateFlow<List<Recipe>>(emptyList())
    val recipes: StateFlow<List<Recipe>> = _recipes

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        loadRecipes()
    }

    /**
     * Loads recipes through FireBaseManager and updates the UI state.
     */
    private fun loadRecipes() {
        _isLoading.value = true

        FireBaseManager.loadRecipes { recipesList ->
            _recipes.value = recipesList
            _isLoading.value = false
        }
    }
}