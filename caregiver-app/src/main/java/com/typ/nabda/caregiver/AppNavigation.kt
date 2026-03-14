package com.typ.nabda.caregiver

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.typ.nabda.feature.caregiver.CaregiverScreen
import com.typ.nabda.feature.caregiver.onboarding.CaregiverHowToScreen
import com.typ.nabda.feature.caregiver.onboarding.CaregiverPermissionsScreen
import com.typ.nabda.feature.caregiver.onboarding.CaregiverWelcomeScreen
import com.typ.nabda.feature.pairing.PairingScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = determineStartupDestination()) {
        composable("caregiver_onboarding_welcome") {
            CaregiverWelcomeScreen(
                onGetStarted = { navController.navigate("caregiver_onboarding_permissions") }
            )
        }

        composable("caregiver_onboarding_permissions") {
            CaregiverPermissionsScreen(
                onContinue = { navController.navigate("caregiver_onboarding_howto") }
            )
        }

        composable("caregiver_onboarding_howto") {
            CaregiverHowToScreen(
                onScanQr = {
                    navController.navigate("pairing/caregiver") {
                        popUpTo("caregiver_onboarding_welcome") { inclusive = true }
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
        }

        composable("caregiver_dashboard") {
            CaregiverScreen()
        }
    }
}
