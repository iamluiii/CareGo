package com.example.carego.screens.availabilityscreen

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AvailableDatesList(dates: List<String>) {
    Text(
        text = "Your Available Dates:",
        fontSize = 18.sp,
        fontFamily = FontFamily.SansSerif,
        modifier = Modifier.padding(bottom = 8.dp)
    )

    if (dates.isEmpty()) {
        Text("No availability set yet.")
    } else {
        dates.forEach { date ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Text(
                    text = date,
                    modifier = Modifier.padding(16.dp),
                    fontFamily = FontFamily.SansSerif
                )
            }
        }
    }
}
