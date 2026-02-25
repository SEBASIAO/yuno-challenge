# Yuno 3DS Authentication SDK

## 1. What Is This?

The Yuno 3DS SDK adds **3-D Secure** authentication to your Android payment flow. 3-D Secure (3DS) is the industry standard for verifying a cardholder's identity during an online purchase -- think of it as the extra "Verify your payment" screen you see when buying something online. This SDK evaluates transaction risk in real time and decides whether to let the payment through silently (frictionless), ask the user to enter a one-time password (challenge), or block the transaction entirely. You integrate it with a few lines of code, and the SDK handles the rest.

---

## 2. Quick Start (2 Minutes)

If you want to get up and running as fast as possible, follow these five steps. Every code block is complete -- just copy and paste.

### Step 1 -- Add the SDK dependency

In your app module's `build.gradle.kts`:

```kotlin
dependencies {
    implementation(project(":sdk"))
}
```

### Step 2 -- Initialize in your Application class

```kotlin
package com.example.myapp

import android.app.Application
import com.yuno.payments.threeds.api.YunoThreeDSAuthenticator

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        YunoThreeDSAuthenticator.initialize(this)
    }
}
```

> Do not forget to register `MyApplication` in your `AndroidManifest.xml`:
> ```xml
> <application android:name=".MyApplication" ... >
> ```

### Step 3 -- Evaluate & decide in your Activity / ViewModel

```kotlin
import com.yuno.payments.threeds.api.YunoThreeDSAuthenticator
import com.yuno.payments.threeds.domain.model.AuthenticationAction
import com.yuno.payments.threeds.domain.model.CustomerTrustLevel
import com.yuno.payments.threeds.domain.model.Transaction

// Build the transaction
val transaction = Transaction(
    id = "txn_abc123",
    amount = 49.99,
    currency = "USD",
    merchantName = "Acme Store",
    cardLast4 = "4242",
    customerTrustLevel = CustomerTrustLevel.RETURNING,
    timestamp = System.currentTimeMillis()
)

// Evaluate risk and get a decision (call from a coroutine)
val decision = YunoThreeDSAuthenticator.evaluateAndDecide(transaction)
```

### Step 4 -- Act on the decision

```kotlin
import com.yuno.payments.threeds.api.YunoThreeDSAuthenticator
import com.yuno.payments.threeds.domain.model.AuthenticationAction

when (decision.action) {
    AuthenticationAction.FRICTIONLESS -> {
        val result = YunoThreeDSAuthenticator.buildFrictionlessResult(decision)
        // Payment approved silently. Proceed to charge.
    }
    AuthenticationAction.CHALLENGE -> {
        // Launch the challenge UI (see Step 5)
        YunoThreeDSAuthenticator.launchChallenge(transaction, decision, challengeLauncher, context)
    }
    AuthenticationAction.BLOCK -> {
        val result = YunoThreeDSAuthenticator.buildBlockedResult(decision)
        // Transaction blocked. Show error to user.
    }
}
```

### Step 5 -- Handle the challenge result

```kotlin
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContracts
import com.yuno.payments.threeds.api.YunoThreeDSAuthenticator
import com.yuno.payments.threeds.domain.model.AuthenticationStatus

// Register the launcher in your Activity/Fragment (before onStart)
val challengeLauncher = registerForActivityResult(
    ActivityResultContracts.StartActivityForResult()
) { activityResult ->
    val result = YunoThreeDSAuthenticator.parseChallengeResult(
        resultCode = activityResult.resultCode,
        data = activityResult.data,
        decision = decision  // the decision from Step 3
    )
    when (result.status) {
        AuthenticationStatus.AUTHENTICATED_CHALLENGE -> { /* Success! Proceed to charge. */ }
        AuthenticationStatus.CHALLENGE_FAILED         -> { /* Wrong OTP. Show error. */ }
        AuthenticationStatus.ABANDONED                -> { /* User pressed back. */ }
        else -> { /* Should not happen in challenge flow */ }
    }
}
```

That is it. You now have a working 3DS flow.

---

## 3. How It Works

The SDK follows a three-phase flow: **Evaluate**, **Decide**, **Act**.

```
                         Your App                              SDK
                         --------                              ---

  1. Build Transaction ──────────────────────────────────>
                                                          2. Evaluate Risk
                                                             - Amount factor
                                                             - Velocity factor
                                                             - Trust level factor
                                                          3. Score & classify
                                                             (LOW / MEDIUM / HIGH / CRITICAL)
                                                          4. Apply risk policy
                                                             ┌─────────────────────┐
                                                             │ LOW    → FRICTIONLESS│
                                                             │ MEDIUM → CHALLENGE   │
                                                             │ HIGH   → CHALLENGE   │
                                                             │ CRITICAL → BLOCK     │
                                                             └─────────────────────┘
                            <── AuthenticationDecision ───
  5. Check decision.action
       │
       ├── FRICTIONLESS ──> buildFrictionlessResult() ──> Done (silent approval)
       │
       ├── CHALLENGE ─────> launchChallenge() ──────────> SDK shows OTP screen
       │                                                      │
       │                    <── ActivityResult ───────────     │
       │                    parseChallengeResult() ──────> AuthenticationResult
       │
       └── BLOCK ─────────> buildBlockedResult() ───────> Done (block transaction)
```

