package com.example.android.eggtimernotificationcompose

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.android.eggtimernotificationcompose.manager.GoogleAssistantManager
import com.example.android.eggtimernotificationcompose.manager.NotificationChannelManager
import com.example.android.eggtimernotificationcompose.view.DrawerContent
import com.example.android.eggtimernotificationcompose.view.EggTimerScreen
import com.example.android.eggtimernotificationcompose.view.ProductsListScreen
import com.example.android.eggtimernotificationcompose.view.RecipesListScreen
import com.example.android.eggtimernotificationcompose.theme.EggTimerNotificationComposeTheme
import com.example.android.eggtimernotificationcompose.theme.LocalSpacing
import com.example.android.eggtimernotificationcompose.viewmodel.EggTimerViewModel
import com.example.android.eggtimernotificationcompose.viewmodel.EggTimerViewModelFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private lateinit var timerAction: EggTimerViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        NotificationChannelManager.createNotificationChannel(
            this,
            getString(R.string.egg_notification_channel_id),
            getString(R.string.egg_notification_channel_name)
        )
        enableEdgeToEdge()
        setContent {
            EggTimerNotificationComposeTheme {
                MainContent()
            }
        }
        handleIntent(intent)
    }

    /**
     * Handle incoming Google Assistant intents
     *
     * @param intent, Google Assistant intent
     */
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent) {
        intent.action?.let { action ->
            val factory = EggTimerViewModelFactory(application)
            timerAction = ViewModelProvider(this, factory)[EggTimerViewModel::class.java]

            if (action == Intent.ACTION_VIEW) {
                val softnessLevel = intent.getStringExtra("softness_level")
                    ?: intent.data?.getQueryParameter("softness_level")
                if (softnessLevel != null) {
                    GoogleAssistantManager.startTimerThroughGoogleAssistant(
                        softnessLevel,
                        resources,
                        timerAction,
                        this
                    )
                } else {
                    GoogleAssistantManager.showInvalidSoftnessLevelError(resources, this)
                }
            }
        }
    }
}

@Composable
fun MainContent() {
    val navController = rememberNavController()

    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(modifier = Modifier.fillMaxWidth(0.7f)) {
                DrawerContent(
                    navController,
                    drawerState,
                    scope
                )
            }
        },  // Define the drawer content here
        content = {
            Scaffold(
                topBar = {
                    MainTopAppBar(
                        navController,
                        onMenuClick = { toggleDrawer(scope, drawerState) })
                },
                floatingActionButtonPosition = FabPosition.End,
                modifier = Modifier.fillMaxSize()
            ) { innerPadding ->
                NavHost(navController, startDestination = "eggtimer") {
                    composable("eggtimer") {
                        EggTimerScreen(
                            modifier = Modifier.padding(
                                innerPadding
                            )
                        )
                    }
                    composable("productlist") { ProductsListScreen() }
                    composable("recipeslist") { RecipesListScreen() }
                }
            }
        })
}

fun toggleDrawer(scope: CoroutineScope, drawerState: DrawerState) {
    scope.launch {
        if (drawerState.isClosed) {
            drawerState.open()
        } else {
            drawerState.close()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainTopAppBar(navController: NavController, onMenuClick: () -> Unit) {
    val currentBackStackEntry = navController.currentBackStackEntryAsState().value
    val currentRoute = currentBackStackEntry?.destination?.route
    val spacing = LocalSpacing.current

    TopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(vertical = spacing.small)
            ) {
                if (currentRoute != "eggtimer") {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                } else {
                    // Hamburger menu icon
                    IconButton(onClick = onMenuClick) {
                        Icon(
                            imageVector = Icons.Default.Menu,  // Use the appropriate icon for a menu
                            contentDescription = "Menu"
                        )
                    }
                }
                Text(stringResource(id = R.string.app_name), Modifier.weight(1f))
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            titleContentColor = MaterialTheme.colorScheme.onPrimary
        ),
        modifier = Modifier.height(100.dp)
    )
}
