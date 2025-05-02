package com.example.carego

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DropdownMenuBox(
    selectedItem: String,
    label: String,
    items: List<String>,
    onItemSelected: (String) -> Unit,
    modifier: Modifier = Modifier.fillMaxWidth()
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        OutlinedTextField(
            value = selectedItem,
            onValueChange = {},
            label = {
                Text(
                    text = label,
                    fontFamily = poppinsFamily,
                    fontSize = 12.sp, // slightly smaller for better fit
                    maxLines = 1,     // prevent overflow
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp), // keep uniform height
            readOnly = true,
            textStyle = LocalTextStyle.current.copy(
                fontSize = 14.sp,
                fontFamily = poppinsFamily
            ),
            trailingIcon = {
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                }
            }
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.heightIn(max = 200.dp) // limit to approx 5 items
        ) {
            items.forEach { option ->
                DropdownMenuItem(
                    text = {
                        Text(option, fontFamily = poppinsFamily, fontSize = 14.sp)
                    },
                    onClick = {
                        onItemSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}


