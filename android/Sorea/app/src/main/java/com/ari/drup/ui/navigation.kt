package com.ari.drup.ui

import android.content.Context
import android.credentials.GetCredentialException
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.NoCredentialException
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.ari.drup.data.User
import com.ari.drup.ui.screens.communitychat.ChatScreen
import com.ari.drup.ui.screens.HolderScreen
import com.ari.drup.ui.screens.mainchat.MainChatScreen
import com.ari.drup.ui.screens.onboarding.RegisterUserScreen
import com.ari.drup.ui.screens.onboarding.SignInScreen
import com.ari.drup.ui.screens.onboarding.WaitingScreen
import com.ari.drup.viewmodels.GroupChatViewModel
import com.ari.drup.viewmodels.HomeScreenViewModel
import com.ari.drup.viewmodels.MainChatViewModel
import com.ari.drup.viewmodels.OnboardingViewModel
import com.ari.drup.viewmodels.regState
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch


@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
@Composable
fun NavGraph (
    mainChatViewModel: MainChatViewModel,
    chatViewModel : GroupChatViewModel,
              vm : OnboardingViewModel,
    homeScreenViewModel: HomeScreenViewModel,
              navHostController: NavHostController,
              context : Context,
              web_client_id: String,
              modifier: Modifier
) {
    var startDestination by remember {  mutableStateOf(Screen.signin.route)}
    LaunchedEffect(Unit) {
        val user: User? = UserCache.cachedUserFlow(context).firstOrNull()
        val email: String? = UserCache.cachedEmailFlow(context).firstOrNull()
        vm.setUserToken()
        if (user != null && email != null) {
            vm.currUser = user
            vm.currentUserEmail = email
            startDestination = Screen.holderScreen.route
        } else {
            startDestination = Screen.signin.route
        }
    }
    var chatId by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()
    val credentialManager = CredentialManager.create(context)
    var supsRegisterState = vm.successRegistered.collectAsState().value
    NavHost(navController = navHostController, startDestination = startDestination) {

        composable(route = Screen.mainChatScreen.route) {
            MainChatScreen(homeScreenViewModel,mainChatViewModel = mainChatViewModel, modifier)
        }

        composable(route = Screen.chatScreen.route) {
            ChatScreen(chatId, chatTitle = chatViewModel.chatTitle.value,chatViewModel,modifier)
        }


        composable(route = Screen.holderScreen.route) {
            HolderScreen(context,vm,chatViewModel,mainChatViewModel,homeScreenViewModel,navHostController,modifier)
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
                        vm.onGetCredentialResponse( context,result.credential)
                    } catch (e: GetCredentialCancellationException) {
                        // User cancelled the One Tap prompt — just log it or ignore
                        Log.d("CredExp", "User cancelled One Tap")
                    } catch (e: NoCredentialException) {
                        // No credentials saved on the device — show a toast or fallback
                        Toast.makeText(context, "No credentials available. Please add a Google Account on the device or check you internet connection.", Toast.LENGTH_SHORT).show()
                    } catch (e: GetCredentialException) {
                        // Other credential errors
                        Log.e("CredExp", "Other credential error", e)
                    } catch (e: Exception) {
                        // Any other unexpected exception
                        Log.e("CredExp", "Unexpected error", e)
                    }
                }
            })
        }
        composable(route=Screen.registerUser.route){
            RegisterUserScreen({
                coroutineScope.launch {
                    vm.navigateWaitingScreen()
                    if (vm.registerNewUser(it)){
                        supsRegisterState.value = regState.success
//                        vm.setUserToken()
                    }
                    else{
                        supsRegisterState.value = regState.failed
                    }
                }
            },modifier)
        }
        composable(route= Screen.onBoardWait.route) {
            WaitingScreen({success->
                if (success){
                    coroutineScope.launch {
                        vm.pushUserToCache(context, vm.currUser!!, vm.currentUserEmail.toString())
                        vm.setUserToken()
                    }
                    navHostController.navigate(Screen.holderScreen.route)
                }else{
                    navHostController.navigate(Screen.signin.route)
                }
            },vm,modifier)
        }

    }
}



sealed class Screen(val route:String){
    object mainChatScreen: Screen(route = "main_chat_screen")
    object chatScreen: Screen(route = "chat_screen")

    object signin : Screen(route = "sign_in")
    object registerUser : Screen(route = "register_user_screen")
    object onBoardWait : Screen(route = "onboard_wait_screen")

    object holderScreen : Screen(route = "holder_screen")

}