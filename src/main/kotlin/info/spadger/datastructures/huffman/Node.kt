package info.spadger.datastructures.huffman

import java.lang.Exception
import java.util.LinkedList

sealed class Weighted {
    abstract fun weight(): Int
    abstract fun createCodes(): List<EncodedValue>
}

class MultiValue(val left: Weighted, val right: Weighted) : Weighted() {
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

class HuffmanTree(private val bytes: ByteArray) {

    val state = createInitialHistogram()
    val codes: List<EncodedValue>

    init {

        codes = if (bytes.isEmpty()) {
            emptyList()
        } else {
            while (state.size > 1) {
                compact()
            }

            if (state.size != 1) {
                throw Exception("More than one state item found")
            }

            state[0].createCodes()
        }
    }

    fun createInitialHistogram(): MutableList<Weighted> {

        val histogram = mutableMapOf<Byte, Weighted>()
        bytes.forEach { byte ->
            val singleByte = histogram[byte] ?: SingleValue(byte).also { histogram[byte] = it }
            (singleByte as SingleValue).increment()
        }

        return histogram
            .values
            .sortedByDescending { it.weight() }
            .toMutableList()
    }

    private fun compact() {

        val right = state.removeLast()
        val left = state.removeLast()

        state.add(MultiValue(left, right))
        state.sortedByDescending { it.weight() }
    }
}