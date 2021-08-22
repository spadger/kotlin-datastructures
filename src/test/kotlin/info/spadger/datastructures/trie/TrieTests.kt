package info.spadger.datastructures.trie

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class TrieTests : StringSpec({

    "an empty trie does not report any contents" {
        val sut = Trie()

        sut.check("") shouldBe false
        sut.check("something") shouldBe false
    }

    "fun a trie does not report containing a string it does not contain" {
        val sut = Trie()
        sut.put("dog")

        sut.check("dogs") shouldBe false
    }

    "a trie will report containing the exact string it contains" {
        val sut = Trie()
        sut.put("dog")

        sut.check("dog") shouldBe true
    }

    "a trie will report containing a substring of a string it contains" {
        val sut = Trie()
        sut.put("dogs")

        sut.check("dog") shouldBe true
    }
})
