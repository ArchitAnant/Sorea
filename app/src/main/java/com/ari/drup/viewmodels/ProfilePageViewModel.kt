package com.ari.drup.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ari.drup.data.FirebaseManager
import com.ari.drup.data.User
import kotlinx.coroutines.launch

class ProfilePageViewModel(
    private val firebaseManager: FirebaseManager,
    private val onboardingViewModel: OnboardingViewModel,
): ViewModel() {
    var user = onboardingViewModel.currUser
    var currentUserEmail = onboardingViewModel.currentUserEmail

    fun updateVisibility(visibility: Int,context: Context){
        firebaseManager.changeVisibilityLevel(currentUserEmail!!,visibility)
        onboardingViewModel.getRegisteredUser(currentUserEmail!!)
        viewModelScope.launch {
            user = firebaseManager.getRegisteredUser(currentUserEmail!!)
            pushUserToCache(context,user!!,currentUserEmail!!)
        }
    }

    suspend fun pushUserToCache(context: Context, user: User, email: String){
        UserCache.saveUser(context,user,email)
    }



}