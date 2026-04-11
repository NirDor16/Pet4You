package com.example.pet4you

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.example.pet4you.ui.navigation.NavGraph
import com.example.pet4you.ui.navigation.Routes
import com.example.pet4you.ui.theme.Pet4YouTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Pet4YouTheme {
                val navController = rememberNavController()
                NavGraph(navController = navController, startDestination = Routes.SPLASH)
            }
        }
    }
}
