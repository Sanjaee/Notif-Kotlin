package com.example.myapplication.ui.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class ViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(LoginViewModel::class.java) -> {
                LoginViewModel(application) as T
            }
            modelClass.isAssignableFrom(RegisterViewModel::class.java) -> {
                RegisterViewModel(application) as T
            }
            modelClass.isAssignableFrom(VerifyOTPViewModel::class.java) -> {
                VerifyOTPViewModel(application) as T
            }
            modelClass.isAssignableFrom(ForgotPasswordViewModel::class.java) -> {
                ForgotPasswordViewModel(application) as T
            }
            modelClass.isAssignableFrom(VerifyEmailViewModel::class.java) -> {
                VerifyEmailViewModel(application) as T
            }
            modelClass.isAssignableFrom(HomeViewModel::class.java) -> {
                HomeViewModel(application) as T
            }
            modelClass.isAssignableFrom(com.example.myapplication.ui.screen.ChatListViewModel::class.java) -> {
                com.example.myapplication.ui.screen.ChatListViewModel(application) as T
            }
            modelClass.isAssignableFrom(com.example.myapplication.ui.screen.SearchUserViewModel::class.java) -> {
                com.example.myapplication.ui.screen.SearchUserViewModel(application) as T
            }
            modelClass.isAssignableFrom(com.example.myapplication.ui.screen.ChatViewModel::class.java) -> {
                com.example.myapplication.ui.screen.ChatViewModel(application) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}

