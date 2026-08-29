package com.eleyas.expensetracker.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.eleyas.expensetracker.model.Vehicle

class VehicleRepository {

    private val firestore = FirebaseFirestore.getInstance()

    private fun vehicleCollection(userId: String) =
        firestore
            .collection("users")
            .document(userId)
            .collection("vehicles")

    fun addVehicle(
        userId: String,
        vehicle: Vehicle,
        onSuccess: () -> Unit = {},
        onError: (Exception) -> Unit = {}
    ) {
        val document = vehicleCollection(userId).document()

        val vehicleWithId = vehicle.copy(
            id = document.id
        )

        document
            .set(vehicleWithId)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener {
                onError(it)
            }
    }

    fun updateVehicle(
        userId: String,
        vehicle: Vehicle,
        onSuccess: () -> Unit = {},
        onError: (Exception) -> Unit = {}
    ) {
        if (vehicle.id.isBlank()) {
            onError(
                IllegalArgumentException("Vehicle ID is empty")
            )
            return
        }

        vehicleCollection(userId)
            .document(vehicle.id)
            .set(vehicle)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener {
                onError(it)
            }
    }

    fun deleteVehicle(
        userId: String,
        vehicleId: String,
        onSuccess: () -> Unit = {},
        onError: (Exception) -> Unit = {}
    ) {
        vehicleCollection(userId)
            .document(vehicleId)
            .delete()
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener {
                onError(it)
            }
    }

    fun observeVehicles(
        userId: String,
        onData: (List<Vehicle>) -> Unit,
        onError: (Exception) -> Unit = {}
    ): ListenerRegistration {

        return vehicleCollection(userId)
            .orderBy("createdAt")
            .addSnapshotListener { snapshot, error ->

                if (error != null) {
                    onError(error)
                    return@addSnapshotListener
                }

                val vehicles = snapshot
                    ?.documents
                    ?.mapNotNull { document ->
                        document.toObject(Vehicle::class.java)
                    }
                    ?: emptyList()

                onData(vehicles)
            }
    }
}