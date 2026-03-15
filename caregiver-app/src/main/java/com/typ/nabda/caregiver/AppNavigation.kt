package com.typ.nabda.caregiver

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.typ.nabda.caregiver.alerts.HighPriorityAlertOverlay
import com.typ.nabda.caregiver.service.CaregiverService
import com.typ.nabda.core.model.ActionPriority
import com.typ.nabda.feature.caregiver.CaregiverScreen
import com.typ.nabda.feature.caregiver.onboarding.CaregiverHowToScreen
import com.typ.nabda.feature.caregiver.onboarding.CaregiverPermissionsScreen
import com.typ.nabda.feature.caregiver.onboarding.CaregiverWelcomeScreen
import com.typ.nabda.feature.pairing.PairingScreen
import com.typ.nabda.infrastructure.localnetwork.client.LocalClientRegistry
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val currentAlert by LocalClientRegistry.alerts.collectAsStateWithLifecycle()

    Box(modifier = Modifier.fillMaxSize()) {
        NavHost(navController = navController, startDestination = "pairing_scanner") {
            composable("pairing_scanner") {
                val viewModel: com.typ.nabda.caregiver.pairing.WifiPairingViewModel = koinViewModel()
                com.typ.nabda.caregiver.pairing.PairingScannerScreen(
                    viewModel = viewModel,
                    onPairingSuccess = {
                        navController.navigate("caregiver_dashboard") {
                            popUpTo("pairing_scanner") { inclusive = true }
                        }
                    }
                )
            }

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

        // Show High Priority Alert Overlay globally
        currentAlert?.let { alert ->
            if (alert.priority == ActionPriority.EMERGENCY || alert.priority == ActionPriority.ASSISTANCE) {
                HighPriorityAlertOverlay(
                    alert = alert,
                    onReceived = {
                        CaregiverService.currentInstance?.acknowledgeAlert(alert.correlationId)
                    }
                )
            }
        }
    }
}
