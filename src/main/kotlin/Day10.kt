import java.io.File

fun main() {
    part1("src/main/resources/day10sample.txt")
    part1("src/main/resources/day10.txt")
    part2("src/main/resources/day10sample.txt")
    part2("src/main/resources/day10.txt")
}

data class ChunkStack(val openers: MutableList<Char> = mutableListOf()) {

    companion object {
        val endPairs = mapOf('[' to ']', '(' to ')', '{' to '}', '<' to '>')
    }

    private fun Char.isOpener(): Boolean = this in endPairs.map { it.key }
    private fun Char.isCloserForTopOfStack(): Boolean = this == endPairs[openers.first()]

    fun pushOrReturnInvalidChar(c: Char): Char? =
        when {
            c.isOpener() -> apply { openers.add(0, c) }.let { null }
            c.isCloserForTopOfStack() -> apply { openers.removeFirst() }.let { null }
            else -> c
        }

    fun popCompliments(): List<Char> = openers.mapNotNull { c -> endPairs[c] }

}

fun String.getFirstCorruptChar(): Char? =
    ChunkStack().let { stack -> this.firstOrNull { c -> stack.pushOrReturnInvalidChar(c) != null } }

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

fun String.getMissingClosers(): List<Char> =
    ChunkStack().apply { this@getMissingClosers.forEach { this.pushOrReturnInvalidChar(it) } }.popCompliments()

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