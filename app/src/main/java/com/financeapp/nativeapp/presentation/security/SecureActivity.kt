package com.financeapp.nativeapp.presentation.security

import android.app.Activity
import android.app.KeyguardManager
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators
import androidx.biometric.BiometricPrompt
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

/** Activity owns authentication; Compose cannot restore or grant access. */
open class SecureActivity : FragmentActivity() {
    private val session = UnlockSession()
    var unlocked by mutableStateOf(false)
        private set
    var pending by mutableStateOf(false)
        private set
    var message by mutableStateOf("Use a biometria ou a credencial do dispositivo.")
        private set
    var credentialAvailable by mutableStateOf(false)
        private set
    private lateinit var prompt: BiometricPrompt
    private var credentialPending = false
    private var initialAttempt = true

    private val credentialLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (credentialPending) {
            credentialPending = false
            if (result.resultCode == Activity.RESULT_OK) grantAccess()
            else finishAttempt("Autenticação cancelada. O aplicativo continua bloqueado.")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        // A restored external credential result must never unlock a new Activity.
        credentialAvailable = getSystemService(KeyguardManager::class.java).isDeviceSecure
        prompt = BiometricPrompt(this, ContextCompat.getMainExecutor(this),
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    grantAccess()
                }
                override fun onAuthenticationFailed() {
                    message = "Biometria não reconhecida. Tente novamente."
                }
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    finishAttempt(when (errorCode) {
                        BiometricPrompt.ERROR_USER_CANCELED,
                        BiometricPrompt.ERROR_CANCELED,
                        BiometricPrompt.ERROR_NEGATIVE_BUTTON ->
                            "Autenticação cancelada. O aplicativo continua bloqueado."
                        BiometricPrompt.ERROR_LOCKOUT,
                        BiometricPrompt.ERROR_LOCKOUT_PERMANENT ->
                            "Biometria bloqueada pelo Android. Use a credencial do dispositivo ou tente mais tarde."
                        else -> "Não foi possível autenticar: $errString"
                    })
                }
            })
    }

    override fun onPostResume() {
        super.onPostResume()
        credentialAvailable = getSystemService(KeyguardManager::class.java).isDeviceSecure
        if (initialAttempt) {
            initialAttempt = false
            authenticate()
        }
    }

    fun authenticate() {
        if (pending || unlocked) return
        val status = BiometricManager.from(this).canAuthenticate(Authenticators.BIOMETRIC_STRONG)
        if (status != BiometricManager.BIOMETRIC_SUCCESS) {
            message = when (status) {
                BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> "Nenhuma biometria compatível cadastrada."
                BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> "Este dispositivo não possui biometria compatível."
                BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> "Sensor biométrico temporariamente indisponível."
                else -> "Biometria segura indisponível neste dispositivo."
            } + if (credentialAvailable) " Use a credencial do dispositivo."
                else " Configure um bloqueio de tela nas configurações do Android."
            return
        }
        if (!session.begin()) return
        pending = true
        prompt.authenticate(BiometricPrompt.PromptInfo.Builder()
            .setTitle("Desbloquear FinanceAPP")
            .setSubtitle("Confirme sua identidade para acessar suas finanças")
            .setAllowedAuthenticators(Authenticators.BIOMETRIC_STRONG)
            .setNegativeButtonText("Cancelar")
            .build())
    }

    @Suppress("DEPRECATION")
    fun authenticateCredential() {
        if (pending || unlocked) return
        val keyguard = getSystemService(KeyguardManager::class.java)
        credentialAvailable = keyguard.isDeviceSecure
        if (!credentialAvailable) {
            message = "Configure um PIN, senha ou padrão nas configurações do Android."
            return
        }
        if (!session.begin()) return
        pending = true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (BiometricManager.from(this).canAuthenticate(Authenticators.DEVICE_CREDENTIAL)
                != BiometricManager.BIOMETRIC_SUCCESS) {
                finishAttempt("Credencial do dispositivo indisponível.")
                return
            }
            credentialPending = true
            prompt.authenticate(BiometricPrompt.PromptInfo.Builder()
                .setTitle("Desbloquear FinanceAPP")
                .setAllowedAuthenticators(Authenticators.DEVICE_CREDENTIAL)
                .build())
        } else {
            // DEVICE_CREDENTIAL alone is unsupported by BiometricPrompt before API 30.
            val intent = keyguard.createConfirmDeviceCredentialIntent(
                "Desbloquear FinanceAPP", "Confirme sua identidade")
            if (intent == null) {
                finishAttempt("Credencial do dispositivo indisponível.")
                return
            }
            credentialPending = true
            try {
                credentialLauncher.launch(intent)
            } catch (_: android.content.ActivityNotFoundException) {
                credentialPending = false
                finishAttempt("Não foi possível abrir a credencial do dispositivo.")
            }
        }
    }

    private fun grantAccess() {
        credentialPending = false
        session.succeed()
        unlocked = session.unlocked
        pending = false
    }

    private fun finishAttempt(text: String) {
        credentialPending = false
        session.finish()
        pending = false
        message = text
    }

    override fun onPause() {
        // Hide protected content immediately, including when opening Recents.
        if (unlocked) {
            session.lock()
            unlocked = false
        }
        super.onPause()
    }

    override fun onStop() {
        if (!credentialPending) {
            val wasPending = pending
            session.lock()
            unlocked = false
            pending = false
            if (wasPending) prompt.cancelAuthentication()
        }
        super.onStop()
    }
}
