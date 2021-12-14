# Day 12: Passage Pathing

[[source]](../src/main/kotlin/Day12.kt)

We need to navigate across a network of big and small caves.

Given a list of how cave passages connect, find all paths from `start` to `end`,
without visiting a small (lower-case named) cave more than once.

For part 2, which will be solved below at the same time, we allow just one small cave to be visited twice.

First, let's parse our input into pairs of joining caverns.
```kotlin
File(inputFile).readLines()
        .map { line -> line.split("-") }
        .map { it[0] to it[1] }
```
I'll want to convert these into a node object. First let's define that, along with a couple of quick convenience methods.
A `Node` tracks this cave, it's name, and the list of all caves it connects to.
```kotlin
class Node(val name: String, private val nodes: MutableSet<Node> = mutableSetOf()) {
    fun connectTo(node: Node) { nodes.add(node) }
    fun isBig() = name[0].isUpperCase()
}
```
Now we need to convert our pairs of caves into a list of `Node`. We connect both nodes in the pair to each other, creating
and nodes that do not yet exist.
```kotlin
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
```
Before we start traversing the nodes, let's create some helper functions that we'll need concerning if a path is allowed
to further traverse into any given `Node`. This includes an optional case for our part 2 constraints: allowing one small cave revisit.
```kotlin
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
```
With that out of the way, let's update `Node` with some methods for traversal.

`traverseNext()` expands the list of nodes the last node in the path points to, filters them down to just valid options, and returns a new list of paths.
`pathsTo()` generates paths in a loop until they have all reached the destination.
```kotlin
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
```
Finally, we use all of this to solve.
```kotlin
private fun solve(inputFile: String, allowSingleRevisit: Boolean) {
    val nodes = File(inputFile).readLines()
        .map { line -> line.split("-") }
        .map { it[0] to it[1] }
        .toNodes()

    nodes.find { it.name == "start" }!!
        .pathsTo(nodes.find { it.name == "end" }!!, allowSingleRevisit)
        .apply { println(this.count()) }
}
```
