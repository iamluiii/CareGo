package com.example.carego

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import com.example.carego.navigation.Screen
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

@Composable
fun LauncherScreen(navController: NavHostController) {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    LaunchedEffect(Unit) {
        val user = auth.currentUser
        if (user != null) {
            user.reload().await()
            if (user.isEmailVerified) {
                val uid = user.uid
                val userDoc = db.collection("users").document(uid).get().await()
                val caregiverDoc = db.collection("caregivers").document(uid).get().await()
                if (userDoc.exists()) {
                    navController.navigate(Screen.UserMainScreen.route) {
                        popUpTo(Screen.LauncherScreen.route) { inclusive = true }
                    }
                } else if (caregiverDoc.exists()) {
                    navController.navigate(Screen.CareGiverMainScreen.route) {
                        popUpTo(Screen.LauncherScreen.route) { inclusive = true }
                    }
                } else {
                    auth.signOut()
                    navController.navigate(Screen.UserLoginScreen.route) {
                        popUpTo(Screen.LauncherScreen.route) { inclusive = true }
                    }
                }
            } else {
                auth.signOut()
                navController.navigate(Screen.UserLoginScreen.route) {
                    popUpTo(Screen.LauncherScreen.route) { inclusive = true }
                }
            }
        } else {
            navController.navigate(Screen.UserLoginScreen.route) {
                popUpTo(Screen.LauncherScreen.route) { inclusive = true }
            }
        }
    }

    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}
