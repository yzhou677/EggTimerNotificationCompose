package com.example.android.eggtimernotificationcompose.viewmodel

import androidx.lifecycle.ViewModel
import com.example.android.eggtimernotificationcompose.manager.FireBaseManager
import com.example.android.eggtimernotificationcompose.model.Product
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ProductsViewModel : ViewModel() {
    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _products

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        loadSortedProducts()
    }

    fun getSortedProducts(sortOrder: Boolean) {
        loadSortedProducts(sortOrder)
    }

    /**
     * Loads products through FireBaseManager and updates the UI state.
     *
     * @param sortOrder, true for ascending order, false for descending order.
     */
    private fun loadSortedProducts(sortOrder: Boolean = true) {
        _isLoading.value = true

        FireBaseManager.loadSortedProducts({ productsList ->
            _products.value = productsList
            _isLoading.value = false
        }, sortOrder)
    }
}


