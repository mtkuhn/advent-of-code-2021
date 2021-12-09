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

class SegmentUtil {
    companion object {
        val ZERO: Set<Char> = "abcefg".toSet()
        val ONE: Set<Char> = "cf".toSet()
        val TWO: Set<Char> = "acdeg".toSet()
        val THREE: Set<Char> = "acdfg".toSet()
        val FOUR: Set<Char> = "bcdf".toSet()
        val FIVE: Set<Char> = "abdfg".toSet()
        val SIX: Set<Char> = "abdefg".toSet()
        val SEVEN: Set<Char> = "acf".toSet()
        val EIGHT: Set<Char> = "abcdefg".toSet()
        val NINE: Set<Char> = "abcdfg".toSet()
        private val ALL_NUMBERS: List<Set<Char>> =
            listOf(ZERO, ONE, TWO, THREE, FOUR, FIVE, SIX, SEVEN, EIGHT, NINE)

        fun allNumbersMappedBy(mapping: Map<Char, Char>): List<Set<Char>> =
            ALL_NUMBERS.map { number ->
                number.map { mapping[it]!! }.toSet()
            }
    }
}

private fun Set<Char>.getCompatibleSegmentsOrNull(): Set<Char>? =
    when(this.size) {
        2 -> SegmentUtil.ONE
        3 -> SegmentUtil.SEVEN
        4 -> SegmentUtil.FOUR
        else -> null
    }

private fun Set<Char>.transformByMap(mapping: Map<Char, Char>): Set<Char> =
    this.map { mapping[it]!! }.toSet()

private fun Set<Char>.toMappedDisplayChar(mapping: Map<Char, Char>): Char =
    when(this) {
        SegmentUtil.ZERO.transformByMap(mapping) -> '0'
        SegmentUtil.ONE.transformByMap(mapping) -> '1'
        SegmentUtil.TWO.transformByMap(mapping) -> '2'
        SegmentUtil.THREE.transformByMap(mapping) -> '3'
        SegmentUtil.FOUR.transformByMap(mapping) -> '4'
        SegmentUtil.FIVE.transformByMap(mapping) -> '5'
        SegmentUtil.SIX.transformByMap(mapping) -> '6'
        SegmentUtil.SEVEN.transformByMap(mapping) -> '7'
        SegmentUtil.EIGHT.transformByMap(mapping) -> '8'
        SegmentUtil.NINE.transformByMap(mapping) -> '9'
        else -> error("Invalid segment display: $this")
    }

private fun generatePossibilitiesMap(wires: List<Set<Char>>): Map<Char, Set<Char>> {
    //note: the key is the 'standard' char for display, the value is possible mappings
    //to start, anything is possible for all mappings
    val possibleMappings = SegmentUtil.EIGHT.associateWith { SegmentUtil.EIGHT }.toMutableMap()

    //find possible values for each given input wire, reduce global possibilities by those groupings
    wires.sortedBy { it.size }
        .forEach { wire ->
            val pos = wire.getCompatibleSegmentsOrNull()
            pos?.forEach { char ->
                possibleMappings[char] = possibleMappings[char]!! intersect wire
            }
        }

    return possibleMappings
}

private fun Map<Char, Set<Char>>.generatePossibleMappings(): List<Map<Char, Char>> =
    this['a']!!.map { a ->
        this['b']!!.filter { it != a }.map { b ->
            this['c']!!.filter { !listOf(a, b).contains(it) }.map { c ->
                this['d']!!.filter { !listOf(a, b, c).contains(it) }.map { d ->
                    this['e']!!.filter { !listOf(a, b, c ,d).contains(it) }.map { e ->
                        this['f']!!.filter { !listOf(a, b, c, d, e).contains(it) }.map { f ->
                            this['g']!!.filter { !listOf(a, b, c, d, e, f).contains(it) }.map { g ->
                                mapOf('a' to a, 'b' to b, 'c' to c, 'd' to d, 'e' to e, 'f' to f, 'g' to g)
                            }
                        }.flatten()
                    }.flatten()
                }.flatten()
            }.flatten()
        }.flatten()
    }.flatten()

private fun Set<Char>.isValidForNumbers(numbers: List<Set<Char>>) =
    numbers.any { number -> this == number }

private fun String.calcOutputValue(): Long {
    val input = this.split(" | ")[0].split(" ").map { it.toSet() }
    val output = this.split(" | ")[1].split(" ").map { it.toSet() }
    val wires = input + output

    val mapping = generatePossibilitiesMap(wires)
        .generatePossibleMappings()
        .map { SegmentUtil.allNumbersMappedBy(it) to it }
        .first { mappedNumbers ->
            wires.all { wire -> wire.isValidForNumbers(mappedNumbers.first) }
        }.second

    return output.map { set -> set.toMappedDisplayChar(mapping) }
        .joinToString("")
        .toLong()
}

private fun part2(inputFile: String) {
    File(inputFile).readLines().asSequence()
        .map { it.calcOutputValue() }
        .sum()
        .apply { println(this) }
}