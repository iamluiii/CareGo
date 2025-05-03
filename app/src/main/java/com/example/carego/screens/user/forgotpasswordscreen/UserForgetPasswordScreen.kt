package com.example.carego.screens.user.forgotpasswordscreen

import android.os.Handler
import android.os.Looper
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.carego.helpers.sendPasswordReset

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserForgetPasswordScreen(navToLogin: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var isButtonEnabled by remember { mutableStateOf(true) }
    val context = LocalContext.current
    val screenWidth = LocalConfiguration.current.screenWidthDp
    val iconSize = (screenWidth * 0.22).dp
    val headerFontSize = (screenWidth * 0.10).sp
    val subtitleFontSize = (screenWidth * 0.05).sp

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                actions = {
                    IconButton(onClick = navToLogin) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            modifier = Modifier.size(50.dp) // ✅ Increase size (default is 24.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color.White
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.height(100.dp))
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = "Lock Icon",
                tint = Color(0xFFed3782),
                modifier = Modifier.size(iconSize)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Forgot",
                fontSize = headerFontSize,
                color = Color(0xFFed3782),
                fontWeight = FontWeight.Bold // ✅ added
            )

            Text(
                text = "Password?",
                fontSize = headerFontSize,
                color = Color(0xFFed3782),   // I assume you want this lighter pink still
                fontWeight = FontWeight.Bold // ✅ added
            )


            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "No worries, we'll send you reset instructions",
                fontSize = subtitleFontSize,
                color = Color(0xFF4102fb),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp)
            )

            Spacer(modifier = Modifier.height(100.dp)) // ✅ Add this to push pink card lower
            Spacer(modifier = Modifier.weight(1f))     // Keeps everything centered


            // Bottom pink card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .background(Color(0xFFFF66C4), shape = RoundedCornerShape(topStart = 48.dp, topEnd = 48.dp))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp, vertical = 32.dp)
                        .verticalScroll(rememberScrollState()), // ✅ enables scroll + layout room
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Top
                ) {
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Email",
                        color = Color(0xFF3e0ae2),
                        fontSize = 14.sp,
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .padding(bottom = 4.dp)
                    )

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        placeholder = { Text("Enter your Email") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .height(60.dp),
                        shape = RoundedCornerShape(50.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedBorderColor = Color.Black,
                            unfocusedBorderColor = Color.Gray,
                            focusedTextColor = Color.Black,         // ✅ correct
                            unfocusedTextColor = Color.Black,       // ✅ correct
                            focusedPlaceholderColor = Color.Gray,   // ✅ soft placeholder
                            unfocusedPlaceholderColor = Color.Gray  // ✅ soft placeholder
                        )
,
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = "Email Icon",
                                tint = Color.Black
                            )
                        }
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            isButtonEnabled = false
                            sendPasswordReset(email.trim(), context)
                            Handler(Looper.getMainLooper()).postDelayed({
                                isButtonEnabled = true
                            }, 2000)
                        },
                        enabled = isButtonEnabled,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4102FB)),
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .height(60.dp),
                        shape = RoundedCornerShape(50)
                    ) {
                        Text("Reset Password", color = Color.White, fontSize = 16.sp)
                    }

                }
            }

        }
    }
}