### Risk Factors

The SDK scores each transaction on three dimensions:

| Factor | What it measures | High risk example |
|---|---|---|
| **Amount** | Transaction dollar value | $5,000 purchase |
| **Velocity** | How many transactions in a time window | 10 transactions in 5 minutes |
| **Trust Level** | Customer + device history | Brand new customer, unknown device |

Each factor produces a weighted score. The scores are combined into a total risk score (0-100), which maps to a `RiskLevel`, which the policy maps to an `AuthenticationAction`.

---

## 4. Installation

### Local module (current setup)

The SDK ships as the `:sdk` module inside this project.

**settings.gradle.kts** (project root):

```kotlin
rootProject.name = "Yuno Challenge"
include(":app")
include(":sdk")
```

**app/build.gradle.kts**:

```kotlin
dependencies {
    implementation(project(":sdk"))
}
```

### Requirements

| Requirement | Version |
|---|---|
| Android `minSdk` | 24 (Android 7.0) |
| Android `compileSdk` | 35 |
| Kotlin | 1.9+ |
| Jetpack Compose | BOM-managed |

No additional permissions are required. The SDK only uses `SharedPreferences` for device fingerprinting.

---

## 5. Basic Integration

This section shows the complete, production-ready integration pattern. Every import is included.

### 5.1 Application Class

Create (or update) your `Application` subclass to initialize the SDK once at app startup.

```kotlin
package com.example.myapp

import android.app.Application
import com.yuno.payments.threeds.api.YunoThreeDSAuthenticator

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Option A: Zero-config (recommended for most apps)
        YunoThreeDSAuthenticator.initialize(this)

        // Option B: Custom config (see Section 7 for details)
        // val config = YunoThreeDSConfig.Builder()
        //     .riskPolicy(RiskPolicy.default())
        //     .velocityWindowMillis(300_000L)
        //     .validOtp("123456")
        //     .build()
        // YunoThreeDSAuthenticator.initialize(this, config)
    }
}
```

Register it in `AndroidManifest.xml`:

```xml
<application
    android:name=".MyApplication"
    android:label="@string/app_name"
    ... >
    <!-- your activities -->
</application>
```

### 5.2 Activity / Screen Integration

Below is a complete `ComponentActivity` that demonstrates the full flow: evaluating risk, launching a challenge, and handling the result.

```kotlin
package com.example.myapp

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.yuno.payments.threeds.api.YunoThreeDSAuthenticator
import com.yuno.payments.threeds.domain.model.AuthenticationAction
import com.yuno.payments.threeds.domain.model.AuthenticationDecision
import com.yuno.payments.threeds.domain.model.AuthenticationResult
import com.yuno.payments.threeds.domain.model.AuthenticationStatus
import com.yuno.payments.threeds.domain.model.CustomerTrustLevel
import com.yuno.payments.threeds.domain.model.Transaction
import kotlinx.coroutines.launch

class PaymentActivity : ComponentActivity() {

    // Hold a reference to the latest decision so we can pass it to parseChallengeResult
    private var latestDecision: AuthenticationDecision? = null

    // IMPORTANT: Register the launcher BEFORE onStart (i.e., in the class body or onCreate)
    private val challengeLauncher: ActivityResultLauncher<android.content.Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { activityResult ->
            val decision = latestDecision ?: return@registerForActivityResult
            val result = YunoThreeDSAuthenticator.parseChallengeResult(
                resultCode = activityResult.resultCode,
                data = activityResult.data,
                decision = decision
            )
            handleAuthenticationResult(result)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    PaymentScreen(onPayClick = { startPaymentFlow() })
                }
            }
        }
    }

    private fun startPaymentFlow() {
        val transaction = Transaction(
            id = "txn_${System.currentTimeMillis()}",
            amount = 49.99,
            currency = "USD",
            merchantName = "Acme Store",
            cardLast4 = "4242",
            customerTrustLevel = CustomerTrustLevel.RETURNING,
            timestamp = System.currentTimeMillis()
        )

        lifecycleScope.launch {
            // Phase 1: Evaluate risk and get decision
            val decision = YunoThreeDSAuthenticator.evaluateAndDecide(transaction)
            latestDecision = decision

            // Phase 2: Act on the decision
            when (decision.action) {
                AuthenticationAction.FRICTIONLESS -> {
                    val result = YunoThreeDSAuthenticator.buildFrictionlessResult(decision)
                    handleAuthenticationResult(result)
                }
                AuthenticationAction.CHALLENGE -> {
                    YunoThreeDSAuthenticator.launchChallenge(
                        transaction = transaction,
                        decision = decision,
                        launcher = challengeLauncher,
                        context = this@PaymentActivity
                    )
                }
                AuthenticationAction.BLOCK -> {
                    val result = YunoThreeDSAuthenticator.buildBlockedResult(decision)
                    handleAuthenticationResult(result)
                }
            }
        }
    }

    private fun handleAuthenticationResult(result: AuthenticationResult) {
        when (result.status) {
            AuthenticationStatus.AUTHENTICATED_FRICTIONLESS -> {
                // Low risk -- approved silently
                Toast.makeText(this, "Payment approved (frictionless)", Toast.LENGTH_SHORT).show()
                // Proceed to charge the card via your backend
            }
            AuthenticationStatus.AUTHENTICATED_CHALLENGE -> {
                // User passed the OTP challenge
                Toast.makeText(this, "Payment approved (challenge passed)", Toast.LENGTH_SHORT).show()
                // Update device trust so future transactions may be frictionless
                lifecycleScope.launch {
                    YunoThreeDSAuthenticator.updateDeviceTrust()
                }
                // Proceed to charge the card via your backend
            }
            AuthenticationStatus.CHALLENGE_FAILED -> {
                // User entered wrong OTP
                Toast.makeText(this, "Authentication failed", Toast.LENGTH_SHORT).show()
            }
            AuthenticationStatus.ABANDONED -> {
                // User pressed back / dismissed the challenge
                val info = result.abandonmentInfo
                Toast.makeText(
                    this,
                    "Challenge abandoned after ${info?.timeSpentMillis ?: 0}ms",
                    Toast.LENGTH_SHORT
                ).show()
            }
            AuthenticationStatus.BLOCKED -> {
                // Transaction was too risky
                Toast.makeText(this, "Transaction blocked for security", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

@Composable
fun PaymentScreen(onPayClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Total: $49.99", style = MaterialTheme.typography.headlineMedium)
        Button(
            onClick = onPayClick,
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text("Pay Now")
        }
    }
}
```

