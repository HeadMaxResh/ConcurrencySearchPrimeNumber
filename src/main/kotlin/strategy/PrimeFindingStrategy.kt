package org.example.strategy

interface PrimeFindingStrategy {

    val description: String
    suspend fun findPrimes(n: Int, numThreads: Int): List<Int>

}