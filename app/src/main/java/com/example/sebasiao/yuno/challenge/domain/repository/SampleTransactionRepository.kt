package com.example.sebasiao.yuno.challenge.domain.repository

import com.example.sebasiao.yuno.challenge.domain.model.SampleTransaction

interface SampleTransactionRepository {
    fun getSampleTransactions(): List<SampleTransaction>
    fun getById(id: String): SampleTransaction?
}
