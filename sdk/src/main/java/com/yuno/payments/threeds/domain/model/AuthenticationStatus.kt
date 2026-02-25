package com.yuno.payments.threeds.domain.model

enum class AuthenticationStatus {
    AUTHENTICATED_FRICTIONLESS,
    AUTHENTICATED_CHALLENGE,
    CHALLENGE_FAILED,
    ABANDONED,
    BLOCKED
}
