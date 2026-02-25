package com.yuno.payments.threeds.domain.model

internal data class TransactionVelocity(
    val transactionsInWindow: Int,
    val windowSizeMillis: Long
)
