import java.io.File

fun main() {
    //Packet.fromBinary("D2FE28".hexToBinary().toMutableList()).apply { println(this) }
    //Packet.fromBinary("38006F45291200".hexToBinary().toMutableList()).apply { println(this) }
    //Packet.fromBinary("EE00D40C823060".hexToBinary().toMutableList()).apply { println(this) }
    //Packet.fromBinary("8A004A801A8002F478".hexToBinary().toMutableList()).apply { println(this) }.apply { println(this.getSumOfVersions()) }

    val bin = File("src/main/resources/day16.txt").readLines().first().hexToBinary().toMutableList()
    val packet = Packet.fromBinary(bin)
    println(packet)
    println(packet.getSumOfVersions())
    println(packet.getValue())
}

fun MutableList<Char>.takeAndRemove(n: Int): List<Char> =
    (0 until n).mapNotNull { this.removeFirst() }

data class Packet(val version: Int, val packetTypeId: Int,
                  val literalValue: Long?,
                  val lengthTypeId: Int?, val lengthByType: Int?,
                  val subPackets: List<Packet>) {
    companion object {

        fun fromBinary(binary: MutableList<Char>): Packet {
            val version = binary.takeAndRemove(3).joinToString("").toInt(2)
            val packetTypeId = binary.takeAndRemove(3).joinToString("").toInt(2)
            val literalValue = if(packetTypeId == 4) binary.nextLiteral() else null
            val lengthTypeId = binary.lengthTypeId(packetTypeId)
            val lengthByType = binary.lengthByType(lengthTypeId)
            val subPackets = if(packetTypeId != 4) binary.binaryToSubPackets(packetTypeId, lengthTypeId, lengthByType) else listOf()

            return Packet(version, packetTypeId, literalValue, lengthTypeId, lengthByType, subPackets)
        }

        fun MutableList<Char>.binaryToSubPackets(packetTypeId: Int, lengthTypeId: Int?, lengthByType: Int?): List<Packet> =
            when {
                packetTypeId != 4 && lengthTypeId == 0 && lengthByType != null -> binaryToSubPacketsByLength(lengthByType)
                packetTypeId != 4 && lengthTypeId == 1 && lengthByType != null -> binaryToSubPacketsByCount(lengthByType)
                else -> emptyList()
            }

        fun MutableList<Char>.binaryToSubPacketsByLength(len: Int): List<Packet> {
            val subList = this.takeAndRemove(len).toMutableList()
            var next = fromBinary(subList)
            val subPackets = mutableListOf<Packet>(next)
            while (next != null && subList.size > 0) {
                next = fromBinary(subList)
                subPackets += next
            }
            return subPackets
        }

        fun MutableList<Char>.binaryToSubPacketsByCount(count: Int): List<Packet> {
            return (0 until count).map { fromBinary(this) }
        }

        fun MutableList<Char>.nextLiteral(): Long? {
            var nybble = this.takeAndRemove(5)
            var litBinary = nybble.drop(1)
            while ((nybble.firstOrNull() ?: '0') == '1' && this.size >= 5) {
                nybble = this.takeAndRemove(5)
                litBinary += nybble.drop(1)
            }
            return if(litBinary.isEmpty()) null else litBinary.joinToString("").toLong(2)
        }

        fun MutableList<Char>.lengthTypeId(packetTypeId: Int) =
            when(packetTypeId) {
                4 -> null
                else -> takeAndRemove(1).joinToString("").toInt(2)
            }

        fun MutableList<Char>.lengthByType(lengthTypeId: Int?) =
            when(lengthTypeId) {
                null -> null
                0 -> this.takeAndRemove(15).joinToString("").toInt(2)
                1 -> this.takeAndRemove(11).joinToString("").toInt(2)
                else -> error("invalid length type")
            }
    }

    fun getSumOfVersions(): Long = subPackets.sumOf { it.getSumOfVersions() } + version

    fun getValue(): Long =
        when(packetTypeId) {
            0 -> subPackets.sumOf { it.getValue() }
            1 -> subPackets.fold(1) { acc, packet -> packet.getValue()*acc }
            2 -> subPackets.minOf { it.getValue() }
            3 -> subPackets.maxOf { it.getValue() }
            4 -> literalValue?:0
            5 -> if(subPackets[0].getValue() > subPackets[1].getValue()) 1 else 0
            6 -> if(subPackets[0].getValue() < subPackets[1].getValue()) 1 else 0
            7 -> if(subPackets[0].getValue() == subPackets[1].getValue()) 1 else 0
            else -> error("bad packet")
        }
}

private fun String.hexToBinary() =
    this.map { it.digitToInt(16).toString(2).padStart(4, '0') }.joinToString("")