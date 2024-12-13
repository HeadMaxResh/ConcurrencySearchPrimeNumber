package org.example

import kotlinx.coroutines.*
import org.example.algoritms.basic.ModifiedSieve
import org.example.algoritms.basic.SieveOfEratosthenes
import org.example.algoritms.parallel.ParallelSieveDataDecomposition
import org.example.algoritms.parallel.ParallelSievePrimeNumberDecomposition
import org.example.algoritms.parallel.ParallelSieveSequentialCheck
import org.example.algoritms.parallel.ParallelSieveWithThreadPool
import org.example.calculator.PrimeCalculator
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
    val threadCounts = generateStepList(40, 10)

    val strategies = listOf(
        ParallelSieveDataDecomposition(),
        ParallelSievePrimeNumberDecomposition(),
        ParallelSieveSequentialCheck(),
        ParallelSieveWithThreadPool()
    )

    println(String.format(
        "%-50s %-10s %-15s %-15s %-15s",
        "Стратегия",
        "Потоки",
        "Время (мс)",
        "Ускорение",
        "Эффективность"
    ))

    for (strategy in strategies) {
        val calculator = PrimeCalculator(strategy)

        repeat(5) {  calculator.calculatePrimes(n, 1) }

        System.gc()
        val sequentialTimeTaken = measureTime {
            val sequentialPrimes =  calculator.calculatePrimes(n, 1)
        }

        for (numThreads in threadCounts) {

            System.gc()
            val parallelTimeTaken = measureTime {
                val parallelPrimes = calculator.calculatePrimes(n, numThreads)
                //println(parallelPrimes)
                //println("Простые числа для ${strategy::class.simpleName} и $numThreads потоков")
            }

            val speedup = sequentialTimeTaken / parallelTimeTaken
            val efficiency = speedup / numThreads

            println(
                String.format(
                    "%-50s %-10d %-15d %-15.2f %f",
                    strategy::class.simpleName,
                    numThreads,
                    parallelTimeTaken.inWholeMilliseconds,
                    speedup,
                    efficiency
                )
            )

        }

    }
}

