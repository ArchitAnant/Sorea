package com.ari.drup.viewmodels

import android.util.Log
import androidx.credentials.Credential
import androidx.credentials.CustomCredential
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.ari.drup.ui.Screen
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.Firebase
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ViewModel (
    private val navHostController: NavHostController
) : ViewModel() {
    var currentUserEmail: String? = null
    fun  onGetCredentialResponse(credential: Credential){
        viewModelScope.launch {
            if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val firebaseCred = GoogleAuthProvider.getCredential(googleIdTokenCredential.idToken,null)
                val authResult = Firebase.auth.signInWithCredential(firebaseCred).await()
                currentUserEmail = authResult.user?.email

                Log.d("UserName","${authResult.user?.displayName}")
                Log.d("UserName","${authResult.user?.email}")
                Log.d("UserName","${authResult.user?.photoUrl}")

                navigateHomePage()
            }
        }
    }

    fun navigateHomePage(){
        navHostController.navigate(Screen.communitySetup.route)
    }
}