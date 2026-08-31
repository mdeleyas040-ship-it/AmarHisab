package com.eleyas.expensetracker.ui.screens

import android.util.Base64
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.eleyas.expensetracker.repository.FirestoreRepository
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.launch
import java.security.SecureRandom

private val LoginGreen = Color(0xFF0B8F45)

private const val WEB_CLIENT_ID =
    "12259347539-bejtr5002jgoho62rkcoiblib53r2c8g.apps.googleusercontent.com"

private fun generateNonce(byteLength: Int = 32): String {

    val randomBytes = ByteArray(byteLength)

    SecureRandom().nextBytes(randomBytes)

    return Base64.encodeToString(
        randomBytes,
        Base64.NO_WRAP or
                Base64.URL_SAFE or
                Base64.NO_PADDING
    )
}

@Composable
fun GoogleLoginScreen(
    onLoginSuccess: () -> Unit
) {

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var loading by remember {
        mutableStateOf(false)
    }

    var errorMessage by remember {
        mutableStateOf("")
    }

    var startAnimation by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(Unit) {
        startAnimation = true
    }

    val credentialManager = remember {
        CredentialManager.create(context)
    }

    fun startGoogleLogin() {

        scope.launch {

            loading = true
            errorMessage = ""

            try {

                val signInWithGoogleOption =
                    GetSignInWithGoogleOption
                        .Builder(
                            serverClientId = WEB_CLIENT_ID
                        )
                        .setNonce(
                            generateNonce()
                        )
                        .build()

                val request =
                    GetCredentialRequest.Builder()
                        .addCredentialOption(
                            signInWithGoogleOption
                        )
                        .build()

                val result =
                    credentialManager.getCredential(
                        context = context,
                        request = request
                    )

                val credential =
                    result.credential

                if (
                    credential is CustomCredential &&
                    credential.type ==
                    GoogleIdTokenCredential
                        .TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                ) {

                    try {

                        val googleCredential =
                            GoogleIdTokenCredential
                                .createFrom(
                                    credential.data
                                )

                        val idToken =
                            googleCredential.idToken

                        val firebaseCredential =
                            GoogleAuthProvider
                                .getCredential(
                                    idToken,
                                    null
                                )

                        FirebaseAuth
                            .getInstance()
                            .signInWithCredential(
                                firebaseCredential
                            )
                            .addOnCompleteListener { task ->

                                if (task.isSuccessful) {

                                    FirestoreRepository.saveUser(

                                        onSuccess = {

                                            loading = false

                                            onLoginSuccess()
                                        },

                                        onError = { error ->

                                            loading = false

                                            errorMessage = error
                                        }
                                    )

                                } else {

                                    loading = false

                                    errorMessage =
                                        task.exception
                                            ?.message
                                            ?: "Firebase Login ব্যর্থ হয়েছে।"
                                }
                            }

                    } catch (
                        e: GoogleIdTokenParsingException
                    ) {

                        loading = false

                        errorMessage =
                            "Google account তথ্য পড়া যায়নি।"
                    }

                } else {

                    loading = false

                    errorMessage =
                        "Google credential পাওয়া যায়নি।"
                }

            } catch (e: Exception) {

                loading = false

                errorMessage =
                    e.message
                        ?: "Google Login করা যায়নি।"
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(28.dp),

        horizontalAlignment =
            Alignment.CenterHorizontally,

        verticalArrangement =
            Arrangement.Center
    ) {

        // 💰 Animated Logo
        AnimatedVisibility(
            visible = startAnimation,
            enter =
                fadeIn(
                    animationSpec = tween(700)
                ) +
                        scaleIn(
                            initialScale = 0.5f,
                            animationSpec = tween(700)
                        )
        ) {

            Text(
                text = "💰",
                fontSize = 85.sp
            )
        }

        Spacer(
            modifier = Modifier.height(18.dp)
        )

        // TITLE
        AnimatedVisibility(
            visible = startAnimation,
            enter =
                fadeIn(
                    animationSpec = tween(
                        durationMillis = 700,
                        delayMillis = 250
                    )
                ) +
                        slideInVertically(
                            initialOffsetY = { 80 },
                            animationSpec = tween(
                                durationMillis = 700,
                                delayMillis = 250
                            )
                        )
        ) {

            Text(
                text = "আমার হিসাব",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = LoginGreen
            )
        }

        Spacer(
            modifier = Modifier.height(8.dp)
        )

        AnimatedVisibility(
            visible = startAnimation,
            enter =
                fadeIn(
                    animationSpec = tween(
                        durationMillis = 600,
                        delayMillis = 450
                    )
                )
        ) {

            Text(
                text = "আপনার আয়, খরচ ও সঞ্চয়ের হিসাব",
                fontSize = 14.sp,
                color = Color.Gray
            )
        }

        Spacer(
            modifier = Modifier.height(35.dp)
        )

        // LOGIN BUTTON
        AnimatedVisibility(
            visible = startAnimation,
            enter =
                fadeIn(
                    animationSpec = tween(
                        durationMillis = 700,
                        delayMillis = 650
                    )
                ) +
                        slideInVertically(
                            initialOffsetY = { 120 },
                            animationSpec = tween(
                                durationMillis = 700,
                                delayMillis = 650
                            )
                        )
        ) {

            if (loading) {

                Column(
                    horizontalAlignment =
                        Alignment.CenterHorizontally
                ) {

                    CircularProgressIndicator(
                        color = LoginGreen
                    )

                    Spacer(
                        modifier = Modifier.height(15.dp)
                    )

                    Text(
                        text = "Google Login হচ্ছে..."
                    )
                }

            } else {

                Button(
                    onClick = {
                        startGoogleLogin()
                    },

                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp),

                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor = LoginGreen
                        )
                ) {

                    Text(
                        text =
                            "🔐  Google দিয়ে Login করুন",

                        fontSize = 16.sp,

                        fontWeight =
                            FontWeight.Bold
                    )
                }
            }
        }

        // ERROR
        AnimatedVisibility(
            visible = errorMessage.isNotEmpty(),
            enter = fadeIn()
        ) {

            Column {

                Spacer(
                    modifier = Modifier.height(18.dp)
                )

                Text(
                    text = "❌ $errorMessage",
                    color = Color.Red,
                    fontSize = 13.sp
                )
            }
        }

        Spacer(
            modifier = Modifier.height(25.dp)
        )

        AnimatedVisibility(
            visible = startAnimation,
            enter =
                fadeIn(
                    animationSpec = tween(
                        durationMillis = 600,
                        delayMillis = 900
                    )
                )
        ) {

            Text(
                text =
                    "একই Google account দিয়ে অন্য ফোনে Login করলে\n" +
                            "আপনার হিসাব একই account-এর সঙ্গে রাখা যাবে।",

                fontSize = 12.sp,

                color = Color.Gray
            )
        }
    }
}