package norswap.robinmap
import java.math.BigInteger
import java.util.ArrayList
import java.util.HashMap
import java.util.Random

// -------------------------------------------------------------------------------------------------

val RANDOM = Random()

// -------------------------------------------------------------------------------------------------

inline fun fori (start: Int, end: Int, f: (Int) -> Unit)
{
    var i = start
    while (i < end)
        f(i++)
}

// -------------------------------------------------------------------------------------------------

fun random_string()
    = BigInteger(130, RANDOM).toString(32)

// -------------------------------------------------------------------------------------------------

class RandomRemove<T: Any> (items: Iterable<T>)
{
    val items = items.toMutableList()

    fun random(): T
    {
        val i = RANDOM.nextInt(items.size)
        val out = items[i]
        items[i] = items.last()
        items.removeAt(items.lastIndex)
        return out
    }

    fun add(item: T)
    {
        items.add(item)
    }
}

// -------------------------------------------------------------------------------------------------

fun <K: Any, V: Any> RobinMapFlat<K, V>.randomk(): K
    = iter_keys().asSequence().drop(RANDOM.nextInt(size)).iterator().next()

// -------------------------------------------------------------------------------------------------

fun <K: Any, V: Any> HashMap<K, V>.randomk(): K
    = keys.asSequence().drop(RANDOM.nextInt(size)).iterator().next()

// -------------------------------------------------------------------------------------------------