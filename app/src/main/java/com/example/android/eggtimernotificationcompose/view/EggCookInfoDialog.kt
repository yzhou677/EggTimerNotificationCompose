package com.example.android.eggtimernotificationcompose.view

import android.app.Application
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import com.example.android.eggtimernotificationcompose.BuildConfig
import com.example.android.eggtimernotificationcompose.R

@Composable
fun EggCookInfoDialog(item: String, index: Int, app: Application) {
    val isTesting = BuildConfig.IS_TESTING
    var theIndex = index
    if (isTesting && index == 0) return
    else if (isTesting) theIndex--

    var showDialog by remember { mutableStateOf(false) }

    IconButton(onClick = { showDialog = true }) {
        Icon(Icons.Filled.Info, contentDescription = "Egg Cook Information")
    }

    val descriptionId = when (theIndex) {
        0 -> R.string.soft_boiled_info
        1 -> R.string.slightly_firmer_info
        2 -> R.string.firm_yolk_info
        3 -> R.string.hard_boiled_info
        else -> null
    }

    if (showDialog && descriptionId != null) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(item) },
            text = { Text(stringResource(id = descriptionId)) },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("OK")
                }
            },
        )
    }
}