### 5.3 ViewModel-Based Integration (Recommended)

For production apps using MVVM, here is the recommended ViewModel pattern.

```kotlin
package com.example.myapp.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yuno.payments.threeds.api.YunoThreeDSAuthenticator
import com.yuno.payments.threeds.domain.model.AuthenticationAction
import com.yuno.payments.threeds.domain.model.AuthenticationDecision
import com.yuno.payments.threeds.domain.model.AuthenticationResult
import com.yuno.payments.threeds.domain.model.AuthenticationStatus
import com.yuno.payments.threeds.domain.model.Transaction
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PaymentUiState(
    val isLoading: Boolean = false,
    val pendingChallenge: PendingChallenge? = null,
    val resultMessage: String? = null,
    val isSuccess: Boolean? = null
)

data class PendingChallenge(
    val transaction: Transaction,
    val decision: AuthenticationDecision
)

class PaymentViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(PaymentUiState())
    val uiState: StateFlow<PaymentUiState> = _uiState.asStateFlow()

    fun startPayment(transaction: Transaction) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val decision = YunoThreeDSAuthenticator.evaluateAndDecide(transaction)

            when (decision.action) {
                AuthenticationAction.FRICTIONLESS -> {
                    val result = YunoThreeDSAuthenticator.buildFrictionlessResult(decision)
                    onAuthenticationComplete(result)
                }
                AuthenticationAction.CHALLENGE -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            pendingChallenge = PendingChallenge(transaction, decision)
                        )
                    }
                    // The UI observes pendingChallenge and launches the challenge
                }
                AuthenticationAction.BLOCK -> {
                    val result = YunoThreeDSAuthenticator.buildBlockedResult(decision)
                    onAuthenticationComplete(result)
                }
            }
        }
    }

    fun onChallengeResult(resultCode: Int, data: android.content.Intent?) {
        val pending = _uiState.value.pendingChallenge ?: return
        val result = YunoThreeDSAuthenticator.parseChallengeResult(
            resultCode = resultCode,
            data = data,
            decision = pending.decision
        )
        onAuthenticationComplete(result)
    }

    private fun onAuthenticationComplete(result: AuthenticationResult) {
        val message = when (result.status) {
            AuthenticationStatus.AUTHENTICATED_FRICTIONLESS -> "Approved (frictionless)"
            AuthenticationStatus.AUTHENTICATED_CHALLENGE -> "Approved (challenge passed)"
            AuthenticationStatus.CHALLENGE_FAILED -> "Authentication failed"
            AuthenticationStatus.ABANDONED -> "Challenge abandoned"
            AuthenticationStatus.BLOCKED -> "Transaction blocked"
        }
        val isSuccess = result.status == AuthenticationStatus.AUTHENTICATED_FRICTIONLESS ||
            result.status == AuthenticationStatus.AUTHENTICATED_CHALLENGE

        if (isSuccess) {
            viewModelScope.launch { YunoThreeDSAuthenticator.updateDeviceTrust() }
        }

        _uiState.update {
            it.copy(
                isLoading = false,
                pendingChallenge = null,
                resultMessage = message,
                isSuccess = isSuccess
            )
        }
    }
}
```

---

## 6. Understanding Results

Every authentication flow ends with an `AuthenticationResult`. Here is what each status means and what your app should do.

### AuthenticationStatus Reference

