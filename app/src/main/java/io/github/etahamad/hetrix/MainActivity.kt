package io.github.etahamad.hetrix

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import io.github.etahamad.hetrix.ui.main.MainScreen
import io.github.etahamad.hetrix.ui.main.MonitorsViewModel
import io.github.etahamad.hetrix.ui.theme.HetrixTheme

class MainActivity : ComponentActivity() {

    private val viewModel: MonitorsViewModel by viewModels {
        (application as HetrixApplication).viewModelFactory
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            HetrixTheme {
                MainScreen(viewModel = viewModel)
            }
        }
    }
}
