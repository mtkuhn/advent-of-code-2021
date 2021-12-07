import java.io.File

fun main() {
    solve("src/main/resources/day7sample.txt", Int::distanceTo)
    solve("src/main/resources/day7.txt", Int::distanceTo)
    solve("src/main/resources/day7sample.txt", Int::crabFuelTo)
    solve("src/main/resources/day7.txt", Int::crabFuelTo)
}

fun getBoundsOnNumbers(list: List<Int>) = ((list.minOrNull()?:0)..(list.maxOrNull()?:0))
fun Int.distanceTo(destination: Int) = maxOf(this, destination)-minOf(this, destination)
fun Int.crabFuelTo(destination: Int) = this.distanceTo(destination).let { (it*(it+1))/2 }

private fun solve(inputFile: String, fuelCalc: Int.(Int)->Int) {
    File(inputFile).readLines().first()
        .split(",")
        .map { it.toInt() }
        .let { crabs ->
            getBoundsOnNumbers(crabs).map { h ->
                h to crabs.sumOf { c -> c.fuelCalc(h) }
            }
        }
        .minByOrNull { it.second }
        .apply { println(this?.second) }
}