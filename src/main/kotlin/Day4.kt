import java.io.File

fun main() {
    part1("src/main/resources/day4sample.txt")
    part1("src/main/resources/day4.txt")
    part2("src/main/resources/day4sample.txt")
    part2("src/main/resources/day4.txt")
}

private class BingoBoard(val array: List<List<Int>>) {

    fun isWin(calledInts: List<Int>): Boolean =
        hasWinningRow(calledInts) || hasWinningCol(calledInts)

    private fun hasWinningRow(calledInts: List<Int>): Boolean =
        array.any { line -> line.all { i -> calledInts.contains(i) }}

    private fun hasWinningCol(calledInts: List<Int>): Boolean =
        array.indices.map { i -> array.map { row -> row[i] } }
            .any { col -> col.all { i -> calledInts.contains(i) }}

    fun getAllUnmarked(calledInts: List<Int>) =
        array.flatten().minus(calledInts.toSet())
}

private fun getBoardsFromLines(lines: List<String>) = lines.asSequence()
    .filter { line -> line.isNotBlank() }
    .map { line -> line.trim().split(" +".toRegex()).map { it.toInt() } }
    .chunked(5)
    .map { board -> BingoBoard(board) }.toList()

private fun findFirstWinningBoardAndCalledNumbers(boards: List<BingoBoard>, callNumbers: List<Int>) =
    callNumbers.indices.firstNotNullOf { n ->
        val numbers = callNumbers.subList(0, n)
        boards.firstOrNull { b -> b.isWin(numbers) }
            ?.let { it to numbers }
    }

private fun part1(inputFile: String) {
    val lines = File(inputFile).readLines()
    val callNumbers = lines[0].split(",").map { it.toInt() }
    val boards = getBoardsFromLines(lines.drop(1))

    val boardAndNumbers = findFirstWinningBoardAndCalledNumbers(boards, callNumbers)

    println(boardAndNumbers.first.getAllUnmarked(boardAndNumbers.second).sum()*boardAndNumbers.second.last())
}

private fun part2(inputFile: String) {
    val lines = File(inputFile).readLines()
    val callNumbers = lines[0].split(",").map { it.toInt() }
    val boards = getBoardsFromLines(lines.drop(1)).toMutableList()

    while(boards.size > 1) {
        boards.remove(findFirstWinningBoardAndCalledNumbers(boards, callNumbers).first)
    }
    val boardAndNumbers = findFirstWinningBoardAndCalledNumbers(boards, callNumbers)

    println(boardAndNumbers.first.getAllUnmarked(boardAndNumbers.second).sum()*boardAndNumbers.second.last())
}