| Status | When it happens | User saw a screen? | What your app should do |
|---|---|---|---|
| `AUTHENTICATED_FRICTIONLESS` | Risk was LOW -- policy said skip the challenge | No | Proceed to charge. Best UX. |
| `AUTHENTICATED_CHALLENGE` | User entered the correct OTP | Yes | Proceed to charge. Call `updateDeviceTrust()`. |
| `CHALLENGE_FAILED` | User entered a wrong OTP | Yes | Show an error. Optionally let them retry. |
| `ABANDONED` | User pressed back or dismissed the challenge screen | Yes | Log it. Optionally let them retry. |
| `BLOCKED` | Risk was CRITICAL -- policy said block | No | Show a clear error. Do NOT proceed to charge. |

### AuthenticationResult Fields

| Field | Type | Present when |
|---|---|---|
| `status` | `AuthenticationStatus` | Always |
| `decision` | `AuthenticationDecision` | Always |
| `challengeCompletedAt` | `Long?` | `AUTHENTICATED_CHALLENGE` only |
| `abandonmentInfo` | `AbandonmentInfo?` | `ABANDONED` only |

### AbandonmentInfo Fields

| Field | Type | Description |
|---|---|---|
| `abandonedAt` | `Long` | Timestamp (epoch millis) when the user abandoned |
| `timeSpentMillis` | `Long` | How long the user was on the challenge screen |
| `otpAttemptsBeforeAbandon` | `Int` | How many OTP attempts they made before leaving |

---

## 7. Customizing Risk Policy (Optional)

> **You probably do not need this.** The default policy works well for most payment flows. Only customize if your business has specific risk tolerance requirements.

The `RiskPolicy` maps each `RiskLevel` to an `AuthenticationAction`. Here is the default:

| RiskLevel | Default Action | Meaning |
|---|---|---|
| `LOW` | `FRICTIONLESS` | Let the payment through silently |
| `MEDIUM` | `CHALLENGE` | Ask for OTP verification |
| `HIGH` | `CHALLENGE` | Ask for OTP verification |
| `CRITICAL` | `BLOCK` | Reject the transaction |

### Creating a Custom Policy

```kotlin
import com.yuno.payments.threeds.api.YunoThreeDSAuthenticator
import com.yuno.payments.threeds.api.YunoThreeDSConfig
import com.yuno.payments.threeds.domain.model.AuthenticationAction
import com.yuno.payments.threeds.domain.model.RiskLevel
import com.yuno.payments.threeds.domain.model.RiskPolicy

// Example: Stricter policy -- challenge even LOW risk, block HIGH and above
val strictPolicy = RiskPolicy(
    actions = mapOf(
        RiskLevel.LOW to AuthenticationAction.CHALLENGE,
        RiskLevel.MEDIUM to AuthenticationAction.CHALLENGE,
        RiskLevel.HIGH to AuthenticationAction.BLOCK,
        RiskLevel.CRITICAL to AuthenticationAction.BLOCK
    )
)

val config = YunoThreeDSConfig.Builder()
    .riskPolicy(strictPolicy)
    .build()

YunoThreeDSAuthenticator.initialize(context, config)
```

### Customizing the Velocity Window

The velocity factor measures how many transactions have occurred in a sliding time window. The default window is **5 minutes** (300,000 ms). You can change it:

```kotlin
import com.yuno.payments.threeds.api.YunoThreeDSConfig

val config = YunoThreeDSConfig.Builder()
    .velocityWindowMillis(600_000L)  // 10 minutes
    .build()
```

### Setting a Custom Valid OTP (Testing Only)

By default, the valid OTP code is `"123456"`. For testing or demo purposes, you can change it:

```kotlin
import com.yuno.payments.threeds.api.YunoThreeDSConfig

val config = YunoThreeDSConfig.Builder()
    .validOtp("999999")
    .build()
```

> **Warning:** In a real production environment, the OTP would be generated and validated server-side. This local OTP is a simulation for the demo/challenge.

---

## 8. A/B Testing Guide

The SDK supports changing the risk policy at runtime, which makes A/B testing straightforward. Here is a real-world, step-by-step guide.

### Why A/B test your risk policy?

You want to find the sweet spot between security and conversion. A strict policy blocks more fraud but also causes more legitimate customers to abandon. A lenient policy has higher conversion but may let risky transactions through.

### Step-by-step

**Step 1 -- Define your policy variants**

```kotlin
import com.yuno.payments.threeds.domain.model.AuthenticationAction
import com.yuno.payments.threeds.domain.model.RiskLevel
import com.yuno.payments.threeds.domain.model.RiskPolicy

// Variant A: Current default (control group)
val policyA = RiskPolicy.default()

// Variant B: More lenient -- only challenge HIGH, frictionless for MEDIUM
val policyB = RiskPolicy(
    actions = mapOf(
        RiskLevel.LOW to AuthenticationAction.FRICTIONLESS,
        RiskLevel.MEDIUM to AuthenticationAction.FRICTIONLESS,
        RiskLevel.HIGH to AuthenticationAction.CHALLENGE,
        RiskLevel.CRITICAL to AuthenticationAction.BLOCK
    )
)

// Variant C: Stricter -- challenge everything except LOW
val policyC = RiskPolicy(
    actions = mapOf(
        RiskLevel.LOW to AuthenticationAction.FRICTIONLESS,
        RiskLevel.MEDIUM to AuthenticationAction.CHALLENGE,
        RiskLevel.HIGH to AuthenticationAction.BLOCK,
        RiskLevel.CRITICAL to AuthenticationAction.BLOCK
    )
)
```

