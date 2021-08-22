package info.spadger.datastructures.huffman

import java.util.LinkedList
import java.util.PriorityQueue

interface AHuffmanTree{
    val codeCount: Int
    val supportedBytes: List<UByte>
    fun encode(byte: UByte): List<Boolean>
}

class EmptyHuffmanTree : AHuffmanTree {
    override val codeCount = 0
    override val supportedBytes = emptyList<UByte>()
    override fun encode(byte: UByte) = throw Exception("The tree had no input values, so nothing can be encoded")
}

class HuffmanTree(root: Weighted): AHuffmanTree {

    val codes: Map<UByte, List<Boolean>>
    override val codeCount: Int
        get() = codes.size

    override val supportedBytes: List<UByte>

    init {
        codes = root.createCodes().associateBy({it.value }, { it.pattern.toList() })
        supportedBytes = codes.keys.sorted()
    }

    override fun encode(byte: UByte): List<Boolean> = codes[byte]!!

    companion object {
        fun fromUncompressed(data: ByteArray): AHuffmanTree {

            if (!data.any()) {
                return EmptyHuffmanTree()
            }

            val hist = createInitialHistogram(data.asUByteArray())
            val root = compact(hist)

            return HuffmanTree(root)
        }

        fun createInitialHistogram(data: UByteArray): Collection<Weighted> {

            val histogram = mutableMapOf<UByte, Weighted>()
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
    }
}

fun <T> linkedListOf(vararg values: T): LinkedList<T> = LinkedList(values.asList())

class EncodedValue(val value: UByte, val pattern: LinkedList<Boolean>)