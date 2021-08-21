package info.spadger.datastructures.huffman

import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.io.OutputStream
import java.util.LinkedList
import java.util.PriorityQueue

sealed class Weighted : Comparable<Weighted> {
    abstract fun weight(): Int
    abstract fun createCodes(): List<EncodedValue>

    override fun compareTo(other: Weighted): Int {
        return weight().compareTo(other.weight())
    }
}

class MultiValue(private val left: Weighted, private val right: Weighted) : Weighted() {
    override fun weight() = left.weight() + right.weight()

    override fun createCodes(): List<EncodedValue> {

        val result = mutableListOf<EncodedValue>()

        when (left) {
            is SingleValue -> result.add(EncodedValue(left.value, LinkedList<Boolean>().also { it.push(false) }))
            is MultiValue -> result.addAll(
                left.createCodes().map { EncodedValue(it.value, it.pattern.also { it.push(false) }) })
        }

        when (right) {
            is SingleValue -> result.add(EncodedValue(right.value, LinkedList<Boolean>().also { it.push(true) }))
            is MultiValue -> result.addAll(
                right.createCodes().map { EncodedValue(it.value, it.pattern.also { it.push(true) }) })
        }

        return result
    }
}

data class SingleValue(val value: Byte) : Weighted() {

    var count: Int = 0

    fun increment() {
        count++
    }

    override fun weight() = count

    // This is a special case there is only one byte in the sequence
    // so each byte can be represented by a single '0' bit
    override fun createCodes() = listOf(EncodedValue(value, LinkedList<Boolean>().also { it.push(false) }))
}

class EncodedValue(val value: Byte, val pattern: LinkedList<Boolean>)

class HuffmanEncoder(private val data: ByteArray) {

    val codes: List<EncodedValue>
    val serialised: UByteArray

    init {
        if (data.isEmpty()) {
            codes = emptyList()
            serialised = UByteArray(0)
        } else {

            val hist = createInitialHistogram()
            val root = compact(hist)

            codes = root.createCodes()
            serialised = serialise()
        }
    }

    fun createInitialHistogram(): Collection<Weighted> {

        val histogram = mutableMapOf<Byte, Weighted>()
        data.forEach { byte ->
            val singleByte = histogram[byte] ?: SingleValue(byte).also { histogram[byte] = it }
            (singleByte as SingleValue).increment()
        }

        return histogram.values
    }

    private fun compact(hist: Collection<Weighted>): Weighted {

        val state = PriorityQueue<Weighted>()
        state.addAll(hist)

        while (state.size > 1) {
            val right = state.remove()
            val left = state.remove()

            state.add(MultiValue(left, right))
        }

        return state.peek()
    }

    fun serialise(): UByteArray {
        ByteArrayOutputStream().use { baos ->
            DataOutputStream(baos).use {

                writePreamble(codes, it)
                writeBody(data, codes, it)
                it.flush()
                return baos.toByteArray().asUByteArray()
            }
        }
    }

    fun writePreamble(codes: List<EncodedValue>, output: DataOutputStream) {

        output.writeShort(codes.size) // The number of codes the decoder will have to iterate through before payload

        for (code in codes.sortedBy { it.value }) { // sorting makes it easier to rationalise for tests

            //Write the byte value
            output.write(code.value.toInt()) // The method needs an int, but only takes the LSByte

            // Write the number of bits represented
            // Worst-case, the longest pattern length for least frequent byte is 256
            output.write(code.pattern.size) // Will never be more than 256 levels deep, so we get away with 1 byte

            // We will write enough bytes to represent the pattern.
            // The other side will know how long this section will be from the size-byte
            output.write(getPatternBytes(code.pattern))
        }
    }

    @OptIn(ExperimentalStdlibApi::class)
    fun getPatternBytes(pattern: List<Boolean>): ByteArray {
        val buffer = ByteArrayOutputStream()

        pattern.outputWithPadding(buffer)

        return buffer.toByteArray()
    }

    private fun writeBody(data: ByteArray, codes: List<EncodedValue>, output: DataOutputStream) {

        output.writeInt(data.size) // The last byte of the bit stream may have up to 7*0 bits for padding

        val codebook = codes.associateBy({it.value}, {it.pattern.toList()})

        val compressedBits = data.map { codebook[it]!! }.flatten()

        compressedBits.outputWithPadding(output)
    }
}

fun <T> linkedListOf(vararg values: T): LinkedList<T> = LinkedList(values.asList())

@OptIn(ExperimentalStdlibApi::class)
fun List<Boolean>.outputWithPadding(output: OutputStream){
    for (chunk in this.chunked(8)) {

        // ensure we have 8 bits, padded with 0 at the end if need be
        val quantised = if (chunk.size == 8) chunk
        else chunk + (0..7 - chunk.size).map { false }

        val folded = quantised.foldIndexed(0) { index, accumulator, current ->
            if(current) accumulator + 1.rotateLeft(7-index) else accumulator
        }
        output.write(folded) // we only write a byte here, and all the code above will only work on 8 bits
    }
}