package info.spadger.datastructures.huffman

import java.io.DataInputStream
import java.io.InputStream
import java.util.Arrays
import java.util.Dictionary

class HuffmanDecoder(input: InputStream) {

    val codebook: Dictionary<Code, UByte>
    val bytes: UByteArray

    init{

        DataInputStream(input).use {
            codebook = createCodebook(it)
            bytes = deserialise(input, codebook)
        }
    }

    fun createCodebook(input: DataInputStream) : Dictionary<Code, UByte> {

        val codeBook = mutableMapOf<Code, UByte>()

        val totalCodes = input.readShort()

        for(i in 0..totalCodes) {
            val value = input.readByte().toUByte()
            val codeLengthInBits = input.readByte().toInt()
            val codes = extractCode(input, codeLengthInBits)

            codebook.put(Code(codes), value)
        }

        return codebook
    }

    fun extractCode(input: DataInputStream, codeLengthInBits: Int): BooleanArray {

        val result = BooleanArray(codeLengthInBits.toInt())
        val byteCount = codeLengthInBits.bitCountToByteCount()

        var readBits = 0

        for(byte in 0..byteCount) {

            val currentByte = input.readByte().toUByte()

            for (bit in 0..7) {
                result[readBits] = currentByte.isSet(bit)
                if(++readBits == codeLengthInBits) {
                    return result
                }
            }
        }
        throw Exception("Cannot get here")
    }

    @OptIn(ExperimentalStdlibApi::class)
    fun UByte.isSet(bitNum: Int) : Boolean {
        val mask = 1.toUByte().rotateLeft(bitNum)
        return this and mask == mask
    }

    private fun deserialise(input: InputStream, codebook: Dictionary<Code, UByte>): UByteArray {
        TODO("Not yet implemented")
    }

}

@OptIn(ExperimentalStdlibApi::class)
class Code(val bits: BooleanArray) {

    val hashCode: Int

    init {

        if(bits.isEmpty()) {
           throw Exception("A Code must contain at least 1 bit")
        }

        hashCode = bits.foldIndexed(0) { i, acc, value ->
            if (value) {
                acc + 1.rotateLeft(i)
            } else acc
        }
    }

    override fun hashCode() = hashCode

    override fun equals(other: Any?) =
        when (other) {
            is Code -> bits.contentEquals(other.bits)
            else -> false
        }
}

fun Int.bitCountToByteCount(): Int = ((this - 1) / 8) + 1