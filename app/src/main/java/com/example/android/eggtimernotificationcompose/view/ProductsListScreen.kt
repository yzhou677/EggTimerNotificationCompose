package com.example.android.eggtimernotificationcompose.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import com.example.android.eggtimernotificationcompose.viewmodel.ProductsViewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.example.android.eggtimernotificationcompose.model.Product
import coil.request.ImageRequest
import com.example.android.eggtimernotificationcompose.R
import com.example.android.eggtimernotificationcompose.theme.LocalSpacing
import com.example.android.eggtimernotificationcompose.theme.colorPrice
import com.example.android.eggtimernotificationcompose.util.openUrl

@Composable
fun ProductsListScreen(viewModel: ProductsViewModel = hiltViewModel()) {
    val products by viewModel.products.collectAsState()
    val isLoading = viewModel.isLoading.collectAsState()
    val spacing = LocalSpacing.current

    val sortAscending = remember { mutableStateOf(true) }

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 80.dp),
        contentPadding = PaddingValues(horizontal = spacing.medium, vertical = spacing.small)
    ) {
        item(span = { GridItemSpan(2) }) { // Span the image across all columns
            Image(
                painter = painterResource(id = R.drawable.eggtimer_ads),
                contentDescription = "Egg Timer Advertisement",
                modifier = Modifier
                    .fillMaxWidth() // Fills the width of the grid
                    .padding(top = spacing.medium)
                    .clip(RoundedCornerShape(20.dp))
            )
        }

        item(span = { GridItemSpan(2) }) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Sorting status indicator
                Text(
                    text = if (sortAscending.value) stringResource(id = R.string.price_low_to_high) else stringResource(id = R.string.price_high_to_low),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                IconButton(
                    onClick = {
                        sortAscending.value = !sortAscending.value
                        viewModel.getSortedProducts(sortAscending.value)
                    }
                ) {
                    Icon(
                        imageVector = if (sortAscending.value) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowUp,
                        contentDescription = if (sortAscending.value) "Sort low to high" else "Sort high to low",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        if (isLoading.value) {
            items(6) {
                ProductSkeleton()
            }
        } else {
            items(products.size) { index ->
                ProductCard(product = products[index])
            }
        }
        item(span = { GridItemSpan(2) }) {
            EndIndicator() // Add end indicator as the last item in the LazyVerticalGrid
        }
    }
}

@Composable
fun ProductCard(product: Product) {
    val context = LocalContext.current
    val spacing = LocalSpacing.current

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.onPrimary
        ),
        modifier = Modifier
            .clickable {
                openUrl(context, product.link)
            }
            .fillMaxWidth()
            .padding(spacing.small),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp,
            pressedElevation = 6.dp,
            focusedElevation = 4.dp,
            hoveredElevation = 4.dp
        )
    ) {
        Column(
            modifier = Modifier
                .padding(spacing.medium)
                .background(color = MaterialTheme.colorScheme.onPrimary),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val imagePainter = rememberAsyncImagePainter(
                ImageRequest.Builder(LocalContext.current).data(data = product.imageUrl)
                    .apply(block = fun ImageRequest.Builder.() {
                        crossfade(true)
                    }).build()
            )
            Image(
                painter = imagePainter,
                contentDescription = "Product Image",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp) // Set a fixed height or adjust as needed
            )
            Text(
                text = product.name,
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.onBackground
                ),
                maxLines = 4,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.width(IntrinsicSize.Max)
            )
            Text(
                text = "$${product.price}",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontSize = 18.sp
                ),
                color = colorPrice
            )
        }
    }
}