package com.example.myapplication.presentation.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.domain.usecase.CheckAuthStateUseCase
import com.example.myapplication.presentation.navigation.NavGraph
import com.example.myapplication.ui.theme.MyApplicationTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    // 1. Внедряем UseCase
    @Inject
    lateinit var checkAuthStateUseCase: CheckAuthStateUseCase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    // 2. Передаем его в NavGraph
                    NavGraph(checkAuthStateUseCase = checkAuthStateUseCase)
                }
            }
        }
    }
}