**Step 2 -- Assign the user to a group (your logic)**

```kotlin
import com.yuno.payments.threeds.domain.model.RiskPolicy

// This is your own A/B framework -- Firebase Remote Config, LaunchDarkly, etc.
val assignedPolicy: RiskPolicy = when (abTestFramework.getVariant("3ds_policy")) {
    "A" -> policyA
    "B" -> policyB
    "C" -> policyC
    else -> RiskPolicy.default()
}
```

**Step 3 -- Apply at runtime**

```kotlin
import com.yuno.payments.threeds.api.YunoThreeDSAuthenticator

// Call this anytime -- before a payment, on app startup, when remote config updates
YunoThreeDSAuthenticator.updateRiskPolicy(assignedPolicy)
```

**Step 4 -- Log the policy that was used**

Every `AuthenticationResult` contains the full `AuthenticationDecision`, which includes `policyApplied`. Use this for analytics:

```kotlin
import com.yuno.payments.threeds.domain.model.AuthenticationResult
import com.yuno.payments.threeds.domain.model.RiskLevel

fun logForAnalytics(result: AuthenticationResult) {
    val policyUsed = result.decision.policyApplied
    val riskScore = result.decision.riskAssessment.score
    val riskLevel = result.decision.riskAssessment.riskLevel
    val action = result.decision.action
    val status = result.status

    // Send to your analytics backend
    analytics.log(
        event = "3ds_authentication",
        params = mapOf(
            "risk_score" to riskScore,
            "risk_level" to riskLevel.name,
            "action" to action.name,
            "status" to status.name,
            "policy_low" to policyUsed.actionFor(RiskLevel.LOW).name,
            "policy_medium" to policyUsed.actionFor(RiskLevel.MEDIUM).name,
            "policy_high" to policyUsed.actionFor(RiskLevel.HIGH).name,
            "policy_critical" to policyUsed.actionFor(RiskLevel.CRITICAL).name
        )
    )
}
```

**Step 5 -- Read the current policy**

```kotlin
import com.yuno.payments.threeds.api.YunoThreeDSAuthenticator

val currentPolicy = YunoThreeDSAuthenticator.getCurrentRiskPolicy()
```

### What to measure

| Metric | How to compute | Good outcome |
|---|---|---|
| **Conversion rate** | Successful auths / total attempts | Higher is better |
| **Challenge pass rate** | `AUTHENTICATED_CHALLENGE` / total challenges shown | Higher means OTP UX is good |
| **Abandonment rate** | `ABANDONED` / total challenges shown | Lower is better |
| **Time in challenge** | `abandonmentInfo.timeSpentMillis` average | Lower means clearer UX |
| **Block rate** | `BLOCKED` / total attempts | Depends on risk appetite |

---

## 9. API Reference

### YunoThreeDSAuthenticator (Object Singleton)

The single entry point for all SDK operations.

#### Methods

| Method | Signature | Description |
|---|---|---|
| `initialize` | `fun initialize(context: Context)` | Initialize with default config. Call once in `Application.onCreate()`. |
| `initialize` | `fun initialize(context: Context, config: YunoThreeDSConfig)` | Initialize with custom config. Call once in `Application.onCreate()`. |
| `evaluateRisk` | `suspend fun evaluateRisk(transaction: Transaction): RiskAssessment` | Evaluate risk only. Returns the raw score and risk level without applying a policy. Useful for analytics or custom decision logic. |
| `evaluateAndDecide` | `suspend fun evaluateAndDecide(transaction: Transaction): AuthenticationDecision` | Evaluate risk AND apply the current policy. Returns the recommended action. This is the method you should use in most cases. |
| `launchChallenge` | `fun launchChallenge(transaction: Transaction, decision: AuthenticationDecision, launcher: ActivityResultLauncher<Intent>, context: Context)` | Start the challenge UI (OTP screen). Only call this when `decision.action == CHALLENGE`. |
| `parseChallengeResult` | `fun parseChallengeResult(resultCode: Int, data: Intent?, decision: AuthenticationDecision): AuthenticationResult` | Parse the `ActivityResult` returned by the challenge screen. Call this inside your `ActivityResultLauncher` callback. |
| `buildFrictionlessResult` | `fun buildFrictionlessResult(decision: AuthenticationDecision): AuthenticationResult` | Build a result for the frictionless path. Call when `decision.action == FRICTIONLESS`. |
| `buildBlockedResult` | `fun buildBlockedResult(decision: AuthenticationDecision): AuthenticationResult` | Build a result for the blocked path. Call when `decision.action == BLOCK`. |
| `updateRiskPolicy` | `fun updateRiskPolicy(policy: RiskPolicy)` | Change the active risk policy at runtime. Takes effect on the next `evaluateAndDecide()` call. |
| `getCurrentRiskPolicy` | `fun getCurrentRiskPolicy(): RiskPolicy` | Returns the currently active risk policy. |
| `updateDeviceTrust` | `suspend fun updateDeviceTrust()` | Record a successful authentication for this device. Improves trust score for future transactions. Call after `AUTHENTICATED_CHALLENGE`. |

