package info.spadger.datastructures.huffman

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldExist
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream

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

    "Preamble should be written correctly" {
        val codes = listOf(
            EncodedValue(81, linkedListOf(true)),
            EncodedValue(123, linkedListOf(false, true, true)),
            EncodedValue(12, linkedListOf(false, true, false, true, false, true, false, true, true, true))
        )

        ByteArrayOutputStream().use { baos ->
            DataOutputStream(baos).use { dos ->

                HuffmanEncoder(ByteArray(0)).writePreamble(codes, dos)

                val output = baos.toByteArray()

                output.size shouldBe 12

                output.map{ it.toUByte() } shouldBe ubyteArrayOf(
                    0.toUByte(),    // First byte of total code count
                    3.toUByte(),    // Second byte of total code count

                    12.toUByte(),   // First code represents the byte value 12
                    10.toUByte(),   // It requires 10 bits for representation
                    0x55.toUByte(), // The first 8 bytes are 0101 0101
                    0xC0.toUByte(), // The next 2 bytes are 11, followed by 6 bytes of padding

                    81.toUByte(),   // Second code represents the byte value 81
                    1.toUByte(),    // It requires 1 bit for representation
                    0x80.toUByte(), // The pattern is a single bit -> 1, followed by 7 bytes of padding,

                    123.toUByte(),  // Second code represents the byte value 123
                    3.toUByte(),    // It requires 3 bits for representation
                    0x60.toUByte(), // The pattern is 011, followed by 5 bytes of padding
                )
            }
        }
    }

    "Pattern bytes should be written correctly" {
        val input = listOf(
            true, false, true, true, false, false, true, true,
            true, false, false, false, true, true, true, true,
            false, true, true, false,
        )

        val bytes = HuffmanEncoder(byteArrayOf()).getPatternBytes(input)
        bytes.size shouldBe 3
        bytes[0].toUByte() shouldBe 179.toUByte() // 10 11 00 11
        bytes[1].toUByte() shouldBe 143.toUByte() // 10 00 11 11
        bytes[2].toUByte() shouldBe 96.toUByte()  // 01 10 00 00 <-- 4 trialing zeros
    }

})