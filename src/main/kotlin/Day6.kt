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
            this.keys.filter { it < 0 }.forEach { key -> //merge negative keys into their mod7 equivalent
                this[key.mod(7)] = (this[key]?:0)+(this[key.mod(7)]?:0)
                this.remove(key)
            }
            this[8] = (this[8]?:0)+(this@calculateFishAfterOneDay[0]?:0) //add original-day0 to day8
        }

fun Map<Int, Long>.calculateFishAfterDays(days: Int): Map<Int, Long> =
    (0 until days).fold(this) { acc, _ -> acc.calculateFishAfterOneDay() }

private fun calcFishFromFileForDays(inputFile: String, days: Int) {
    File(inputFile).readLines()
        .map { l -> l.split(",").map { s -> s.toInt() } }
        .flatten()
        .groupBy { it }
        .mapValues { it.value.size.toLong() }
        .calculateFishAfterDays(days)
        .apply { println(this.values.sum()) }
}