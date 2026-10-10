package com.belectronicsvp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.belectronicsvp.ui.navigation.AppNavGraph
import com.belectronicsvp.ui.theme.BElectronicsTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            BElectronicsTheme {
                val navController = rememberNavController()
                AppNavGraph(
                    navController = navController,
                    app = BElectronicsApp.instance
                )
            }
        }
    }
}
