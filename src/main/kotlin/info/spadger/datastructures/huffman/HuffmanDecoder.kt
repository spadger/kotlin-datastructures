package info.spadger.datastructures.huffman

import java.io.DataInputStream
import java.io.InputStream
import java.io.OutputStream
import java.util.LinkedList

@kotlin.ExperimentalUnsignedTypes
@kotlin.ExperimentalStdlibApi
class HuffmanDecoder(input: InputStream, output: OutputStream) {

    init {

        DataInputStream(input).use {
            val codebook = createCodebook(it)
            val huffmanTree = DecodingHuffmanTree(codebook)
            deserialise(input, huffmanTree)
        }
    }

    fun createCodebook(input: DataInputStream): List<EncodedValue> {

        val totalCodes = input.readShort()

        return (0..totalCodes).map {
            val value = input.readByte().toUByte()
            val codeLengthInBits = input.readByte().toInt()
            val pattern = extractPattern(input, codeLengthInBits)

            EncodedValue(value, pattern)
        }
    }

    fun extractPattern(input: DataInputStream, codeLengthInBits: Int): LinkedList<Boolean> {

        val result = LinkedList<Boolean>()
        val byteCount = codeLengthInBits.bitCountToByteCount()

        var readBits = 0

        for (byte in 0..byteCount) {

            val currentByte = input.readByte().toUByte()

            for (bit in 0..7) {
                result.add(currentByte.isSet(bit))
                if (++readBits == codeLengthInBits) {
                    return result
                }
            }
        }
        throw Exception("Cannot get here")
    }

    fun UByte.isSet(bitNum: Int): Boolean {
        val mask = 1.toUByte().rotateLeft(bitNum)
        return this and mask == mask
    }

    private fun deserialise(input: InputStream, huffmanTree: DecodingHuffmanTree): UByteArray {
        TODO("Not yet implemented")
    }
}
fun Int.bitCountToByteCount(): Int = ((this - 1) / 8) + 1
