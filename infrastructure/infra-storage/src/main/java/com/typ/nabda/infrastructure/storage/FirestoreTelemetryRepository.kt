package com.typ.nabda.infrastructure.storage

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.typ.nabda.core.model.TelemetryHeartbeatPayload
import com.typ.nabda.core.model.TelemetryRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirestoreTelemetryRepository(
    private val firestore: FirebaseFirestore,
) : TelemetryRepository {

    override suspend fun storeTelemetry(caregiverId: String, payload: TelemetryHeartbeatPayload) {
        val deviceId = payload.deviceId
        val timestamp = payload.timestamp

        val docPath = "caregiverDevices/$caregiverId/trackedDevices/$deviceId"
        val telemetryPath = "$docPath/telemetry"

        try {
            firestore.runBatch { batch ->
                // Update Metadata
                val metadataRef = firestore.document(docPath)
                batch.update(
                    metadataRef, mapOf(
                        "lastSeen" to timestamp,
                        "currentBattery" to payload.batteryPercentage,
                        "connectivity" to payload.connectivitySource.name,
                        "isCharging" to payload.isCharging
                    )
                )

                // Add Telemetry Record
                val recordRef = firestore.collection(telemetryPath).document(timestamp.toString())
                batch.set(recordRef, payload)
            }.await()

            // Cap history to 100 records
            capTelemetryHistory(caregiverId, deviceId)
        } catch (e: Exception) {
            Log.e("FirestoreTelemetry", "Failed to store telemetry for $deviceId", e)
        }
    }

    private suspend fun capTelemetryHistory(caregiverId: String, deviceId: String) {
        val telemetryCollection = firestore.collection("caregiverDevices/$caregiverId/trackedDevices/$deviceId/telemetry")

        try {
            val snapshot = telemetryCollection
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .await()

            if (snapshot.size() > 100) {
                val excessDocs = snapshot.documents.subList(100, snapshot.size())
                firestore.runBatch { batch ->
                    excessDocs.forEach { batch.delete(it.reference) }
                }.await()
            }
        } catch (e: Exception) {
            Log.e("FirestoreTelemetry", "Failed to cap history for $deviceId", e)
        }
    }

    override fun observeLatest(caregiverId: String, deafDeviceId: String): Flow<TelemetryHeartbeatPayload?> = callbackFlow {
        val registration = firestore.collection("caregiverDevices/$caregiverId/trackedDevices/$deafDeviceId/telemetry")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(1)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("FirestoreTelemetry", "Error observing latest telemetry", error)
                    return@addSnapshotListener
                }

                val latest = snapshot?.documents?.firstOrNull()?.toObject(TelemetryHeartbeatPayload::class.java)
                trySend(latest)
            }

        awaitClose { registration.remove() }
    }
}
