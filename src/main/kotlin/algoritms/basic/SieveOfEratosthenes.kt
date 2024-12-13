package org.example.algoritms.basic

import org.example.strategy.PrimeFindingStrategy
import kotlin.math.sqrt

class SieveOfEratosthenes() : PrimeFindingStrategy {

    override val description: String
        get() = "Классический алгоритм решета Эратосфена"

    override suspend fun findPrimes(n: Int, numThreads: Int): List<Int> {
        val isPrime = BooleanArray(n + 1) { true }
        isPrime[0] = false
        isPrime[1] = false

        for (i in 2..sqrt(n.toDouble()).toInt()) {
            if (isPrime[i]) {
                for (j in i * i..n step i) {
                    isPrime[j] = false
                }
            }
        }

        return (2..n).filter { isPrime[it] }
    }

}