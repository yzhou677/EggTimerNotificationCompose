package com.example.android.eggtimernotificationcompose.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.android.eggtimernotificationcompose.manager.FireBaseManagerInterface
import com.example.android.eggtimernotificationcompose.model.Recipe
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.setMain
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.verify

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class RecipesViewModelTest {

    @get:Rule
    var rule: TestRule = InstantTaskExecutorRule()

    @Mock
    private lateinit var fireBaseManager: FireBaseManagerInterface

    private lateinit var viewModel: RecipesViewModel

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)
        viewModel = RecipesViewModel(fireBaseManager)
    }

    @Test
    fun testInitialLoadingState() {
        assertTrue(viewModel.isLoading.value)
    }

    @Test
    fun testInitialRecipesState() {
        assertTrue(viewModel.recipes.value.isEmpty())
    }

    @Test
    fun testGetSortedRecipes() {
        val captor = argumentCaptor<(List<Recipe>) -> Unit>()

        verify(fireBaseManager).loadRecipes(captor.capture())
    }
}