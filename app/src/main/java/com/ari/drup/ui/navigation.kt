package com.ari.drup.ui

import android.R
import android.content.Context
import android.credentials.GetCredentialException
import android.os.Build
import android.provider.Settings.System.getString
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.ari.drup.ui.screens.ChatScreen
import com.ari.drup.ui.screens.CommunityPage
import com.ari.drup.ui.screens.SignInScreen
import com.ari.drup.viewmodels.GroupChatViewModel
import com.ari.drup.viewmodels.ViewModel
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import kotlinx.coroutines.launch



@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
@Composable
fun NavGraph (chatViewModel : GroupChatViewModel,
              vm : ViewModel,
              navHostController: NavHostController,
              context : Context,
              web_client_id: String,
              modifier: Modifier
) {
//    val currentUser = Firebase.auth.currentUser
    var chatId by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()
    val credentialManager = CredentialManager.create(context)
    NavHost(navController = navHostController, startDestination = Screen.signin.route) {

        composable(route = Screen.communitySetup.route) {
            CommunityPage({id,title ->
                chatId = id
                chatViewModel.chatTitle.value = title
                Log.d("title",chatViewModel.chatTitle.value)
                navHostController.navigate(Screen.chatScreen.route)
            },
                modifier)
        }
        composable(route = Screen.chatScreen.route) {
            ChatScreen(chatId, chatTitle = chatViewModel.chatTitle.value,chatViewModel,modifier)
        }
        composable(route = Screen.signin.route) {
            SignInScreen({

                val googleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(web_client_id)
                    .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                coroutineScope.launch {
                    try {
                        val result = credentialManager.getCredential(
                            request = request,
                            context = context
                        )
                        vm.onGetCredentialResponse(result.credential)
                    }
                    catch (e : GetCredentialException){
                        Log.e("CredExp","Cred  Error",e)
                    }
                }
            })
        }
    }
}

sealed class Screen(val route:String){
    object homeScreen: Screen(route = "home_screen")
    object communitySetup: Screen(route = "community_screen")
    object chatScreen: Screen(route = "chat_screen")
    object  signin : Screen(route = "sign_in")
}