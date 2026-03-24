package com.typ.nabda.deaf

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.typ.nabda.feature.deafblind.DeafBlindScreen
import com.typ.nabda.feature.deafblind.VibratorTestScreen
import com.typ.nabda.feature.deafblind.onboarding.DeafBlindPermissionsScreen
import com.typ.nabda.feature.pairing.PairingScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = determineStartupDestination()) {

        composable("deafblind_onboarding_permissions") {
            DeafBlindPermissionsScreen(
                onContinue = {
                    navController.navigate("deafblind_dashboard") {
                        popUpTo("deafblind_onboarding_permissions") { inclusive = true }
                    }
                }
            )
        }

        composable("pairing") { backStackEntry ->

        val isCaregiver = false

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

        composable("test_vibration") {
            VibratorTestScreen()
        }

        composable("deafblind_dashboard") {
            DeafBlindScreen {
                navController.navigate("test_vibration")
            }
        }
    }
}
