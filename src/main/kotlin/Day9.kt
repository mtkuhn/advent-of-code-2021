import java.io.File

fun main() {
    part1("src/main/resources/day9sample.txt")
    part1("src/main/resources/day9.txt")
    part2("src/main/resources/day9sample.txt")
    part2("src/main/resources/day9.txt")
}

typealias HeightMap = List<List<Int>>

data class Elevation(val x: Int, val y: Int, val height: Int)

private operator fun HeightMap.get(c: Pair<Int, Int>) = Elevation(c.first, c.second, this[c.first][c.second])

private fun HeightMap.sequenceOfAll(): Sequence<Elevation> =
    generateSequence(this[0 to 0]) { c ->
        if(c.y < this[0].indices.last) { this[c.x to c.y+1] }
        else if (c.x < this.indices.last) { this[c.x+1 to 0] }
        else { null }
    }

private fun Elevation.getAdjacent(map: HeightMap): Sequence<Elevation> =
    sequenceOf(
        (this.x to this.y+1), (this.x to this.y-1),
        (this.x+1 to this.y), (this.x-1 to this.y)
    ).filter {
        it.first in (map.indices) && it.second in (map[0].indices)
    }.map { map[it] }

private fun HeightMap.findLowPoints(): Sequence<Elevation> =
    this.sequenceOfAll().filter { e -> e.getAdjacent(this).all { it.height > e.height } }

private fun part1(inputFile: String) {
    File(inputFile).readLines()
        .map { it.map { n -> n.digitToInt() } }
        .findLowPoints()
        .map { e -> e.height + 1 }
        .apply { println(this.sum()) }
}

//part 2

private fun Elevation.extendAsBasin(map: HeightMap): Set<Elevation> =
        this.getAdjacent(map)
            .filter { it.height != 9 && it.height > this.height }
            .flatMap { it.extendAsBasin(map) + it + this }
            .toSet()

private fun part2(inputFile: String) {
    File(inputFile).readLines()
        .map { it.map { n -> n.digitToInt() } }
        .let { hmap ->
            hmap.findLowPoints().map { it.extendAsBasin(hmap) }
        }
        .map { it.size }
        .sortedDescending()
        .take(3)
        .reduce { acc, n -> acc * n }
        .apply { println(this) }
}