package com.ari.drup.viewmodels

import android.content.Context
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.credentials.Credential
import androidx.credentials.CustomCredential
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.ari.drup.data.FirebaseManager
import com.ari.drup.data.User
import com.ari.drup.ui.Screen
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.Firebase
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

enum class regState {
    waiting,
    success,
    failed
}

class OnboardingViewModel (
    private val navHostController: NavHostController
) : ViewModel() {
    var currentUserEmail: String? = null
    var currUser : User? = null
    val firebaseManager = FirebaseManager()

    private val _successRegistered = MutableStateFlow(mutableStateOf(regState.waiting))
    val successRegistered = _successRegistered.asStateFlow()

    fun setUser(user: User){
        currUser = user;
    }

    fun getUser() : User?{
        return currUser;
    }

    fun  onGetCredentialResponse(context: Context, credential: Credential){
        viewModelScope.launch {
            if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val firebaseCred = GoogleAuthProvider.getCredential(googleIdTokenCredential.idToken,null)
                val authResult = Firebase.auth.signInWithCredential(firebaseCred).await()
                currentUserEmail = authResult.user?.email
                Log.d("credential",currentUserEmail.toString())
                if (currentUserEmail != null) {

                    navHostController.navigate(Screen.onBoardWait.route)
                    if (isUserRegistered(currentUserEmail.toString())){
                        currUser = firebaseManager.getRegisteredUser(currentUserEmail.toString())
                        _successRegistered.value.value = regState.success
                        //log the user for debug
                        pushUserToCache(context,currUser!!,currentUserEmail.toString())

                        Log.d("reg_user",currUser.toString())
                        navigateHolderPage()
                    }
                    else{
                        _successRegistered.value.value = regState.waiting
                        Log.d("credential","not registered")
                        registerUserPage()
                    }
                }
            }
        }
    }


    suspend fun pushUserToCache(context: Context, user: User, email: String){
        UserCache.saveUser(context,user,email)
    }

    suspend fun isUserRegistered(email: String) : Boolean{
        return firebaseManager.checkRegisteredUsers(email)
    }

    suspend fun registerNewUser(newUser : User): Boolean{
        navigateWaitingScreen()
        if (firebaseManager.registerNewUser(newUser,currentUserEmail.toString())){
            currUser = newUser
            return true
        }
        return false
    }


    fun navigateHolderPage(){
        navHostController.navigate(Screen.holderScreen.route)
    }
    fun registerUserPage(){
        navHostController.navigate(Screen.registerUser.route)
    }

    fun navigateWaitingScreen(){
        navHostController.navigate(Screen.onBoardWait.route)
    }
}