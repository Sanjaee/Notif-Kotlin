package com.example.myapplication.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.myapplication.ui.screen.*

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object ForgotPassword : Screen("forgot_password")
    object Home : Screen("home")
    object Profile : Screen("profile")
    object SearchUser : Screen("search_user")
    
    object Chat : Screen("chat/{conversationId}/{otherDisplayName}") {
        fun createRoute(conversationId: String, otherDisplayName: String = "Chat") =
            "chat/$conversationId/${android.net.Uri.encode(otherDisplayName)}"
    }
    
    object VerifyOTP : Screen("verify_otp/{email}") {
        fun createRoute(email: String) = "verify_otp/$email"
    }
    
    object VerifyOTPReset : Screen("verify_otp_reset/{email}") {
        fun createRoute(email: String) = "verify_otp_reset/$email"
    }
    
    object SetNewPassword : Screen("set_new_password/{email}/{otpCode}") {
        fun createRoute(email: String, otpCode: String) = "set_new_password/${android.net.Uri.encode(email)}/${android.net.Uri.encode(otpCode)}"
    }
    
    object VerifyEmail : Screen("verify_email/{token}") {
        fun createRoute(token: String) = "verify_email/$token"
    }
}

// Slide animation helper - Gojek style
// Masuk: slide dari kanan ke kiri
// Keluar (back): slide dari kiri ke kanan
private fun slideInFromRight() = slideInHorizontally(
    initialOffsetX = { it },
    animationSpec = tween(300)
) + fadeIn(animationSpec = tween(300))

private fun slideOutToLeft() = slideOutHorizontally(
    targetOffsetX = { -it },
    animationSpec = tween(300)
) + fadeOut(animationSpec = tween(300))

private fun slideInFromLeft() = slideInHorizontally(
    initialOffsetX = { -it },
    animationSpec = tween(300)
) + fadeIn(animationSpec = tween(300))

