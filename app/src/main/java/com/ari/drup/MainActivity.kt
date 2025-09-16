package com.ari.drup

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.ari.drup.ui.NavGraph
import com.ari.drup.ui.theme.DrupTheme
import com.ari.drup.viewmodels.GroupChatViewModel
import com.ari.drup.viewmodels.ViewModel


class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    @SuppressLint("ViewModelConstructorInComposable")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(
                Color.Black.hashCode(),
//                Color.Black.hashCode()
            ),
            navigationBarStyle = SystemBarStyle.dark(
                Color.Black.hashCode(),
//                Color.Black.hashCode()
            )
        )
        val chatViewModel = GroupChatViewModel()

        setContent {
            DrupTheme {
                val navHostController = rememberNavController()
                val vm = ViewModel(navHostController)
                Scaffold(modifier = Modifier
                    .background(Color.Black)
                    .fillMaxSize()) { innerPadding ->
                    NavGraph(
                        chatViewModel = chatViewModel,
                        vm = vm,
                        navHostController = navHostController,
                        context = this,
                        web_client_id = getString(R.string.WEB_CLIENT_ID),
                        modifier = Modifier
                            .padding(innerPadding)
                            .background(Color.Black)
                    )
                }

            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    DrupTheme {
        Greeting("Android")
    }
}