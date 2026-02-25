package com.yuno.payments.threeds.domain.model

data class Transaction(
    val id: String,
    val amount: Double,
    val currency: String,
    val merchantName: String,
    val cardLast4: String,
    val customerTrustLevel: CustomerTrustLevel,
    val timestamp: Long
)