private fun slideOutToRight() = slideOutHorizontally(
    targetOffsetX = { it },
    animationSpec = tween(300)
) + fadeOut(animationSpec = tween(300))

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun NavGraph(
    startDestination: String = Screen.Login.route,
    onLogout: () -> Unit = {}
) {
    val navController = rememberNavController()
    
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(
            route = Screen.Login.route,
            enterTransition = { slideInFromRight() },
            exitTransition = { slideOutToLeft() },
            popEnterTransition = { slideInFromLeft() },
            popExitTransition = { slideOutToRight() }
        ) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                },
                onNavigateToForgotPassword = {
                    navController.navigate(Screen.ForgotPassword.route)
                },
                onNavigateToVerifyOTP = { email ->
                    navController.navigate(Screen.VerifyOTP.createRoute(email))
                }
            )
        }
        
        composable(
            route = Screen.Register.route,
            enterTransition = { slideInFromRight() },
            exitTransition = { slideOutToLeft() },
            popEnterTransition = { slideInFromLeft() },
            popExitTransition = { slideOutToRight() }
        ) {
            RegisterScreen(
                onRegisterSuccess = {
                    // Will navigate to VerifyOTP from ViewModel
                },
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                },
                onNavigateToVerifyOTP = { email ->
                    navController.navigate(Screen.VerifyOTP.createRoute(email)) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(
            route = Screen.VerifyOTP.route,
            arguments = listOf(navArgument("email") { type = NavType.StringType }),
            enterTransition = { slideInFromRight() },
            exitTransition = { slideOutToLeft() },
            popEnterTransition = { slideInFromLeft() },
            popExitTransition = { slideOutToRight() }
        ) { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email") ?: ""
            VerifyOTPScreen(
                email = email,
                onVerifySuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
        
        composable(
            route = Screen.ForgotPassword.route,
            enterTransition = { slideInFromRight() },
            exitTransition = { slideOutToLeft() },
            popEnterTransition = { slideInFromLeft() },
            popExitTransition = { slideOutToRight() }
        ) {
            ForgotPasswordScreen(
                onResetSuccess = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.ForgotPassword.route) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.ForgotPassword.route) { inclusive = true }
                    }
                },
                onNavigateToVerifyReset = { email ->
                    navController.navigate(Screen.VerifyOTPReset.createRoute(email))
                }
            )
        }
        
        composable(
            route = Screen.VerifyOTPReset.route,
            arguments = listOf(navArgument("email") { type = NavType.StringType }),
            enterTransition = { slideInFromRight() },
            exitTransition = { slideOutToLeft() },
            popEnterTransition = { slideInFromLeft() },
            popExitTransition = { slideOutToRight() }
        ) { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email") ?: ""
            VerifyOTPResetScreen(
                email = email,
                onOtpVerified = { verifiedEmail, otpCode ->
                    navController.navigate(Screen.SetNewPassword.createRoute(verifiedEmail, otpCode))
                },
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
        
        composable(
            route = Screen.SetNewPassword.route,
            arguments = listOf(
                navArgument("email") { type = NavType.StringType },
                navArgument("otpCode") { type = NavType.StringType }
            ),
            enterTransition = { slideInFromRight() },
            exitTransition = { slideOutToLeft() },
            popEnterTransition = { slideInFromLeft() },
            popExitTransition = { slideOutToRight() }
        ) { backStackEntry ->
            val email = android.net.Uri.decode(backStackEntry.arguments?.getString("email") ?: "")
            val otpCode = android.net.Uri.decode(backStackEntry.arguments?.getString("otpCode") ?: "")
            SetNewPasswordScreen(
                email = email,
                otpCode = otpCode,
                onResetSuccess = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
        
        composable(
            route = Screen.VerifyEmail.route,
            arguments = listOf(navArgument("token") { type = NavType.StringType }),
            enterTransition = { slideInFromRight() },
            exitTransition = { slideOutToLeft() },
            popEnterTransition = { slideInFromLeft() },
            popExitTransition = { slideOutToRight() }
        ) { backStackEntry ->
            val token = backStackEntry.arguments?.getString("token") ?: ""
            VerifyEmailScreen(
                token = token,
                onVerifySuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
        
        composable(
            route = Screen.Home.route,
            enterTransition = { slideInFromRight() },
            exitTransition = { slideOutToLeft() },
            popEnterTransition = { slideInFromLeft() },
            popExitTransition = { slideOutToRight() }
        ) {
            ChatListScreen(
                onOpenChat = { convId: String, otherName: String ->
                    navController.navigate(Screen.Chat.createRoute(convId, otherName))
                },
                onSearchUser = {
                    navController.navigate(Screen.SearchUser.route)
                },
                onOpenProfile = {
                    navController.navigate(Screen.Profile.route)
                },
                onUnauthorized = {
                    onLogout()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
        
        composable(
            route = Screen.Profile.route,
            enterTransition = { slideInFromRight() },
            exitTransition = { slideOutToLeft() },
            popEnterTransition = { slideInFromLeft() },
            popExitTransition = { slideOutToRight() }
        ) {
            ProfileScreen(
                onBack = { navController.popBackStack() },
                onLogout = {
                    onLogout()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
        
        composable(
            route = Screen.SearchUser.route,
            enterTransition = { slideInFromRight() },
            exitTransition = { slideOutToLeft() },
            popEnterTransition = { slideInFromLeft() },
            popExitTransition = { slideOutToRight() }
        ) {
            SearchUserScreen(
                onBack = { navController.popBackStack() },
                onUserSelected = { conversationId, otherDisplayName ->
                    navController.navigate(Screen.Chat.createRoute(conversationId, otherDisplayName)) {
                        popUpTo(Screen.SearchUser.route) { inclusive = true }
                    }
                },
                onUnauthorized = {
                    onLogout()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
        
        composable(
            route = Screen.Chat.route,
            arguments = listOf(
                navArgument("conversationId") { type = NavType.StringType },
                navArgument("otherDisplayName") { type = NavType.StringType }
            ),
            enterTransition = { slideInFromRight() },
            exitTransition = { slideOutToLeft() },
            popEnterTransition = { slideInFromLeft() },
            popExitTransition = { slideOutToRight() }
        ) { backStackEntry ->
            val conversationId = backStackEntry.arguments?.getString("conversationId") ?: ""
            val otherDisplayName = android.net.Uri.decode(backStackEntry.arguments?.getString("otherDisplayName") ?: "Chat")
            ChatScreen(
                conversationId = conversationId,
                otherDisplayName = otherDisplayName,
                onBack = { navController.popBackStack() },
                onUnauthorized = {
                    onLogout()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}

