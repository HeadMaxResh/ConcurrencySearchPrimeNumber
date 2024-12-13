package org.example.script

import org.example.algoritms.parallel.ParallelSieveDataDecomposition
import org.example.algoritms.parallel.ParallelSievePrimeNumberDecomposition
import org.example.algoritms.parallel.ParallelSieveSequentialCheck
import org.example.algoritms.parallel.ParallelSieveWithThreadPool
import org.example.calculator.PrimeCalculator
import kotlin.system.measureTimeMillis
import kotlin.time.measureTime

suspend fun startBenchmarkPrimesApp() {
    while (true) {
        val n = promptUser("Введите максимальное число для поиска простых чисел (n):", 5000)
        val count = promptUser("Введите количество шагов (count):", 40)
        val step = promptUser("Введите шаг между количеством потоков (step):", 10)

        println("Запуск с параметрами: n=$n, count=$count, step=$step")
        benchmarkPrimes(n, count, step)

        if (programExit()) break
    }
}

private suspend fun benchmarkPrimes(n: Int, count: Int, step: Int) {
    val threadCounts = generateStepList(count, step)

    val strategies = listOf(
        ParallelSieveDataDecomposition(),
        ParallelSievePrimeNumberDecomposition(),
        ParallelSieveSequentialCheck(),
        ParallelSieveWithThreadPool()
    )

    println(
        String.format(
            "%-50s %-10s %-15s %-15s %-15s",
            "Стратегия",
            "Потоки",
            "Время (нс)",
            "Ускорение",
            "Эффективность"
        )
    )

    for (strategy in strategies) {
        val calculator = PrimeCalculator(strategy)

        repeat(5) { calculator.calculatePrimes(n, 1) }

        System.gc()
        val sequentialTimeTaken = measureTime {
            calculator.calculatePrimes(n, 1)
        }

        for (numThreads in threadCounts) {
            System.gc()
            val parallelTimeTaken = measureTime {
                calculator.calculatePrimes(n, numThreads)
            }

            val speedup = sequentialTimeTaken / parallelTimeTaken
            val efficiency = speedup / numThreads

            println(
                String.format(
                    "%-50s %-10d %-15d %-15.2f %f",
                    strategy::class.simpleName,
                    numThreads,
                    parallelTimeTaken.inWholeNanoseconds,
                    speedup,
                    efficiency
                )
            )
        }
    }
}

private fun programExit(): Boolean {
    println("Выйти из программы (да/нет):")
    val continueInput = readlnOrNull()?.trim()?.lowercase()
    if (continueInput == "да") {
        println("Завершение программы.")
        return true
    }
    return false
}

private fun promptUser(message: String, defaultValue: Int): Int {
    println("$message (по умолчанию: $defaultValue)")
    val input = readlnOrNull()?.toIntOrNull()
    return input ?: defaultValue
}

private fun generateStepList(count: Int, step: Int): List<Int> {
    return (1..count).map { i -> i * step }
}