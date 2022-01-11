import java.io.File
import kotlin.math.abs
import kotlin.math.max

fun main() {
    //part1("src/main/resources/day19sample.txt")
    part1("src/main/resources/day19.txt")
    //part2("src/main/resources/day19sample.txt")
    //part2("src/main/resources/day19.txt")
}

enum class Axis(val getter: (Position3D) -> Int, val axisType: String, val inverse: Boolean) {
    X({ it.x }, "X", false),
    Y({ it.y }, "Y", false),
    Z({ it.z }, "Z", false),
    NEG_X({ -it.x }, "X", true),
    NEG_Y({ -it.y }, "Y", true),
    NEG_Z({ -it.z }, "Z", true)
}

data class Position3D(val x: Int, val y: Int, val z: Int) {
    operator fun plus(p: Position3D) = Position3D(this.x + p.x, this.y + p.y, this.z + p.z)
    operator fun minus(p: Position3D) = Position3D(this.x - p.x, this.y - p.y, this.z - p.z)
    fun rotateBy(o: Orientation) = Position3D(o.xMapTo.getter.invoke(this), o.yMapTo.getter.invoke(this), o.zMapTo.getter.invoke(this))

    companion object {
        fun of(s: String) = s.split(",").map { it.trim().toInt() }.let { Position3D(it[0], it[1], it[2]) }
        fun origin() = Position3D(0, 0, 0)
    }
}

data class Orientation(val xMapTo: Axis, val yMapTo: Axis, val zMapTo: Axis) {
    companion object {
        fun standard() = Orientation(Axis.X, Axis.Y, Axis.Z)

        fun all(): List<Orientation> =
            Axis.values().flatMap { xAxisMapping ->
                Axis.values().filter { it.axisType != xAxisMapping.axisType }.flatMap { yAxisMapping ->
                    Axis.values()
                        .filter { it.axisType != xAxisMapping.axisType && it.axisType != yAxisMapping.axisType }
                        .map { zAxisMapping ->
                            if (xAxisMapping.inverse && yAxisMapping.inverse && zAxisMapping.inverse) null
                            else Orientation(xAxisMapping, yAxisMapping, zAxisMapping)
                        }
                }
            }.filterNotNull()
    }
}

data class Scanner(val name: String,
                   val pos: Position3D = Position3D.origin(),
                   val orientation: Orientation = Orientation.standard(),
                   val beacons: Set<Position3D>,
                   private val cachedAlignments: MutableMap<Scanner, Scanner?> = mutableMapOf()
) {
    private fun rotateBy(rotation: Orientation): Scanner =
        Scanner(this.name, this.pos, rotation,
            beacons.map {
                Position3D(rotation.xMapTo.getter.invoke(it), rotation.yMapTo.getter.invoke(it), rotation.zMapTo.getter.invoke(it))
            }.toSet()
        )

    private fun shiftBy(shiftPos: Position3D): Scanner = Scanner(this.name, this.pos + shiftPos, this.orientation, this.beacons.map { p -> p + shiftPos }.toSet())

    private fun getAtAllOrientations(): List<Scanner> = Orientation.all().map { rotateBy(it) }

    fun alignCached(destination: Scanner): Scanner? {
        if(!cachedAlignments.contains(destination)) cachedAlignments[destination] = align(destination)
        return cachedAlignments[destination]
    }

    fun align(destination: Scanner): Scanner? =
        destination.getAtAllOrientations().mapNotNull { rotatedOrigin ->
            findShiftOnAxis(rotatedOrigin, Axis.X)?.let { rotatedOrigin.shiftBy(Position3D(it, 0, 0)) }
        }.mapNotNull { s ->
            findShiftOnAxis(s, Axis.Y)?.let { s.shiftBy(Position3D(0, it, 0)) }
        }.mapNotNull { s ->
            findShiftOnAxis(s, Axis.Z)?.let { s.shiftBy(Position3D(0, 0, it)) }
        }.apply { if(this.size > 1) error("too many alignments") }
            //.map { s -> Scanner(s.name, s.pos, s.orientation, s.beacons.map { p -> p + s.pos }.toSet()) }
            .filter { s -> (s.beacons intersect this.beacons).size == 12 }
            .firstOrNull()
            .apply { println("  aligning ${this@Scanner.name} to ${destination.name} -> ${this?.name?:"null"}") }

    private fun findShiftOnAxis(destination: Scanner, axis: Axis): Int? {
        val max = ((this.beacons.maxOfOrNull { abs(axis.getter.invoke(it)) }?:0) +
                (destination.beacons.maxOfOrNull { abs(axis.getter.invoke(it)) }?:0))+500 //todo: figure out how to make this work better
        val possibleShifts = (-max .. max).filter { shift ->
            (this.beacons.map { axis.getter.invoke(it) }.toSet()
                    intersect destination.beacons.map { axis.getter.invoke(it) + shift}.toSet())
                .size >= 12
        }
        if(possibleShifts.size > 1) error("Too many possibilities for shift")

        return possibleShifts.firstOrNull()
    }
}

private fun part1(fileName: String) {
    val scanners = File(fileName).readLines().parseScannerData().toMutableList()
    scanners.forEach { println(it) }

    val originScanner = scanners.first()
    val originPerspectiveScanners = mutableListOf(originScanner)
    scanners.removeAt(0)

    println("aligning")
    while(scanners.isNotEmpty()) {
        println("iterate aligned=${originPerspectiveScanners.map { it.name }}; raw=${scanners.map { it.name }};")
        val foundScannersAligned = mutableListOf<Scanner>()
        val foundScannersRaw = mutableListOf<Scanner>()
        scanners.toList().forEach { rawScanner ->
            originPerspectiveScanners.toList().forEach { oScanner ->
                val alignedScanner = oScanner.alignCached(rawScanner)
                if(alignedScanner != null) {
                    println(alignedScanner)
                    foundScannersAligned.add(alignedScanner)
                    foundScannersRaw.add(rawScanner)
                }
            }
        }
        if(foundScannersRaw.size == 0) {
            println("ending early")
            break
        }
        originPerspectiveScanners.addAll(foundScannersAligned)
        scanners.removeAll(foundScannersRaw)
    }

    originPerspectiveScanners.flatMap { it.beacons }.distinct().apply { println(this.size) }
}

fun List<String>.parseScannerData(): List<Scanner> {
    val scannerSets = mutableListOf<Scanner>()
    var workingSet = mutableSetOf<Position3D>()
    var workingName = this.first().split(" ")[2]
    this.drop(1).filter { it.isNotBlank() }.forEach { line ->
        if(line.contains("scanner")) {
            scannerSets += Scanner(workingName, Position3D.origin(), Orientation.standard(), workingSet)
            workingSet = mutableSetOf<Position3D>()
            workingName = line.split(" ")[2]
        } else {
            workingSet += Position3D.of(line)
        }
    }
    scannerSets += Scanner(workingName, Position3D.origin(), Orientation.standard(), workingSet)
    return scannerSets
}