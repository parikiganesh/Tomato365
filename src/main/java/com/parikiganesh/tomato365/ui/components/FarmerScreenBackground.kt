package com.parikiganesh.tomato365.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.parikiganesh.tomato365.R

@Composable
fun FarmerScreenBackground(
    backgroundImageName: String = stringResource(R.string.background_image_name),
    overlayAlpha: Float = 0.72f,
    content: @Composable BoxScope.() -> Unit
) {
    val context = LocalContext.current
    val isPreview = LocalInspectionMode.current
    val backgroundResId = remember(backgroundImageName) {
        context.resources.getIdentifier(backgroundImageName, "drawable", context.packageName)
    }
    val previewBackgroundResId = remember {
        context.resources.getIdentifier("tomato_bg", "drawable", context.packageName)
    }
    val resolvedBackgroundResId = if (isPreview) previewBackgroundResId else backgroundResId

    Box(modifier = Modifier.fillMaxSize()) {
        if (resolvedBackgroundResId != 0) {
            Image(
                painter = painterResource(id = resolvedBackgroundResId),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFFF7FBF6),
                                Color(0xFFFFFFFF),
                                Color(0xFFF3FAF2)
                            )
                        )
                    )
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White.copy(alpha = overlayAlpha))
        )

        content()
    }
}
