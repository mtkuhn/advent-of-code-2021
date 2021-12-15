import java.io.File
import java.util.PriorityQueue

fun main() {
    val sampleGrid = "src/main/resources/day15sample.txt".mapFromFile()
    val grid = "src/main/resources/day15.txt".mapFromFile()
    solve(sampleGrid)
    solve(grid)
    solve(sampleGrid.fiveByFive())
    solve(grid.fiveByFive())
}

typealias RiskGrid = List<List<Int>>

class GridPathNode(val x: Int, val y: Int, val riskTotal: Int) {
    fun moveToPoint(pt: Pair<Int, Int>, grid: RiskGrid) =
        GridPathNode(pt.first, pt.second, grid[pt.second][pt.first]+riskTotal)

    fun position() = this.x to this.y
}

private fun GridPathNode.getAdjacent(grid: RiskGrid): List<GridPathNode> =
    listOf(this.x to this.y+1, this.x to this.y-1, this.x+1 to this.y, this.x-1 to this.y)
        .filter { it.first >= 0 && it.second >= 0 && it.second < grid.size && it.first < grid[0].size }
        .map { this.moveToPoint(it, grid) }

private fun String.mapFromFile(): RiskGrid = File(this).readLines().map { line -> line.map { c -> c.digitToInt() } }

private fun RiskGrid.score(node: GridPathNode) = node.riskTotal + size*2 - node.x - node.y

private fun RiskGrid.isAtFinish(node: GridPathNode) = node.y == this.indices.last && node.x == this[0].indices.last

fun RiskGrid.incrementedBy(n: Int) = this.map { line -> line.map { i -> ((i+n-1)%9)+1 } }

fun RiskGrid.addRight(newGrid: RiskGrid): RiskGrid = this.mapIndexed { idx, line -> line + newGrid[idx] }

fun RiskGrid.addBelow(newGrid: RiskGrid): RiskGrid = this + newGrid

fun RiskGrid.fiveByFive(): RiskGrid =
    (1 until 5).fold(this) { acc, i -> acc.addRight(this.incrementedBy(i)) }
    .let { wideMap -> (1 until 5).fold(wideMap) { acc, i -> acc.addBelow(wideMap.incrementedBy(i)) } }

private fun solve(grid: RiskGrid) {
    var minNode = GridPathNode(0, 0, 0)
    val fringe = PriorityQueue<Pair<GridPathNode, Int>>(compareBy { it.second })
        .apply{ add(minNode to grid.score(minNode)) }
    val checked = mutableSetOf<Pair<Int, Int>>()

    while(!grid.isAtFinish(minNode)) {
        fringe.addAll(
            minNode.getAdjacent(grid)
                .filter { !checked.contains(it.position()) } //don't bother if we already checked this path
                .map { it to grid.score(it) }
        )
        checked.add(minNode.position())
        minNode = fringe.remove().first
    }

    println(minNode.riskTotal)
}