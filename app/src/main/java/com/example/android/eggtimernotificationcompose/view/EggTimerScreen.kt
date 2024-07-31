package com.example.android.eggtimernotificationcompose.view

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.android.eggtimernotificationcompose.R
import com.example.android.eggtimernotificationcompose.model.CustomTimer
import com.example.android.eggtimernotificationcompose.theme.LocalSpacing
import com.example.android.eggtimernotificationcompose.viewmodel.EggTimerViewModel
import com.example.android.eggtimernotificationcompose.viewmodel.EggTimerViewModelFactory
import com.example.android.eggtimernotificationcompose.util.setElapsedTime

@Composable
fun EggTimerScreen(
    modifier: Modifier = Modifier
) {
    val app = LocalContext.current.applicationContext as Application
    val factory = EggTimerViewModelFactory(app)
    val viewModel: EggTimerViewModel = viewModel(factory = factory)

    val spacing = LocalSpacing.current

    val elapsedTime by viewModel.elapsedTime.observeAsState(0L)
    val timeSelection by viewModel.timeSelection.observeAsState(0)
    val isAlarmOn by viewModel.isAlarmOn.observeAsState(false)
    val items by viewModel.eggTimerItems.observeAsState(emptyList())

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(spacing.medium),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        WigglingImage(viewModel = viewModel)

        Spacer(modifier = Modifier.height(spacing.medium))

        Box(
            modifier = Modifier
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            BasicText(
                text = stringResource(id = R.string.egg_prompt),
                style = MaterialTheme.typography.headlineMedium.copy(
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Medium,
                )
            )
        }

        Spacer(modifier = Modifier.height(spacing.large))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = spacing.large, vertical = spacing.medium)
        ) {
            CustomSpinner(
                timeSelection,
                viewModel,
                items = items.toTypedArray(),
                modifier = Modifier.weight(1f),
                isAlarmOn = isAlarmOn,
                app
            )

            Switch(
                checked = isAlarmOn,
                onCheckedChange = { viewModel.setAlarm(it) },
                modifier = Modifier.align(Alignment.CenterVertically)
            )
        }

        Spacer(modifier = Modifier.height(spacing.extraLarge))

        BasicText(
            text = setElapsedTime(elapsedTime),
            style = MaterialTheme.typography.headlineMedium.copy(
                color = MaterialTheme.colorScheme.onBackground
            ),
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(horizontal = spacing.large, vertical = spacing.medium)
        )
    }
}


@Composable
fun CustomSpinner(
    timeSelection: Int,
    viewModel: EggTimerViewModel,
    items: Array<String>,
    modifier: Modifier = Modifier,
    isAlarmOn: Boolean,
    app: Application
) {
    var expanded by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }
    val spacing = LocalSpacing.current

    Box(modifier = modifier
        .padding(spacing.small)
        .clickable { expanded = !expanded && !isAlarmOn }
        .background(
            color = MaterialTheme.colorScheme.primaryContainer,
            shape = RoundedCornerShape(8.dp)
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = spacing.medium, vertical = spacing.small)
        ) {
            Text(
                text = items[timeSelection],
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontSize = 18.sp
                ),
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(8.dp)
                )
        ) {
            items.forEachIndexed { index, label ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = label, style = MaterialTheme.typography.bodyLarge.copy(
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    fontSize = 18.sp
                                )
                            )
                        },
                        onClick = {
                            viewModel.setTimeSelected(index)
                            expanded = false
                        },
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .weight(1f)
                            .padding(horizontal = spacing.medium, vertical = spacing.small)
                    )

                    if (index < viewModel.defaultEggTimerOptionsSize) {
                        EggCookInfoDialog(label, index, app)
                    }

                    if (index >= viewModel.defaultEggTimerOptionsSize) {
                        IconButton(
                            onClick = {
                                viewModel.deleteCustomTimer(index)
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Remove item",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }
            // Add the custom item
            CustomListItem {
                expanded = false
                showDialog = true
            }
        }

        if (showDialog) {
            CustomDialog(
                onDismiss = { showDialog = false },
                onSave = { text, minutes ->
                    viewModel.saveCustomTimers(CustomTimer(text, minutes))
                    showDialog = false
                }
            )
        }
    }
}

@Composable
fun CustomListItem(onClick: () -> Unit) {
    val spacing = LocalSpacing.current

    DropdownMenuItem(
        text = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(id = R.string.custom),
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontSize = 18.sp
                    )
                )
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        },
        onClick = onClick,
        modifier = Modifier
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(horizontal = spacing.medium, vertical = spacing.small)
    )
}

@Composable
fun CustomDialog(
    onDismiss: () -> Unit,
    onSave: (String, Int) -> Unit
) {
    var text by remember {
        mutableStateOf("")
    }
    var minutes by remember {
        mutableStateOf("")
    }
    var isTextValid by remember {
        mutableStateOf(true)
    }
    var isMinutesValid by remember {
        mutableStateOf(true)
    }
    val spacing = LocalSpacing.current

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties()) {
        Surface(
            shape = RoundedCornerShape(spacing.medium),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.padding(spacing.large)) {
                Text(
                    text = stringResource(id = R.string.custom_timer),
                    style = MaterialTheme.typography.headlineMedium.copy(
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    modifier = Modifier.padding(bottom = spacing.medium)
                )
                OutlinedTextField(
                    value = text,
                    onValueChange = {
                        text = it
                        isTextValid = it.all { char -> char.isLetterOrDigit() }
                    },
                    label = { Text(stringResource(id = R.string.label)) },
                    modifier = Modifier.fillMaxWidth(),
                    isError = !isTextValid
                )
                if (!isTextValid) {
                    Text(
                        text = stringResource(id = R.string.custom_timer_label_error),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                OutlinedTextField(
                    value = minutes,
                    onValueChange = {
                        minutes = it
                        val minutesInt = it.toIntOrNull()
                        isMinutesValid = minutesInt != null && minutesInt in 1..99
                    },
                    label = { Text(stringResource(id = R.string.custom_timer_minutes)) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                    isError = !isMinutesValid
                )
                if (!isMinutesValid) {
                    Text(
                        text = stringResource(id = R.string.custom_timer_minutes_error),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = spacing.medium)
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(id = R.string.cancel))
                    }
                    TextButton(
                        onClick = {
                            if (isTextValid && isMinutesValid && text.isNotBlank() && minutes.toIntOrNull() != null) {
                                onSave(text, minutes.toInt())
                            }
                        }
                    ) {
                        Text(stringResource(id = R.string.save))
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewEggTimerScreen() {
    MaterialTheme {
        Surface {
            EggTimerScreen()
        }
    }
}