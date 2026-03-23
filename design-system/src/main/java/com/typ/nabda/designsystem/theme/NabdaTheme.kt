package com.typ.nabda.designsystem.theme

import android.os.Build
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Shapes
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.PreviewDynamicColors
import androidx.compose.ui.unit.dp

private val DarkColorScheme = darkColorScheme(
    primary = NabdaBrown,
    onPrimary = NabdaCream,
    secondary = NabdaBrown,
    tertiary = NabdaBrown,
    background = Color(0xFF1C1B1F),
    surface = Color(0xFF1C1B1F),
    onBackground = NabdaCream,
    onSurface = NabdaCream,
)

private val LightColorScheme = lightColorScheme(
    primary = NabdaBrown,
    onPrimary = Color.White,
    secondary = NabdaBrown,
    tertiary = NabdaBrown,
    background = NabdaCream,
    onBackground = NabdaBrown,
    surface = NabdaCream,
    onSurface = NabdaBrown,
    error = NabdaRed,
    onError = Color.White
)

val NabdaShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(24.dp)
)

@Composable
fun NabdaTheme(
    darkTheme: Boolean = false,
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = NabdaShapes,
        content = content
    )
}

@PreviewDynamicColors
@Composable
private fun NabdaThemePreview() {
    NabdaTheme {
        Scaffold { paddingValues ->
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .padding(16.dp)
            ) {
                Text(
                    text = "Primary Color Text",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = "Secondary Color Text",
                    color = MaterialTheme.colorScheme.secondary,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = "Background Color Text",
                    color = MaterialTheme.colorScheme.background,
                    style = MaterialTheme.typography.bodyLarge
                )

                Button(
                    onClick = { },
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    Text("Primary Button")
                }
            }
        }
    }
}
