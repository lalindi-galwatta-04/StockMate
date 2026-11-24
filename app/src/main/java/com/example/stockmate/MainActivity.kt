
package com.example.stockmate

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.stockmate.navigation.StockMateNavigation
import com.example.stockmate.ui.theme.StockMateTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            StockMateTheme {
                StockMateNavigation()
            }
        }
    }
}
