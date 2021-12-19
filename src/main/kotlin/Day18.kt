import java.io.File
import kotlin.math.ceil
import kotlin.math.floor

fun main() {
    solve("src/main/resources/day18sample.txt")
    greatestSum("src/main/resources/day18sample.txt")
    solve("src/main/resources/day18.txt")
    greatestSum("src/main/resources/day18.txt") //4453 not right
}

fun solve(file: String) {
    File(file).readLines()
        .map { it.toSnailfishNumber(null)}
        .reduce { acc, next -> acc.snailFishAdd(next) }
        .magnitude()
        .apply { println(this) }
}

fun greatestSum(file: String) {
    val nums = File(file).readLines()
    nums.map { n1 ->
        nums.filter { it != n1 }.map { n2 ->
            n1.toSnailfishNumber(null).snailFishAdd(n2.toSnailfishNumber(null)).magnitude()
        }
    }.flatten().maxOrNull().apply { println(this) }
}

abstract class SnailfishNumber(var parent: SnailfishPair?) {
    fun snailFishAdd(b: SnailfishNumber): SnailfishNumber {
        val sn = SnailfishPair(this, b)
        var exploders = 1
        var splitters = 1
        while(exploders > 0 || splitters > 0) {
            exploders = 0
            splitters = 0

            sn.getPairsAtDepth(4).firstOrNull()
                ?.let {
                    it.explode()
                    exploders = 1
                }

            if(exploders == 0) {
                sn.getAllLiterals().filter { it.value >= 10 }.firstOrNull()
                    ?.apply { splitters = 1 }
                    ?.split()
            }
        }

        return sn
    }

    fun getRootPair(): SnailfishPair {
        var root = this
        while(root.parent != null) {
            root = root.parent as SnailfishPair
        }
        return root as SnailfishPair
    }

    fun firstLeftLiteral(): SnailfishLiteral? =
        this.getRootPair().getAllLiterals().let { literals ->
            literals.elementAtOrNull(literals.indexOf(this)-1)
        }

    fun firstRightLiteral(): SnailfishLiteral? =
        this.getRootPair().getAllLiterals().let { literals ->
            literals.elementAtOrNull(literals.indexOf(this)+1)
        }

    abstract fun magnitude(): Long
}

class SnailfishLiteral(var value: Int, parent: SnailfishPair? = null): SnailfishNumber(parent) {
    override fun toString() = value.toString()

    fun split() {
         val new = SnailfishPair(
            SnailfishLiteral(floor(value/2.0).toInt()),
            SnailfishLiteral(ceil(value/2.0).toInt()),
            this.parent
        )
        this.parent?.replace(this, new)
    }

    override fun magnitude() = value.toLong()
}

class SnailfishPair(var left: SnailfishNumber, var right: SnailfishNumber, parent: SnailfishPair? = null): SnailfishNumber(parent) {
    init {
        left.parent = this
        right.parent = this
    }

    override fun toString() = "{$left,$right}"
    fun both() = listOf(left, right)
    fun getPairsAtNextDepth(): List<SnailfishPair> = this.both().filterIsInstance<SnailfishPair>()

    fun getPairsAtDepth(targetDepth: Int): List<SnailfishPair> {
        var pairs = listOf(this)
        for(depth in 0 until targetDepth) {
            pairs = pairs.flatMap { it.getPairsAtNextDepth() }
        }
        return pairs
    }

    fun getAllLiterals(): List<SnailfishLiteral> =
        this.both().map { if(it is SnailfishPair) it.getAllLiterals() else listOf(it) }.flatten() as List<SnailfishLiteral>

    fun explode() {
        val leftLiteral = this.left.firstLeftLiteral()
        val rightLiteral = this.right.firstRightLiteral()
        if(leftLiteral != null) leftLiteral.value += (this.left as SnailfishLiteral).value
        if(rightLiteral != null) rightLiteral.value += (this.right as SnailfishLiteral).value
        this.parent?.replace(this, SnailfishLiteral(0, this.parent))
    }

    fun replace(oldElement: SnailfishNumber, newElement:SnailfishNumber) {
        if(oldElement == left) left = newElement
        else if(oldElement == right) right = newElement
    }

    override fun magnitude(): Long = (3*this.left.magnitude()) + (2*this.right.magnitude())

}

fun String.toSnailfishNumber(parent: SnailfishPair?): SnailfishNumber =
    if(this.matches("\\d+".toRegex())) {
        SnailfishLiteral(this.toInt(), parent)
    } else {
        this.parsePair().let { pair ->
            SnailfishPair(pair.first.toSnailfishNumber(null), pair.second.toSnailfishNumber(null))
        }
    }

fun String.parsePair(): Pair<String, String> {
    var bracketCount = 0
    val left = this.drop(1).takeWhile { c ->
        bracketCount += when(c) { '[' -> 1; ']' -> -1; else -> 0 }
        !(c == ',' && bracketCount == 0)
    }
    val right = this.drop(left.length+2).dropLast(1) //+2 for comma, space, last for end bracket
    return left to right
}