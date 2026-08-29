package com.eleyas.expensetracker.repository

import com.eleyas.expensetracker.model.ServiceEntry
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class ServiceRepository {

    private val firestore = FirebaseFirestore.getInstance()

    private fun serviceCollection(
        userId: String,
        vehicleId: String
    ) =
        firestore
            .collection("users")
            .document(userId)
            .collection("vehicles")
            .document(vehicleId)
            .collection("services")

    fun addService(
        userId: String,
        entry: ServiceEntry,
        onSuccess: () -> Unit = {},
        onError: (Exception) -> Unit = {}
    ) {
        val document = serviceCollection(
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

    fun updateService(
        userId: String,
        entry: ServiceEntry,
        onSuccess: () -> Unit = {},
        onError: (Exception) -> Unit = {}
    ) {
        if (entry.id.isBlank()) {
            onError(
                IllegalArgumentException(
                    "Service entry ID is empty"
                )
            )
            return
        }

        serviceCollection(
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

    fun deleteService(
        userId: String,
        vehicleId: String,
        serviceId: String,
        onSuccess: () -> Unit = {},
        onError: (Exception) -> Unit = {}
    ) {
        serviceCollection(
            userId,
            vehicleId
        )
            .document(serviceId)
            .delete()
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener {
                onError(it)
            }
    }

    fun observeServices(
        userId: String,
        vehicleId: String,
        onData: (List<ServiceEntry>) -> Unit,
        onError: (Exception) -> Unit = {}
    ): ListenerRegistration {

        return serviceCollection(
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
                                ServiceEntry::class.java
                            )
                        }
                        ?: emptyList()

                onData(entries)
            }
    }
}