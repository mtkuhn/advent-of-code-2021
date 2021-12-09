import java.io.File
import java.lang.RuntimeException

fun main() {
    part1("src/main/resources/day2sample.txt")
    part1("src/main/resources/day2.txt")
    part2("src/main/resources/day2sample.txt")
    part2("src/main/resources/day2.txt")
}

operator fun Pair<Int, Int>.plus(pos: Pair<Int, Int>) = first+pos.first to second+pos.second

private fun part1(inputFile: String) {
    File(inputFile).readLines().asSequence()
        .map { line -> line.split(' ') }
        .map { parts ->
            when (parts[0]) {
                "forward" -> parts[1].toInt() to 0
                "down" -> 0 to parts[1].toInt()
                "up" -> 0 to -parts[1].toInt()
                else -> 0 to 0
            }
        }
        .reduce { acc, move -> acc + move }
        .apply { println(this.first * this.second) }
}

data class SubPosition(var horizontal: Int, var vertical: Int, var aim: Int) {
    fun applyCommand(type: String, value: Int): SubPosition =
        when (type) {
            "forward" -> SubPosition(horizontal+value, vertical-(aim*value), aim)
            "down" -> SubPosition(horizontal, vertical, aim+value)
            "up" -> SubPosition(horizontal, vertical, aim-value)
            else -> throw RuntimeException("invalid command")
        }
}

private fun part2(inputFile: String) {
    File(inputFile).readLines().asSequence()
        .map { line -> line.split(' ') }
        .fold(SubPosition(0, 0, 0)) { acc, move -> acc.applyCommand(move[0], move[1].toInt()) }
        .apply { println(this.horizontal * -this.vertical) }
}