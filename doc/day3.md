# Day 3: Binary Diagnostic

[[source]](../src/main/kotlin/Day3.kt)

We need to check the sub's diagnostics from a binary readout.

# Part 1
The `power consumption` is the `gamma rate` multiplied by the `epsilon rate`.

`Gamma rate` is the most common binary number from each position within the line, converted to decimal.

`Epsilon rate` is the least common binary number from each position within the line, converted to decimal.

My solution this time is a little messy. First I get the list of lines and a count of the lines, which I'll need later.
```
    val lines = File(inputFile).readLines()
    val lineCount = lines.size
```
Next I find the most common digits in each position, first by converting each line to a list of ints (which will all be 1 or 0),
then by doing a reduction in which each iteration works to sum up values per each position. You can think of this as a summation across vectors.
Finally, we can compare each sum to the number of lines. If it's over half then we know 1 is most common.
```
    val mostCommonDigits = lines.map { line -> line.toCharArray().map { char -> char.digitToInt() }.toMutableList() } //convert to a list of ints
        .reduce { acc, intArray -> //sum up the ints per position
            acc.onEachIndexed { i, _ -> acc[i] += intArray[i] }
        }
        .map { sums -> if(sums >= lineCount/2.0) 1 else 0 } //determine the most common by comparing the sum to total number of lines
        .joinToString("")
```
Then all that's left is to do the product and print.
```
    val gammaRate = mostCommonDigits.toInt(2) //binaryArrayToInt()
    val epsilonRate = mostCommonDigits.map { if(it == '0') '1' else '0' }.joinToString("").toInt(2) //binaryArrayToInt()
    println(gammaRate*epsilonRate)
```

# Part 2

Now we need to find the `life support rating`, which is the product of the `oxygen generator rating` and `CO2 scrubber rating`.

`Oxygen generator rating` is found by filtering down to only those with the most common value in the first bit position, 
then filter those by the most common bit value in the 2nd position of only those remaining. Repeat until only one is left.

`CO2 scrubber rating` is the same, only least common bit values.

I realized part 1 needed some major reworking to do this cleanly, so I separated it out into a few functions.

First, I need to be able to find the most common bit for a given position on an arbitrary list of lines. This is done
very similarly to part 1. I also added an arg for `useMostCommon` that lets me quickly swap between most common and least
common values.
```
private fun List<String>.findCommonBitForPosition(position: Int, useMostCommon: Boolean) =
    this.sumOf { line -> line.toCharArray()[position].digitToInt() }
        .let { sum -> if(sum >= this.size/2.0) 1 else 0 }
        .let { n -> if(!useMostCommon) { if(n == 0) 1 else 0 } else n }
```
Next I need to find all matches among a list of lines for a given bit position. We never want to drop below one
solution, so I short-circuit in this case rather than filter out everything. One again we hae a `useMostCommon` arg to
switch between most and least common values.
```
private fun List<String>.findMatchingByBitCriteria(position: Int, useMostCommon: Boolean): List<String> =
    if(this.size == 1) this
    else {
        val mostCommonBitValue = this.findCommonBitForPosition(position, useMostCommon)
        this.filter { it[position].digitToInt() == mostCommonBitValue }
    }
```
I also have a function to fold over all positions and return the one matching result we are ultimately looking for.
We still include the `useMostCommon` arg.
```
private fun List<String>.findRating(useMostCommon: Boolean): String =
    foldIndexed(this) { index, acc, _ -> acc.findMatchingByBitCriteria(index, useMostCommon) }.first()
```
Finally, we bring it all together. One value for most common, and one for least, then multiply.
```
private fun part2(inputFile: String) {
    val lines = File(inputFile).readLines()
    val oxygenGeneratorRating = lines.findRating(true).toInt(2)
    val co2scrubberRating = lines.findRating(false).toInt(2)
    println(oxygenGeneratorRating*co2scrubberRating)
}
```
