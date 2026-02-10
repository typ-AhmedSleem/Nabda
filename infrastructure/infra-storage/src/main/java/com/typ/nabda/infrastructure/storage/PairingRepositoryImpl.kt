package com.typ.nabda.infrastructure.storage

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.typ.nabda.core.model.PairedDevice
import com.typ.nabda.core.pairing.PairingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

private val Context.dataStore by preferencesDataStore(name = "pairing_settings")

class PairingRepositoryImpl(
    private val context: Context,
    private val firestore: FirebaseFirestore,
) : PairingRepository {

    private val KEY_PAIRED_DEVICE_UUID = stringPreferencesKey("paired_device_uuid")
    private val KEY_PAIRED_DEVICE_TOKEN = stringPreferencesKey("paired_device_token") // Local cache of token
    private val KEY_MY_UUID = stringPreferencesKey("my_uuid")

    override suspend fun getPairedDevice(): PairedDevice? {
        val preferences = context.dataStore.data.first()
        val uuid = preferences[KEY_PAIRED_DEVICE_UUID]
        val token = preferences[KEY_PAIRED_DEVICE_TOKEN]
        return if (uuid != null && token != null) {
            PairedDevice(uuid, token)
        } else {
            null
        }
    }

    override suspend fun setPairedDevice(device: PairedDevice) {
        context.dataStore.edit { preferences ->
            preferences[KEY_PAIRED_DEVICE_UUID] = device.uuid
            preferences[KEY_PAIRED_DEVICE_TOKEN] = device.fcmToken
        }

        val myUUID = getMyUUID()
        if (myUUID.isNotEmpty()) {
            val userUpdate = hashMapOf("pairedWith" to device.uuid)
            firestore.collection("users").document(myUUID)
                .set(userUpdate, SetOptions.merge())
                .await()
        }
    }

    override suspend fun clearPairedDevice() {
        context.dataStore.edit { preferences ->
            preferences.remove(KEY_PAIRED_DEVICE_UUID)
            preferences.remove(KEY_PAIRED_DEVICE_TOKEN)
        }
        val myUUID = getMyUUID()
        if (myUUID.isNotEmpty()) {
            val userUpdate = hashMapOf<String, Any?>("pairedWith" to null)
            firestore.collection("users").document(myUUID)
                .set(userUpdate, SetOptions.merge())
                .await()
        }
    }

    override fun getPairedDeviceFlow(): Flow<PairedDevice?> {
        return context.dataStore.data.map { preferences ->
            val uuid = preferences[KEY_PAIRED_DEVICE_UUID]
            val token = preferences[KEY_PAIRED_DEVICE_TOKEN]
            if (uuid != null && token != null) {
                PairedDevice(uuid, token)
            } else {
                null
            }
        }
    }

    override suspend fun setMyUUID(uuid: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_MY_UUID] = uuid
        }
    }

    override suspend fun getMyUUID(): String {
        val preferences = context.dataStore.data.first()
        return preferences[KEY_MY_UUID] ?: ""
    }
}
