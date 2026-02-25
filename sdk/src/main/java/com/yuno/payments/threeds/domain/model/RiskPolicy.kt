package com.yuno.payments.threeds.domain.model

data class RiskPolicy(
    val actions: Map<RiskLevel, AuthenticationAction>
) {
    fun actionFor(level: RiskLevel): AuthenticationAction =
        actions[level] ?: DEFAULT_ACTIONS.getValue(level)

    companion object {
        private val DEFAULT_ACTIONS = mapOf(
            RiskLevel.LOW to AuthenticationAction.FRICTIONLESS,
            RiskLevel.MEDIUM to AuthenticationAction.CHALLENGE,
            RiskLevel.HIGH to AuthenticationAction.CHALLENGE,
            RiskLevel.CRITICAL to AuthenticationAction.BLOCK
        )

        fun default() = RiskPolicy(DEFAULT_ACTIONS)
    }
}
