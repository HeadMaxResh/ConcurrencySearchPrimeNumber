package org.example.algoritms.range

class RangePrimeChecker {

    companion object {
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
                isPrime[j - start] = false
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
            val isComposite = BooleanArray(end - start + 1) { false }
            val firstMultiple = if (start % prime == 0) start else start + (prime - start % prime)
            for (j in firstMultiple..end step prime) {
                isComposite[j - start] = true
            }
            return (start..end).filter { isComposite[it - start] }
        }

        fun findPrimesFromRange(m: Int, n: Int, results: List<Int>): List<Int> {
            val allNumbers = (m + 1..n).toList()
            val compositeSet = results.toSet()
            return allNumbers.filterNot { it in compositeSet }
        }
    }

}