import java.io.File

fun main() {
    part1("src/main/resources/day8sample.txt")
    part1("src/main/resources/day8.txt")
    part2("src/main/resources/day8sample.txt")
    part2("src/main/resources/day8.txt")
}

private fun part1(inputFile: String) {
    File(inputFile).readLines().asSequence()
        .map { it.split(" | ").last() }
        .map { it.split(" ") }
        .map { output ->
            output.mapNotNull { digit ->
                when(digit.length) {
                    2 -> 1
                    3 -> 7
                    4 -> 4
                    7 -> 8
                    else -> null
                }
            }
        }
        .flatten()
        .count()
        .apply { println(this) }
}

//part 2

enum class SegmentDisplay(val pattern: Set<Char>, val char: Char) {
    ZERO("abcefg".toSet(), '0'),
    ONE("cf".toSet(), '1'),
    TWO("acdeg".toSet(), '2'),
    THREE("acdfg".toSet(), '3'),
    FOUR("bcdf".toSet(), '4'),
    FIVE("abdfg".toSet(), '5'),
    SIX("abdefg".toSet(), '6'),
    SEVEN("acf".toSet(), '7'),
    EIGHT("abcdefg".toSet(), '8'),
    NINE("abcdfg".toSet(), '9')
}

fun displaysToRemappedPattern(mapping: Map<Char, Char>): Map<SegmentDisplay, Set<Char>> =
    SegmentDisplay.values().associateWith { display ->
        display.pattern.map { mapping[it]!! }.toSet()
    }

private fun Set<Char>.getCompatibleSegmentsOrNull(): Set<Char>? =
    when(this.size) {
        2 -> SegmentDisplay.ONE.pattern
        3 -> SegmentDisplay.SEVEN.pattern
        4 -> SegmentDisplay.FOUR.pattern
        else -> null
    }

private fun Set<Char>.toMappedDisplayChar(mapping: Map<Char, Char>): Char =
    displaysToRemappedPattern(mapping).entries.first { it.value == this }.key.char


private fun generatePossibilitiesMap(wires: List<Set<Char>>): Map<Char, Set<Char>> {
    //note: the key is the 'standard' char for display, the value is possible mappings
    //to start, anything is possible for all mappings
    val possibleMappings = SegmentDisplay.EIGHT.pattern.associateWith { SegmentDisplay.EIGHT.pattern }.toMutableMap()

    //find possible values for each given input wire, reduce possible mappings by those groupings
    wires.sortedBy { it.size }
        .forEach { wire ->
            val pos = wire.getCompatibleSegmentsOrNull()
            pos?.forEach { char ->
                possibleMappings[char] = possibleMappings[char]!! intersect wire
            }
        }

    return possibleMappings
}

private fun Map<Char, Char>.extendMappingsToNextElement(possibilities: Map<Char, Set<Char>>): List<Map<Char, Char>> =
    if(this.size <= SegmentDisplay.EIGHT.pattern.indices.last) {
        SegmentDisplay.EIGHT.pattern.elementAt(this.size).let { charToMap ->
            possibilities[charToMap]!!
                .filter { posChar -> !this.values.contains(posChar) }
                .map { posChar -> this + mapOf(charToMap to posChar) }
                .map { it.extendMappingsToNextElement(possibilities) }
                .flatten()
        }
    }
    else { listOf(this) }

private fun Map<Char, Set<Char>>.generatePossibleMappings(): List<Map<Char, Char>> =
    emptyMap<Char, Char>().extendMappingsToNextElement(this)

private fun Set<Char>.isValidForNumbers(numbers: List<Set<Char>>) =
    numbers.any { number -> this == number }

private fun String.calcOutputValueForLine(): Long {
    val inputDisplays = this.split(" | ")[0].split(" ").map { it.toSet() }
    val outputDisplays = this.split(" | ")[1].split(" ").map { it.toSet() }
    val allDisplays = inputDisplays + outputDisplays

    val mapping = generatePossibilitiesMap(allDisplays)
        .generatePossibleMappings()
        .map { displaysToRemappedPattern(it) to it }
        .first { displayMapToMapping ->
            allDisplays.all { display -> display.isValidForNumbers(displayMapToMapping.first.values.toList()) }
        }.second

    return outputDisplays.map { set -> set.toMappedDisplayChar(mapping) }
        .joinToString("")
        .toLong()
}

private fun part2(inputFile: String) {
    File(inputFile).readLines().asSequence()
        .map { it.calcOutputValueForLine() }
        .sum()
        .apply { println(this) }
}