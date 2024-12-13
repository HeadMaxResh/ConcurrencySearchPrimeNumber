package org.example.algoritms.parallel

import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.example.algoritms.basic.SieveOfEratosthenes
import org.example.algoritms.range.RangePrimeChecker
import org.example.strategy.PrimeFindingStrategy
import java.util.concurrent.Executors
import kotlin.math.sqrt

class ParallelSieveWithThreadPool: PrimeFindingStrategy {

    override suspend fun findPrimes(n: Int, numThreads: Int): List<Int> = coroutineScope {
        val m: Int = sqrt(n.toDouble()).toInt()
        val basePrimes = SieveOfEratosthenes().findPrimes(m, numThreads)

        val threadPool = Executors.newFixedThreadPool(numThreads).asCoroutineDispatcher()
        val chunkSize = ((n - m) + numThreads - 1) / numThreads

        threadPool.use { _ ->
            val results = List(numThreads) { index ->
                async(threadPool) {
                    val start = m + index * chunkSize + 1
                    val end = minOf(start + chunkSize - 1, n)
                    if (start <= end) {
                        RangePrimeChecker.checkRangeForPrimes(start, end, basePrimes)
                    } else {
                        emptyList()
                    }
                }
            }.awaitAll().flatten()

            return@coroutineScope basePrimes + results
        }
    }

}