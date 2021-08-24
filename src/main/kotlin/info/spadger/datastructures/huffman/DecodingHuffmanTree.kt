package info.spadger.datastructures.huffman

import java.io.OutputStream
import java.util.LinkedList

class DecodingHuffmanTree(private val decompressedDataLength: Int, codebook: List<EncodedValue>) {

    val root: Node
    var emittedBytes = 0
    var currentNode: Node

    init {

        if (codebook.groupBy { it.value }.any { it.value.size > 1 }) {
            throw Exception("Codebook contains multiple codes for the same decompressed value")
        }

        root = createRoot(codebook)
        currentNode = root
    }

    fun createRoot(codebook: List<EncodedValue>): Node =
        if (codebook.size == 1) {

            val encodedValue = codebook.first()

            if (encodedValue.pattern.size != 1 || encodedValue.pattern[0]) {
                throw Exception("Only one code was found so expecting a single '0' bit")
            }

            SingleValueNode(encodedValue.value)
        } else {
            val root = DualValueNode()

            for (value in codebook) {
                root.ensurePath(value.pattern, value.value)
            }
            root.assertFullyFormed()
            root
        }

    fun decode(compressedInput: Sequence<Boolean>, uncompressedOutput: OutputStream) {

        emittedBytes = 0
        compressedInput.forEach {

            currentNode.accept(it) { decompressedByte -> emit(decompressedByte, uncompressedOutput) }
                ?.let { currentNode = it }

            if (emittedBytes == decompressedDataLength) {
                return
            }
        }
    }

    fun emit(byte: UByte, uncompressedOutput: OutputStream) {
        uncompressedOutput.write(byte.toInt())
        emittedBytes++
        currentNode = root
    }
}

sealed class Node {
    abstract fun accept(direction: Boolean, emit: (UByte) -> Unit): Node?
    abstract fun assertFullyFormed()
}

class SingleValueNode(val value: UByte) : Node() {
    override fun accept(direction: Boolean, emit: (UByte) -> Unit): Node? {
        throw Exception("Single-level nodes cannot accept visits")
    }

    override fun assertFullyFormed() {}
}

class DualValueNode : Node() {
    var left: Node? = null
    var right: Node? = null

    override fun accept(direction: Boolean, emit: (UByte) -> Unit): Node? {
        val child = if (direction) right else left

        return when (child) {
            null -> throw Exception("Shouldn't be possible")
            is SingleValueNode -> {
                emit(child.value); null
            }
            is DualValueNode -> child
        }
    }

    fun ensurePath(directions: LinkedList<Boolean>, finalValue: UByte) {
        val directionCount = directions.size
        val currentDirection = directions.pop()

        when {
            directionCount == 1 && currentDirection == false -> {
                ensureEmpty(left)
                left = SingleValueNode(finalValue)
            }
            directionCount == 1 && currentDirection == true -> {
                ensureEmpty(right)
                right = SingleValueNode(finalValue)
            }

            directionCount > 1 && currentDirection == false -> {
                val child = ensureEmptyOrInProgress(left)
                left = child
                child.ensurePath(directions, finalValue)
            }

            directionCount > 1 && currentDirection == true -> {
                val child = ensureEmptyOrInProgress(right)
                right = child
                child.ensurePath(directions, finalValue)
            }
        }
    }

    fun ensureEmpty(node: Node?) {
        if (node != null) {
            throw Exception("Expecting an empty node for a leaf")
        }
    }

    fun ensureEmptyOrInProgress(node: Node?) =
        when (node) {
            null -> DualValueNode()
            is DualValueNode -> node
            else -> throw Exception("Need to create a node but a node of the wrong type already exists")
        }

    override fun assertFullyFormed() {
        left?.assertFullyFormed() ?: throw Exception("Left node cannot be null")
        right?.assertFullyFormed() ?: throw Exception("Right node cannot be null")
    }
}
