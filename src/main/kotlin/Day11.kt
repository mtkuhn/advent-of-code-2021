import java.io.File

fun main() {
    part1("src/main/resources/day11sample.txt")
    part1("src/main/resources/day11.txt")
    part2("src/main/resources/day11sample.txt")
    part2("src/main/resources/day11.txt")
}

typealias Coordinate = Pair<Int, Int>
typealias Grid = List<MutableList<Int>>

private fun Grid.sequenceOfAll(): List<Coordinate> =
    generateSequence(0 to 0) { c ->
        if(c.second < this[0].indices.last) { c.first to c.second+1 }
        else if (c.first < this.indices.last) { c.first+1 to 0 }
        else { null }
    }.toList()

private fun Coordinate.getAdjacent(grid: Grid): List<Coordinate> =
    listOf(
        (this.first to this.second+1), (this.first to this.second-1),
        (this.first+1 to this.second), (this.first-1 to this.second),
        (this.first+1 to this.second+1), (this.first+1 to this.second-1),
        (this.first-1 to this.second+1), (this.first-1 to this.second-1)
    ).filter {
        it.first in (grid.indices) && it.second in (grid[0].indices)
    }

private fun Grid.incAll(): Grid =
    this.apply{ this.sequenceOfAll().forEach { this[it.first][it.second] += 1 } }

private fun Grid.flashAt(c: Coordinate) {
    this[c.first][c.second] = 0
    c.getAdjacent(this)
        .filter { this[it.first][it.second] > 0 } //already flashed
        .onEach { this[it.first][it.second] += 1 } //increase energy
        .onEach { if(this[it.first][it.second] > 9) { this.flashAt(it) } } //recursively flash
}

private fun Grid.runTurn(): Grid =
    this.apply { incAll() }
        .apply { sequenceOfAll().forEach { if(this[it.first][it.second] > 9) flashAt(it) } }

private fun Grid.countFlashes(): Int = this.sequenceOfAll().count { this[it.first][it.second] == 0 }

private fun part1(inputFile: String) {
    val grid = File(inputFile).readLines()
        .map { line -> line.map { c -> c.digitToInt() }.toMutableList() }

    generateSequence(grid.runTurn()) { g -> g.runTurn() }
        .take(100)
        .sumOf { it.countFlashes() }
        .apply { println(this) }
}

private fun part2(inputFile: String) {
    val grid = File(inputFile).readLines()
        .map { line -> line.map { c -> c.digitToInt() }.toMutableList() }

    generateSequence(grid) { g -> g.runTurn() }
        .takeWhile { it.countFlashes() < 100 }
        .count()
        .apply { println(this) }
}