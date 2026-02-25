package com.example.sebasiao.yuno.challenge.data.repository

import com.example.sebasiao.yuno.challenge.domain.model.SampleTransaction
import com.example.sebasiao.yuno.challenge.domain.model.TransactionScenario
import com.example.sebasiao.yuno.challenge.domain.repository.SampleTransactionRepository
import com.yuno.payments.threeds.domain.model.CustomerTrustLevel

class HardcodedSampleTransactionRepository : SampleTransactionRepository {

    private val transactions = listOf(
        // FRICTIONLESS_LOW_RISK (3)
        SampleTransaction(
            id = "txn_001",
            amount = 15.99,
            currency = "USD",
            merchantName = "Coffee Corner",
            cardLast4 = "4242",
            customerTrustLevel = CustomerTrustLevel.TRUSTED,
            scenario = TransactionScenario.FRICTIONLESS_LOW_RISK,
            scenarioDescription = "Low amount from trusted customer - frictionless auth"
        ),
        SampleTransaction(
            id = "txn_002",
            amount = 9.50,
            currency = "USD",
            merchantName = "Bakery Delight",
            cardLast4 = "1111",
            customerTrustLevel = CustomerTrustLevel.TRUSTED,
            scenario = TransactionScenario.FRICTIONLESS_LOW_RISK,
            scenarioDescription = "Small purchase from trusted customer - frictionless"
        ),
        SampleTransaction(
            id = "txn_003",
            amount = 22.00,
            currency = "USD",
            merchantName = "Book Store",
            cardLast4 = "2222",
            customerTrustLevel = CustomerTrustLevel.RETURNING,
            scenario = TransactionScenario.FRICTIONLESS_LOW_RISK,
            scenarioDescription = "Low amount from returning customer - frictionless"
        ),

        // CHALLENGE_MEDIUM_RISK (6)
        SampleTransaction(
            id = "txn_004",
            amount = 350.00,
            currency = "USD",
            merchantName = "Tech Store",
            cardLast4 = "1234",
            customerTrustLevel = CustomerTrustLevel.RETURNING,
            scenario = TransactionScenario.CHALLENGE_MEDIUM_RISK,
            scenarioDescription = "Medium amount from returning customer - challenge flow"
        ),
        SampleTransaction(
            id = "txn_005",
            amount = 480.00,
            currency = "USD",
            merchantName = "Fashion Outlet",
            cardLast4 = "3456",
            customerTrustLevel = CustomerTrustLevel.RETURNING,
            scenario = TransactionScenario.CHALLENGE_MEDIUM_RISK,
            scenarioDescription = "Medium amount online purchase - OTP challenge"
        ),
        SampleTransaction(
            id = "txn_006",
            amount = 275.00,
            currency = "USD",
            merchantName = "Sports Gear",
            cardLast4 = "5678",
            customerTrustLevel = CustomerTrustLevel.RETURNING,
            scenario = TransactionScenario.CHALLENGE_MEDIUM_RISK,
            scenarioDescription = "Medium amount returning customer - challenge required"
        ),
        SampleTransaction(
            id = "txn_007",
            amount = 600.00,
            currency = "USD",
            merchantName = "Home Depot",
            cardLast4 = "7890",
            customerTrustLevel = CustomerTrustLevel.NEW,
            scenario = TransactionScenario.CHALLENGE_MEDIUM_RISK,
            scenarioDescription = "Medium amount from new customer - challenge flow"
        ),
        SampleTransaction(
            id = "txn_008",
            amount = 520.00,
            currency = "USD",
            merchantName = "Travel Agency",
            cardLast4 = "4321",
            customerTrustLevel = CustomerTrustLevel.RETURNING,
            scenario = TransactionScenario.CHALLENGE_MEDIUM_RISK,
            scenarioDescription = "Travel booking medium risk - challenge authentication"
        ),
        SampleTransaction(
            id = "txn_009",
            amount = 310.00,
            currency = "USD",
            merchantName = "Wellness Spa",
            cardLast4 = "6543",
            customerTrustLevel = CustomerTrustLevel.TRUSTED,
            scenario = TransactionScenario.CHALLENGE_MEDIUM_RISK,
            scenarioDescription = "Unusual amount for trusted customer - challenge verification"
        ),

        // BLOCKED_HIGH_RISK (3)
        SampleTransaction(
            id = "txn_010",
            amount = 5000.00,
            currency = "USD",
            merchantName = "Luxury Goods",
            cardLast4 = "9876",
            customerTrustLevel = CustomerTrustLevel.NEW,
            scenario = TransactionScenario.BLOCKED_HIGH_RISK,
            scenarioDescription = "High amount from new customer - blocked"
        ),
        SampleTransaction(
            id = "txn_011",
            amount = 8500.00,
            currency = "USD",
            merchantName = "Jewelry Palace",
            cardLast4 = "0000",
            customerTrustLevel = CustomerTrustLevel.NEW,
            scenario = TransactionScenario.BLOCKED_HIGH_RISK,
            scenarioDescription = "Very high amount from new customer - blocked"
        ),
        SampleTransaction(
            id = "txn_012",
            amount = 12000.00,
            currency = "USD",
            merchantName = "Electronics Mega",
            cardLast4 = "9999",
            customerTrustLevel = CustomerTrustLevel.NEW,
            scenario = TransactionScenario.BLOCKED_HIGH_RISK,
            scenarioDescription = "Extremely high amount from new customer - blocked"
        ),

        // VELOCITY_TRIGGER (3)
        SampleTransaction(
            id = "txn_013",
            amount = 75.00,
            currency = "USD",
            merchantName = "Quick Mart",
            cardLast4 = "5555",
            customerTrustLevel = CustomerTrustLevel.RETURNING,
            scenario = TransactionScenario.VELOCITY_TRIGGER,
            scenarioDescription = "Multiple rapid transactions - velocity check triggered"
        ),
        SampleTransaction(
            id = "txn_014",
            amount = 45.00,
            currency = "USD",
            merchantName = "Gas Station",
            cardLast4 = "5555",
            customerTrustLevel = CustomerTrustLevel.RETURNING,
            scenario = TransactionScenario.VELOCITY_TRIGGER,
            scenarioDescription = "Rapid refuel transactions - velocity flag"
        ),
        SampleTransaction(
            id = "txn_015",
            amount = 120.00,
            currency = "USD",
            merchantName = "Online Grocery",
            cardLast4 = "5555",
            customerTrustLevel = CustomerTrustLevel.TRUSTED,
            scenario = TransactionScenario.VELOCITY_TRIGGER,
            scenarioDescription = "Multiple quick purchases - velocity detection"
        ),

        // TRUSTED_DEVICE (1)
        SampleTransaction(
            id = "txn_016",
            amount = 200.00,
            currency = "USD",
            merchantName = "Electronics Hub",
            cardLast4 = "8888",
            customerTrustLevel = CustomerTrustLevel.TRUSTED,
            scenario = TransactionScenario.TRUSTED_DEVICE,
            scenarioDescription = "Trusted device recognized - reduced friction"
        ),

        // NEW_CUSTOMER (1)
        SampleTransaction(
            id = "txn_017",
            amount = 1200.00,
            currency = "USD",
            merchantName = "Premium Store",
            cardLast4 = "3333",
            customerTrustLevel = CustomerTrustLevel.NEW,
            scenario = TransactionScenario.NEW_CUSTOMER,
            scenarioDescription = "First-time customer - extra verification required"
        ),

        // ABANDONMENT_TEST (1)
        SampleTransaction(
            id = "txn_018",
            amount = 450.00,
            currency = "USD",
            merchantName = "Online Shop",
            cardLast4 = "7777",
            customerTrustLevel = CustomerTrustLevel.RETURNING,
            scenario = TransactionScenario.ABANDONMENT_TEST,
            scenarioDescription = "Test abandonment tracking during challenge"
        )
    )

    override fun getSampleTransactions(): List<SampleTransaction> = transactions

    override fun getById(id: String): SampleTransaction? =
        transactions.find { it.id == id }
}
