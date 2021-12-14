import java.io.File

fun main() {
    part1("src/main/resources/day14sample.txt")
    part1("src/main/resources/day14.txt")
    part2("src/main/resources/day14sample.txt")
    part2("src/main/resources/day14.txt")
}

private fun String.applyPairInsertions(pairInsertionMap: Map<String, String>) =
    windowed(2, 1, true)
        .joinToString("") { win -> win[0] + (pairInsertionMap[win] ?: "") }

private fun part1(inputFile: String) {
    val pairInsertionRegex = "(..) -> (.)".toRegex()
    val pairInsertionMap = File(inputFile).readLines()
        .drop(2)
        .mapNotNull { line -> pairInsertionRegex.find(line)?.destructured?.toList() }
        .associate { lineList -> lineList[0] to lineList[1] }
    val polymerTemplate = File(inputFile).readLines().first()

    val step = generateSequence(polymerTemplate) { it.applyPairInsertions(pairInsertionMap) }
        .take(11)
        .last()

    val charCounts = step.toList().distinct().associateWith { c -> step.count { it == c } }
    println(charCounts.maxOf { it.value } - charCounts.minOf { it.value })
}

private fun Map<String, Long>.splitByInsertionRules(pairConversionMap: Map<String, List<String>>): Map<String, Long> =
    this.map { entry ->
        (pairConversionMap[entry.key]?.associate { it to entry.value })?:error("bad map: $entry")
    }.consolidateMappedCounts()

private fun List<Map<String, Long>>.consolidateMappedCounts(): Map<String, Long> {
    val consolidatedKeys = map { it.keys }.flatten().distinct()
    return consolidatedKeys.associateWith { key -> this.sumOf { it[key] ?: 0L } }
}

private fun Map<String, Long>.pairsToSinglesWrong(): Map<String, Long> =
    this.map { entry ->
        listOf(entry.key[0].toString() to entry.value, entry.key[1].toString() to entry.value).toMap()
    }.consolidateMappedCounts()

private fun Map<String, Long>.pairsToSingles(): Map<String, Long> =
    this.map { entry ->
        listOf(entry.key[0].toString() to entry.value).toMap()
    }.consolidateMappedCounts()


private fun part2(inputFile: String) {
    val pairInsertionRegex = "(..) -> (.)".toRegex()
    val pairInsertionMap = File(inputFile).readLines()
        .drop(2)
        .mapNotNull { line -> pairInsertionRegex.find(line)?.destructured?.toList() }
        .associate { lineList -> lineList[0] to lineList[1] }

    val pairConversionMap = pairInsertionMap.entries.associate {
        it.key to listOf(it.key[0]+it.value, it.value+it.key[1])
    }

    val template = File(inputFile).readLines().first()
    val pairCountsInTemplate = File(inputFile).readLines().first()
        .windowed(2)
        .groupingBy { it }.eachCount()
        .map { it.key to it.value.toLong() }.toMap()

    val step = generateSequence(pairCountsInTemplate) { it.splitByInsertionRules(pairConversionMap) }
        .take(41).last()
        .pairsToSingles()
        .toMutableMap()
        .apply { this[template.last().toString()] = this[template.last().toString()]!! + 1 }

    println(step.maxOf { it.value } - step.minOf { it.value })
}
