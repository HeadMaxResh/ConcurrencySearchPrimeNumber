package org.example.algoritms.basic

import org.example.strategy.PrimeFindingStrategy
import kotlin.math.sqrt

class ModifiedSieve() : PrimeFindingStrategy {

    override val description: String
        get() = "Модифицированный алгоритм решета Эратосфена"


    override suspend fun findPrimes(n: Int, numThreads: Int): List<Int> {
        val m: Int = sqrt(n.toDouble()).toInt()
        val basePrimes = SieveOfEratosthenes().findPrimes(m, numThreads)
        val isPrime = BooleanArray(n - m + 1) { true }
        for (prime in basePrimes) {
            val start = m + (prime - m % prime) % prime
            for (j in start..n step prime) {
                isPrime[j - m] = false
            }
        }
        return basePrimes + (m..n).filter { isPrime[it - m] }
    }

}