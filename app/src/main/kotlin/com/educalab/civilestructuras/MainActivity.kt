package com.educalab.civilestructuras

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.educalab.civilestructuras.ui.navigation.ConstructopolisNavGraph
import com.educalab.civilestructuras.ui.theme.ConstructopolisTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val container = (application as ConstructopolisApp).container
        setContent {
            ConstructopolisTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    ConstructopolisNavGraph(container = container)
                }
            }
        }
    }
}
