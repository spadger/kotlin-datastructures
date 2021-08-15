package info.spadger.datastructures.huffman

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe

class Test : StringSpec({

    "Initial state should be built correctly from input bytes" {

        val input = byteArrayOf(3, 0, 0, 5, 5, 5, 0, 0, 0, 1, 2, 0, 5, 5, 2, 3, 3)

        val sut = HuffmanTree(input)

        val result = sut.createInitialHistogram()
        result shouldBe listOf(
            SingleValue(0),
            SingleValue(5),
            SingleValue(3),
            SingleValue(2),
            SingleValue(1)
        )

        result[0].weight() shouldBe 6
        result[1].weight() shouldBe 5
        result[2].weight() shouldBe 3
        result[3].weight() shouldBe 2
        result[4].weight() shouldBe 1
    }

    "An empty byte-array yields an empty set of codes" {
        val input = byteArrayOf()
        val sut = HuffmanTree(input)

        val result = sut.codes

        result.shouldBeEmpty()
    }

    "A byte-array with only a single specific byte yields the code 0" {
        val input = byteArrayOf(100, 100, 100, 100, 100)
        val sut = HuffmanTree(input)

        val result = sut.codes

        result shouldHaveSize 1

        with(result[0]) {
            value shouldBe 100
            pattern shouldHaveSize 1
            pattern[0] shouldBe false
        }
    }


    "A multiple bytes yields a valid tree" {
        val input = byteArrayOf(100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 2, 1)
        val sut = HuffmanTree(input)

        val result = sut.codes

        result shouldHaveSize 3

        with(result[0]) {
            value shouldBe 100
            pattern shouldHaveSize 1
            pattern[0] shouldBe false
        }

        with(result[1]) {
            value shouldBe 2
            pattern shouldHaveSize 2
            pattern[0] shouldBe true
            pattern[1] shouldBe false
        }

        with(result[2]) {
            value shouldBe 1
            pattern shouldHaveSize 2
            pattern[0] shouldBe true
            pattern[1] shouldBe true
        }
    }

    "Temp - playground" {

        val input = byteArrayOf(3, 0, 0, 5, 5, 5, 0, 0, 0, 1, 2, 0, 5, 5, 2, 3, 3)

        val sut = HuffmanTree(input)

        val result = sut.state

    }
})