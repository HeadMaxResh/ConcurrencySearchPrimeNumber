package org.example

import kotlinx.coroutines.runBlocking
import org.example.script.runBenchmarkApp


fun generateStepList(count: Int, step: Int): List<Int> {
    return (1..count).map { i -> i * step }
}

fun main() = runBlocking { runBenchmarkApp() }



