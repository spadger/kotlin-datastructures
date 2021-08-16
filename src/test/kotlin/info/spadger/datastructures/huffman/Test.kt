package info.spadger.datastructures.huffman

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldExist
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe

class Test : StringSpec({

    "Initial state should be built correctly from input bytes" {

        val input = byteArrayOf(3, 0, 0, 5, 5, 5, 0, 0, 0, 1, 2, 0, 5, 5, 2, 3, 3)

        val sut = HuffmanEncoder(input)

        val result = sut.createInitialHistogram()

        result.size shouldBe 5

        result shouldContain SingleValue(0)
        result shouldContain SingleValue(5)
        result shouldContain SingleValue(3)
        result shouldContain SingleValue(2)
        result shouldContain SingleValue(1)
    }

    "An empty byte-array yields an empty set of codes" {
        val input = byteArrayOf()
        val sut = HuffmanEncoder(input)

        val result = sut.codes

        result.shouldBeEmpty()
    }

    "A byte-array with only a single specific byte yields the code 0" {
        val input = byteArrayOf(100, 100, 100, 100, 100)
        val sut = HuffmanEncoder(input)

        val result = sut.codes

        result shouldHaveSize 1

        with(result[0]) {
            value shouldBe 100
            pattern shouldHaveSize 1
            pattern[0] shouldBe false
        }
    }


    "A multiple bytes yields a valid tree" {
        val input = byteArrayOf(100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 1, 2, 2)
        val sut = HuffmanEncoder(input)

        val result = sut.codes

        result shouldHaveSize 3

        result.shouldExist {
            it.value == 100.toByte() &&
                    it.pattern.size == 1 &&
                    it.pattern[0] == false
        }

        result.shouldExist {
            it.value == 2.toByte() &&
            it.pattern.size == 2 &&
            it.pattern[0] == true &&
            it.pattern[1] == false
        }

        result.shouldExist {
            it.value == 1.toByte() &&
            it.pattern.size == 2 &&
            it.pattern[0] == true &&
            it.pattern[1] == true
        }
    }

    "Temp - playground" {

        val input = byteArrayOf(3, 0, 0, 5, 5, 5, 0, 0, 0, 1, 2, 0, 5, 5, 2, 3, 3)

        val sut = HuffmanEncoder(input)

//        val result = sut.state
    }

    "Pattern bytes should be written correctly" {
        val input = listOf(
            true,  false, true,  true,  false, false, true, true,
            true,  false, false, false, true,  true,  true, true,
            false, true,  true,  false,
        )

        val bytes = HuffmanEncoder(byteArrayOf()).getPatternBytes(input)
        bytes.size shouldBe 3
        bytes[0].toUByte() shouldBe 179.toUByte() // 10 11 00 11
        bytes[1].toUByte() shouldBe 143.toUByte() // 10 00 11 11
        bytes[2].toUByte() shouldBe 96.toUByte()  // 01 10 00 00 <-- 4 trialing zeros
    }

})