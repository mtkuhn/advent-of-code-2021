#Day 1: Sonar Sweep
The elves lost Santa's keys to the depths, and we need to use a submarine to find and retrieve them.

## Part 1
Given a list of depths proceeding outward from your submarine, 
find how many times the depth measurement increases from the previous value.

A Kotlin sequence `.zipWithNext()` made quick work of this. 
It maps a value and it's next value, allowing me to quickly find the diff of the two values.
```
    File(inputFile).readLines().asSequence()
        .map { it.toInt() }
        .zipWithNext { a, b -> b - a }
        .count { it > 0 }
        .apply { println(this) }
```

## Part 2
Instead of taking the values provided, instead consider the sums for
a 3-measurement sliding window before looking for increasing values.

Again, Kotlin sequences have us covered with the `.windowed()` function.
By giving it a size of 3 I instantly get exactly what we want, sliding windows of 3 values that can be easily summed.

```
    File(inputFile).readLines().asSequence()
        .map { it.toInt() }
        .windowed(3)
        .map { it.sum() }
        .zipWithNext { a, b -> b - a }
        .count { it > 0 }
        .apply { println(this) }
```