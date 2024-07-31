package com.example.android.eggtimernotificationcompose.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.android.eggtimernotificationcompose.theme.LocalSpacing

@Composable
fun RecipesListSkeleton() {
    val spacing = LocalSpacing.current

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.1f)
        ),
        modifier = Modifier
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
            // Placeholder for the image
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(180.dp)
                    .background(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(4.dp)
                    )
            )

            // Placeholder for the text column
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(spacing.medium),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Placeholder for the recipe name
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(20.dp)
                        .background(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(4.dp)
                        )
                )
                Spacer(Modifier.height(spacing.small))
                // Placeholder for the calories text
                Box(
                    modifier = Modifier
                        .width(50.dp)
                        .height(15.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(4.dp)
                        )
                )
            }
        }
    }
}