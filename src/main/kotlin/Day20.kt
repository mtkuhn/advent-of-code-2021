import java.io.File

fun main() {
    part1("src/main/resources/day20sample.txt")
    //part1("src/main/resources/day20.txt")
}

private fun part1(fileName: String) {
    val imgEnhanceAlgo = File(fileName).readLines().takeWhile { it.isNotBlank() }.joinToString("")
    val image = File(fileName).readLines().takeLastWhile { it.isNotBlank() }
    println(imgEnhanceAlgo)
    println(image)
}