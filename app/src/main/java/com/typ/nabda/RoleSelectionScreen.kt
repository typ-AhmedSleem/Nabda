package com.typ.nabda

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun RoleSelectionScreen(
    onRoleSelected: (isCaregiver: Boolean) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Welcome to Nabda", style = MaterialTheme.typography.headlineLarge)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Choose your role", style = MaterialTheme.typography.titleMedium)

        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = { onRoleSelected(false) }, // DeafBlind
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
        ) {
            Text("I am Deaf-Blind User")
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { onRoleSelected(true) }, // Caregiver
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
        ) {
            Text("I am Caregiver")
        }
    }
}