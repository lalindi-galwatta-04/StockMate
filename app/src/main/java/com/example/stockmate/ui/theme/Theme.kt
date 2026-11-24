
package com.example.stockmate.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val StockMateColorScheme = lightColorScheme(

    primary = StockMateOrange,
    onPrimary = StockMateCharcoal,

    secondary = StockMateSurface,
    onSecondary = StockMateWhite,

    tertiary = StockMateSuccess,
    onTertiary = StockMateWhite,

    background = StockMateBackground,
    onBackground = StockMateText,

    surface = StockMateWhite,
    onSurface = StockMateText,

    surfaceVariant = StockMateBackground,
    onSurfaceVariant = StockMateMuted,

    outline = StockMateBorder,

    error = StockMateDanger,
    onError = StockMateWhite
)

@Composable
fun StockMateTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = StockMateColorScheme,
        typography = Typography,
        content = content
    )
}
