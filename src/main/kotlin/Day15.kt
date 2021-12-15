import java.io.File
import java.util.*

fun main() {
    val sampleGrid = "src/main/resources/day15sample.txt".mapFromFile()
    val grid = "src/main/resources/day15.txt".mapFromFile()
    //sampleGrid.fiveByfive().forEach { it.forEach { print(it) }; println() }
    solve(sampleGrid)
    solve(grid)
    solve(sampleGrid.fiveByfive())
    solve(grid.fiveByfive())
}

typealias RiskGrid = List<List<Int>> //already defined in day11

class GridPathNode(val x: Int, val y: Int, val riskTotal: Int, val prev: GridPathNode?) {
    override fun toString() = "<$x,$y|$riskTotal>"

    fun moveToPoint(pt: Pair<Int, Int>, grid: RiskGrid) =
        GridPathNode(pt.first, pt.second, grid[pt.second][pt.first]+riskTotal, this)

    fun position() = this.x to this.y
}

private fun GridPathNode.getAdjacent(grid: RiskGrid): List<GridPathNode> =
    listOf(this.x to this.y+1, this.x to this.y-1, this.x+1 to this.y, this.x-1 to this.y,)
        .filter { it.first >= 0 && it.second >= 0 && it.second < grid.size && it.first < grid[0].size }
        .map { this.moveToPoint(it, grid) }

private fun RiskGrid.score(node: GridPathNode) = node.riskTotal + size*2 - node.x - node.y

private fun RiskGrid.isAtFinish(node: GridPathNode) = node.y == this.indices.last && node.x == this[0].indices.last

private fun String.mapFromFile(): RiskGrid = File(this).readLines().map { line -> line.map { c -> c.digitToInt() } }
fun RiskGrid.incrementedBy(n: Int) = this.map { line -> line.map { i -> ((i+n-1)%9)+1 } }
fun RiskGrid.addRight(newGrid: RiskGrid): RiskGrid = this.mapIndexed { idx, line -> line + newGrid[idx] }
fun RiskGrid.addBelow(newGrid: RiskGrid): RiskGrid = this + newGrid
fun RiskGrid.fiveByfive(): RiskGrid =
    addRight(incrementedBy(1))
        .addRight(incrementedBy(2))
        .addRight(incrementedBy(3))
        .addRight(incrementedBy(4))
        .let { wideMap ->
            wideMap.addBelow(wideMap.incrementedBy(1))
            .addBelow(wideMap.incrementedBy(2))
            .addBelow(wideMap.incrementedBy(3))
            .addBelow(wideMap.incrementedBy(4))
        }

private fun solve(grid: RiskGrid) {
    var minNode = GridPathNode(0, 0, 0, null)
    val fringeX = mutableMapOf(minNode to 0)
    val fringe = PriorityQueue<Pair<GridPathNode, Int>>(compareBy { it.second })
    fringe.add(minNode to grid.score(minNode))
    val checked = mutableSetOf<Pair<Int, Int>>()
    var iteration = 0

    while(!grid.isAtFinish(minNode)) {
        //println("iter=$iteration | fringe=${fringe.size} | checked=${checked.size} | minScore=${minNode.riskTotal}")
        val newPaths = minNode.getAdjacent(grid)
            .filter { minNode.prev?.position() != it.position() } //no doubling back to prior path
            .filter { !checked.contains(it.position()) } //don't bother if we already checked this path
        checked.add(minNode.position())
        fringe.addAll(newPaths.map { it to grid.score(it) })
        iteration++
        minNode = fringe.remove().first
    }

    println(minNode.riskTotal)
}