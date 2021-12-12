import java.io.File

fun main() {
    solve("src/main/resources/day12sample.txt", false)
    solve("src/main/resources/day12sample2.txt", false)
    solve("src/main/resources/day12.txt", false)
    solve("src/main/resources/day12sample.txt", true)
    solve("src/main/resources/day12sample2.txt", true)
    solve("src/main/resources/day12.txt", true)
}

class Node(val name: String, private val nodes: MutableSet<Node> = mutableSetOf()) {
    fun connectTo(node: Node) { nodes.add(node) }
    fun isBig() = name[0].isUpperCase()

    private fun List<Node>.traverseNext(allowSingleRevisit: Boolean): List<List<Node>> =
        this.last().nodes
            .filter { n -> canVisitCave(n, allowSingleRevisit) }
            .map { n -> this + n }

    fun pathsTo(destination: Node, allowSingleRevisit: Boolean): List<List<Node>> {
        var paths = listOf(listOf(this))
        while(paths.any { path -> path.last() != destination }) {
            paths = paths.map { path ->
                if(path.last() == destination) listOf(path)
                else path.traverseNext(allowSingleRevisit)
            }.flatten()
        }
        return paths
    }
}

private fun MutableList<Node>.findOrAddNode(name: String): Node =
    this.find { it.name == name }?:(Node(name).apply { this@findOrAddNode.add(this) })

private fun List<Pair<String, String>>.toNodes(): List<Node> {
    val nodes = mutableListOf<Node>()
    this.forEach { connection ->
        val origin = nodes.findOrAddNode(connection.first)
        val destination = nodes.findOrAddNode(connection.second)
        origin.connectTo(destination)
        destination.connectTo(origin)
    }
    return nodes
}

private fun List<Node>.hasSingleSmallCaveRevisit() =
    filter { !it.isBig() }.any { n -> this.count { it == n } > 1 }

private fun List<Node>.canVisitCave(node: Node, allowSingleRevisit: Boolean) =
    when {
        node.isBig() -> true
        node.name == "start" -> false
        !this.contains(node) -> true
        allowSingleRevisit && !hasSingleSmallCaveRevisit() && this.count { it == node } < 2 -> true
        else -> false
    }

private fun solve(inputFile: String, allowSingleRevisit: Boolean) {
    val nodes = File(inputFile).readLines()
        .map { line -> line.split("-") }
        .map { it[0] to it[1] }
        .toNodes()

    nodes.find { it.name == "start" }!!
        .pathsTo(nodes.find { it.name == "end" }!!, allowSingleRevisit)
        .apply { println(this.count()) }
}