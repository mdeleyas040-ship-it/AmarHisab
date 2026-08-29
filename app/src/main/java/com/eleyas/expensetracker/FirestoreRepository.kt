package com.eleyas.expensetracker

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.eleyas.expensetracker.model.Transaction

object FirestoreRepository {

    private val auth =
        FirebaseAuth.getInstance()

    private val firestore =
        FirebaseFirestore.getInstance()

    // Current Google user's UID
    private fun currentUserId(): String? {
        return auth.currentUser?.uid
    }

    // --------------------------------------------------
    // SAVE USER PROFILE
    // --------------------------------------------------

    fun saveUser(
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {

        val uid = currentUserId()

        if (uid == null) {
            onError("User login করা নেই।")
            return
        }

        val user = auth.currentUser

        val userData = hashMapOf(
            "uid" to uid,
            "name" to (user?.displayName ?: ""),
            "email" to (user?.email ?: ""),
            "photoUrl" to (user?.photoUrl?.toString() ?: "")
        )

        firestore
            .collection("users")
            .document(uid)
            .set(userData)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { exception ->
                onError(
                    exception.message
                        ?: "User data save করা যায়নি।"
                )
            }
    }

    // --------------------------------------------------
    // SAVE TRANSACTION
    // --------------------------------------------------

    fun saveTransaction(
        transaction: Transaction,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {

        val uid = currentUserId()

        if (uid == null) {
            onError("User login করা নেই।")
            return
        }

        val transactionData = hashMapOf(
            "id" to transaction.id,
            "type" to transaction.type,
            "amount" to transaction.amount,
            "currency" to transaction.currency,
            "category" to transaction.category,
            "reason" to transaction.reason,
            "date" to transaction.date,
            "receiptImage" to (transaction.receiptImage ?: "")
        )

        firestore
            .collection("users")
            .document(uid)
            .collection("transactions")
            .document(transaction.id.toString())
            .set(transactionData)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { exception ->
                onError(
                    exception.message
                        ?: "Transaction save করা যায়নি।"
                )
            }
    }

    // --------------------------------------------------
    // LOAD ALL TRANSACTIONS
    // --------------------------------------------------

    fun loadTransactions(
        onSuccess: (List<Transaction>) -> Unit,
        onError: (String) -> Unit
    ) {

        val uid = currentUserId()

        if (uid == null) {
            onError("User login করা নেই।")
            return
        }

        firestore
            .collection("users")
            .document(uid)
            .collection("transactions")
            .get()
            .addOnSuccessListener { snapshot ->

                val transactions =
                    snapshot.documents.mapNotNull { document ->

                        try {

                            Transaction(
                                id =
                                    document
                                        .getLong("id")
                                        ?: return@mapNotNull null,

                                type =
                                    document
                                        .getString("type")
                                        ?: "",

                                amount =
                                    document
                                        .getDouble("amount")
                                        ?: 0.0,

                                currency =
                                    document
                                        .getString("currency")
                                        ?: "MVR",

                                category =
                                    document
                                        .getString("category")
                                        ?: "",

                                reason =
                                    document
                                        .getString("reason")
                                        ?: "",

                                date =
                                    document
                                        .getString("date")
                                        ?: "",

                                receiptImage =
                                    document
                                        .getString("receiptImage")
                                        ?.ifBlank { null }
                            )

                        } catch (
                            exception: Exception
                        ) {

                            null
                        }
                    }
                        .sortedByDescending {
                            it.id
                        }

                onSuccess(transactions)
            }
            .addOnFailureListener { exception ->

                onError(
                    exception.message
                        ?: "Transaction load করা যায়নি।"
                )
            }
    }

    // --------------------------------------------------
    // DELETE TRANSACTION
    // --------------------------------------------------

    fun deleteTransaction(
        transactionId: Long,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {

        val uid = currentUserId()

        if (uid == null) {
            onError("User login করা নেই।")
            return
        }

        firestore
            .collection("users")
            .document(uid)
            .collection("transactions")
            .document(transactionId.toString())
            .delete()
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { exception ->
                onError(
                    exception.message
                        ?: "Transaction delete করা যায়নি।"
                )
            }
    }
}