---

### YunoThreeDSConfig

Configuration object created via the Builder pattern.

```kotlin
import com.yuno.payments.threeds.api.YunoThreeDSConfig
import com.yuno.payments.threeds.domain.model.RiskPolicy

val config = YunoThreeDSConfig.Builder()
    .riskPolicy(RiskPolicy.default())       // Optional. Default: sensible policy.
    .velocityWindowMillis(300_000L)          // Optional. Default: 5 minutes.
    .validOtp("123456")                      // Optional. Default: "123456".
    .build()
```

| Builder Method | Parameter | Default | Description |
|---|---|---|---|
| `riskPolicy()` | `RiskPolicy` | `RiskPolicy.default()` | The policy mapping risk levels to actions |
| `velocityWindowMillis()` | `Long` | `300_000` (5 min) | Sliding window for velocity calculation |
| `validOtp()` | `String` | `"123456"` | The OTP code accepted as correct (demo/testing) |

---

### Models

#### Transaction

Represents a payment transaction to be authenticated.

```kotlin
import com.yuno.payments.threeds.domain.model.CustomerTrustLevel
import com.yuno.payments.threeds.domain.model.Transaction

val transaction = Transaction(
    id = "txn_abc123",             // Unique transaction identifier
    amount = 150.00,               // Transaction amount
    currency = "USD",              // ISO 4217 currency code
    merchantName = "Acme Store",   // Merchant display name
    cardLast4 = "4242",            // Last 4 digits of the card
    customerTrustLevel = CustomerTrustLevel.RETURNING,  // Trust classification
    timestamp = System.currentTimeMillis()               // Epoch millis
)
```

| Field | Type | Description |
|---|---|---|
| `id` | `String` | Unique identifier for this transaction |
| `amount` | `Double` | Transaction amount |
| `currency` | `String` | Currency code (e.g., "USD", "EUR") |
| `merchantName` | `String` | Name shown on the challenge screen |
| `cardLast4` | `String` | Last 4 card digits, shown on the challenge screen |
| `customerTrustLevel` | `CustomerTrustLevel` | How much you trust this customer |
| `timestamp` | `Long` | When the transaction was initiated (epoch millis) |

#### CustomerTrustLevel

```kotlin
enum class CustomerTrustLevel {
    NEW,        // First-time customer. Higher risk.
    RETURNING,  // Has transacted before. Medium risk.
    TRUSTED     // Established customer with history. Lower risk.
}
```

#### RiskLevel

The output of risk scoring. You do not create these -- the SDK assigns them.

```kotlin
enum class RiskLevel {
    LOW,       // Score indicates minimal risk
    MEDIUM,    // Score indicates moderate risk
    HIGH,      // Score indicates significant risk
    CRITICAL   // Score indicates extreme risk
}
```

#### AuthenticationAction

What the policy recommends for a given risk level.

```kotlin
enum class AuthenticationAction {
    FRICTIONLESS,  // No user interaction needed
    CHALLENGE,     // User must complete OTP verification
    BLOCK          // Transaction should be rejected
}
```

#### RiskPolicy

Maps each `RiskLevel` to an `AuthenticationAction`.

```kotlin
import com.yuno.payments.threeds.domain.model.AuthenticationAction
import com.yuno.payments.threeds.domain.model.RiskLevel
import com.yuno.payments.threeds.domain.model.RiskPolicy

// Use the default
val defaultPolicy = RiskPolicy.default()

// Or create a custom one
val customPolicy = RiskPolicy(
    actions = mapOf(
        RiskLevel.LOW to AuthenticationAction.FRICTIONLESS,
        RiskLevel.MEDIUM to AuthenticationAction.CHALLENGE,
        RiskLevel.HIGH to AuthenticationAction.BLOCK,
        RiskLevel.CRITICAL to AuthenticationAction.BLOCK
    )
)

// Query what action a level maps to
val action = customPolicy.actionFor(RiskLevel.MEDIUM) // returns CHALLENGE
```

| Method | Description |
|---|---|
| `RiskPolicy.default()` | Returns the default policy (LOW=FRICTIONLESS, MEDIUM=CHALLENGE, HIGH=CHALLENGE, CRITICAL=BLOCK) |
| `actionFor(level: RiskLevel): AuthenticationAction` | Returns the action for a given risk level. Falls back to the default if the level is not in the map. |

#### RiskAssessment

The raw output of risk evaluation. Returned by `evaluateRisk()`.

| Field | Type | Description |
|---|---|---|
| `score` | `Int` | Numeric risk score (0-100, higher = riskier) |
| `riskLevel` | `RiskLevel` | Classified risk level based on the score |
| `factorResults` | `List<RiskFactorResult>` | Breakdown of individual risk factor contributions |

#### RiskFactorResult

One entry in the risk breakdown.

