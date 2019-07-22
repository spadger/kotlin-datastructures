package info.spadger.datastructures.trie

import org.junit.jupiter.api.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TrieTests {

    @Test
    fun `an empty trie does not report any contents`() {
        val sut = Trie()

        assertFalse(sut.check(""))
        assertFalse(sut.check("something"))
    }

    @Test
    fun `a trie does not report containing a string it does not contain`() {
        val sut = Trie()
        sut.put("dog")

        assertFalse(sut.check("dogs"))
    }

    @Test
    fun `a trie will report containing the exact string it contains`() {
        val sut = Trie()
        sut.put("dog")

        assertTrue(sut.check("dog"))
    }

    @Test
    fun `a trie will report containing a substring of a string it contains`() {
        val sut = Trie()
        sut.put("dogs")

        assertTrue(sut.check("dog"))
    }
}