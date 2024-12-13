package org.example.algoritms.parallel

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import org.example.algoritms.basic.SieveOfEratosthenes
import org.example.algoritms.range.RangePrimeChecker
import org.example.algoritms.range.RangePrimeChecker.Companion.checkRangeForComposite
import org.example.algoritms.range.RangePrimeChecker.Companion.findPrimesFromRange
import org.example.strategy.PrimeFindingStrategy
import kotlin.math.sqrt

class ParallelSievePrimeNumberDecomposition: PrimeFindingStrategy {

    override val description: String
        get() = "Декомпозиция набора простых чисел"

    override suspend fun findPrimes(n: Int, numThreads: Int): List<Int> = runBlocking {
        val m: Int = sqrt(n.toDouble()).toInt()
        val basePrimes = SieveOfEratosthenes().findPrimes(m, numThreads)

        val chunkSize = maxOf(2, (basePrimes.size + numThreads - 1) / numThreads)
        val primeChunks = basePrimes.chunked(chunkSize)

        val results = primeChunks.map { primesSubset ->
            async {
                checkRangeForComposite(m + 1, n, primesSubset)
            }
        }.awaitAll().flatten().distinct()

        val primesInRange = findPrimesFromRange(m, n, results)

        return@runBlocking basePrimes + primesInRange
    }

}