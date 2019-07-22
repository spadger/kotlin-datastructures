package info.spadger.datastructures.trie

class Node (val value : Char){

    val children = arrayOfNulls<Node>(28)

    fun add(value : String) {
        if(value.isNotEmpty()) {
            val childNode = getBucket(value[0])

            if (value.length > 1) {
                childNode.add(value.substring(1))
            }
        }
    }

    fun getBucket(value : Char) : Node {
        val index = getBucketIndex(value)

        var bucket = children[index];

        if(bucket == null) {
            bucket = Node(value)
            children[index] = bucket
        }
        return bucket
    }

    fun getBucketIndex(value: Char) : Int {
        return when(value) {
            ' ' -> 26
            '-' -> 27
            else -> value - 'a'
        }
    }

    fun check(value : String) : Boolean {
        if (value.isEmpty()) {
            return true
        }

        val charValue = value[0]
        val index = getBucketIndex(charValue)
        val child = children[index] ?: return false;

        return child.check(value.substring(1))
    }
}