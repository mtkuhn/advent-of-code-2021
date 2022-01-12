import java.io.File
import java.net.URI

internal object Resources {
    fun resourceAsString(fileName: String, delimiter: String = ""): String =
        resourceAsListOfString(fileName).reduce { a, b -> "$a$delimiter$b" }

    fun resourceAsText(fileName: String): String =
        File(fileName.toURI()).readText()

    fun resourceAsListOfString(fileName: String): List<String> =
        File(fileName.toURI()).readLines()

    fun resourceAsListOfInt(fileName: String): List<Int> =
        resourceAsListOfString(fileName).map { it.toInt() }

    fun resourceAsListOfLong(fileName: String): List<Long> =
        resourceAsListOfString(fileName).map { it.toLong() }

    private fun String.toURI(): URI =
        Resources.javaClass.classLoader.getResource(this)?.toURI() ?: throw IllegalArgumentException("Cannot find Resource: $this")
}

fun main() {
    Day192(Resources.resourceAsString("day19.txt", "\n")).solvePart1().apply { println(this) }
}

class Day192(input: String) {

    private val scanners: List<Set<Point3d>> = parseInput(input)

    fun <T> Collection<T>.pairs(): List<Pair<T, T>> =
        this.flatMapIndexed { index, a ->
            this.drop(index).map { b -> a to b }
        }

    fun solvePart1(): Int =
        solve().beacons.size

    fun solvePart2(): Int =
        solve().scanners.pairs().maxOf { it.first distanceTo it.second }

    private fun solve(): Solution {
        val baseSector = scanners.first().toMutableSet()
        val foundScanners = mutableSetOf(Point3d(0,0,0))
        val unmappedSectors = ArrayDeque<Set<Point3d>>().apply { addAll(scanners.drop(1)) }
        while(unmappedSectors.isNotEmpty()) {
            val thisSector = unmappedSectors.removeFirst()
            when (val transform = findTransformIfIntersects(baseSector, thisSector)) {
                null -> unmappedSectors.add(thisSector)
                else -> {
                    baseSector.addAll(transform.beacons)
                    foundScanners.add(transform.scanner)
                }
            }
        }
        return Solution(foundScanners, baseSector)
    }

    private fun findTransformIfIntersects(left: Set<Point3d>, right: Set<Point3d>): Transform? =
        (0 until 6).firstNotNullOfOrNull { face ->
            (0 until 4).firstNotNullOfOrNull { rotation ->
                val rightReoriented = right.map { it.face(face).rotate(rotation) }.toSet()
                left.firstNotNullOfOrNull { s1 ->
                    rightReoriented.firstNotNullOfOrNull { s2 ->
                        val difference = s1 - s2
                        val moved = rightReoriented.map { it + difference }.toSet()
                        if (moved.intersect(left).size >= 12) {
                            Transform(difference, moved)
                        } else null
                    }
                }
            }
        }

    private class Transform(val scanner: Point3d, val beacons: Set<Point3d>)
    private class Solution(val scanners: Set<Point3d>, val beacons: Set<Point3d>)

    private fun parseInput(input: String): List<Set<Point3d>> =
        input.split("\n\n").map { singleScanner ->
            singleScanner
                .lines()
                .drop(1)
                .map { Point3d.of(it) }
                .toSet()
        }
}

