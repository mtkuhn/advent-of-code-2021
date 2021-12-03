# Day 2: Dive!

[[source]](../src/main/kotlin/Day2.kt)

Our sub can take commands!

# Part 1
Commands come in the form of `forward X`, `down x`, or `up X`, 
each of which can change our horizontal and vertical positions directly by X.

First I parse the command out into an array of Strings, then use a `when` clause to switch between operations depending
on the first value. Each updates a `Pair` result object that contains the horizontal and vertical values respectively.

```
    File(inputFile).readLines().asSequence()
        .map { line -> line.split(' ') }
        .map { parts ->
            when (parts[0]) {
                "forward" -> parts[1].toInt() to 0
                "down" -> 0 to parts[1].toInt()
                "up" -> 0 to -parts[1].toInt()
                else -> 0 to 0
            }
        }
```

Then we can use a `reduce` to accumulate the sum of these commands into a single pair of coordinates.

```
        .reduce { acc, move -> acc + move }
        .apply { println(this.first * this.second) }
```

This is aided by an operation for adding pairs:
```
operator fun Pair<Int, Int>.plus(pos: Pair<Int, Int>) = first+pos.first to second+pos.second
```

# Part 2
Actually, the commands for up and down change an `aim` value instead of the vertical. The forward command moves the sub
the same horizontally, but also changes the vertical by `aim * value`.

For this I found it useful to create an object for tracking the values involved along with a method for applying a
command.
```
data class SubPosition(var horizontal: Int, var vertical: Int, var aim: Int) {
    fun applyCommand(type: String, value: Int): SubPosition =
        when (type) {
            "forward" -> SubPosition(horizontal+value, vertical-(aim*value), aim)
            "down" -> SubPosition(horizontal, vertical, aim+value)
            "up" -> SubPosition(horizontal, vertical, aim-value)
            else -> throw RuntimeException("invalid command")
        }
}
```

Using this, it's just a simple `fold` (much like the prior `reduce`, but with an initial value) to get the end result.
``` 
    File(inputFile).readLines().asSequence()
        .map { line -> line.split(' ') }
        .fold(SubPosition(0, 0, 0)) { acc, move -> acc.applyCommand(move[0], move[1].toInt()) }
        .apply { println(this.horizontal * -this.vertical) }
```