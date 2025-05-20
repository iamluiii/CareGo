package com.example.carego.navigation

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.carego.ChatHistoryScreen
import com.example.carego.ChatScreen
import com.example.carego.LauncherScreen
import com.example.carego.LoginScreen
import com.example.carego.RescheduleDialog
import com.example.carego.SettingsScreen
import com.example.carego.SignUpScreen
import com.example.carego.VerificationScreen
import com.example.carego.screens.caregiver.forgetpassword.CareGiverForgetPasswordScreen
import com.example.carego.screens.caregiver.mainscreen.CareGiverMainScreen
import com.example.carego.screens.caregiver.mainscreen.CareGiverProfileScreen
import com.example.carego.screens.caregiver.mainscreen.PendingBookingDetailsScreen
import com.example.carego.screens.caregiver.mainscreen.TransactionHistoryScreen
import com.example.carego.screens.user.forgotpasswordscreen.UserForgetPasswordScreen
import com.example.carego.screens.user.mainscreen.BookingScreen
import com.example.carego.screens.user.mainscreen.ProfileScreen
import com.example.carego.screens.user.mainscreen.UserMainScreen
import java.nio.charset.StandardCharsets

sealed class Screen(val route: String) {

    data object UserLoginScreen : Screen("user_login_screen")
    data object UserForgetPasswordScreen : Screen("user_forgot_password_screen")
    data object CareGiverForgetPasswordScreen : Screen("caregiver_forgot_password_screen")
    data object UserMainScreen : Screen("user_main_screen")
    data object CareGiverMainScreen : Screen("caregiver_main_screen")
    data object LauncherScreen : Screen("launcher_screen")

    data object BookingScreen : Screen("booking_screen/{appointmentId}/{caregiverId}/{date}/{timeSlot}") {
        fun createRoute(appointmentId: String, caregiverId: String, date: String, timeSlot: String): String {
            return "booking_screen/$appointmentId/$caregiverId/$date/$timeSlot"
        }
    }
    data object ChatScreen : Screen("chat/{appointmentId}/{userType}") {
        fun createRoute(appointmentId: String, userType: String): String {
            return "chat/$appointmentId/$userType"
        }
    }

    data object RescheduleDialog : Screen("reschedule/{appointmentId}/{userType}") {
        fun createRoute(appointmentId: String, userType: String): String {
            return "reschedule/$appointmentId/$userType"
        }
    }




    data object PendingBookingDetailsScreen : Screen("pending_booking_details/{appointmentId}") {
        fun createRoute(appointmentId: String): String {
            return "pending_booking_details/$appointmentId"
        }
    }
}



@Composable
fun CareGoNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.LauncherScreen.route
    ) {
        composable(Screen.LauncherScreen.route) {
            LauncherScreen(navController)
        }
        composable("user_forgot_password") {
            UserForgetPasswordScreen(
                navToLogin = { navController.navigate("login") }
            )
        }

        composable("transaction_history") {
            TransactionHistoryScreen(navController)
        }
        composable(
            route = Screen.RescheduleDialog.route,
            arguments = listOf(
                navArgument("appointmentId") { type = NavType.StringType },
                navArgument("userType") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val appointmentId = backStackEntry.arguments?.getString("appointmentId") ?: ""
            val userType = backStackEntry.arguments?.getString("userType") ?: "user"
            RescheduleDialog(appointmentId = appointmentId, userType = userType, navController = navController)
        }






        composable(route = Screen.UserLoginScreen.route) {
            LoginScreen(navController)
        }
        composable("signup_screen") {
            SignUpScreen(navController = navController)
        }

        composable(route = Screen.UserForgetPasswordScreen.route) {
            UserForgetPasswordScreen(
                navToLogin = {
                    navController.navigate(Screen.UserLoginScreen.route)
                }
            )
        }
        composable("chat_history") {
            ChatHistoryScreen(navController)
        }

        composable("settings") {
            SettingsScreen(navController)
        }

        composable(route = Screen.CareGiverForgetPasswordScreen.route) {
            CareGiverForgetPasswordScreen()
        }
        composable("SignUpScreen") {
            SignUpScreen(navController)
        }

        composable(route = Screen.UserMainScreen.route) {
            UserMainScreen(navController = navController)
        }
        composable("verification_screen") {
            VerificationScreen(navController = navController)
        }
        composable("verification_screen") {
            VerificationScreen(navController)
        }

        composable(
            route = Screen.ChatScreen.route,
            arguments = listOf(
                navArgument("appointmentId") { type = NavType.StringType },
                navArgument("userType") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val appointmentId = backStackEntry.arguments?.getString("appointmentId")
            val userType = backStackEntry.arguments?.getString("userType")

            if (appointmentId.isNullOrBlank() || userType.isNullOrBlank()) {
                // Prevent navigation if appointmentId or userType is missing
                Toast.makeText(navController.context, "Invalid chat details", Toast.LENGTH_SHORT).show()
                navController.popBackStack()
                return@composable
            }

            ChatScreen(appointmentId = appointmentId, navController = navController, userType = userType)
        }


        composable("profile") {
            ProfileScreen(navController = navController) // ✅ Correct
        }
        composable("caregiver_profile") {
            CareGiverProfileScreen(navController)
        }


        composable(route = Screen.CareGiverMainScreen.route) {
            CareGiverMainScreen(navController = navController)
        }
        composable(
            route = Screen.PendingBookingDetailsScreen.route,
            arguments = listOf(navArgument("appointmentId") { type = NavType.StringType })
        ) { backStackEntry ->
            val appointmentId = backStackEntry.arguments?.getString("appointmentId") ?: ""
            PendingBookingDetailsScreen(appointmentId = appointmentId, navController = navController)
        }
        composable(
            route = Screen.BookingScreen.route,
            arguments = listOf(
                navArgument("appointmentId") { type = NavType.StringType },
                navArgument("caregiverId") { type = NavType.StringType },
                navArgument("date") { type = NavType.StringType },
                navArgument("timeSlot") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val appointmentId = backStackEntry.arguments?.getString("appointmentId") ?: ""
            val caregiverId = backStackEntry.arguments?.getString("caregiverId") ?: ""
            val date = java.net.URLDecoder.decode(backStackEntry.arguments?.getString("date") ?: "", StandardCharsets.UTF_8.toString())
            val timeSlot = java.net.URLDecoder.decode(backStackEntry.arguments?.getString("timeSlot") ?: "", StandardCharsets.UTF_8.toString())

            BookingScreen(
                appointmentId = appointmentId,
                caregiverId = caregiverId,
                date = date,
                timeSlot = timeSlot,
                onBookingSuccess = {
                    navController.popBackStack()
                },
                onCancel = {
                    navController.popBackStack()
                }
            )
        }

    }
}

