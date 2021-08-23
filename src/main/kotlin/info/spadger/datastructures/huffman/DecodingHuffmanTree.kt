package info.spadger.datastructures.huffman

import java.util.LinkedList

class DecodingHuffmanTree {

    val root: Node

    constructor(codebook: List<EncodedValue>) {

        if (codebook.groupBy { it.value }.any { it.value.size > 1 }) {
            throw Exception("Codebook contains multiple codes for the same decompressed value")
        }

        root = createRoot(codebook)
    }

    fun createRoot(codebook: List<EncodedValue>): Node =
        if (codebook.size == 1) {

            val encodedValue = codebook.first()

            if (encodedValue.pattern.size != 1 || encodedValue.pattern[0]) {
                throw Exception("Only one code was found so expecting a single '0' bit")
            }

            SingleValueNode(encodedValue.value)
        } else {
            val root = InProgressNode()

            for (value in codebook) {
                root.ensurePath(value.pattern, value.value)
            }
            root.reify()
        }
}

sealed class Node {
    abstract fun accept(direction: Boolean)
}

class SingleValueNode(val value: UByte) : Node() {
    override fun accept(direction: Boolean) {
        TODO("Not yet implemented")
    }
}

class DualValueNode(val left: Node, val right: Node) : Node() {
    override fun accept(direction: Boolean) {
        TODO("Not yet implemented")
    }
}

class InProgressNode : Node() {
    var left: Node? = null
    var right: Node? = null

    override fun accept(direction: Boolean) = throw Exception("InProgressNode is not callable")

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
            null -> InProgressNode()
            is InProgressNode -> node
            else -> throw Exception("Need to create a node but a node of the wrong type already exists")
        }

    fun reify(): Node {

        val reifiedLeft = when (left) {
            null -> throw Exception("Left node cannot be null")
            is InProgressNode -> (left as InProgressNode).reify()
            else -> left!!
        }

        val reifiedRight = when (right) {
            null -> throw Exception("Right node cannot be null")
            is InProgressNode -> (right as InProgressNode).reify()
            else -> right!!
        }

        return DualValueNode(reifiedLeft, reifiedRight)
    }
}
