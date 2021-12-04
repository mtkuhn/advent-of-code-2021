import java.io.File

fun main() {
    part1("src/main/resources/day4sample.txt")
    part1("src/main/resources/day4.txt")
    part2("src/main/resources/day4sample.txt")
    part2("src/main/resources/day4.txt")
}

private class BingoBoard(val array: List<List<Int>>) {

    fun findMinimalWinningNumberList(allCallingNumbers: List<Int>): List<Int> =
        allCallingNumbers.indices.firstNotNullOf { n ->
            val numbers = allCallingNumbers.subList(0, n)
            if(isWin(numbers)) numbers else null
        }

    private fun isWin(calledInts: List<Int>): Boolean = hasWinningRow(calledInts) || hasWinningCol(calledInts)

    private fun hasWinningRow(calledInts: List<Int>): Boolean =
        array.any { line -> line.all { i -> calledInts.contains(i) }}

    private fun hasWinningCol(calledInts: List<Int>): Boolean =
        array.indices.map { i -> array.map { row -> row[i] } }
            .any { col -> col.all { i -> calledInts.contains(i) }}

    private fun getAllUnmarked(calledInts: List<Int>) = array.flatten().minus(calledInts.toSet())

    fun getScore(calledInts: List<Int>) = getAllUnmarked(calledInts).sum()*calledInts.last()
}

private fun getBoardsFromLines(lines: List<String>) = lines.asSequence()
    .filter { line -> line.isNotBlank() }
    .map { line -> line.trim().split(" +".toRegex()).map { it.toInt() } }
    .chunked(5)
    .map { board -> BingoBoard(board) }.toSet()

private fun part1(inputFile: String) {
    val lines = File(inputFile).readLines()
    val callNumbers = lines[0].split(",").map { it.toInt() }
    val firstWinScore = getBoardsFromLines(lines.drop(1))
        .associateWith { b -> b.findMinimalWinningNumberList(callNumbers) }
        .minByOrNull { boardToWinList -> boardToWinList.value.size }
        ?.let { firstWin -> firstWin.key.getScore(firstWin.value) }?:0
    println(firstWinScore)
}

private fun part2(inputFile: String) {
    val lines = File(inputFile).readLines()
    val callNumbers = lines[0].split(",").map { it.toInt() }
    val lastWinScore = getBoardsFromLines(lines.drop(1))
        .associateWith { b -> b.findMinimalWinningNumberList(callNumbers) }
        .maxByOrNull { boardToWinList -> boardToWinList.value.size }
        ?.let { firstWin -> firstWin.key.getScore(firstWin.value) }?:0
    println(lastWinScore)
}