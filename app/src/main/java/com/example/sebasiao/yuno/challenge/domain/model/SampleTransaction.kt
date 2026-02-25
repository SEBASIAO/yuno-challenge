package com.example.sebasiao.yuno.challenge.domain.model

import com.yuno.payments.threeds.domain.model.CustomerTrustLevel

data class SampleTransaction(
    val id: String,
    val amount: Double,
    val currency: String,
    val merchantName: String,
    val cardLast4: String,
    val customerTrustLevel: CustomerTrustLevel,
    val scenario: TransactionScenario,
    val scenarioDescription: String
)
