package com.example.android.eggtimernotificationcompose.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.android.eggtimernotificationcompose.model.Recipe
import com.example.android.eggtimernotificationcompose.theme.LocalSpacing
import com.example.android.eggtimernotificationcompose.viewmodel.RecipesViewModel
import com.example.android.eggtimernotificationcompose.util.openUrl

@Composable
fun RecipesListScreen(viewModel: RecipesViewModel = viewModel()) {
    val recipes by viewModel.recipes.collectAsState()
    val isLoading = viewModel.isLoading.collectAsState()
    val spacing = LocalSpacing.current

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 100.dp),
        contentPadding = PaddingValues(horizontal = spacing.medium, vertical = spacing.small)
    ) {
        if (isLoading.value) {
            items(6) {
                RecipesListSkeleton()
            }
        } else {
            items(recipes.size) { index ->
                RecipeCard(recipe = recipes[index])
            }
        }

        item {
            EndIndicator() // Add end indicator as the last item in the LazyColumn
        }
    }
}

@Composable
fun RecipeCard(recipe: Recipe) {
    val context = LocalContext.current
    val spacing = LocalSpacing.current

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.onPrimary
        ),
        modifier = Modifier
            .clickable {
                openUrl(context, recipe.link)
            }
            .fillMaxWidth()
            .padding(spacing.small),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp,
            pressedElevation = 12.dp,
            focusedElevation = 8.dp,
            hoveredElevation = 8.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(spacing.medium)
                .background(color = MaterialTheme.colorScheme.onPrimary),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val imagePainter = rememberAsyncImagePainter(
                ImageRequest.Builder(LocalContext.current).data(data = recipe.imageUrl)
                    .apply(block = fun ImageRequest.Builder.() {
                        crossfade(true)
                    }).build()
            )
            Image(
                painter = imagePainter,
                contentDescription = "Recipe Image",
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .height(180.dp)
                    .clipToBounds(),
                contentScale = ContentScale.Crop
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(spacing.medium),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = recipe.name,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.onBackground
                    ),
                    maxLines = 6,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.width(IntrinsicSize.Max)
                )
                if (recipe.calories.toInt() > 0) {
                    Text(
                        text = "${recipe.calories.toInt()} kcal",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

