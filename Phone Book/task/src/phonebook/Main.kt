package phonebook

import java.io.File
import java.util.*
import kotlin.math.floor
import kotlin.math.sqrt

const val directoryPath = "/Users/jwgibanez/Downloads/directory.txt"
const val findPath = "/Users/jwgibanez/Downloads/find.txt"

val directory = mutableListOf<String>()
val sortedDirectory = mutableListOf<String>()
var bubbleSortEnd = 0L
var counter = 0
var lines = 0
var bubbleSortMillisLimit = 0L

fun main() {
    val startLinear = System.currentTimeMillis()
    println("Start searching (linear search)...")
    load()
    linearSearch()
    val endLinear = System.currentTimeMillis()
    printFound(startLinear, endLinear)
    println()
    bubbleSortMillisLimit = endLinear + 10 * (endLinear - startLinear)

    lines = 0
    counter = 0
    val startBubbleSortJumpSearch = System.currentTimeMillis()
    println("Start searching (bubble sort + jump search)...")
    if (!bubbleSort()) {
        val startLinearSearch = System.currentTimeMillis()
        linearSearch()
        val endLinearSearch = System.currentTimeMillis()
        printFound(startBubbleSortJumpSearch, endLinearSearch)
        printSortTime(startBubbleSortJumpSearch, bubbleSortEnd, true)
        printSearchTime(startLinearSearch, endLinearSearch)
    } else {
        val startJumpSearch = System.currentTimeMillis()
        jumpSearch()
        val endBubbleSortJumpSearch = System.currentTimeMillis()
        printFound(startBubbleSortJumpSearch, endBubbleSortJumpSearch)
        printSortTime(startBubbleSortJumpSearch, bubbleSortEnd, false)
        printSearchTime(startJumpSearch, endBubbleSortJumpSearch)
    }
    println()

    lines = 0
    counter = 0
    sortedDirectory.clear()
    val startQuickSortBinarySearch = System.currentTimeMillis()
    println("Start searching (quick sort + binary search)...")
    val sorted = quickSort(directory)
    val quickSortEnd = System.currentTimeMillis()
    sortedDirectory.addAll(sorted)
    val startBinarySearch = System.currentTimeMillis()
    binarySearch()
    val endQuickSortBinarySearch = System.currentTimeMillis()
    printFound(startQuickSortBinarySearch, endQuickSortBinarySearch)
    printSortTime(startQuickSortBinarySearch, quickSortEnd, false)
    printSearchTime(startBinarySearch, endQuickSortBinarySearch)
    println()

    lines = 0
    counter = 0
    val hashTableStart = System.currentTimeMillis()
    println("Start searching (hash table)...")
    val hashTable = createHashTable()
    val hashTableCreateEnd = System.currentTimeMillis()
    searchHashTable(hashTable)
    val searchHashTableEnd = System.currentTimeMillis()
    printFound(hashTableStart, searchHashTableEnd)
    printCreateTime(hashTableStart, hashTableCreateEnd)
    printSearchTime(hashTableCreateEnd, searchHashTableEnd)
}

fun printFound(start: Long, end: Long) {
    val diff = end - start
    val millis = diff % 1000
    val seconds = (diff / 1000) % 60
    val minutes = diff / 1000 / 60
    println("Found $counter / $lines entries. Time taken: $minutes min. $seconds sec. $millis ms.")
}

fun printSortTime(start: Long, end: Long, stopped: Boolean) {
    val diff = end - start
    val millis = diff % 1000
    val seconds = (diff / 1000) % 60
    val minutes = diff / 1000 / 60
    if (stopped) {
        println("Sorting time: $minutes min. $seconds sec. $millis ms. - STOPPED, moved to linear search")
    } else {
        println("Sorting time: $minutes min. $seconds sec. $millis ms.")
    }
}

fun printSearchTime(start: Long, end: Long) {
    val diff = end - start
    val millis = diff % 1000
    val seconds = (diff / 1000) % 60
    val minutes = diff / 1000 / 60
    println("Searching time: $minutes min. $seconds sec. $millis ms.")
}

fun load() {
    try {
        val file = File(directoryPath)
        file.forEachLine {
            val (number, name) = it.split("\\s+".toRegex(), 2)
            directory.add(name)
        }
    } catch (e: NoSuchFileException) {
        println("No such file $directoryPath")
    }
}

