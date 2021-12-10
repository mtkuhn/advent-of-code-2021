import java.io.File

fun main() {
    part1("src/main/resources/day10sample.txt")
    part1("src/main/resources/day10.txt")
    part2("src/main/resources/day10sample.txt")
    part2("src/main/resources/day10.txt")
}

data class ChunkStack(val openers: MutableList<Char> = mutableListOf()) {

    companion object {
        val chunkEndPairs = setOf('[' to ']', '(' to ')', '{' to '}', '<' to '>')
    }

    fun pushOrReturnInvalidChar(c: Char): Char? {
        if(c in chunkEndPairs.map { it.first }) {
            openers += c
        } else {
            val expectedPairing = chunkEndPairs.first { it.first == openers.last() }
            if(c == expectedPairing.second) {
                openers.removeLast()
            } else {
                return c
            }
        }
        return null
    }

    fun popCompliments(): List<Char> =
        openers.map { c -> chunkEndPairs.first { it.first == c }.second }.reversed()

}

fun String.getFirstCorruptChar(): Char? {
    val chunkStack = ChunkStack()
    return this.firstOrNull { chunkStack.pushOrReturnInvalidChar(it) != null }
}

fun Char.getSyntaxCheckPoints(): Int =
    when(this) {
        ')' -> 3
        ']' -> 57
        '}' -> 1197
        '>' -> 25137
        else -> error("Unknown closer")
    }

private fun part1(inputFile: String) {
    File(inputFile).readLines().asSequence()
        .map { line -> line.getFirstCorruptChar() }
        .filterNotNull()
        .map { it.getSyntaxCheckPoints() }
        .apply { println(this.sum()) }
}

//part 2

fun Char.getAutoCompletePoints(): Int =
    when(this) {
        ')' -> 1
        ']' -> 2
        '}' -> 3
        '>' -> 4
        else -> error("Unknown closer")
    }

fun String.getMissingClosers(): List<Char> {
    val chunkStack = ChunkStack()
    this.forEach { chunkStack.pushOrReturnInvalidChar(it) }
    return chunkStack.popCompliments()
}

private fun part2(inputFile: String) {
    File(inputFile).readLines().asSequence()
        .filter { line -> line.getFirstCorruptChar() == null }
        .map { it.getMissingClosers() }
        .map { completeChars ->
            completeChars.map { char -> char.getAutoCompletePoints() }
                .fold(0L) { acc, pts -> (acc*5)+pts }
        }
        .sorted().toList()
        .let { n -> n[n.size/2] }
        .apply { println(this) }
}