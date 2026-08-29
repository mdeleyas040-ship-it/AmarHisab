package com.eleyas.expensetracker.repository

import com.eleyas.expensetracker.model.Household
import com.eleyas.expensetracker.model.HouseholdMember
import com.eleyas.expensetracker.model.Transaction
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Locale

/**
 * Amar Hisab — ফ্যামিলি / Shared Household
 *
 * Firestore structure:
 *   households/{id}                  → name, code, createdBy, createdAt, members[]
 *   households/{id}/homeTransactions → পরিবারের সবার shared home income/expense
 *
 * শুধু "home" / "home_expense" type transaction এখানে sync হয়।
 * Personal income/expense ইউজারের নিজের collection-এই থাকে।
 */
object HouseholdRepository {

    fun generateCode(): String {
        // Confusing characters (0/O, 1/I) বাদ দিয়ে ৬-digit join code
        val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
        return (1..6).map { chars.random() }.joinToString("")
    }

    private fun memberToMap(m: HouseholdMember): Map<String, Any> = mapOf(
        "uid" to m.uid,
        "name" to m.name,
        "email" to m.email,
        "photoUrl" to m.photoUrl,
        "joinedAt" to m.joinedAt
    )

    fun createHousehold(
        firestore: FirebaseFirestore,
        household: Household,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val data = mapOf(
            "name" to household.name,
            "code" to household.code,
            "createdBy" to household.createdBy,
            "createdAt" to household.createdAt,
            "members" to household.members.map { memberToMap(it) }
        )
        firestore.collection("households").document(household.id).set(data)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it.message ?: "Household তৈরি হয়নি") }
    }

    fun findHouseholdByCode(
        firestore: FirebaseFirestore,
        code: String,
        onFound: (Household?) -> Unit
    ) {
        val normalized = code.trim().uppercase(Locale.US)
        if (normalized.isBlank()) {
            onFound(null)
            return
        }
        firestore.collection("households")
            .whereEqualTo("code", normalized)
            .limit(1)
            .get()
            .addOnSuccessListener { snapshot ->
                onFound(snapshot.documents.firstOrNull()?.let { docToHousehold(it) })
            }
            .addOnFailureListener { onFound(null) }
    }

    fun addMember(
        firestore: FirebaseFirestore,
        householdId: String,
        member: HouseholdMember,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        firestore.collection("households").document(householdId)
            .update("members", FieldValue.arrayUnion(memberToMap(member)))
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it.message ?: "Join করা যায়নি") }
    }

    fun removeMember(
        firestore: FirebaseFirestore,
        householdId: String,
        uid: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        firestore.collection("households").document(householdId).get()
            .addOnSuccessListener { doc ->
                val remaining = docToHousehold(doc)?.members?.filter { it.uid != uid } ?: emptyList()
                doc.reference.update("members", remaining.map { memberToMap(it) })
                    .addOnSuccessListener { onSuccess() }
                    .addOnFailureListener { onError(it.message ?: "Leave করা যায়নি") }
            }
            .addOnFailureListener { onError(it.message ?: "Household load হয়নি") }
    }

    fun saveSharedHomeTransaction(
        firestore: FirebaseFirestore,
        householdId: String,
        transaction: Transaction,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        val data = mapOf(
            "id" to transaction.id,
            "type" to transaction.type,
            "amount" to transaction.amount,
            "currency" to transaction.currency,
            "category" to transaction.category,
            "reason" to transaction.reason,
            "date" to transaction.date,
            "receiptImage" to (transaction.receiptImage ?: ""),
            "walletId" to transaction.walletId,
            "addedByUid" to (transaction.addedByUid ?: ""),
            "addedByName" to (transaction.addedByName ?: "")
        )
        firestore.collection("households").document(householdId)
            .collection("homeTransactions")
            .document(transaction.id.toString())
            .set(data)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it.message ?: "Error") }
    }

    fun deleteSharedHomeTransaction(
        firestore: FirebaseFirestore,
        householdId: String,
        transactionId: Long,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        firestore.collection("households").document(householdId)
            .collection("homeTransactions")
            .document(transactionId.toString())
            .delete()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it.message ?: "Error") }
    }

    fun docToHousehold(doc: DocumentSnapshot): Household? {
        if (!doc.exists()) return null
        return try {
            val members = (doc.get("members") as? List<*>)?.mapNotNull { m ->
                (m as? Map<*, *>)?.let { map ->
                    HouseholdMember(
                        uid = map["uid"] as? String ?: "",
                        name = map["name"] as? String ?: "",
                        email = map["email"] as? String ?: "",
                        photoUrl = map["photoUrl"] as? String ?: "",
                        joinedAt = (map["joinedAt"] as? Number)?.toLong() ?: 0L
                    )
                }
            } ?: emptyList()
            Household(
                id = doc.id,
                name = doc.getString("name") ?: "",
                code = doc.getString("code") ?: "",
                createdBy = doc.getString("createdBy") ?: "",
                createdAt = doc.getLong("createdAt") ?: 0L,
                members = members
            )
        } catch (_: Exception) { null }
    }

    fun homeDocToTransaction(doc: DocumentSnapshot): Transaction? = try {
        Transaction(
            id = doc.getLong("id") ?: 0L,
            type = doc.getString("type") ?: "",
            amount = doc.getDouble("amount") ?: 0.0,
            currency = doc.getString("currency") ?: "BDT",
            category = doc.getString("category") ?: "",
            reason = doc.getString("reason") ?: "",
            date = doc.getString("date") ?: "",
            receiptImage = doc.getString("receiptImage")?.takeIf { it.isNotBlank() },
            walletId = doc.getString("walletId") ?: "default_cash",
            addedByUid = doc.getString("addedByUid")?.takeIf { it.isNotBlank() },
            addedByName = doc.getString("addedByName")?.takeIf { it.isNotBlank() }
        )
    } catch (_: Exception) { null }
}