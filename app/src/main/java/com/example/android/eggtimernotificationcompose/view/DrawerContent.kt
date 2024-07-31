package com.example.android.eggtimernotificationcompose.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.DrawerState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.scale
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.android.eggtimernotificationcompose.R
import com.example.android.eggtimernotificationcompose.theme.LocalSpacing
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun DrawerContent(navController: NavController, drawerState: DrawerState, scope: CoroutineScope) {
    val spacing = LocalSpacing.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Box(
            modifier = Modifier
                .height(200.dp)
                .fillMaxWidth()
                .clipToBounds()
        ) {
            Image(
                painter = painterResource(id = R.drawable.egg_notification),
                contentDescription = "Egg Timer Advertisement",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .align(Alignment.Center)
                    .scale(1.2f)
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = spacing.medium, vertical = spacing.small)
        ) {
            Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = "Recipe",
                tint = MaterialTheme.colorScheme.primary
            )
            Text(stringResource(id = R.string.hamburger_menu_recipe),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        navController.navigate("recipeslist") {
                            launchSingleTop = true
                        }
                        scope.launch {
                            drawerState.close()
                        }
                    }
                    .padding(spacing.small)
                    .weight(1f),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground)
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ShoppingCart,
                contentDescription = "Shopping Cart",
                tint = MaterialTheme.colorScheme.primary
            )
            Text(stringResource(id = R.string.hamburger_menu_shop),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        navController.navigate("productlist") {
                            launchSingleTop = true
                        }
                        scope.launch {
                            drawerState.close()
                        }
                    }
                    .padding(spacing.small)
                    .weight(1f),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground)
        }
    }
}