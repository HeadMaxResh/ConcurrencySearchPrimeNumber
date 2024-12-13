package org.example.algoritms.parallel

import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.example.algoritms.basic.SieveOfEratosthenes
import org.example.algoritms.range.RangePrimeChecker.Companion.checkRangeForComposite
import org.example.algoritms.range.RangePrimeChecker.Companion.findPrimesFromRange
import org.example.strategy.PrimeFindingStrategy
import java.util.concurrent.Executors
import kotlin.math.sqrt

class ParallelSieveWithThreadPoolSingleCheck() : PrimeFindingStrategy {

    override val description: String
        get() = "Применение пула потоков по одному базовому"

    override suspend fun findPrimes(n: Int, numThreads: Int): List<Int>  = coroutineScope {
        val m: Int = sqrt(n.toDouble()).toInt()
        val basePrimes = SieveOfEratosthenes().findPrimes(m, numThreads)

        val threadPool = Executors.newFixedThreadPool(numThreads).asCoroutineDispatcher()

        threadPool.use { _ ->
            val results = basePrimes.map { prime ->
                async(threadPool) {
                    checkRangeForComposite(m + 1, n, prime)
                }
            }.awaitAll().flatten().distinct().sorted()

            val primesInRange = findPrimesFromRange(m, n, results)

            return@coroutineScope basePrimes + primesInRange
        }
    }
}