| Field | Type | Description |
|---|---|---|
| `name` | `String` | Human-readable factor name (e.g., "Amount Risk") |
| `score` | `Int` | This factor's raw score |
| `weight` | `Double` | How much this factor contributes to the total |
| `description` | `String` | Human-readable explanation |

#### AuthenticationDecision

The combined result of risk evaluation + policy application. Returned by `evaluateAndDecide()`.

| Field | Type | Description |
|---|---|---|
| `riskAssessment` | `RiskAssessment` | The full risk evaluation |
| `action` | `AuthenticationAction` | What the policy recommends: `FRICTIONLESS`, `CHALLENGE`, or `BLOCK` |
| `policyApplied` | `RiskPolicy` | The exact policy that was used (useful for A/B testing logs) |

#### AuthenticationResult

The final outcome of the entire authentication flow.

| Field | Type | Description |
|---|---|---|
| `status` | `AuthenticationStatus` | The final status (see table in Section 6) |
| `decision` | `AuthenticationDecision` | The decision that led to this result |
| `challengeCompletedAt` | `Long?` | Epoch millis when the challenge was completed (only for `AUTHENTICATED_CHALLENGE`) |
| `abandonmentInfo` | `AbandonmentInfo?` | Details about the abandonment (only for `ABANDONED`) |

#### AuthenticationStatus

```kotlin
enum class AuthenticationStatus {
    AUTHENTICATED_FRICTIONLESS,  // Approved without user interaction
    AUTHENTICATED_CHALLENGE,     // Approved after successful OTP
    CHALLENGE_FAILED,            // OTP verification failed
    ABANDONED,                   // User dismissed the challenge screen
    BLOCKED                      // Transaction rejected by policy
}
```

#### AbandonmentInfo

Detailed information about why and when a user abandoned the challenge.

| Field | Type | Description |
|---|---|---|
| `abandonedAt` | `Long` | Epoch millis when the abandonment occurred |
| `timeSpentMillis` | `Long` | How long the user was on the challenge screen |
| `otpAttemptsBeforeAbandon` | `Int` | Number of OTP submissions before abandoning |

---

## 10. Troubleshooting / FAQ

### "YunoThreeDSAuthenticator has not been initialized"

**Cause:** You called `evaluateRisk()`, `evaluateAndDecide()`, or `launchChallenge()` before calling `initialize()`.

**Fix:** Make sure you call `YunoThreeDSAuthenticator.initialize(this)` in your `Application.onCreate()`. Double-check that your `Application` subclass is registered in `AndroidManifest.xml` with `android:name`.

---

### The challenge screen never appears

**Cause 1:** You are calling `launchChallenge()` but the `decision.action` is not `CHALLENGE`.

**Fix:** Always check `decision.action` before calling `launchChallenge()`. Only launch when the action is `AuthenticationAction.CHALLENGE`.

**Cause 2:** Your `ActivityResultLauncher` was registered too late (after `onStart`).

**Fix:** Register the launcher as a class property or in `onCreate()`, never inside a callback or coroutine. The Android Activity Result API requires registration before `STARTED` state.

---

### I always get FRICTIONLESS / never see a challenge

**Cause:** The transaction's risk score is too low. This can happen with small amounts, `TRUSTED` customer trust level, and low velocity.

**Fix:** To test the challenge flow, use a higher amount, `CustomerTrustLevel.NEW`, or submit multiple transactions quickly to trigger velocity scoring. Alternatively, use a custom policy that challenges all risk levels:

```kotlin
import com.yuno.payments.threeds.domain.model.AuthenticationAction
import com.yuno.payments.threeds.domain.model.RiskLevel
import com.yuno.payments.threeds.domain.model.RiskPolicy

val alwaysChallengePolicy = RiskPolicy(
    actions = mapOf(
        RiskLevel.LOW to AuthenticationAction.CHALLENGE,
        RiskLevel.MEDIUM to AuthenticationAction.CHALLENGE,
        RiskLevel.HIGH to AuthenticationAction.CHALLENGE,
        RiskLevel.CRITICAL to AuthenticationAction.BLOCK
    )
)
YunoThreeDSAuthenticator.updateRiskPolicy(alwaysChallengePolicy)
```

---

### What is the default valid OTP?

The default OTP code is **`123456`**. Enter this on the challenge screen to simulate a successful verification. You can change it via `YunoThreeDSConfig.Builder().validOtp("your_code").build()`.

---

### Can I call `evaluateAndDecide()` from the main thread?

No. `evaluateAndDecide()` and `evaluateRisk()` are `suspend` functions. Call them from a coroutine scope such as `viewModelScope.launch { }` or `lifecycleScope.launch { }`. They perform internal computations and should not block the main thread.

---

### Do I need internet access?

No. The current SDK performs all risk evaluation locally on the device. There are no network calls. Device fingerprinting uses `SharedPreferences` and transaction velocity is tracked in memory.

---

### How does device trust work?

When you call `updateDeviceTrust()` after a successful challenge, the SDK increments an internal counter and timestamps the event in `SharedPreferences`. On subsequent transactions, the trust level factor uses this data to lower the risk score for the same device. Over time, a device that consistently passes challenges will see more frictionless transactions.

---

### Can I use this SDK without Jetpack Compose?

