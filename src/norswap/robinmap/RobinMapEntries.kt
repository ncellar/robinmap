@file:Suppress("NOTHING_TO_INLINE")
package norswap.robinmap
import java.util.NoSuchElementException

class RobinMapEntries<K: Any, V: Any>(size_exp: Int = 4): Iterable<Pair<K, V>>
{
    // ---------------------------------------------------------------------------------------------

    companion object {
        val MAX_LOAD = 0.75
    }

    // ---------------------------------------------------------------------------------------------

    private class Entry <K: Any, V: Any> (var h: Int, var k: K, var v: V, var probe: Int)

    // ---------------------------------------------------------------------------------------------

    private var array = arrayOfNulls<Entry<K, V>>(1 shl size_exp)

    private var items = 0

    private var max_probe = 0

    // ---------------------------------------------------------------------------------------------

    val size: Int
        get() = items

    // ---------------------------------------------------------------------------------------------

    val empty: Boolean
        get() = items == 0

    // ---------------------------------------------------------------------------------------------

    private inline fun Int.up(): Int
        = if (this + 1 == array.size) 0 else this + 1

    // ---------------------------------------------------------------------------------------------

    operator fun get (k: K): V?
    {
        val h = k.hashCode()
        var i = h and array.size - 1
        var probe = 0

        while (probe <= max_probe)
        {
            val cur = array[i]

            if (cur == null || cur.probe < probe)
                return null

            if (h == cur.h && k == cur.k)
                return cur.v

            i = i.up()
            ++ probe
        }

        return null
    }

    // ---------------------------------------------------------------------------------------------

    @Suppress("NOTHING_TO_INLINE")
    private inline fun Entry<K, V>.fill (h: Int, k: K, v: V, probe: Int)
    {
        this.h = h
        this.k = k
        this.v = v
        this.probe = probe
    }

    // ---------------------------------------------------------------------------------------------

    private fun put (_h: Int, _k: K, _v: V, cache: Entry<K, V>?): V?
    {
        var h = _h
        var k = _k
        var v = _v

        var i = h and array.size - 1
        var probe = 0
        var old: V? = null

        while (true)
        {
            val cur = array[i]

            if (cur == null)
            {
                if (cache != null) {
                    array[i] = cache
                    cache.fill(h, k, v, probe)
                }
                else {
                    array[i] = Entry(h, k, v, probe)
                }

                ++ items

                if (probe > max_probe)
                    max_probe = probe

                break
            }
            else if (h == cur.h && k == cur.k)
            {
                old = cur.v
                cur.v = v
                break
            }
            else if (cur.probe < probe)
            {
                if (probe > max_probe)
                    max_probe = probe

                val h1 = cur.h
                val k1 = cur.k
                val v1 = cur.v
                val p1 = cur.probe

                cur.fill(h, k, v, probe)

                h = h1
                k = k1
                v = v1
                probe = p1
            }

            i = i.up()
            ++ probe
        }

        return old
    }

    // ---------------------------------------------------------------------------------------------

    operator fun set (key: K, value: V): V?
    {
        if (array.size * MAX_LOAD < items)
            resize()

        return put(key.hashCode(), key, value, null)
    }

    // ---------------------------------------------------------------------------------------------

    private inline fun remove_index (index: Int, limit: Int)
    {
        assert(0 <= index && index < array.size)
        assert(0 <= limit && limit < array.size)
        assert(index != limit)

        var i = index
        while (true)
        {
            val j = i.up()
            if (j == limit) {
                array[i] = null
                break
            }
            val next = array[j]!!
            -- next.probe
            array[i] = next
            i = j
        }
    }

    // ---------------------------------------------------------------------------------------------

    fun remove (k: K): V?
    {
        val h = k.hashCode()
        var i = h and array.size - 1
        var probe = 0
        var index = 0
        var out: V? = null

        while (probe <= max_probe)
        {
            val cur = array[i]

            if (cur == null || cur.probe < probe)
                break

            if (h == cur.h && k == cur.k)
            {
                index = i
                out = cur.v
                break
            }

            i = i.up()
            ++ probe
        }

        if (out == null) return out
        -- items

        while (true) {
            i = i.up()
            val cur = array[i]
            if (cur == null || cur.probe == 0) break
        }

        remove_index(index, i)
        return out
    }

    // ---------------------------------------------------------------------------------------------

    private fun resize ()
    {
        val old = array
        array = arrayOfNulls<Entry<K, V>>(old.size * 2)
        max_probe = 0
        items = 0

        for (e in old)
            if (e != null)
                put(e.h, e.k, e.v, e)
    }

    // ---------------------------------------------------------------------------------------------

    private inline fun <U: Any> gen_iterator(crossinline f: (Entry<K, V>) -> U)
        = object: Iterator<U>
    {
        var i = 0

        init { advance() }

        private fun advance() {
            while (i < array.size && array[i] == null) ++ i
        }

        override fun hasNext()
            = i < array.size

        override fun next(): U
        {
            if (i == array.size)
                throw NoSuchElementException()

            val out = f(array[i] as Entry<K, V>)
            ++ i
            advance()
            return out
        }
    }

    // ---------------------------------------------------------------------------------------------

    override fun iterator(): Iterator<Pair<K, V>>
        = gen_iterator { Pair(it.k, it.v) }

    // ---------------------------------------------------------------------------------------------

    fun iter_keys(): Iterator<K>
        = gen_iterator { it.k }

    // ---------------------------------------------------------------------------------------------

    fun iter_values(): Iterator<V>
        = gen_iterator { it.v }

    // ---------------------------------------------------------------------------------------------

    fun keys() = Iterable { iter_keys() }

    // ---------------------------------------------------------------------------------------------

    fun values() = Iterable { iter_values() }

    // ---------------------------------------------------------------------------------------------

    override fun toString(): String
    {
        val b = StringBuilder()
        b += "{"
        for ((k, v) in this) b += "$k=$v, "
        if (!empty) b.delete(b.length - 2, b.length)
        b += "}"
        return b.toString()
    }

    // ---------------------------------------------------------------------------------------------
}
