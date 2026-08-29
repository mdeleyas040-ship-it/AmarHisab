package com.eleyas.expensetracker

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

private val LoginBackground = Color(0xFF101217)
private val LoginCard = Color(0xFF181B21)
private val AmarGreen = Color(0xFF00E676)
private val WhiteText = Color(0xFFF5F5F5)
private val GrayText = Color(0xFF9E9E9E)

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val auth = remember { FirebaseAuth.getInstance() }
    var loading by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize().background(LoginBackground)) {
        // Decorative background element
        Box(
            modifier = Modifier
                .size(300.dp)
                .offset(y = (-100).dp)
                .clip(CircleShape)
                .background(AmarGreen.copy(alpha = 0.05f))
                .align(Alignment.TopCenter)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 30.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // App Logo / Icon
            Surface(
                modifier = Modifier.size(100.dp),
                shape = CircleShape,
                color = AmarGreen.copy(alpha = 0.1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(text = "💰", fontSize = 50.sp)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Amar Hisab",
                color = WhiteText,
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold
            )

            Text(
                text = "আপনার আয়-ব্যয়ের সঠিক হিসাব",
                color = GrayText,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Login Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = LoginCard),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "লগইন করুন",
                        color = WhiteText,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "গুগল অ্যাকাউন্ট ব্যবহার করে নিরাপদে প্রবেশ করুন। প্রতিটি অ্যাকাউন্টের ডাটা আলাদা থাকবে।",
                        color = GrayText,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    if (loading) {
                        CircularProgressIndicator(color = AmarGreen)
                    } else {
                        Button(
                            onClick = {
                                loading = true
                                scope.launch {
                                    handleGoogleLogin(context, auth, onLoginSuccess) {
                                        loading = false
                                        Toast.makeText(context, it, Toast.LENGTH_LONG).show()
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("G", color = Color(0xFF4285F4), fontSize = 24.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("Continue with Google", color = Color.Black, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Footer Secret Backdoor
            var tapCount by remember { mutableStateOf(0) }
            val recoveryPrefs = remember { context.getSharedPreferences("admin_recovery", Context.MODE_PRIVATE) }

            TextButton(
                onClick = {
                    tapCount++
                    if (tapCount >= 3) {
                        recoveryPrefs.edit().putBoolean("recovery_active", true).apply()
                        onLoginSuccess()
                    }
                }
            ) {
                Text(
                    text = "Amar Hisab • Your money, your control",
                    color = GrayText.copy(alpha = 0.4f),
                    fontSize = 11.sp
                )
            }
        }
    }
}

private suspend fun handleGoogleLogin(
    context: Context,
    auth: FirebaseAuth,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
) {
    try {
        val credentialManager = CredentialManager.create(context)

        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(
                "12259347539-bejtr5002jgoho62rkcoiblib53r2c8g.apps.googleusercontent.com"
            )
            .setAutoSelectEnabled(false)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        val result = credentialManager.getCredential(context, request)

        val credential = result.credential

        if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {

            val googleCredential = GoogleIdTokenCredential.createFrom(
                credential.data
            )

            val firebaseCredential = GoogleAuthProvider.getCredential(
                googleCredential.idToken,
                null
            )

            auth.signInWithCredential(firebaseCredential).await()

            FirestoreRepository.saveUser(
                onSuccess = {
                    onSuccess()
                },
                onError = {
                    onSuccess()
                }
            )

        } else {
            onError("Google credential পাওয়া যায়নি।")
        }

    } catch (e: androidx.credentials.exceptions.GetCredentialException) {
        onError(
            "Google Login failed: ${e.message}"
        )
    } catch (e: Exception) {
        onError(
            "Error: ${e.message}"
        )
    }
}