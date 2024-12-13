package org.example.algoritms.parallel

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import org.example.algoritms.basic.SieveOfEratosthenes
import org.example.algoritms.range.RangePrimeChecker
import org.example.strategy.PrimeFindingStrategy
import kotlin.math.sqrt

class ParallelSieveDataDecomposition: PrimeFindingStrategy {

    override suspend fun findPrimes(n: Int, numThreads: Int): List<Int> = runBlocking {
        val m: Int = sqrt(n.toDouble()).toInt()
        val basePrimes = SieveOfEratosthenes().findPrimes(m, numThreads)

        val chunkSize = ((n - m) + numThreads - 1) / numThreads

        val results = List(numThreads) { index ->
            async {
                val start = m + index * chunkSize
                val end = minOf(start + chunkSize - 1, n)
                if (start <= end) {
                    RangePrimeChecker.checkRangeForPrimes(start, end, basePrimes)
                } else {
                    emptyList()
                }
            }
        }.awaitAll().flatten().distinct()

        return@runBlocking basePrimes + results
    }

}