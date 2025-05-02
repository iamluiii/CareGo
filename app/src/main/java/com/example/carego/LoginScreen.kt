package com.example.carego

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.carego.navigation.Screen
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Composable
fun LoginScreen(navController: NavController) {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    LaunchedEffect(Unit) {
        val user = auth.currentUser
        if (user != null && !user.isEmailVerified) {
            auth.signOut()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(R.drawable.caregobackground),
            contentDescription = null,
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.fillMaxSize()
        )
        LoginContent(auth, db, navController)
    }
}

@Composable
fun LoginContent(auth: FirebaseAuth, db: FirebaseFirestore, navController: NavController) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoginButtonEnabled by remember { mutableStateOf(true) }
    var isLoading by remember { mutableStateOf(false) }

    var showVerifyDialog by remember { mutableStateOf(false) }
    var resendCooldown by remember { mutableStateOf(0) }
    var hasSentInitialVerification by remember { mutableStateOf(false) }
    var pollingActive by remember { mutableStateOf(false) }

    fun startResendCooldown() {
        coroutineScope.launch {
            resendCooldown = 60
            while (resendCooldown > 0) {
                delay(1000)
                resendCooldown--
            }
        }
    }

    fun startVerificationPolling() {
        if (pollingActive) return
        pollingActive = true
        coroutineScope.launch {
            while (pollingActive) {
                delay(5000)
                val user = auth.currentUser
                user?.reload()?.await()
                if (user?.isEmailVerified == true) {
                    pollingActive = false
                    showVerifyDialog = false
                    Toast.makeText(context, "Email verified. Logging in...", Toast.LENGTH_SHORT).show()
                    navigateBasedOnRole(user.uid, db, navController)
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(R.drawable.caregologo),
            contentDescription = "Logo",
            modifier = Modifier.size(200.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text("Welcome", fontFamily = poppinsFamily, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF6bdbe0))
        Spacer(modifier = Modifier.height(20.dp))

        EmailField(email) { email = it }
        Spacer(modifier = Modifier.height(12.dp))
        PasswordField(password, passwordVisible, { passwordVisible = it }) { password = it }
        Spacer(modifier = Modifier.height(20.dp))

        if (isLoading) {
            CircularProgressIndicator(color = Color(0xFFed3782))
        } else {
            Button(
                onClick = {
                    if (email.isBlank() || password.isBlank()) {
                        Toast.makeText(context, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    isLoginButtonEnabled = false
                    isLoading = true

                    coroutineScope.launch {
                        try {
                            val result = auth.signInWithEmailAndPassword(email.trim(), password).await()
                            val user = result.user
                            user?.reload()?.await()
                            if (user != null && user.isEmailVerified) {
                                Toast.makeText(context, "Login successful", Toast.LENGTH_SHORT).show()
                                navigateBasedOnRole(user.uid, db, navController)
                            } else {
                                showVerifyDialog = true
                                hasSentInitialVerification = false
                                startVerificationPolling()
                            }
                        } catch (e: Exception) {
                            Toast.makeText(context, "Login failed: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                        delay(2000)
                        isLoginButtonEnabled = true
                        isLoading = false
                    }
                },
                enabled = isLoginButtonEnabled,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFed3782))
            ) {
                Text("Log In", fontFamily = poppinsFamily)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            TextButton(
                onClick = {
                    coroutineScope.launch {
                        navController.navigate(Screen.UserForgetPasswordScreen.route)
                        delay(2000)
                    }
                }
            ) {
                Text("Forget Password", fontFamily = poppinsFamily, color = Color.Black)
            }
            Spacer(modifier = Modifier.width(8.dp))
            TextButton(
                onClick = {
                    coroutineScope.launch {
                        navController.navigate("signup_screen")
                        delay(2000)
                    }
                }
            ) {
                Text("Sign Up", fontFamily = poppinsFamily, color = Color.Black)
            }
        }
    }

    if (showVerifyDialog) {
        AlertDialog(
            onDismissRequest = {
                showVerifyDialog = false
                pollingActive = false
            },
            title = { Text("Email Not Verified", fontFamily = poppinsFamily, fontWeight = FontWeight.Bold) },
            text = { Text("Please verify your email address to continue using the app.", fontFamily = poppinsFamily) },
            confirmButton = {
                Button(
                    onClick = {
                        auth.currentUser?.sendEmailVerification()?.addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(context, "Verification email sent.", Toast.LENGTH_SHORT).show()
                                hasSentInitialVerification = true
                                startResendCooldown()
                            } else {
                                Toast.makeText(context, "Failed to send email.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    enabled = resendCooldown == 0
                ) {
                    if (!hasSentInitialVerification) {
                        Text("Verify Now", fontFamily = poppinsFamily)
                    } else if (resendCooldown > 0) {
                        Text("Resend in ${resendCooldown}s", fontFamily = poppinsFamily)
                    } else {
                        Text("Resend Verification Email", fontFamily = poppinsFamily)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showVerifyDialog = false
                    pollingActive = false
                }) {
                    Text("Close", fontFamily = poppinsFamily)
                }
            }
        )
    }
}

suspend fun navigateBasedOnRole(uid: String, db: FirebaseFirestore, navController: NavController) {
    val userDoc = db.collection("users").document(uid).get().await()
    val caregiverDoc = db.collection("caregivers").document(uid).get().await()
    if (userDoc.exists()) {
        navController.navigate("user_main_screen") {
            popUpTo("login_screen") { inclusive = true }
        }
    } else if (caregiverDoc.exists()) {
        navController.navigate("caregiver_main_screen") {
            popUpTo("login_screen") { inclusive = true }
        }
    }
}

@Composable
fun EmailField(email: String, onEmailChange: (String) -> Unit) {
    OutlinedTextField(
        value = email,
        onValueChange = onEmailChange,
        label = { Text("Email", fontFamily = poppinsFamily) },
        singleLine = true,
        leadingIcon = {
            Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF6bdbe0))
        },
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun PasswordField(
    password: String,
    passwordVisibility: Boolean,
    onPasswordVisibilityChange: (Boolean) -> Unit,
    onPasswordChange: (String) -> Unit
) {
    OutlinedTextField(
        value = password,
        onValueChange = onPasswordChange,
        label = { Text("Password", fontFamily = poppinsFamily) },
        singleLine = true,
        visualTransformation = if (passwordVisibility) VisualTransformation.None else PasswordVisualTransformation(),
        leadingIcon = {
            Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFF6bdbe0))
        },
        trailingIcon = {
            val visibilityIcon = if (passwordVisibility) "Hide" else "Show"
            TextButton(onClick = { onPasswordVisibilityChange(!passwordVisibility) }) {
                Text(visibilityIcon, fontFamily = poppinsFamily, color = Color(0xFF6bdbe0))
            }
        },
        modifier = Modifier.fillMaxWidth()
    )
}
