package org.example

import kotlinx.coroutines.*
import org.example.algoritms.basic.ModifiedSieve
import org.example.algoritms.basic.SieveOfEratosthenes
import org.example.algoritms.parallel.ParallelSieveDataDecomposition
import org.example.algoritms.parallel.ParallelSievePrimeNumberDecomposition
import org.example.algoritms.parallel.ParallelSieveSequentialCheck
import org.example.algoritms.parallel.ParallelSieveWithThreadPool
import org.example.calculator.PrimeCalculator
import org.example.script.runBenchmarkApp
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.sqrt
import kotlin.time.measureTime


fun main() = runBlocking { runBenchmarkApp() }
