package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.R
import com.example.ui.theme.LocalHesabatColors

@Composable
fun ScenicBackground(modifier: Modifier = Modifier) {
  val glassColors = LocalHesabatColors.current

  Box(modifier = modifier.fillMaxSize()) {
    Image(
        painter = painterResource(id = R.drawable.bg_landscape),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = Modifier.fillMaxSize(),
    )
    Box(
        modifier =
            Modifier.fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(glassColors.scrimTop, glassColors.scrimBottom)
                    )
                )
    )
  }
}
