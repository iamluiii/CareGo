package com.example.carego.helpers

import android.content.Context
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth

fun sendPasswordReset(email: String, context: Context) {
    if (email.isBlank()) {
        Toast.makeText(context, "Please enter your email", Toast.LENGTH_SHORT).show()
        return
    }

    FirebaseAuth.getInstance().sendPasswordResetEmail(email)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(context, "Password reset email sent", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Failed to send reset email", Toast.LENGTH_SHORT).show()
            }
        }
}
