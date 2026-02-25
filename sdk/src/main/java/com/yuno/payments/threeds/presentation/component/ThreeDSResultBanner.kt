package com.yuno.payments.threeds.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yuno.payments.threeds.presentation.theme.ThreeDSGreen
import com.yuno.payments.threeds.presentation.theme.ThreeDSTheme

@Composable
internal fun ThreeDSResultBanner(
    isSuccess: Boolean,
    message: String,
    modifier: Modifier = Modifier
) {
    val iconColor = if (isSuccess) {
        ThreeDSGreen
    } else {
        MaterialTheme.colorScheme.error
    }
    val iconText = if (isSuccess) "\u2713" else "\u2717"

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = iconText,
            style = MaterialTheme.typography.displayLarge,
            color = iconColor,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ThreeDSResultBannerSuccessPreview() {
    ThreeDSTheme {
        ThreeDSResultBanner(
            isSuccess = true,
            message = "Verification successful"
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ThreeDSResultBannerFailurePreview() {
    ThreeDSTheme {
        ThreeDSResultBanner(
            isSuccess = false,
            message = "Verification failed"
        )
    }
}
