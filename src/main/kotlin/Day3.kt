import java.io.File

fun main() {
    part1("src/main/resources/day3sample.txt")
    part1("src/main/resources/day3.txt")
    part2("src/main/resources/day3sample.txt")
    part2("src/main/resources/day3.txt")
}

//part 1

private fun part1(inputFile: String) {
    val lines = File(inputFile).readLines()
    val lineCount = lines.size
    val mostCommonDigits = lines.map { line -> line.toCharArray().map { char -> char.digitToInt() }.toMutableList() } //convert to a list of ints
        .reduce { acc, intArray -> //sum up the ints per position
            acc.onEachIndexed { i, _ -> acc[i] += intArray[i] }
        }
        .map { sums -> if(sums >= lineCount/2.0) 1 else 0 } //determine the most common by comparing the sum to total number of lines
        .joinToString("")
    val gammaRate = mostCommonDigits.toInt(2) //binaryArrayToInt()
    val epsilonRate = mostCommonDigits.map { if(it == '0') '1' else '0' }.joinToString("").toInt(2) //binaryArrayToInt()
    println(gammaRate*epsilonRate)
}

//part 2

private fun List<String>.findCommonBitForPosition(position: Int, useMostCommon: Boolean) =
    this.sumOf { line -> line.toCharArray()[position].digitToInt() }
        .let { sum -> if(sum >= this.size/2.0) 1 else 0 }
        .let { n -> if(!useMostCommon) { if(n == 0) 1 else 0 } else n }

private fun List<String>.findMatchingByBitCriteria(position: Int, useMostCommon: Boolean): List<String> =
    if(this.size == 1) this
    else {
        val mostCommonBitValue = this.findCommonBitForPosition(position, useMostCommon)
        this.filter { it[position].digitToInt() == mostCommonBitValue }
    }

private fun List<String>.findRating(useMostCommon: Boolean): String =
    foldIndexed(this) { index, acc, _ -> acc.findMatchingByBitCriteria(index, useMostCommon) }.first()

private fun part2(inputFile: String) {
    val lines = File(inputFile).readLines()
    val oxygenGeneratorRating = lines.findRating(true).toInt(2)
    val co2scrubberRating = lines.findRating(false).toInt(2)
    println(oxygenGeneratorRating*co2scrubberRating)
}