The SDK's public API (`YunoThreeDSAuthenticator`) is framework-agnostic. You can call it from any Activity, Fragment, or ViewModel. The challenge screen itself uses Compose internally, but this is transparent to you -- it launches as a standard `Activity` via `ActivityResultLauncher`, so it works the same whether your app uses Compose, XML Views, or a hybrid.

---

### How do I inspect the risk score breakdown?

Use `evaluateRisk()` instead of `evaluateAndDecide()` to get the raw `RiskAssessment`:

```kotlin
import com.yuno.payments.threeds.api.YunoThreeDSAuthenticator
import com.yuno.payments.threeds.domain.model.CustomerTrustLevel
import com.yuno.payments.threeds.domain.model.Transaction

val transaction = Transaction(
    id = "txn_debug",
    amount = 500.00,
    currency = "USD",
    merchantName = "Debug Store",
    cardLast4 = "0000",
    customerTrustLevel = CustomerTrustLevel.NEW,
    timestamp = System.currentTimeMillis()
)

// In a coroutine:
val assessment = YunoThreeDSAuthenticator.evaluateRisk(transaction)
println("Total score: ${assessment.score}")
println("Risk level: ${assessment.riskLevel}")
assessment.factorResults.forEach { factor ->
    println("  ${factor.name}: score=${factor.score}, weight=${factor.weight} -- ${factor.description}")
}
```

---

### Thread safety

`YunoThreeDSAuthenticator` is an `object` (singleton) and is safe to call from any thread. The `initialize()` method is synchronized internally. The risk policy field (`currentRiskPolicy`) is read/written from the main flow and `updateRiskPolicy()` -- if you update the policy from multiple threads simultaneously, the last write wins. In practice this is not a concern because policy changes happen at well-defined points (app startup, A/B config load).

---

## 11. Sample App

The project includes a sample application in the `:app` module that demonstrates all SDK scenarios.

### How to run

1. Clone the repository:

```bash
git clone <repository-url>
cd YunoChallenge
```

2. Open the project in Android Studio (Hedgehog or later recommended).

3. Select the `app` run configuration from the toolbar.

4. Run on an emulator or physical device (API 24+).

### What the sample app demonstrates

The sample app includes pre-built transaction scenarios that exercise every path through the SDK:

| Scenario | What it tests |
|---|---|
| `FRICTIONLESS_LOW_RISK` | Small amount, trusted customer -- goes through silently |
| `CHALLENGE_MEDIUM_RISK` | Medium amount, returning customer -- triggers OTP screen |
| `BLOCKED_HIGH_RISK` | Large amount, new customer -- transaction blocked |
| `VELOCITY_TRIGGER` | Rapid successive transactions -- velocity factor raises risk |
| `TRUSTED_DEVICE` | Device with prior successful authentications |
| `NEW_CUSTOMER` | First-time customer with no device history |
| `ABANDONMENT_TEST` | Designed for testing the back-press / abandonment flow |
| `CUSTOM` | Build your own transaction parameters |

### Project structure

```
YunoChallenge/
├── app/                          # Sample application
│   └── src/main/java/.../
│       ├── domain/
│       │   ├── model/            # SampleTransaction, TransactionScenario
│       │   ├── repository/       # SampleTransactionRepository interface
│       │   └── usecase/          # GetSampleTransactionsUseCase, GetSampleTransactionByIdUseCase
│       ├── data/                 # Repository implementations
│       └── presentation/         # UI screens, ViewModels
├── sdk/                          # The 3DS SDK (this documentation)
│   └── src/main/java/com/yuno/payments/threeds/
│       ├── api/                  # YunoThreeDSAuthenticator, YunoThreeDSConfig
│       ├── domain/
│       │   ├── model/            # Transaction, RiskPolicy, AuthenticationResult, etc.
│       │   ├── repository/       # Repository interfaces
│       │   └── usecase/          # Internal use cases
│       ├── data/
│       │   ├── repository/       # Repository implementations
│       │   └── risk/             # Risk scoring engine and factors
│       ├── presentation/
│       │   ├── challenge/        # ChallengeActivity, ChallengeScreen
│       │   ├── component/        # Reusable UI components
│       │   └── theme/            # SDK internal theme
│       └── di/                   # Internal dependency wiring
├── build.gradle.kts              # Root build file
├── settings.gradle.kts           # Module includes
└── CLAUDE.md                     # Architecture rules
```

---

## Quick Reference Card

```
// Initialize (once, in Application)
YunoThreeDSAuthenticator.initialize(context)

// Authenticate a transaction
val decision = YunoThreeDSAuthenticator.evaluateAndDecide(transaction)

// Branch on the decision
when (decision.action) {
    FRICTIONLESS -> buildFrictionlessResult(decision)
    CHALLENGE    -> launchChallenge(transaction, decision, launcher, context)
    BLOCK        -> buildBlockedResult(decision)
}

// After challenge completes
val result = parseChallengeResult(resultCode, data, decision)

// After successful auth
updateDeviceTrust()
```

---

*SDK version: 1.0.0 | Min SDK: 24 | Package: `com.yuno.payments.threeds`*
