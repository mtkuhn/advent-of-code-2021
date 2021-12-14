# Day 11: Dumbo Octopus

[[source]](../src/main/kotlin/Day11.kt)

We see a grid of Octopuses ahead (denoted by their current energy level). 
Each gains one per turn and flashes if it's energy is greater than 9.
Each flash causes further buildups of one energy in all 8 neighbors, which can trigger more flashes. 
They can only flash once per turn.

## Part 1

How many flashes happen over the course of 100 turns?

Turns out I could copy/paste a fair amount of code from Day 9 (Smoke Basin). I renamed and altered some of it,
but to start we define `Coordinate`, `Grid`, and some operators.
```kotlin
typealias Coordinate = Pair<Int, Int>
typealias Grid = List<MutableList<Int>>
private operator fun Grid.get(c: Coordinate) = this[c.first][c.second]
private operator fun Grid.set(c: Coordinate, n: Int) { this[c.first][c.second] = n }
```
And here are a couple of methods so that we can iterate over everything or just
adjacent octopuses.
```kotlin
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
```
Now we start working with the energy levels. `incAll()` simply promotes the energy level of all octopuses by 1.
`flashAt` sets the current octopus to 0 energy, then for the adjacents it increases their energy (skipping those that already flashed) 
and if necessary recursively calls flash for them.
```kotlin
private fun Grid.incAll(): Grid = this.apply{ this.sequenceOfAll().forEach { this[it] += 1 } }

private fun Grid.flashAt(c: Coordinate) {
    this[c] = 0
    c.getAdjacent(this)
        .filter { this[it] > 0 } //already flashed
        .onEach { this[it] += 1 } //increase energy
        .onEach { if(this[it] > 9) { this.flashAt(it) } } //recursively flash
}
```
Next, we need to combine these into a single turn. We also need the ability to count how many flashed (those we left at 0).
```kotlin
private fun Grid.runTurn(): Grid =
    this.apply { incAll() }
        .apply { sequenceOfAll().forEach { if(this[it] > 9) flashAt(it) } }

private fun Grid.countFlashes(): Int = this.sequenceOfAll().count { this[it] == 0 }
```
Now we generate a sequence of turns, take 100, and generate a sum of counts to get our answer.
```kotlin
private fun part1(inputFile: String) {
    val grid = File(inputFile).readLines()
        .map { line -> line.map { c -> c.digitToInt() }.toMutableList() }

    generateSequence(grid.runTurn()) { g -> g.runTurn() }
        .take(100)
        .sumOf { it.countFlashes() }
        .apply { println(this) }
}
```

## Part 2
The flashing is synchronizing! How many turns until they all flash in unison?

Our previous setup works well for this. We can just take from the generated sequence until we see the
correct number of flashes.
```kotlin
private fun part2(inputFile: String) {
    val grid = File(inputFile).readLines()
        .map { line -> line.map { c -> c.digitToInt() }.toMutableList() }

    generateSequence(grid) { g -> g.runTurn() }
        .takeWhile { it.countFlashes() < 100 }
        .count()
        .apply { println(this) }
}
```