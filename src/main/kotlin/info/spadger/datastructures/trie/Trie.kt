package info.spadger.datastructures.trie

class Trie {

    val rootNode = info.spadger.datastructures.trie.Node(':')

    fun put(value: String) {
        rootNode.add(value)
    }

    fun check(value: String): Boolean {

        if (value.isEmpty()) {
            return false
        }

        return rootNode.check(value)
    }
}
