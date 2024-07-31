package com.example.android.eggtimernotificationcompose.view

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.android.eggtimernotificationcompose.R
import com.example.android.eggtimernotificationcompose.theme.LocalSpacing
import com.example.android.eggtimernotificationcompose.viewmodel.EggTimerViewModel

/**
 * Displays an animated egg image when timer counts.
 *
 * @param viewModel, EggTimerViewModel instance.
 * @param spacing, Spacing instance, predefined dimensions.
 */
@Composable
fun WigglingImage(viewModel: EggTimerViewModel) {
    val isAlarmOn by viewModel.isAlarmOn.observeAsState(false)
    val spacing = LocalSpacing.current

    val infiniteTransition = rememberInfiniteTransition(label = "InfiniteTransition")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = if (isAlarmOn) 10f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 300, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "FloatAnimation"
    )

    Image(
        painter = painterResource(id = R.drawable.cooked_egg),
        contentDescription = "Egg",
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .padding(spacing.small)
            .graphicsLayer {
                rotationZ = rotation
            },
        contentScale = ContentScale.Fit
    )
}