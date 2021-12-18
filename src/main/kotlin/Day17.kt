fun main() {

    val sampleTarget = (20..30) to (-10..-5)
    sampleTarget.findVectors()

    val part1 = (119..176) to (-141..-84)
    part1.findVectors()
}

data class ProbeStepPos(val stepNumberInRange: Int, val vectorAmount: Int, val maxPosition: Int)

fun Pair<IntRange, IntRange>.findVectors() {
    val xIntersections = (1..this.first.last)
        .flatMap {
            it.xIntersections(this.first)
        }//.apply { println(this) }

    val yIntersections = (this.second.first.. 500)
        .flatMap {
            it.yIntersections(this.second)
        }//.apply { println(this) }

    val maxMatching = xIntersections.map { x ->
        x to yIntersections
            .filter { y -> x.stepNumberInRange == y.stepNumberInRange }
            .maxByOrNull { it.maxPosition }
    }.filter { it.second != null }.apply { println(this.count()) }

    val max = maxMatching.maxByOrNull { it.second!!.maxPosition }
        .apply { println(this) }

    val allMatching = xIntersections.flatMap { x ->
        yIntersections.filter { y -> x.stepNumberInRange == y.stepNumberInRange }.map {
            x to it
        }
    }.filter { it.second != null }
        .map { it.first.vectorAmount to it.second.vectorAmount }
        .distinct()
        .apply { println(this.count()) }

}
// 0 9 17 24 30 35 39 42 44 45
// 0 6 11 15 18 20 21 21 21 21
fun Int.xIntersections(target: IntRange): List<ProbeStepPos> {
    var x = 0
    var i = 0
    var maxX = 0
    val intersections = mutableListOf<ProbeStepPos>()
    while ((this+500) >= i) { //times 2 to gen more steps that y might take
        //println("$this $x $i")
        x += if(this-i > 0) this-i else 0
        i++
        if(x > maxX) maxX = x
        if(x in target) intersections.add(ProbeStepPos(i, this, maxX))
    }
    return intersections
}

fun Int.yIntersections(target: IntRange): List<ProbeStepPos> {
    var y = 0
    var i = 0
    var maxY = 0
    val intersections = mutableListOf<ProbeStepPos>()
    while (y >= target.first) {
        //println("$this $y $i")
        y += this-i
        i++
        if(y > maxY) maxY = y
        if(y in target) intersections.add(ProbeStepPos(i, this, maxY))
    }
    return intersections
}

