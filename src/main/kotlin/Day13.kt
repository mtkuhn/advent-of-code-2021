import java.io.File

fun main() {
    part1("src/main/resources/day13sample.txt")
    part1("src/main/resources/day13.txt")
    part2("src/main/resources/day13sample.txt")
    part2("src/main/resources/day13.txt")
}

//typealias Point = Pair<Int, Int> //already defined in day5
typealias PaperFold = Pair<Char, Int>

fun parsePoints(inputFile: String): Set<Point> =
    File(inputFile).readLines()
        .takeWhile { it.contains(",") }
        .map { line -> line.split(",") }
        .map { line -> line[0].toInt() to line[1].toInt() }
        .toSet()

fun parseFolds(inputFile: String): List<PaperFold> =
    File(inputFile).readLines()
        .takeLastWhile { it.contains("fold") }
        .map { line -> line.takeLastWhile { it != ' ' }.split("=") }
        .map { line -> line[0][0] to line[1].toInt() }

fun Set<Point>.foldPaperBy(fold: PaperFold): Set<Point> =
    when(fold.first) {
        'x' -> {
            this.filter { it.first > fold.second }
                .map { fold.second-(it.first-fold.second) to it.second }
                .toSet() union this.filter { it.first < fold.second }
        }
        'y' -> {
            this.filter { it.second > fold.second }
                .map { it.first to fold.second-(it.second-fold.second) }
                .toSet() union this.filter { it.second < fold.second }
        }
        else -> error("invalid fold type")
    }

fun Set<Point>.printOnGrid() {
    val width = this.maxOf { it.first }
    val height = this.maxOf { it.second }
    (0 .. height).forEach { y ->
        (0 .. width).forEach { x ->
            if(this.contains(x to y)) print("#") else print(".")
        }
        println()
    }
}

private fun part1(inputFile: String) {
    val points = parsePoints(inputFile)
    val folds = parseFolds(inputFile)

    points.foldPaperBy(folds.first()).apply { println(this.size) }
}

private fun part2(inputFile: String) {
    val points = parsePoints(inputFile)
    val folds = parseFolds(inputFile)

    folds.fold(points) { acc, f -> acc.foldPaperBy(f) }
        .apply { this.printOnGrid() }
}