package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import com.example.data.local.AppDatabase
import com.example.data.repository.EcoRepository
import com.example.ui.screens.EcoMindApp
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.EcoViewModel
import com.example.ui.viewmodel.EcoViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Edge-to-edge support
        enableEdgeToEdge()
        
        // Setup central Room Database unit & repo pipeline structure
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = EcoRepository(database)
        
        // Factory construct user ViewModel
        val factory = EcoViewModelFactory(application, repository)
        val viewModel = ViewModelProvider(this, factory)[EcoViewModel::class.java]

        setContent {
            MyApplicationTheme {
                EcoMindApp(viewModel = viewModel)
            }
        }
    }
}
