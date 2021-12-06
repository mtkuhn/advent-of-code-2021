# Day 6: Lanternfish

[[source]](../src/main/kotlin/Day6.kt)

We get distracted and start modeling the spawn rates of Lanternfish.

# Part 1

Lanternfish spawn every 7 days once at adulthood. Juveniles need 2 days
to grow, producing their own spawn 9 days after birth.

Given a CSV input of numbers representing days remaining until producing a new fish, how
many fish will there be after 80 days?

It will be harder to do this fish by fish, so I start by mapping the input into a `Map<Int, Long>` denoting the
age and count of fish at that age respectively.
```kotlin
    File(inputFile).readLines()
        .map { l -> l.split(",").map { s -> s.toInt() } }
        .flatten()
        .groupBy { it }
        .mapValues { it.value.size.toLong() }
```

Now I want a method that can take that input and act out one day on it. There's a few things to do here:
- Countdown the spawn timer for all. This means shifting the map key down by 1.
- Ensure we don't go negative on the timer. Any negative keys must get merged into their mod7 eqivalent.
- Add our original day0 count into day8.
```kotlin
fun Map<Int, Long>.calculateFishAfterOneDay(): Map<Int, Long> =
    this.mapKeys { it.key-1 }//shift all keys back by one
        .toMutableMap()
        .apply {
            this[6] = (this[6]?:0)+(this[-1]?:0) //add -1 and 6 (same modular value)
            this[-1] = 0 //clear -1
            this[8] = (this[8]?:0)+(this@calculateFishAfterOneDay[0]?:0) //add original-day0 to day8
        }
```
I'm working with multiple scopes of `this` above. The function `calculateFishAfterOneDay()` references the original `Map<Int, Long>` it was called on, 
but the `apply` function as it own `this` as well. The most recent scope is used, but I can look back at the higher-level
by calling `this@calculateFishAfterOneDay`.

Now we want to iterate over a number of days. So we create a sequence from which we can take any numbers of days.
```kotlin
fun Map<Int, Long>.getFishDaySequence() = generateSequence(this.calculateFishAfterOneDay()) { it.calculateFishAfterOneDay() }
```

Finally we pull it all together.
```kotlin
calcFishFromFileForDays("src/main/resources/day6.txt", 80)

private fun calcFishFromFileForDays(inputFile: String, days: Int) {
    File(inputFile).readLines()
        .map { l -> l.split(",").map { s -> s.toInt() } }
        .flatten()
        .groupBy { it }
        .mapValues { it.value.size.toLong() }
        .getFishDaySequence().take(days).last()
        .apply { println(this.values.sum()) }
}
```

# Part 2
Same, but now it's 256 days. I think this is meant to ensure you came up with a performant solution, and mine
worked great out of the box, with one exception. My original solution kept the fish counts in the map as a `Int`
instead of `Long`, resulting in an overflow as the numbers increased. A quick refactor took care of that issue.

```kotlin
calcFishFromFileForDays("src/main/resources/day6sample.txt", 256)
```