package info.spadger.datastructures.huffman

import io.kotest.assertions.throwables.shouldThrowAny
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import java.io.ByteArrayOutputStream
import java.util.LinkedList

@kotlin.ExperimentalUnsignedTypes
class DecodingHuffmanTreeTests : StringSpec({

    "Should be buildable from a valid codebook" {

        val codes = listOf(
            EncodedValue(1.toUByte(), LinkedList(listOf(false, false))),
            EncodedValue(2.toUByte(), LinkedList(listOf(false, true))),
            EncodedValue(3.toUByte(), LinkedList(listOf(true, false))),
            EncodedValue(4.toUByte(), LinkedList(listOf(true, true, false))),
            EncodedValue(5.toUByte(), LinkedList(listOf(true, true, true))),
        )

        val sut = DecodingHuffmanTree(0, codes)
        sut.toString()
    }

    "Should not be buildable from a codebook missing leaves gaps (right)" {
        val codes = listOf(
            EncodedValue(1.toUByte(), LinkedList(listOf(false, false))),
            EncodedValue(2.toUByte(), LinkedList(listOf(false, true))),
            EncodedValue(3.toUByte(), LinkedList(listOf(true, false))),
            EncodedValue(5.toUByte(), LinkedList(listOf(true, true, false))),
        )

        val ex = shouldThrowAny {
            DecodingHuffmanTree(0, codes)
        }

        ex.message shouldBe "Right node cannot be null"
    }

    "Should not be buildable from a codebook missing leaves gaps (left)" {
        val codes = listOf(
            EncodedValue(1.toUByte(), LinkedList(listOf(false, false))),
            EncodedValue(2.toUByte(), LinkedList(listOf(false, true))),
            EncodedValue(3.toUByte(), LinkedList(listOf(true, false))),
            EncodedValue(5.toUByte(), LinkedList(listOf(true, true, true))),

            // no true, true, false
        )

        val ex = shouldThrowAny {
            DecodingHuffmanTree(0, codes)
        }

        ex.message shouldBe "Left node cannot be null"
    }

    "Should not be buildable from a codebook with duplicated leaves" {
        val codes = listOf(
            EncodedValue(1.toUByte(), LinkedList(listOf(false, false))),
            EncodedValue(2.toUByte(), LinkedList(listOf(false, true))),
            EncodedValue(3.toUByte(), LinkedList(listOf(true, false))),
            EncodedValue(4.toUByte(), LinkedList(listOf(true, true))),

            EncodedValue(5.toUByte(), LinkedList(listOf(true, true))), // error
        )

        val ex = shouldThrowAny {
            DecodingHuffmanTree(0, codes)
        }

        ex.message shouldBe "Expecting an empty node for a leaf"
    }

    "Should not be buildable from a codebook with a leaf at the same position as a branch" {
        val codes = listOf(
            EncodedValue(1.toUByte(), LinkedList(listOf(false, false))),
            EncodedValue(2.toUByte(), LinkedList(listOf(false, true))),
            EncodedValue(3.toUByte(), LinkedList(listOf(true, false))),
            EncodedValue(4.toUByte(), LinkedList(listOf(true, true))),

            EncodedValue(5.toUByte(), LinkedList(listOf(true))), // error
        )

        val ex = shouldThrowAny {
            DecodingHuffmanTree(0, codes)
        }

        ex.message shouldBe "Expecting an empty node for a leaf"
    }

    "Should not be buildable from a codebook with the same value defined twice" {
        val codes = listOf(
            EncodedValue(1.toUByte(), LinkedList(listOf(false))),
            EncodedValue(1.toUByte(), LinkedList(listOf(true))),
        )

        val ex = shouldThrowAny {
            DecodingHuffmanTree(0, codes)
        }

        ex.message shouldBe "Codebook contains multiple codes for the same decompressed value"
    }

    "Should be able to deserialise a valid data stream" {
        val codebook = listOf(
            EncodedValue(0.toUByte(), LinkedList(listOf(false, false))),
            EncodedValue(1.toUByte(), LinkedList(listOf(false, true))),
            EncodedValue(2.toUByte(), LinkedList(listOf(true, false))),
            EncodedValue(3.toUByte(), LinkedList(listOf(true, true, false))),
            EncodedValue(4.toUByte(), LinkedList(listOf(true, true, true))),
        )

        val data = listOf(
            codebook[0],
            codebook[0],
            codebook[4],
            codebook[2],
            codebook[4],
            codebook[3],
            codebook[2],
            codebook[0],
            codebook[4],
            codebook[3],
        )
        val compressedData = data.flatMap { it.pattern }.asSequence()
        val decodedByteCount = data.size

        val outputSink = ByteArrayOutputStream()

        DecodingHuffmanTree(decodedByteCount, codebook).decode(compressedData, outputSink)

        val expected = data.map { it.value }
        val actual = outputSink.toByteArray().toUByteArray()

        expected shouldBe actual
    }
})
