import java.io.File

fun main() {
    solve("src/main/resources/day20sample.txt") //3351
    solve("src/main/resources/day20.txt")
}

private data class Pixel(val x: Int, val y: Int) {
    fun adjacentPoints(): List<Pixel> =
        listOf(Pixel(x-1, y-1), Pixel(x, y-1), Pixel(x+1, y-1),
            Pixel(x-1, y), this, Pixel(x+1, y),
            Pixel(x-1, y+1), Pixel(x, y+1), Pixel(x+1, y+1))
}

private data class Image(val lightPixels: Set<Pixel>, val infiniteChar: Char = '.') {
    val bounds = (lightPixels.minOf { it.x } .. lightPixels.maxOf { it.x })to (lightPixels.minOf { it.y } .. lightPixels.maxOf { it.y })

    fun draw() {
        bounds.second.forEach { y ->
            bounds.first.forEach { x ->
                if(lightPixels.contains(Pixel(x, y))) print("#")
                else print(".")
            }
            println()
        }
    }

    fun enhance(algorithm: String): Image {
        val newLightPixels = mutableSetOf<Pixel>()

        (bounds.second.first-1..bounds.second.last+1).forEach { y ->
            (bounds.first.first-1..bounds.first.last+1).forEach { x ->
                val pixelToEval = Pixel(x, y)
                pixelToEval.adjacentPoints()
                    .map { gridPixel ->
                        when {
                            !isInBounds(gridPixel) -> infiniteChar
                            lightPixels.contains(gridPixel) -> '#'
                            else -> '.'
                        }
                    }.map { if (it == '#') 1 else 0 }
                    .joinToString("").toInt(2)
                    .apply { if (algorithm[this] == '#') newLightPixels.add(pixelToEval) }
            }
        }

        val newImageInfiniteValue = when {
            infiniteChar == '.' && algorithm.first()=='#' -> '#'
            infiniteChar == '#' && algorithm.last()=='.' -> '.'
            else -> infiniteChar
        }
        return Image(newLightPixels, newImageInfiniteValue)
    }

    private fun isInBounds(pixel: Pixel) = bounds.first.contains(pixel.x) && bounds.second.contains(pixel.y)
}

private fun solve(fileName: String) {
    val imageEnhanceAlgorithm = File(fileName).readLines()
        .takeWhile { it.isNotBlank() }.joinToString("")
    val lightPixels = File(fileName).readLines()
        .takeLastWhile { it.isNotBlank() }
        .flatMapIndexed { i, y ->
            y.mapIndexedNotNull { j, x ->
                if(x == '#') Pixel(j, i) else null
            }
        }.toSet().let { Image(it) }
    lightPixels.enhance(imageEnhanceAlgorithm).enhance(imageEnhanceAlgorithm).apply { println(this.lightPixels.size) }
    generateSequence(lightPixels) { acc -> acc.enhance(imageEnhanceAlgorithm) }.take(51).last().apply { println(this.lightPixels.size) }
}

