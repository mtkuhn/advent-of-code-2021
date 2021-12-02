import java.io.File

fun main() {
    part1("src/main/resources/day1sample.csv")
    part1("src/main/resources/day1.csv")
    part2("src/main/resources/day1sample.csv")
    part2("src/main/resources/day1.csv")
}

private fun part1(inputFile: String) {
    File(inputFile).readLines().asSequence()
        .map { it.toInt() }
        .zipWithNext { a, b -> b - a }
        .count { it > 0 }
        .apply { println(this) }
}

private fun part2(inputFile: String) {
    File(inputFile).readLines().asSequence()
        .map { it.toInt() }
        .windowed(3)
        .map { it.sum() }
        .zipWithNext { a, b -> b - a }
        .count { it > 0 }
        .apply { println(this) }
}