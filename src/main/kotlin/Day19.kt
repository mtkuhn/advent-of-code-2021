import java.io.File
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.sin

fun main() {
    //part1("src/main/resources/day19sample.txt")
    part1("src/main/resources/day19.txt")
}

enum class Axis(val getter: (Position3D) -> Int) {
    X({ it.x }),
    Y({ it.y }),
    Z({ it.z })
}

data class Position3D(val x: Int, val y: Int, val z: Int) {
    operator fun plus(p: Position3D) = Position3D(this.x + p.x, this.y + p.y, this.z + p.z)
    operator fun minus(p: Position3D) = Position3D(this.x - p.x, this.y - p.y, this.z - p.z)
    fun rotateBy(o: Orientation) = Position3D(
        x*(quickCos(o.y)*quickCos(o.z))
                + y*(quickSin(o.x)*quickSin(o.y)*quickCos(o.z) - quickCos(o.x)*quickSin(o.z))
                + z*(quickCos(o.x)*quickSin(o.y)*quickCos(o.z) + quickSin(o.x)*quickSin(o.z)),
        x*(quickCos(o.y)*quickSin(o.z))
                + y*(quickSin(o.x)*quickSin(o.y)*quickSin(o.z) + quickCos(o.x)*quickCos(o.z))
                + z*(quickCos(o.x)*quickSin(o.y)*quickSin(o.z) - quickSin(o.x)*quickCos(o.z)),
        x*(0-quickSin(o.y))
                + y*(quickSin(o.x)*quickCos(o.y))
                + z*(quickCos(o.x)*quickCos(o.y))
    )

    private fun quickSin(degrees: Int) =
        when(degrees%360) {
            0 -> 0
            90 -> 1
            180 -> 0
            270 -> -1
            else -> error("bad rotation value")
        }

    private fun quickCos(degrees: Int) = quickSin(degrees+90)

    companion object {
        fun of(s: String) = s.split(",").map { it.trim().toInt() }.let { Position3D(it[0], it[1], it[2]) }
        fun origin() = Position3D(0, 0, 0)
    }
}

data class Orientation(val x: Int, val y: Int, val z: Int) {
    companion object {
        fun standard() = Orientation(0, 0, 0)

        fun all(): List<Orientation> = //todo: there is some duplication here
            (0..3).flatMap { xRot ->
                (0..3).flatMap { yRot ->
                    (0..3).map { zRot ->
                        Orientation(xRot*90, yRot*90, zRot*90)
                    }
                }
            }
    }
}

data class Scanner(val name: String,
                   val pos: Position3D = Position3D.origin(),
                   val orientation: Orientation = Orientation.standard(),
                   val beacons: Set<Position3D>
) {
    private fun rotateBy(rotation: Orientation): Scanner =
        Scanner(this.name, this.pos, rotation,
            beacons.map { it.rotateBy(rotation) }.toSet()
        )

    private fun shiftBy(shiftPos: Position3D): Scanner = Scanner(this.name, this.pos + shiftPos, this.orientation, this.beacons.map { p -> p + shiftPos }.toSet())

    private fun getAtAllOrientations(): List<Scanner> = Orientation.all().map { rotateBy(it) }
    fun align(destination: Scanner): Scanner? =
        destination.getAtAllOrientations().firstNotNullOfOrNull { orient ->
            this.beacons.firstNotNullOfOrNull { o ->
                orient.beacons.map { d -> orient.shiftBy(o - d) }
                    .firstOrNull { shifted -> (shifted.beacons intersect this.beacons).size >= 12 }
            }
        }
}

private fun part1(fileName: String) {
    val scanners = File(fileName).readLines().parseScannerData().toMutableList()

    val originScanner = scanners.first()
    var bigPicture = originScanner
    scanners.remove(originScanner)

    while(scanners.isNotEmpty()) {
        scanners.toList().forEach { rawScanner ->
            listOf(bigPicture).forEach() { oScanner ->
                val alignedScanner = oScanner.align(rawScanner)
                if(alignedScanner != null) {
                    scanners.remove(rawScanner)
                    bigPicture = Scanner("big", Position3D.origin(), Orientation.standard(), bigPicture.beacons + alignedScanner.beacons)
                }
            }
        }
    }

    bigPicture.apply { println(this.beacons.size) }
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