# Day 10: Syntax Scoring

[[source]](../src/main/kotlin/Day10.kt)

Our navigation system is spewing some bad data. Let's take a look at it.

## Part 1

The readout shows some lines that are incomplete or corrupt. We ignore incompleteness for now.

Corrupt lines are those that do not correctly open and close using `()`, `[]`, `{}`, or `<>`. 
Given a list of lines, find the first corrupt character. That is, the first unbalanced closer.

We need to convert each `Char` to a corresponding score and sum them.

My strategy on this is to use a stack to track corresponding openers and closers. Openers will be pushed onto the stack
as normal. Closers will not be pushed and instead will remove their corresponding opener. If no matching opener is
at the top of the stack, then we report the corrupted `Char`.

```kotlin
data class ChunkStack(val openers: MutableList<Char> = mutableListOf()) {

    companion object {
        val endPairs = mapOf('[' to ']', '(' to ')', '{' to '}', '<' to '>')
    }

    private fun Char.isOpener(): Boolean = this in endPairs.map { it.key }
    private fun Char.isCloserForTopOfStack(): Boolean = this == endPairs[openers.first()]

    fun pushOrReturnInvalidChar(c: Char): Char? =
        when {
            c.isOpener() -> apply { openers.add(0, c) }.let { null }
            c.isCloserForTopOfStack() -> apply { openers.removeFirst() }.let { null }
            else -> c
        }

}
```

Using this, we can write a quick function to find the first corruption per line.
```kotlin
fun String.getFirstCorruptChar(): Char? =
    ChunkStack().let { cs -> this.firstOrNull { c -> cs.pushOrReturnInvalidChar(c) != null } }
```

And we also need to handle the scoring of `Char`:
```kotlin
fun Char.getSyntaxCheckPoints(): Int =
    when(this) {
        ')' -> 3
        ']' -> 57
        '}' -> 1197
        '>' -> 25137
        else -> error("Unknown closer")
    }
```

Now it's just a matter of converting lines to corrupted chars, to scores, and summing.
```kotlin
private fun part1(inputFile: String) {
    File(inputFile).readLines().asSequence()
        .map { line -> line.getFirstCorruptChar() }
        .filterNotNull()
        .map { it.getSyntaxCheckPoints() }
        .apply { println(this.sum()) }
}
```

## Part 2

Now we want to ignore corruptions and focus only on incomplete lines. We need to fill in the missing `Char` values
to complete those lines, then do some complicated scoring on them.

First, the point conversions per `Char`.
```kotlin
fun Char.getAutoCompletePoints(): Int =
    when(this) {
        ')' -> 1
        ']' -> 2
        '}' -> 3
        '>' -> 4
        else -> error("Unknown closer")
    }
```

For the meat of part 2, we can use our `ChunkStack` to simply pop the compliment of everything it holds when we reach 
the end of incomplete lines.
```kotlin
//in the ChunkStack class
fun popCompliments(): List<Char> = openers.mapNotNull { c -> endPairs[c] }
```
```kotlin
fun String.getMissingClosers(): List<Char> =
    ChunkStack().apply { this@getMissingClosers.forEach { this.pushOrReturnInvalidChar(it) } }.popCompliments()
```

Finally, we bring it together:
```kotlin
private fun part2(inputFile: String) {
    File(inputFile).readLines().asSequence()
        .filter { line -> line.getFirstCorruptChar() == null }
        .map { it.getMissingClosers() }
        .map { completeChars ->
            completeChars.map { char -> char.getAutoCompletePoints() }
                .fold(0L) { acc, pts -> (acc*5)+pts }
        }
        .sorted().toList()
        .let { n -> n[n.size/2] }
        .apply { println(this) }
}
```