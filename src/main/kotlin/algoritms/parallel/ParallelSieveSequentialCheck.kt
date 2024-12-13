package org.example.algoritms.parallel

import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.example.algoritms.basic.SieveOfEratosthenes
import org.example.algoritms.range.RangePrimeChecker
import org.example.strategy.PrimeFindingStrategy
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.sqrt

class ParallelSieveSequentialCheck: PrimeFindingStrategy {

    override suspend fun findPrimes(n: Int, numThreads: Int): List<Int> = coroutineScope {
        val m: Int = sqrt(n.toDouble()).toInt()
        val basePrimes = SieveOfEratosthenes().findPrimes(m, numThreads)
        val currentIndex = AtomicInteger(0)

        val threadPool = Executors.newFixedThreadPool(numThreads).asCoroutineDispatcher()
        val primeQueue = basePrimes.toMutableList()
        val lock = Any()

        threadPool.use { _ ->
            val results = (1..numThreads).map {
                async(threadPool) {
                    val localPrimes = ConcurrentHashMap.newKeySet<Int>()
                    while (true) {
                        val currentPrime: Int? = synchronized(lock) {
                            if (primeQueue.isNotEmpty()) {
                                primeQueue.removeAt(0)
                            } else {
                                null
                            }
                        }

                        if (currentPrime == null) break

                        localPrimes.addAll(RangePrimeChecker.checkRangeForComposite(m + 1, n, currentPrime))
                    }
                    localPrimes
                }
            }.awaitAll().flatten().distinct().sorted()

            val primesInRange = RangePrimeChecker.findPrimesFromRange(m, n, results)

            return@coroutineScope basePrimes + primesInRange
        }
    }
}