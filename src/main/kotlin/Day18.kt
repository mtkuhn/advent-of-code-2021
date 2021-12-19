import java.io.File
import kotlin.math.ceil
import kotlin.math.floor

fun main() {
    sumAll("src/main/resources/day18sample.txt")
    greatestSum("src/main/resources/day18sample.txt")
    sumAll("src/main/resources/day18.txt")
    greatestSum("src/main/resources/day18.txt")
}

fun sumAll(file: String) {
    File(file).readLines()
        .map { SnailfishNumber.ofString(it) }
        .reduce { acc, next -> acc + next }
        .magnitude()
        .apply { println(this) }
}

fun greatestSum(file: String) {
    val nums = File(file).readLines()
    nums.map { n1 ->
        nums.filter { it != n1 }.map { n2 ->
            (SnailfishNumber.ofString(n1) + SnailfishNumber.ofString(n2)).magnitude()
        }
    }.flatten().maxOrNull().apply { println(this) }
}

abstract class SnailfishNumber(var parent: SnailfishPair? = null) {

    companion object {
        fun ofString(str: String, parent: SnailfishPair? = null): SnailfishNumber =
            if(str.matches("\\d+".toRegex())) {
                SnailfishLiteral(str.toInt(), parent)
            } else {
                str.parsePair().let { pair ->
                    SnailfishPair(ofString(pair.first), ofString(pair.second))
                }
            }
    }

    operator fun plus(b: SnailfishNumber): SnailfishNumber {
        val sn = SnailfishPair(this, b)
        var wasRuleApplied = true
        while(wasRuleApplied) {
            wasRuleApplied = explodeIfNeeded(sn) || splitIfNeeded(sn)
        }
        return sn
    }

    private fun explodeIfNeeded(sn: SnailfishPair): Boolean =
        sn.getPairsAtDepth(4).firstOrNull()?.explode()?:false

    private fun splitIfNeeded(sn: SnailfishPair): Boolean =
        sn.getAllLiterals().firstOrNull { it.value >= 10 }?.split()?:false

    fun getRootPair(): SnailfishPair =
        generateSequence(this) { it.parent }
            .takeWhile { it != null }
            .last() as SnailfishPair

    abstract fun magnitude(): Long
}

class SnailfishLiteral(var value: Int, parent: SnailfishPair? = null): SnailfishNumber(parent) {
    override fun toString() = value.toString()

    fun split(): Boolean {
         val new = SnailfishPair(
            SnailfishLiteral(floor(value/2.0).toInt()),
            SnailfishLiteral(ceil(value/2.0).toInt()),
            this.parent
        )
        this.parent?.replace(this, new)
        return true
    }

    fun firstLeftLiteral(): SnailfishLiteral? =
        this.getRootPair().getAllLiterals().let { literals ->
            literals.elementAtOrNull(literals.indexOf(this)-1)
        }

    fun firstRightLiteral(): SnailfishLiteral? =
        this.getRootPair().getAllLiterals().let { literals ->
            literals.elementAtOrNull(literals.indexOf(this)+1)
        }

    override fun magnitude() = value.toLong()
}

class SnailfishPair(private var left: SnailfishNumber,
                    private var right: SnailfishNumber,
                    parent: SnailfishPair? = null): SnailfishNumber(parent) {
    init {
        left.parent = this
        right.parent = this
    }

    override fun toString() = "{$left,$right}"

    private fun both() = listOf(left, right)

    private fun getPairsAtNextDepth(): List<SnailfishPair> = this.both().filterIsInstance<SnailfishPair>()

    fun getPairsAtDepth(targetDepth: Int): List<SnailfishPair> {
        var pairs = listOf(this)
        for(depth in 0 until targetDepth) {
            pairs = pairs.flatMap { it.getPairsAtNextDepth() }
        }
        return pairs
    }

    fun getAllLiterals(): List<SnailfishLiteral> =
        this.both().map {
            when (it) {
                is SnailfishPair -> it.getAllLiterals()
                is SnailfishLiteral -> listOf(it)
                else -> error("unknown type")
            }
        }.flatten()

    fun explode(): Boolean {
        val leftLiteral = if(left is SnailfishLiteral) (left as SnailfishLiteral).firstLeftLiteral() else null
        val rightLiteral = if(right is SnailfishLiteral) (right as SnailfishLiteral).firstRightLiteral() else null
        if(leftLiteral != null) leftLiteral.value += (left as SnailfishLiteral).value
        if(rightLiteral != null) rightLiteral.value += (right as SnailfishLiteral).value
        this.parent?.replace(this, SnailfishLiteral(0, this.parent))
        return true
    }

    fun replace(oldElement: SnailfishNumber, newElement:SnailfishNumber) {
        if(oldElement == left) left = newElement
        else if(oldElement == right) right = newElement
    }

    override fun magnitude(): Long = (3*this.left.magnitude()) + (2*this.right.magnitude())

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