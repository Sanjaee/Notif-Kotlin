package com.example.myapplication.data.api

import com.example.myapplication.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface AuthApiService {
    @POST("api/v1/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<ApiResponse<RegisterResponse>>
    
    @POST("api/v1/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<ApiResponse<AuthResponse>>
    
    @POST("api/v1/auth/verify-otp")
    suspend fun verifyOTP(@Body request: VerifyOTPRequest): Response<ApiResponse<AuthResponse>>
    
    @POST("api/v1/auth/resend-otp")
    suspend fun resendOTP(@Body request: ResendOTPRequest): Response<ApiResponse<Nothing>>
    
    @POST("api/v1/auth/forgot-password")
    suspend fun forgotPassword(@Body request: ForgotPasswordRequest): Response<ApiResponse<Nothing>>
    
    @POST("api/v1/auth/verify-reset-password")
    suspend fun verifyResetPassword(@Body request: VerifyResetPasswordRequest): Response<ApiResponse<Nothing>>
    
    @POST("api/v1/auth/verify-email")
    suspend fun verifyEmail(@Body request: VerifyEmailRequest): Response<ApiResponse<AuthResponse>>
    
    @GET("api/v1/auth/me")
    suspend fun getMe(@Header("Authorization") token: String): Response<ApiResponse<Map<String, User>>>
    
    @POST("api/v1/auth/google-oauth")
    suspend fun googleOAuth(@Body request: GoogleOAuthRequest): Response<ApiResponse<AuthResponse>>
}

