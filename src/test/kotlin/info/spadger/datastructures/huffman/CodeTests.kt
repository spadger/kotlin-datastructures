package info.spadger.datastructures.huffman

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

class CodeTests : StringSpec({

    "Empty codes are not permitted" {

        val ex = shouldThrow<Exception> {
            Code(booleanArrayOf())
        }

        ex.message shouldBe "A Code must contain at least 1 bit"
    }

    "Codes with the same value should be equal" {
        Code(booleanArrayOf(false)) shouldBe Code(booleanArrayOf(false))
        Code(booleanArrayOf(true)) shouldBe Code(booleanArrayOf(true))
        Code(booleanArrayOf(false, true)) shouldBe Code(booleanArrayOf(false, true))
        Code(booleanArrayOf(true, false)) shouldBe Code(booleanArrayOf(true, false))
    }

    "Codes with the a different value should not be equal" {
        Code(booleanArrayOf(false)) shouldNotBe Code(booleanArrayOf(true))
        Code(booleanArrayOf(true)) shouldNotBe Code(booleanArrayOf(false))
        Code(booleanArrayOf(false, true)) shouldNotBe Code(booleanArrayOf(true, false))
        Code(booleanArrayOf(true)) shouldNotBe Code(booleanArrayOf(true, false))
        Code(booleanArrayOf(false)) shouldNotBe Code(booleanArrayOf(true, false))
    }

    "A code is not equal to a different type" {
        Code(booleanArrayOf(true)) shouldNotBe 1
    }

    "Two codes with the same values should share the same hash" {
        Code(booleanArrayOf(true)).hashCode() shouldBe Code(booleanArrayOf(true)).hashCode()
        Code(booleanArrayOf(true, false)).hashCode() shouldBe Code(booleanArrayOf(true, false)).hashCode()
        Code(booleanArrayOf(true, true)).hashCode() shouldBe Code(booleanArrayOf(true, true)).hashCode()
        Code(booleanArrayOf(false, false)).hashCode() shouldBe Code(booleanArrayOf(false, false)).hashCode()
        Code(booleanArrayOf(true, false, true, false, true, false, true, false, true, false, true, false, true, false)).hashCode() shouldBe Code(booleanArrayOf(true, false, true, false, true, false, true, false, true, false, true, false, true, false)).hashCode()
    }
})
