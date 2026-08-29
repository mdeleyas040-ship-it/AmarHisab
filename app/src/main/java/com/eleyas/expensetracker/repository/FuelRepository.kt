package com.eleyas.expensetracker.repository

import com.eleyas.expensetracker.model.FuelEntry
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class FuelRepository {

    private val firestore = FirebaseFirestore.getInstance()

    private fun fuelCollection(
        userId: String,
        vehicleId: String
    ) =
        firestore
            .collection("users")
            .document(userId)
            .collection("vehicles")
            .document(vehicleId)
            .collection("fuel")

    fun addFuel(
        userId: String,
        entry: FuelEntry,
        onSuccess: () -> Unit = {},
        onError: (Exception) -> Unit = {}
    ) {
        val document = fuelCollection(
            userId,
            entry.vehicleId
        ).document()

        val entryWithId = entry.copy(
            id = document.id
        )

        document
            .set(entryWithId)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener {
                onError(it)
            }
    }

    fun updateFuel(
        userId: String,
        entry: FuelEntry,
        onSuccess: () -> Unit = {},
        onError: (Exception) -> Unit = {}
    ) {
        if (entry.id.isBlank()) {
            onError(
                IllegalArgumentException(
                    "Fuel entry ID is empty"
                )
            )
            return
        }

        fuelCollection(
            userId,
            entry.vehicleId
        )
            .document(entry.id)
            .set(entry)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener {
                onError(it)
            }
    }

    fun deleteFuel(
        userId: String,
        vehicleId: String,
        fuelId: String,
        onSuccess: () -> Unit = {},
        onError: (Exception) -> Unit = {}
    ) {
        fuelCollection(
            userId,
            vehicleId
        )
            .document(fuelId)
            .delete()
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener {
                onError(it)
            }
    }

    fun observeFuel(
        userId: String,
        vehicleId: String,
        onData: (List<FuelEntry>) -> Unit,
        onError: (Exception) -> Unit = {}
    ): ListenerRegistration {

        return fuelCollection(
            userId,
            vehicleId
        )
            .orderBy("createdAt")
            .addSnapshotListener { snapshot, error ->

                if (error != null) {
                    onError(error)
                    return@addSnapshotListener
                }

                val entries =
                    snapshot
                        ?.documents
                        ?.mapNotNull { document ->
                            document.toObject(
                                FuelEntry::class.java
                            )
                        }
                        ?: emptyList()

                onData(entries)
            }
    }
}