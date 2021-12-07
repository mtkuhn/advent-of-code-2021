# Day 7: The Treachery of Whales

[[source]](../src/main/kotlin/Day7.kt)

We attempt to use the positioning of crab submarines to help us escape a whale.

# Part 1

Given a list of horizontal positions of crabs, determine the way of aligning them at the same
horizontal position that uses the least amount of fuel.

Fuel is defined as equal to the difference between the crab's position and the destination.

First things first, let's parse our input in to a list of ints.
```kotlin
    File(inputFile).readLines().first()
        .split(",")
        .map { it.toInt() }
```
My strategy from here is to attempt moving the full list of crabs to various locations and taking whichever uses the
least fuel. We know our answer will be within the bounds of our min and max numbers, so this can be somewhat limited. 
Let's start with some convenience methods around this.
```kotlin
fun getBoundsOnNumbers(list: List<Int>) = ((list.minOrNull()?:0)..(list.maxOrNull()?:0))
fun Int.distanceTo(destination: Int) = maxOf(this, destination)-minOf(this, destination)
```
Then we iterate over the bounds while mapping to the sum of fuel spent, and take the min. The whole solve function looks like this:
```kotlin
private fun solve(inputFile: String, fuelCalc: Int.(Int)->Int) {
    File(inputFile).readLines().first()
        .split(",")
        .map { it.toInt() }
        .let { crabs ->
            getBoundsOnNumbers(crabs).map { h ->
                crabs.sumOf { c -> c.fuelCalc(h) }
            }
        }
        .minByOrNull { it }
        .apply { println(this) }
}
```
I'm reading in the fuel calculation as a higher-order function. In this case it's the distanceTo() method we established.
```kotlin
solve("src/main/resources/day7.txt", Int::distanceTo)
```
And with that we have our answer!

# Part 2

Same as part 1, but now the fuel cost increases per step. The first step costs 1, the second step costs 2, the third step costs 3, and so on.

Turns out there's a nice math trick to this kind of sequence! I'll admit I googled `sum of a sequence` to find it, 
but the sum of this specific sequence is `f(n) = (n+(n+1))/2`. I define a new method for this.
```kotlin
fun Int.crabFuelTo(destination: Int) = this.distanceTo(destination).let { (it*(it+1))/2 }
```
Now we just swap out the higher-order function to find the solution.
```kotlin
solve("src/main/resources/day7sample.txt", Int::crabFuelTo)
```