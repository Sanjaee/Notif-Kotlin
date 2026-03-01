package com.example.myapplication.data.model

import com.google.gson.annotations.SerializedName

data class User(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("email")
    val email: String,
    
    @SerializedName("username")
    val username: String? = null,
    
    @SerializedName("phone")
    val phone: String? = null,
    
    @SerializedName("full_name")
    val fullName: String,
    
    @SerializedName("user_type")
    val userType: String,
    
    @SerializedName("profile_photo")
    val profilePhoto: String? = null,
    
    @SerializedName("date_of_birth")
    val dateOfBirth: String? = null,
    
    @SerializedName("gender")
    val gender: String? = null,
    
    @SerializedName("is_active")
    val isActive: Boolean,
    
    @SerializedName("is_verified")
    val isVerified: Boolean,
    
    @SerializedName("last_login")
    val lastLogin: String? = null,
    
    @SerializedName("login_type")
    val loginType: String,
    
    @SerializedName("created_at")
    val createdAt: String,
    
    @SerializedName("updated_at")
    val updatedAt: String
)

