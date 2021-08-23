package info.spadger.datastructures.huffman

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf

@kotlin.ExperimentalUnsignedTypes
class EncodingHuffmanTreeTests : StringSpec({

    "Initial state should be built correctly from input bytes" {

        val input = byteArrayOf(3, 0, 0, 5, 5, 5, 0, 0, 0, 1, 2, 0, 5, 5, 2, 3, 3).asUByteArray()

        val result = EncodingHuffmanTree.createInitialHistogram(input)

        result.size shouldBe 5

        result shouldContain SingleValue(0.toUByte())
        result shouldContain SingleValue(5.toUByte())
        result shouldContain SingleValue(3.toUByte())
        result shouldContain SingleValue(2.toUByte())
        result shouldContain SingleValue(1.toUByte())
    }

    "An empty byte-array yields an empty set of codes" {
        val input = byteArrayOf()
        val sut = EncodingHuffmanTree.fromUncompressedData(input)

        sut.codeCount shouldBe 0

        sut.shouldBeInstanceOf<EmptyEncodingHuffmanTree>()
    }

    "A byte-array with only a single specific byte yields the code 0" {
        val input = byteArrayOf(100, 100, 100, 100, 100)
        val sut = EncodingHuffmanTree.fromUncompressedData(input)

        sut.codeCount shouldBe 1
        sut.encode(100.toUByte()) shouldBe listOf(false)
    }

    "A multiple bytes yields a valid tree" {
        val input = byteArrayOf(100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 1, 2, 2)
        val sut = EncodingHuffmanTree.fromUncompressedData(input)

        sut.codeCount shouldBe 3

        sut.encode(100.toUByte()) shouldBe listOf(false)
        sut.encode(2.toUByte()) shouldBe listOf(true, false)
        sut.encode(1.toUByte()) shouldBe listOf(true, true)
    }
})
