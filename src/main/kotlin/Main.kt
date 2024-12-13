package org.example

import kotlinx.coroutines.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.sqrt
import kotlin.time.measureTime


fun generateStepList(count: Int, step: Int): List<Int> {
    return (1..count).map { i -> i * step }
}

fun main() = runBlocking {

    val n = 500
    val threadCounts = generateStepList(1, 10)

    println(String.format("%-10s %-15s %-15s %-15s", "Потоки", "Время (мс)", "Ускорение", "Эффективность"))

    repeat(5) { parallelSieveSequentialCheck(n, 1) }

    System.gc()
    val sequentialTimeTaken = measureTime {
        val sequentialPrimes = parallelSieveSequentialCheck(n, 1)
    }

    for (numThreads in threadCounts) {

        System.gc()
        val parallelTimeTaken = measureTime {
            val parallelPrimes = parallelSieveSequentialCheck(n, numThreads)
            println(parallelPrimes)
        }

        val speedup = sequentialTimeTaken / parallelTimeTaken
        val efficiency = speedup / numThreads

        println(
            String.format(
                "%-10d %-15d %-15.2f %f",
                numThreads,
                parallelTimeTaken.inWholeMilliseconds,
                speedup,
                efficiency
            )
        )

    }
}

fun sieveOfEratosthenes(n: Int): List<Int> {
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

fun modifiedSieve(n: Int): List<Int> {
    val m: Int = sqrt(n.toDouble()).toInt()
    val basePrimes = sieveOfEratosthenes(m)
    val isPrime = BooleanArray(n - m + 1) { true }
    for (prime in basePrimes) {
        val start = m + (prime - m % prime) % prime
        for (j in start..n step prime) {
            isPrime[j - m] = false
        }
    }
    return basePrimes + (m..n).filter { isPrime[it - m] }

}

suspend fun checkRangeForPrimes(start: Int, end: Int, primes: List<Int>): List<Int> {
    val isPrime = BooleanArray(end - start + 1) { true }
    for (prime in primes) {
        val firstMultiple = if (start % prime == 0) start else start + (prime - start % prime)
        for (j in firstMultiple..end step prime) {
            isPrime[j - start] = false
        }
    }

    return (start..end).filter { isPrime[it - start] }
}

fun checkRangeForPrimes(start: Int, end: Int, prime: Int): List<Int> {
    val isPrime = BooleanArray(end - start + 1) { true }
    val firstMultiple = if (start % prime == 0) start else start + (prime - start % prime)
    for (j in firstMultiple..end step prime) {
        isPrime[j - (start)] = false
    }
    return (start..end).filter { isPrime[it - start] }
}

suspend fun checkRangeForComposite(start: Int, end: Int, primes: List<Int>): List<Int> {
    val isComposite = BooleanArray(end - start + 1) { false }
    for (prime in primes) {
        val firstMultiple = if (start % prime == 0) start else start + (prime - start % prime)
        for (j in firstMultiple..end step prime) {
            isComposite[j - start] = true
        }
    }

    return (start..end).filter { isComposite[it - start] }
}

fun checkRangeForComposite(start: Int, end: Int, prime: Int): List<Int> {
    val isPrime = BooleanArray(end - start + 1) { false }
    val firstMultiple = if (start % prime == 0) start else start + (prime - start % prime)
    for (j in firstMultiple..end step prime) {
        isPrime[j - (start)] = true
    }
    return (start..end).filter { isPrime[it - start] }
}

private fun findPrimesFromRange(
    m: Int,
    n: Int,
    results: List<Int>,
): List<Int> {
    val allNumbers = (m + 1..n).toList()
    val compositeSet = results.toSet()
    val primesInRange = allNumbers.filterNot { it in compositeSet }
    return primesInRange
}

suspend fun parallelSieveDataDecomposition(n: Int, numThreads: Int): List<Int> = runBlocking {
    val m: Int = sqrt(n.toDouble()).toInt()
    val basePrimes = sieveOfEratosthenes(m)

    val chunkSize = ((n - m) + numThreads - 1) / numThreads

    val results = List(numThreads) { index ->
        async {
            val start = m + index * chunkSize
            val end = minOf(start + chunkSize - 1, n)
            if (start <= end) {
                checkRangeForPrimes(start, end, basePrimes)
            } else {
                emptyList()
            }
        }
    }.awaitAll().flatten().distinct()

    return@runBlocking basePrimes + results
}

fun parallelSievePrimeNumberDecomposition(n: Int, numThreads: Int): List<Int> = runBlocking {
    val m: Int = sqrt(n.toDouble()).toInt()
    val basePrimes = sieveOfEratosthenes(m)

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

suspend fun parallelSieveWithThreadPoolSingleCheck(n: Int, numThreads: Int): List<Int> = coroutineScope {
    val m: Int = sqrt(n.toDouble()).toInt()
    val basePrimes = sieveOfEratosthenes(m)

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

suspend fun parallelSieveWithThreadPool(n: Int, numThreads: Int): List<Int> = coroutineScope {
    val m: Int = sqrt(n.toDouble()).toInt()
    val basePrimes = sieveOfEratosthenes(m)

    val threadPool = Executors.newFixedThreadPool(numThreads).asCoroutineDispatcher()

    val chunkSize = ((n - m) + numThreads - 1) / numThreads

    threadPool.use { _ ->
        val results = List(numThreads) { index ->
            async(threadPool) {
                val start = m + index * chunkSize + 1
                val end = minOf(start + chunkSize - 1, n)
                if (start <= end) {
                    checkRangeForPrimes(start, end, basePrimes)
                } else {
                    emptyList()
                }
            }
        }.awaitAll().flatten()

        return@coroutineScope basePrimes + results
    }
}

suspend fun parallelSieveSequentialCheck(n: Int, numThreads: Int): List<Int> = coroutineScope {
    val m: Int = sqrt(n.toDouble()).toInt()
    val basePrimes = sieveOfEratosthenes(m)
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

                    localPrimes.addAll(checkRangeForComposite(m + 1, n, currentPrime))
                }
                localPrimes
            }
        }.awaitAll().flatten().distinct().sorted()

        val primesInRange = findPrimesFromRange(m, n, results)

        return@coroutineScope basePrimes + primesInRange
    }
}

