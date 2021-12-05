import java.io.File

fun main() {
    part1("src/main/resources/day5sample.txt")
    part1("src/main/resources/day5.txt")
    part2("src/main/resources/day5sample.txt")
    part2("src/main/resources/day5.txt")
}

typealias Point = Pair<Int, Int>
typealias Line = Pair<Point, Point>

fun Int.rangeBetween(i: Int): IntProgression =
    if(this > i) { i..this }
    else { i downTo this }

fun Line.isHorizontal() = this.first.first == this.second.first
fun Line.isVertical() = this.first.second == this.second.second
fun Line.getHorizontalPoints() = (this.first.second.rangeBetween(this.second.second)).map { y -> this.first.first to y }
fun Line.getVerticalPoints() = (this.first.first.rangeBetween(this.second.first)).map { x -> x to this.first.second }
fun Line.get45DegreePoints() = getVerticalPoints().map { it.first }.zip(getHorizontalPoints().map { it.second })

fun Line.getAllCoveredPoints(allow45degree: Boolean): List<Point> =
    when {
        this.isHorizontal() -> getHorizontalPoints()
        this.isVertical() -> getVerticalPoints()
        allow45degree -> get45DegreePoints()
        else -> emptyList()
    }

private fun part1(inputFile: String) {
    val lineRegex = "(\\d+),(\\d+) -> (\\d+),(\\d+)".toRegex()
    File(inputFile).readLines().asSequence()
        .mapNotNull { input -> lineRegex.find(input)?.destructured?.toList() }
        .map { list -> (list[0].toInt() to list[1].toInt()) to (list[2].toInt() to list[3].toInt()) }
        .map { line -> line.getAllCoveredPoints(false) }
        .flatten()
        .groupBy { it }
        .filter { it.value.size > 1 }
        .size
        .apply { println(this) }
}

private fun part2(inputFile: String) {
    val lineRegex = "(\\d+),(\\d+) -> (\\d+),(\\d+)".toRegex()
    File(inputFile).readLines().asSequence()
        .mapNotNull { input -> lineRegex.find(input)?.destructured?.toList() }
        .map { list -> (list[0].toInt() to list[1].toInt()) to (list[2].toInt() to list[3].toInt()) }
        .map { line -> line.getAllCoveredPoints(true) }
        .flatten()
        .groupBy { it }
        .filter { it.value.size > 1 }
        .size
        .apply { println(this) }
}