fun linearSearch() {
    try {
        val file = File(findPath)
        file.forEachLine {
            lines++
            if (directory.contains(it)) counter++
        }
    } catch (e: NoSuchFileException) {
        println("No such file $findPath")
    }
}

fun bubbleSort(): Boolean {
    sortedDirectory.addAll(directory)
    while (true) {
        if (bubbleSortMillisLimit < System.currentTimeMillis()) {
            bubbleSortEnd = System.currentTimeMillis()
            return false
        }
        var didNotChange = true
        for (i in 0 until sortedDirectory.size - 1) {
            if (sortedDirectory[i] > sortedDirectory[i+1]) {
                val temp = sortedDirectory[i]
                sortedDirectory[i] = sortedDirectory[i+1]
                sortedDirectory[i+1] = temp
                didNotChange = false
            }
        }
        if (didNotChange) break
    }
    bubbleSortEnd = System.currentTimeMillis()
    return true
}

fun jumpSearch() {
    val sqrtN = floor(sqrt(sortedDirectory.size.toDouble())).toInt()
    try {
        val file = File(findPath)
        file.forEachLine {
            lines++
            var idx = sqrtN - 1
            var leftBorder = 0
            while (true) {
                if (sortedDirectory[idx] == it) {
                    counter++
                    break
                } else if (sortedDirectory[idx] < it) {
                    leftBorder = idx + 1
                    idx += sqrtN
                    if (idx > sortedDirectory.lastIndex) {
                        idx = sortedDirectory.lastIndex
                    }
                } else {
                    for (i in idx-1 downTo leftBorder) {
                        if (sortedDirectory[i] == it) {
                            counter++
                            break
                        }
                    }
                    break
                }
            }
        }
    } catch (e: NoSuchFileException) {
        println("No such file $findPath")
    }
}

fun quickSort(array: List<String>): List<String> {
    if (array.size < 2) {
        return array
    } else {
        val last = array.last()
        val pivot = mutableListOf(last)
        val left = mutableListOf<String>()
        val right = mutableListOf<String>()
        for (i in 0 until array.lastIndex) {
            val value = array[i]
            when {
                value == last -> {
                    pivot.add(pivot.lastIndex, value)
                }
                value > last -> {
                    right.add(value)
                }
                else -> {
                    left.add(value)
                }
            }
        }
        return quickSort(left) + pivot + quickSort(right)
    }
}

fun binarySearch() {
    val search = { list: List<String>, query: String ->
        var found = false
        if (list.isNotEmpty()) {
            var left = 0
            var right = list.lastIndex
            while (true) {
                val idx = (left + right) / 2
                val value = sortedDirectory[idx]
                if (value == query) {
                    found = true
                    break
                } else if (value < query) {
                    left = idx + 1
                    if (left > right)
                        break
                } else {
                    right = idx - 1
                    if (left > right)
                        break
                }
            }
        }
        found
    }
    try {
        val file = File(findPath)
        file.forEachLine {
            lines++
            val found = search(sortedDirectory, it)
            if (found) counter++
        }
    } catch (e: NoSuchFileException) {
        println("No such file $findPath")
    }
}

fun createHashTable(): Hashtable<String, String> {
    val hashTable = Hashtable<String, String>()
    try {
        val file = File(directoryPath)
        file.forEachLine {
            val (number, name) = it.split("\\s+".toRegex(), 2)
            hashTable[name] = number
        }
    } catch (e: NoSuchFileException) {
        println("No such file $directoryPath")
    }
    return hashTable
}

fun searchHashTable(hashTable: Hashtable<String, String>) {
    try {
        val file = File(findPath)
        file.forEachLine {
            lines++
            val found = hashTable.containsKey(it)
            if (found) counter++
        }
    } catch (e: NoSuchFileException) {
        println("No such file $findPath")
    }
}

fun printCreateTime(start: Long, end: Long) {
    val diff = end - start
    val millis = diff % 1000
    val seconds = (diff / 1000) % 60
    val minutes = diff / 1000 / 60
    println("Creating time: $minutes min. $seconds sec. $millis ms.")
}