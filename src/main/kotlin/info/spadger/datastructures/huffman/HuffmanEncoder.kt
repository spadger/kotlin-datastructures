package info.spadger.datastructures.huffman

import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.io.OutputStream

@kotlin.ExperimentalUnsignedTypes
class HuffmanEncoder(data: ByteArray) {

    val serialised: UByteArray

    init {
        if (data.isEmpty()) {
            serialised = UByteArray(0)
        } else {

            val tree = HuffmanTree.fromUncompressed(data)
            serialised = serialise(tree, data.asUByteArray())
        }
    }

    fun serialise(tree: AHuffmanTree, data: UByteArray): UByteArray {
        ByteArrayOutputStream().use { baos ->
            DataOutputStream(baos).use {

                writePreamble(tree, it)
                writeBody(data, tree, it)
                it.flush()
                return baos.toByteArray().asUByteArray()
            }
        }
    }

    fun writePreamble(tree: AHuffmanTree, output: DataOutputStream) {

        output.writeShort(tree.codeCount) // The number of codes the decoder will have to iterate through before payload

        for (code in tree.supportedBytes) { // sorting makes it easier to rationalise for tests

            // Write the byte value
            output.write(code.toInt()) // The method needs an int, but only takes the LSByte

            val pattern = tree.encode(code)

            // Write the number of bits represented
            // Worst-case, the longest pattern length for least frequent byte is 256
            output.write(pattern.size) // Will never be more than 256 levels deep, so we get away with 1 byte

            // We will write enough bytes to represent the pattern.
            // The other side will know how long this section will be from the size-byte
            output.write(getPatternBytes(pattern))
        }
    }

    fun getPatternBytes(pattern: List<Boolean>): ByteArray {
        val buffer = ByteArrayOutputStream()

        pattern.outputWithPadding(buffer)

        return buffer.toByteArray()
    }

    private fun writeBody(data: UByteArray, tree: AHuffmanTree, output: DataOutputStream) {

        output.writeInt(data.size) // The last byte of the bit stream may have up to 7*0 bits for padding

        val compressedBits = data.map { tree.encode(it) }.flatMap { it }

        compressedBits.outputWithPadding(output)
    }
}

@OptIn(ExperimentalStdlibApi::class)
fun List<Boolean>.outputWithPadding(output: OutputStream) {
    for (chunk in this.chunked(8)) {

        // ensure we have 8 bits, padded with 0 at the end if need be
        val quantised = if (chunk.size == 8) chunk
        else chunk + (0..7 - chunk.size).map { false }

        val folded = quantised.foldIndexed(0) { index, accumulator, current ->
            if (current) accumulator + 1.rotateLeft(7 - index) else accumulator
        }
        output.write(folded) // we only write a byte here, and all the code above will only work on 8 bits
    }
}
