package org.example.strategy

interface PrimeFindingStrategy {
    suspend fun findPrimes(n: Int, numThreads: Int): List<Int>
}