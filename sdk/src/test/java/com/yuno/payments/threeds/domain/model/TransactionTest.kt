package com.yuno.payments.threeds.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class TransactionTest {

    private fun createTransaction(
        id: String = "txn-001",
        amount: Double = 100.0,
        currency: String = "USD",
        merchantName: String = "Test Merchant",
        cardLast4: String = "1234",
        customerTrustLevel: CustomerTrustLevel = CustomerTrustLevel.NEW,
        timestamp: Long = 1000L
    ) = Transaction(
        id = id,
        amount = amount,
        currency = currency,
        merchantName = merchantName,
        cardLast4 = cardLast4,
        customerTrustLevel = customerTrustLevel,
        timestamp = timestamp
    )

    @Test
    fun transaction_equality_withSameValues() {
        val transaction1 = createTransaction()
        val transaction2 = createTransaction()

        assertEquals(transaction1, transaction2)
        assertEquals(transaction1.hashCode(), transaction2.hashCode())
    }

    @Test
    fun transaction_copy_modifiesSpecifiedFields() {
        val original = createTransaction()

        val modified = original.copy(
            amount = 250.0,
            currency = "EUR"
        )

        assertEquals(250.0, modified.amount, 0.001)
        assertEquals("EUR", modified.currency)
        assertEquals(original.id, modified.id)
        assertEquals(original.merchantName, modified.merchantName)
        assertEquals(original.cardLast4, modified.cardLast4)
        assertEquals(original.customerTrustLevel, modified.customerTrustLevel)
        assertEquals(original.timestamp, modified.timestamp)
        assertNotEquals(original, modified)
    }
}
