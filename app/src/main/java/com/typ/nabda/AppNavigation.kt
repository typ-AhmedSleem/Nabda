package com.typ.nabda

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.typ.nabda.feature.caregiver.CaregiverScreen
import com.typ.nabda.feature.deafblind.DeafBlindScreen
import com.typ.nabda.feature.pairing.PairingScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "role_selection") {
        composable("role_selection") {
            RoleSelectionScreen(
                onRoleSelected = { isCaregiver ->
                    if (isCaregiver) {
                        navController.navigate("pairing/caregiver")
                    } else {
                        navController.navigate("pairing/deafblind")
                    }
                }
            )
        }

        composable("pairing/{role}") { backStackEntry ->
            val role = backStackEntry.arguments?.getString("role")
            val isCaregiver = role == "caregiver"

            PairingScreen(
                isCaregiver = isCaregiver,
                onPairingComplete = {
                    if (isCaregiver) {
                        navController.navigate("caregiver_dashboard") {
                            popUpTo("role_selection") { inclusive = true }
                        }
                    } else {
                        navController.navigate("deafblind_dashboard") {
                            popUpTo("role_selection") { inclusive = true }
                        }
                    }
                }
            )
            // Note: We might want to pass 'isCaregiver' to PairingScreen to set initial state
            // But ParsingViewModel logic handles state. For demo, manually switching in UI is fine.
        }

        composable("caregiver_dashboard") {
            CaregiverScreen()
        }

        composable("deafblind_dashboard") {
            DeafBlindScreen()
        }
    }
}
