package info.spadger.datastructures.trie

class Node (val value : Char){

    val children = HashMap<Char, Node>()

    fun add(value : String) {
        if(value.isNotEmpty()) {
            val childNode = getBucket(value[0])

            if (value.length > 1) {
                childNode.add(value.substring(1))
            }
        }
    }

    fun getBucket(value : Char) : Node {
        var bucket = children[value];

        if(bucket == null) {
            bucket = Node(value)
            children[value] = bucket
        }
        return bucket
    }

    fun check(value : String) : Boolean {
        if (value.isEmpty()) {
            return true
        }

        val charValue = value[0]
        val child = children[charValue] ?: return false;

        return child.check(value.substring(1))
    }
}