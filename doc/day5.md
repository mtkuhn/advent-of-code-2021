# Day 5: Hydrothermal Venture

[[source]](../src/main/kotlin/Day5.kt)

Hydrothermal vents present a danger for us when the lines of smoke from them intersect.

# Part 1

Given an input of x1,y1 -> x2,y2 for the lines of smoke, find the position on a grid of integers where two or more 
smoke lines intersect. Consider only horizontal and vertical lines (no diagonals).

First things first, we need to parse our input. I used a regex to pull out numbers then convert each line definition
into a `Pair<Pair<Int, Int>, Pair<Int, Int>>`.
```kotlin
val lineRegex = "(\\d+),(\\d+) -> (\\d+),(\\d+)".toRegex()
    File(inputFile).readLines().asSequence()
        .mapNotNull { input -> lineRegex.find(input)?.destructured?.toList() }
        .map { list -> (list[0].toInt() to list[1].toInt()) to (list[2].toInt() to list[3].toInt()) }
```
And because `Pair<Pair<Int, Int>, Pair<Int, Int>>` is a mouthful, I'm going to use a trick I saw [@tginsberg](https://github.com/tginsberg) use in Day4 
and define some type aliases to make this more understandable. With this, we can refer to these same data structures
as simply as `Line` and `Point`.
```kotlin
typealias Point = Pair<Int, Int>
typealias Line = Pair<Point, Point>
```
Next, there's a lot of things we want to do with our `Point` and `Line`. Namely, we want to know if a line is vertical or horizontal,
and we want to be able to generate the points on our grid that are contained in those lines. To create the points, we
map over the range of changing values for one half of our pair, while the other remains static.
```kotlin
fun Line.isHorizontal() = this.first.first == this.second.first
fun Line.isVertical() = this.first.second == this.second.second
fun Line.getHorizontalPoints() = (this.first.second.progressionTo(this.second.second)).map { y -> this.first.first to y }
fun Line.getVerticalPoints() = (this.first.first.progressionTo(this.second.first)).map { x -> x to this.first.second }
```
Those that know Kotlin might be wondering what `progressionTo()` is. Frustratingly, Kotlin only honors ranges/progressions
that are defined in the correct order, so I made this convenience method to sort it out.
```kotlin
fun Int.progressionTo(i: Int): IntProgression =
    if(this > i) { i..this }
    else { i downTo this }
```
Next, we generalize how we find points for any line. Ignore everything about 45 degrees on this, that's a spoiler for Part 2.
```kotlin
fun Line.getAllPoints(allow45degree: Boolean): List<Point> =
    when {
        this.isHorizontal() -> getHorizontalPoints()
        this.isVertical() -> getVerticalPoints()
        allow45degree -> get45DegreePoints()
        else -> emptyList()
    }
```
Now that we're able to quickly get points along a line, getting the answer is as simple as counting unique points.
```kotlin
private fun part1(inputFile: String) {
    val lineRegex = "(\\d+),(\\d+) -> (\\d+),(\\d+)".toRegex()
    File(inputFile).readLines().asSequence()
        .mapNotNull { input -> lineRegex.find(input)?.destructured?.toList() }
        .map { list -> (list[0].toInt() to list[1].toInt()) to (list[2].toInt() to list[3].toInt()) }
        .map { line -> line.getAllPoints(false) }
        .flatten()
        .groupBy { it }
        .filter { it.value.size > 1 }
        .size
        .apply { println(this) }
}
```

# Part 2
As you probably already guessed, now we need to support lines at 45-degree angles. The hook for this was included in the
code above, we just need to build out `get45DegreePoints()`. This is accomplished by getting the x values as if it was a
vertical line, and zipping it with the y value as if it were a horizontal line.
```kotlin
fun Line.get45DegreePoints() = getVerticalPoints().map { it.first }.zip(getHorizontalPoints().map { it.second })
```
Then our solution is different only by one boolean value.
```kotlin
private fun part2(inputFile: String) {
    val lineRegex = "(\\d+),(\\d+) -> (\\d+),(\\d+)".toRegex()
    File(inputFile).readLines().asSequence()
        .mapNotNull { input -> lineRegex.find(input)?.destructured?.toList() }
        .map { list -> (list[0].toInt() to list[1].toInt()) to (list[2].toInt() to list[3].toInt()) }
        .map { line -> line.getAllPoints(true) }
        .flatten()
        .groupBy { it }
        .filter { it.value.size > 1 }
        .size
        .apply { println(this) }
}
```
