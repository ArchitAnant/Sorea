package com.ari.drup.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ari.drup.data.FirebaseManager
import com.ari.drup.data.Friend
import com.ari.drup.data.FriendRequest
import com.ari.drup.data.User
import com.ari.drup.data.notification.RealtimeManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.collections.emptyList
import kotlin.collections.mutableListOf

class ProfilePageViewModel(
    private val firebaseManager: FirebaseManager,
    private val onboardingViewModel: OnboardingViewModel,
): ViewModel() {
    val realtimeManager = RealtimeManager()
    var user = onboardingViewModel.currUser
    var currentUserEmail = onboardingViewModel.currentUserEmail

    private val _friendList = MutableStateFlow(mutableListOf<Friend>())
    val friendList = _friendList.asStateFlow()

    fun updateVisibility(visibility: Int,context: Context){
        firebaseManager.changeVisibilityLevel(currentUserEmail!!,visibility)
        onboardingViewModel.getRegisteredUser(currentUserEmail!!)
        viewModelScope.launch {
            user = firebaseManager.getRegisteredUser(currentUserEmail!!)
            pushUserToCache(context,user!!,currentUserEmail!!)
        }
    }

    fun pushRequest(recUsername:String){
        realtimeManager.sendRequestNotification(recUsername, FriendRequest(user!!.username,currentUserEmail!!))
    }

    suspend fun pushUserToCache(context: Context, user: User, email: String){
        UserCache.saveUser(context,user,email)
    }

    suspend fun refreshUsernames(){
        firebaseManager.clearAllUsers()
        firebaseManager.setUsernames()
    }

    fun getRegisteredUsernames(): List<String>{
        return firebaseManager.getUsernames()
    }

    fun fetchFriendList() {
        viewModelScope.launch {
            _friendList.value = firebaseManager.getFriendList(currentUserEmail!!)
        }
    }

}