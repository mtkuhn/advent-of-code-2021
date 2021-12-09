# Day 9: Smoke Basin

[[source]](../src/main/kotlin/Day9.kt)

Smoke settles in low points within our height map of the floor. We can avoid trouble by avoiding those points.

## Part 1
Given a grid of numbers that acts as a height map of the ground, find all of the `low points` that are surrounded by
higher numbers in each of its immediate neighbors in the four cardinal directions.
The answer is the sum of `height + 1` for all of the `low points`.

Today's another day for lots of helper functions. Let's start with parsing out our map. 

I'm also defining an `Elevaton` class to track both coordinates and the height values for them, and overloading our map's get
operator to work on these `Elevation` objects.

```kotlin
typealias HeightMap = List<List<Int>>

data class Elevation(val x: Int, val y: Int, val height: Int)

private operator fun HeightMap.get(c: Pair<Int, Int>) = Elevation(c.first, c.second, this[c.first][c.second])

private fun part1(inputFile: String) {
    File(inputFile).readLines()
        .map { it.map { n -> n.digitToInt() } }
}
```

With that we can start doing some fun things, like generating a `Sequence<Elevation>` for everything in our map.
```kotlin
private fun HeightMap.sequenceOfAll(): Sequence<Elevation> =
    generateSequence(this[0 to 0]) { c ->
        if(c.y < this[0].indices.last) { this[c.x to c.y+1] }
        else if (c.x < this.indices.last) { this[c.x+1 to 0] }
        else { null }
    }
```

And we can find all Elevations adjacent to one we're interested in. We need to make sure we check the bounds and
return only those that are valid.

```kotlin
private fun Elevation.getAdjacent(map: HeightMap): Sequence<Elevation> =
    sequenceOf(
        (this.x to this.y+1), (this.x to this.y-1),
        (this.x+1 to this.y), (this.x-1 to this.y)
    ).filter {
        it.first in (map.indices) && it.second in (map[0].indices)
    }.map { map[it] }
```

Using these we can filter down to low points within a given sequence of elevations.

```kotlin
private fun HeightMap.findLowPoints(): Sequence<Elevation> =
    this.sequenceOfAll().filter { e -> e.getAdjacent(this).all { it.height > e.height } }

private fun part1(inputFile: String) {
    File(inputFile).readLines()
        .map { it.map { n -> n.digitToInt() } }
        .findLowPoints()
        .map { e -> e.height + 1 }
        .apply { println(this.sum()) }
}
```

## Part 2

The `low points` aren't enough, we need to know the whole `basin` that drains to that point. Heights of `9` are not in basins,
but everything else is a part of basin that drains to a `low point`.

The answer is the product of the size of the largest 3 basins.

I start by creating a method to determine our basin. We start with a low point, and extend out recursively to
locate any adjacent elevation that is less than 9 but greater than the currently checked elevation.
```kotlin
private fun Elevation.extendAsBasin(map: HeightMap): Set<Elevation> =
    this.getAdjacent(map)
        .filter { it.height != 9 && it.height > this.height }
        .flatMap { it.extendAsBasin(map) }
        .toSet() + setOf(this)
```
Then the answer is simple:
```kotlin
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
```
