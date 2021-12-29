import java.io.File
import kotlin.math.abs
import kotlin.math.max

fun main() {
    part1("src/main/resources/day19sample.txt")
    part1("src/main/resources/day19.txt")
    //part2("src/main/resources/day19sample.txt")
    //part2("src/main/resources/day19.txt")
}

enum class Axis(val getter: (Position3D) -> Int) {
    X({ it.x }),
    Y({ it.y }),
    Z({ it.z }),
    NEG_X({ -it.x }),
    NEG_Y({ -it.y }),
    NEG_Z({ -it.z })
}

data class Position3D(val x: Int, val y: Int, val z: Int) {
    companion object {
        fun of(s: String) = s.split(",").map { it.trim().toInt() }.let { Position3D(it[0], it[1], it[2]) }
    }
}

data class FullAlignment(val xAlignment: AxisAlignment, val yAlignment: AxisAlignment, val zAlignment: AxisAlignment)
data class AxisAlignment(val axisMapping: Pair<Axis, Axis>, val shift: Int)
data class RelativeScannerLocation(val orientationScanner: Set<Position3D>,
                                   val relativeScanner: Set<Position3D>,
                                   val alignment: FullAlignment)

private fun part1(inputFile: String) {


    val scannerSets = File(inputFile).readLines().parseScannerData()

    val uniqueMappings = scannerSets
        .asSequence()
        .map { s1 ->
            scannerSets.filter { it != s1 }.map { s2 -> setOf(s1, s2) }
        }.flatten().distinct()
        .map { it.toList() }
        .map { it[0].alignAndMerge(it[1]) }
        .toList()
}

private fun Set<Position3D>.alignAndMerge(scanner: Set<Position3D>): Set<Position3D>? {
    val alignment = this.findMatchingAlignment(scanner)
    return if(alignment == null) null
    else {
        this + scanner.map { pos ->
            val x = pos.run(alignment.xAlignment.axisMapping.second.getter) + alignment.xAlignment.shift
            val y = pos.run(alignment.yAlignment.axisMapping.second.getter) + alignment.yAlignment.shift
            val z = pos.run(alignment.zAlignment.axisMapping.second.getter) + alignment.zAlignment.shift
            Position3D(x, y, z)
        }.toSet()
    }
}

private fun Set<Position3D>.findMatchingAlignment(scanner: Set<Position3D>): FullAlignment? {
    val alignments = listOf(Axis.X, Axis.Y, Axis.Z).map { thisAxis ->
        val thisOnAxis = this.map(thisAxis.getter).toSet()
        val thisMaxAbs = thisOnAxis.maxOfOrNull { abs(it) } ?:0

        Axis.values().map { comparisonAxis ->
            val max = max(thisMaxAbs,scanner.map(comparisonAxis.getter).maxOfOrNull { abs(it) }?:0)
            (-max*2..max*2).filter { shift ->
                (scanner.map(comparisonAxis.getter).map { it+shift }.toSet() intersect thisOnAxis).size >= 12
            }.map { AxisAlignment(thisAxis to comparisonAxis, it) }
        }.flatten()
    }.flatten()

    return if(alignments.size == 3) FullAlignment(alignments[0], alignments[1], alignments[2]) else null
}

fun List<String>.parseScannerData(): List<Set<Position3D>> {
    val scannerSets = mutableListOf<Set<Position3D>>()
    var workingSet = mutableSetOf<Position3D>()
    this.drop(1).filter { it.isNotBlank() }.forEach { line ->
        if(line.contains("scanner")) {
            scannerSets += workingSet
            workingSet = mutableSetOf<Position3D>()
        } else {
            workingSet += Position3D.of(line)
        }
    }
    scannerSets += workingSet
    return scannerSets
}