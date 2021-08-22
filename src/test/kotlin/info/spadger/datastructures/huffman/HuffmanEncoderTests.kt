package info.spadger.datastructures.huffman

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream

@kotlin.ExperimentalUnsignedTypes
class HuffmanEncoderTests : StringSpec({
    "Temp - playground" {

        val input = byteArrayOf(3, 0, 0, 5, 5, 5, 0, 0, 0, 1, 2, 0, 5, 5, 2, 3, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 5, 5, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0)

        val sut = HuffmanEncoder(input)

//        val result = sut.serialise()
//
//        val renderedB = result.toHexString().replace(" ", System.lineSeparator())
    }

    "Preamble should be written correctly" {
        val codes = mapOf(
            81.toUByte() to listOf(true),
            123.toUByte() to listOf(false, true, true),
            12.toUByte() to listOf(false, true, false, true, false, true, false, true, true, true)
        )

        val tree = object: AHuffmanTree {
            override val codeCount: Int = 3
            override val supportedBytes = listOf(12.toUByte(), 81.toUByte(), 123.toUByte())
            override fun encode(byte: UByte): List<Boolean> = codes[byte]!!
        }

        ByteArrayOutputStream().use { baos ->
            DataOutputStream(baos).use { dos ->

                HuffmanEncoder(ByteArray(0)).writePreamble(tree, dos)

                val output = baos.toByteArray()

                output.size shouldBe 12

                output.asUByteArray() shouldBe ubyteArrayOf(
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

@kotlin.ExperimentalUnsignedTypes
fun UByteArray.toHexString(): String = joinToString(" ") { it.toString(radix = 16).padStart(2, '0') }