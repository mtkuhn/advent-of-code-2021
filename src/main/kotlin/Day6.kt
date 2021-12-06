import java.io.File

fun main() {
    calcFishFromFileForDays("src/main/resources/day6sample.txt", 80)
    calcFishFromFileForDays("src/main/resources/day6.txt", 80)
    calcFishFromFileForDays("src/main/resources/day6sample.txt", 256)
    calcFishFromFileForDays("src/main/resources/day6.txt", 256)
}

fun Map<Int, Long>.calculateFishAfterOneDay(): Map<Int, Long> =
    this.mapKeys { it.key-1 }//shift all keys back by one
        .toMutableMap()
        .apply {
            this[6] = (this[6]?:0)+(this[-1]?:0) //add -1 and 6 (same modular value)
            this[-1] = 0 //clear -1
            this[8] = (this[8]?:0)+(this@calculateFishAfterOneDay[0]?:0) //add original-day0 to day8
        }

fun Map<Int, Long>.getFishDaySequence() = generateSequence(this.calculateFishAfterOneDay()) { it.calculateFishAfterOneDay() }

private fun calcFishFromFileForDays(inputFile: String, days: Int) {
    File(inputFile).readLines()
        .map { l -> l.split(",").map { s -> s.toInt() } }
        .flatten()
        .groupBy { it }
        .mapValues { it.value.size.toLong() }
        .getFishDaySequence().take(days).last()
        .apply { println(this.values.sum()) }
}