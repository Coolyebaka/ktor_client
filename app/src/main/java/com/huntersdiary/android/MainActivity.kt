package com.huntersdiary.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.huntersdiary.android.core.ui.navigation.AppNavGraph
import com.huntersdiary.android.core.ui.theme.HuntersDiaryTheme
import com.huntersdiary.android.feature.auth.presentation.AuthViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : ComponentActivity() {
    private val authViewModel: AuthViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HuntersDiaryTheme {
                AppNavGraph(authViewModel = authViewModel)
            }
        }
    }
}
