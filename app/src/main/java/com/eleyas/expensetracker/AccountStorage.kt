import android.content.Context
import com.google.firebase.auth.FirebaseAuth

object AccountStorage {

    private const val BASE_PREFS = "amar_hisab"

    private const val LEGACY_PREFS =
        "amar_hisab"

    private const val OWNER_PREFS =
        "amar_hisab_account_owner"

    private const val OWNER_UID_KEY =
        "legacy_owner_uid"

    fun getCurrentUserId(): String {
        return FirebaseAuth.getInstance().currentUser?.uid
            ?: "guest"
    }

    fun getPrefs(context: Context, userId: String) =
        context.getSharedPreferences(
            "${BASE_PREFS}_$userId",
            Context.MODE_PRIVATE
        )

    fun clearCurrentAccountData(context: Context, userId: String) {
        getPrefs(context, userId)
            .edit()
            .clear()
            .apply()
    }


    /**
     * পুরোনো amar_hisab data শুধুমাত্র
     * প্রথম/মূল account-এর UID-তে migrate করবে।
     */
    fun migrateLegacyDataToCurrentAccount(
        context: Context,
        currentUid: String
    ) {
        if (currentUid == "guest") return

        val ownerPrefs = context.getSharedPreferences(OWNER_PREFS, Context.MODE_PRIVATE)
        val existingOwnerUid = ownerPrefs.getString(OWNER_UID_KEY, null)

        // ইতিমধ্যে কোনো account-এর সাথে assign করা থাকলে আর migrate করবে না।
        if (existingOwnerUid != null) return

        val legacyPrefs = context.getSharedPreferences(LEGACY_PREFS, Context.MODE_PRIVATE)
        val legacyData = legacyPrefs.all

        if (legacyData.isEmpty()) return

        val accountPrefs = getPrefs(context, currentUid)

        // যদি এই account-এ ইতিমধ্যে নিজস্ব data থাকে, তবে overwrite করব না।
        if (accountPrefs.contains("transactions") || accountPrefs.contains("loans")) {
            // এই account-এ ইতিমধ্যে data আছে, তাই migration skip করা নিরাপদ।
            // কিন্তু আমরা owner set করে দিতে পারি যাতে অন্য কেউ না পায়।
            ownerPrefs.edit().putString(OWNER_UID_KEY, currentUid).apply()
            return
        }

        val editor = accountPrefs.edit()
        legacyData.forEach { (key, value) ->
            when (value) {
                is String -> editor.putString(key, value)
                is Int -> editor.putInt(key, value)
                is Long -> editor.putLong(key, value)
                is Float -> editor.putFloat(key, value)
                is Boolean -> editor.putBoolean(key, value)
                is Set<*> -> editor.putStringSet(key, value.filterIsInstance<String>().toSet())
            }
        }
        editor.apply()

        // এই legacy data এখন এই UID-এর।
        ownerPrefs.edit().putString(OWNER_UID_KEY, currentUid).apply()
        
        // লিগ্যাসি ফাইলটি খালি করে দেওয়া ভালো যাতে ভবিষ্যতে কোনো confusion না হয়।
        // legacyPrefs.edit().clear().apply() // ঐচ্ছিক: user যদি পরে guest mode-এ দেখতে চায়।
    }
}