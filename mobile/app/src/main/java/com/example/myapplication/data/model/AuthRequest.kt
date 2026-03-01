package com.example.myapplication.data.model

import com.google.gson.annotations.SerializedName

data class RegisterRequest(
    @SerializedName("full_name")
    val fullName: String,
    
    @SerializedName("email")
    val email: String,
    
    @SerializedName("username")
    val username: String? = null,
    
    @SerializedName("phone")
    val phone: String? = null,
    
    @SerializedName("password")
    val password: String,
    
    @SerializedName("user_type")
    val userType: String? = null,
    
    @SerializedName("gender")
    val gender: String? = null,
    
    @SerializedName("date_of_birth")
    val dateOfBirth: String? = null
)

data class LoginRequest(
    @SerializedName("email")
    val email: String,
    
    @SerializedName("password")
    val password: String
)

data class VerifyOTPRequest(
    @SerializedName("email")
    val email: String,
    
    @SerializedName("otp_code")
    val otpCode: String
)

data class ResendOTPRequest(
    @SerializedName("email")
    val email: String
)

data class ForgotPasswordRequest(
    @SerializedName("email")
    val email: String
)

data class VerifyResetOTPRequest(
    @SerializedName("email")
    val email: String,
    
    @SerializedName("otp_code")
    val otpCode: String
)

data class VerifyResetPasswordRequest(
    @SerializedName("email")
    val email: String,
    
    @SerializedName("otp_code")
    val otpCode: String,
    
    @SerializedName("new_password")
    val newPassword: String
)

data class VerifyEmailRequest(
    @SerializedName("token")
    val token: String
)

data class GoogleOAuthRequest(
    @SerializedName("email")
    val email: String,
    
    @SerializedName("full_name")
    val fullName: String,
    
    @SerializedName("profile_photo")
    val profilePhoto: String? = null,
    
    @SerializedName("google_id")
    val googleId: String
)
