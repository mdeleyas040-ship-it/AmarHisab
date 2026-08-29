package com.eleyas.expensetracker.repository

import com.eleyas.expensetracker.model.PartEntry
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class PartRepository {

    private val firestore = FirebaseFirestore.getInstance()

    private fun partsCollection(
        userId: String,
        vehicleId: String
    ) =
        firestore
            .collection("users")
            .document(userId)
            .collection("vehicles")
            .document(vehicleId)
            .collection("parts")

    fun addPart(
        userId: String,
        entry: PartEntry,
        onSuccess: () -> Unit = {},
        onError: (Exception) -> Unit = {}
    ) {
        val document = partsCollection(
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

    fun updatePart(
        userId: String,
        entry: PartEntry,
        onSuccess: () -> Unit = {},
        onError: (Exception) -> Unit = {}
    ) {
        if (entry.id.isBlank()) {
            onError(
                IllegalArgumentException(
                    "Part entry ID is empty"
                )
            )
            return
        }

        partsCollection(
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

    fun deletePart(
        userId: String,
        vehicleId: String,
        partId: String,
        onSuccess: () -> Unit = {},
        onError: (Exception) -> Unit = {}
    ) {
        partsCollection(
            userId,
            vehicleId
        )
            .document(partId)
            .delete()
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener {
                onError(it)
            }
    }

    fun observeParts(
        userId: String,
        vehicleId: String,
        onData: (List<PartEntry>) -> Unit,
        onError: (Exception) -> Unit = {}
    ): ListenerRegistration {

        return partsCollection(
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
                                PartEntry::class.java
                            )
                        }
                        ?: emptyList()

                onData(entries)
            }
    }
}