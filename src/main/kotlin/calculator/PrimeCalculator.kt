package org.example.calculator

import org.example.strategy.PrimeFindingStrategy

class PrimeCalculator(private val strategy: PrimeFindingStrategy) {
    suspend fun calculatePrimes(n: Int, numThreads: Int): List<Int> {
        return strategy.findPrimes(n, numThreads)
    }
}