package com.typ.nabda.infrastructure.storage

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.typ.nabda.core.messaging.TokenRepository
import com.typ.nabda.core.pairing.PairingRepository
import kotlinx.coroutines.tasks.await

class TokenRepositoryImpl(
    private val firestore: FirebaseFirestore,
    private val pairingRepository: PairingRepository,
) : TokenRepository {

    override suspend fun updateMyToken(token: String) {
        val myUUID = pairingRepository.getMyUUID()
        if (myUUID.isNotEmpty()) {
            val userUpdate = hashMapOf("fcmToken" to token)
            firestore.collection("users").document(myUUID)
                .set(userUpdate, SetOptions.merge())
                .await()
        }
    }

    override suspend fun getRemoteToken(uuid: String): String? {
        return try {
            val doc = firestore.collection("users").document(uuid).get().await()
            doc.getString("fcmToken")
        } catch (e: Exception) {
            null
        }
    }
}
