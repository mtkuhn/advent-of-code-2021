# Day 4: Giant Squid

[[source]](../src/main/kotlin/Day4.kt)

A giant squid wants to play bingo with us.

# Part 1
Given an input of numbers to be called in a given order, and a list of 5x5 bingo boards, find the score of the winning board.

The score is the sum of all unmarked (aka yet uncalled) numbers on the board multiplied by the last (winning) number called.

First I'm going to define `BingoBoard` object that can be initialized from a 2D list. I'll add some functions to this later.
```kotlin
private class BingoBoard(val array: List<List<Int>>) { }
```

The next thing we need to do is parse the input. The first line has the called numbers, the rest are 5x5 string separated
numbers, split by empty lines.
```kotlin 
    val lines = File(inputFile).readLines()
    val callNumbers = lines[0].split(",").map { it.toInt() }
    val boards = getBoardsFromLines(lines.drop(1))
```
To split the boards, we first drop the first line with the called numbers, remove blank names, then chunk our lines into
lists of 5 lines each. This gets us a `List<List<Int>>` of size 5 that we can pass into our object.
```kotlin
private fun getBoardsFromLines(lines: List<String>) = lines.asSequence()
    .filter { line -> line.isNotBlank() }
    .map { line -> line.trim().split(" +".toRegex()).map { it.toInt() } }
    .chunked(5)
    .map { board -> BingoBoard(board) }.toSet()
```
Now we're at a point where need to flesh out our `BingoBoard` with some helpful methods for evaluating the board state.

I chose to pass in a list of called numbers when needed rather than create a data structure to denote a marked status. 
There's probably some performance to be gained in going the other way with it, but this seems to be quick enough for our input.

I'm not going to bother detailing each of these, but the point is that we can now easily evaluate which minimal list of called numbers
is the first winner and determine the score given a list of called numbers for the board.
```kotlin
private class BingoBoard(val array: List<List<Int>>) {

    fun findMinimalWinningNumberList(allCallingNumbers: List<Int>): List<Int> =
        allCallingNumbers.indices.firstNotNullOf { n ->
            val numbers = allCallingNumbers.subList(0, n)
            if(isWin(numbers)) numbers else null
        }

    private fun isWin(calledInts: List<Int>): Boolean = hasWinningRow(calledInts) || hasWinningCol(calledInts)

    private fun hasWinningRow(calledInts: List<Int>): Boolean =
        array.any { line -> line.all { i -> calledInts.contains(i) }}

    private fun hasWinningCol(calledInts: List<Int>): Boolean =
        array.indices.map { i -> array.map { row -> row[i] } }
            .any { col -> col.all { i -> calledInts.contains(i) }}

    private fun getAllUnmarked(calledInts: List<Int>) = array.flatten().minus(calledInts.toSet())

    fun getScore(calledInts: List<Int>) = getAllUnmarked(calledInts).sum()*calledInts.last()
}
```

With that, finding the first winning board is simple: we find the winning numbers for each board and take the smallest set.

```kotlin
private fun part1(inputFile: String) {
    val lines = File(inputFile).readLines()
    val callNumbers = lines[0].split(",").map { it.toInt() }
    val firstWinScore = getBoardsFromLines(lines.drop(1))
        .associateWith { b -> b.findMinimalWinningNumberList(callNumbers) }
        .minByOrNull { boardToWinList -> boardToWinList.value.size }
        ?.let { firstWin -> firstWin.key.getScore(firstWin.value) }?:0
    println(firstWinScore)
}
```

# Part 2
We want to let the squid win by picking the board that wins last.

With the work we did in Part 1 this is now trivial. We do the exact same thing, but now favoring the board with the longest
set of winning numbers.
```kotlin
private fun part2(inputFile: String) {
    val lines = File(inputFile).readLines()
    val callNumbers = lines[0].split(",").map { it.toInt() }
    val lastWinScore = getBoardsFromLines(lines.drop(1))
        .associateWith { b -> b.findMinimalWinningNumberList(callNumbers) }
        .maxByOrNull { boardToWinList -> boardToWinList.value.size }
        ?.let { firstWin -> firstWin.key.getScore(firstWin.value) }?:0
    println(lastWinScore)
}
```