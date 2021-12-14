import java.io.File

fun main() {
    solve("src/main/resources/day14sample.txt", 10)
    solve("src/main/resources/day14.txt", 10)
    solve("src/main/resources/day14sample.txt", 40)
    solve("src/main/resources/day14.txt", 40)
}

private fun Map<String, Long>.splitByInsertionRules(pairConversionMap: Map<String, List<String>>): Map<String, Long> =
    map { entry -> pairConversionMap[entry.key]!!.associateWith { entry.value } }.sumMappedCounts()

private fun List<Map<String, Long>>.sumMappedCounts(): Map<String, Long> =
    flatMap { it.keys }.distinct().associateWith { key -> this.sumOf { it[key] ?: 0L } }

/**
 * Sum only the first char of each key, as the second was duplicated when we split windowed pairs.
 * Except the last char of the template was not duplicated, so add 1 for it.
 */
private fun Map<String, Long>.pairsToSingles(template: String): Map<String, Long> =
    this.toMutableMap()
        .apply{ this[template.last().toString()] = 1 }
        .map { entry -> mapOf(entry.key[0].toString() to entry.value) }
        .sumMappedCounts()

private fun solve(inputFile: String, steps: Int) {
    val template = File(inputFile).readLines().first()

    val pairInsertionRegex = """(?<pair>..) -> (?<insert>.)""".toRegex()

    //parse out the rules, then change them to a mapping of chars to a list of pairs the rule would create
    val pairSplitterMap = File(inputFile).readLines()
        .drop(2)
        .mapNotNull { line -> pairInsertionRegex.matchEntire(line)?.groups }
        .map { group -> group["pair"]!!.value to group["insert"]!!.value }
        .associate { rule -> rule.first to listOf(rule.first[0]+rule.second, rule.second+rule.first[1]) }

    //count how many of each windowed pair we have in the initial template
    val pairCountsInTemplate = template.windowed(2)
        .groupingBy { it }
        .eachCount()
        .mapValues { it.value.toLong() }

    //iteratively split each pair according to the splitter map and get a new sum, then count distinct chars for those pairs
    val step = generateSequence(pairCountsInTemplate) { it.splitByInsertionRules(pairSplitterMap) }
        .take(steps+1).last()
        .pairsToSingles(template)

    println(step.maxOf { it.value } - step.minOf { it.value })
}
