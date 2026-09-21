package com.eleyas.expensetracker.dutyroster

import android.content.Context
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

object DutyRosterManager {
    fun scan(
        context: Context,
        uri: Uri,
        onSuccess: (List<RosterDay>) -> Unit,
        onError: (String) -> Unit
    ) {
        val image = runCatching {
            InputImage.fromFilePath(context, uri)
        }.getOrElse {
            onError("ছবিটি পড়া যাচ্ছে না")
            return
        }

        val recognizer =
            TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

        recognizer.process(image)
            .addOnSuccessListener { text ->
                val days = DutyRosterParser.parse(text)

                if (days.isEmpty()) {
                    onError(
                        "Roster-এর date/employee table শনাক্ত করা যায়নি। পরিষ্কার ছবি দিন।"
                    )
                } else {
                    onSuccess(days)
                }
            }
            .addOnFailureListener {
                onError(
                    "Roster scan failed: ${it.message ?: "Unknown error"}"
                )
            }
            .addOnCompleteListener {
                recognizer.close()
            }
    }
}
