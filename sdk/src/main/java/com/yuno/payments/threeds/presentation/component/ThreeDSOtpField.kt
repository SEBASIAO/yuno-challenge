package com.yuno.payments.threeds.presentation.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.yuno.payments.threeds.presentation.theme.ThreeDSTheme

private const val MAX_OTP_LENGTH = 6

@Composable
internal fun ThreeDSOtpField(
    otp: String,
    onOtpChanged: (String) -> Unit,
    isError: Boolean = false,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = otp,
        onValueChange = { newValue ->
            if (newValue.length <= MAX_OTP_LENGTH && newValue.all { it.isDigit() }) {
                onOtpChanged(newValue)
            }
        },
        modifier = modifier.fillMaxWidth(),
        enabled = enabled,
        label = { Text(text = "Verification Code") },
        placeholder = { Text(text = "000000") },
        isError = isError,
        textStyle = MaterialTheme.typography.headlineSmall.copy(
            textAlign = TextAlign.Center
        ),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number
        ),
        singleLine = true
    )
}

@Preview(showBackground = true)
@Composable
private fun ThreeDSOtpFieldPreview() {
    ThreeDSTheme {
        ThreeDSOtpField(
            otp = "123",
            onOtpChanged = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ThreeDSOtpFieldErrorPreview() {
    ThreeDSTheme {
        ThreeDSOtpField(
            otp = "999",
            onOtpChanged = {},
            isError = true
        )
    }
}
