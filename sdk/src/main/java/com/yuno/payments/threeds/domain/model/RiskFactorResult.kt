package com.yuno.payments.threeds.domain.model

data class RiskFactorResult(
    val name: String,
    val score: Int,
    val weight: Double,
    val description: String
)
