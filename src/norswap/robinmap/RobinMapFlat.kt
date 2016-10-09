@file:Suppress("NOTHING_TO_INLINE", "UNCHECKED_CAST")
package norswap.robinmap
import java.util.NoSuchElementException

class RobinMapFlat<K: Any, V: Any>(size_exp: Int = 4): Iterable<Pair<K, V>>
{
    // ---------------------------------------------------------------------------------------------

    companion object {
        val MAX_LOAD = 0.75
        private val EMPTY = -1
    }

    // ---------------------------------------------------------------------------------------------

    private var hp: IntArray
    private var kv:  Array<Any?>

    init {
        val size = 1 shl (size_exp + 1)
        hp = IntArray(size)
        hp.fill(EMPTY)
        kv = arrayOfNulls(size)
    }

    private var items = 0

    private var max_probe = 0

    // ---------------------------------------------------------------------------------------------

    private inline val capacity: Int
        get() = hp.size / 2

    // ---------------------------------------------------------------------------------------------

    private inline fun IntArray.h (i: Int) = this[2 * i]
    private inline fun IntArray.p (i: Int) = this[2 * i + 1]

    private inline fun Array<Any?>.k (i: Int) = this[2 * i] as K
    private inline fun Array<Any?>.v (i: Int) = this[2 * i + 1] as V

    // ---------------------------------------------------------------------------------------------

    private inline fun h (i: Int) = hp.h(i)
    private inline fun p (i: Int) = hp.p(i)
    private inline fun k (i: Int) = kv.k(i)
    private inline fun v (i: Int) = kv.v(i)

    // ---------------------------------------------------------------------------------------------

    private inline fun seth (i: Int, h: Int) { hp[2 * i]     = h }
    private inline fun setp (i: Int, p: Int) { hp[2 * i + 1] = p }
    private inline fun setk (i: Int, k: K)   { kv[2 * i]     = k }
    private inline fun setv (i: Int, v: V)   { kv[2 * i + 1] = v }

    // ---------------------------------------------------------------------------------------------

    private inline fun IntArray.swap (i: Int, v: Int): Int
    {
        val out = this[i]
        this[i] = v
        return out
    }

    private inline fun Array<Any?>.swap (i: Int, v: Any): Any?
    {
        val out = this[i]
        this[i] = v
        return out
    }

    // ---------------------------------------------------------------------------------------------

    private inline fun swaph (i: Int, h: Int): Int
        = hp.swap(2 * i, h)

    private inline fun swapp (i: Int, p: Int): Int
        = hp.swap(2 * i + 1, p)

    private inline fun swapk (i: Int, k: K): K
        = kv.swap(2 * i, k) as K

    private inline fun swapv (i: Int, v: V): V
        = kv.swap(2 * i + 1, v) as V

    // ---------------------------------------------------------------------------------------------

    private inline fun set (i: Int, h: Int, p: Int, k: K, v: V)
    {
        seth(i, h)
        setp(i, p)
        setk(i, k)
        setv(i, v)
    }

    // ---------------------------------------------------------------------------------------------

    private inline fun erase (i: Int)
    {
        seth(i, EMPTY)
        setp(i, EMPTY)
    }

    // ---------------------------------------------------------------------------------------------

    private inline fun copy (from: Int, to: Int)
    {
        seth(to, h(from))
        setp(to, p(from))
        setk(to, k(from))
        setv(to, v(from))
    }

    // ---------------------------------------------------------------------------------------------

    val size: Int
        get() = items

    // ---------------------------------------------------------------------------------------------

    val empty: Boolean
        get() = items == 0

    // ---------------------------------------------------------------------------------------------

    private inline fun Int.up(): Int
        = if (this + 1 == capacity) 0 else this + 1

    // ---------------------------------------------------------------------------------------------

    operator fun get (k: K): V?
    {
        val h = k.hashCode()
        var i = h and (capacity - 1)
        var p = 0 // probe

        while (p <= max_probe)
        {
            if (p(i) < p)
                return null

            if (h == h(i) && k == k(i))
                return v(i)

            i = i.up()
            ++ p
        }

        return null
    }

    // ---------------------------------------------------------------------------------------------

    private fun put (_h: Int, _k: K, _v: V): V?
    {
        var h = _h
        var k = _k
        var v = _v

        var i = h and (capacity - 1)
        var p = 0 // probe
        var old: V? = null

        while (true)
        {
            val curp = p(i)
            if (curp == EMPTY)
            {
                set(i, h, p, k, v)
                ++ items
                if (p > max_probe)
                    max_probe = p
                break
            }
            else if (h == h(i) && k == k(i))
            {
                old = swapv(i, v)
                break
            }
            else if (curp < p)
            {
                if (p > max_probe)
                    max_probe = p

                h = swaph(i, h)
                p = swapp(i, p)
                k = swapk(i, k)
                v = swapv(i, v)
            }

            i = i.up()
            ++ p
        }

        return old
    }


    // ---------------------------------------------------------------------------------------------

    private fun resize ()
    {
        val old_hp = hp
        val old_kv = kv
        val old_capacity = capacity
        val new_size = hp.size * 2

        hp = IntArray(new_size)
        hp.fill(EMPTY)
        kv = kotlin.arrayOfNulls(new_size)
        max_probe = 0
        items = 0

        var i = 0
        while (i < old_capacity)
        {
            if (old_hp.p(i) != EMPTY)
                put(old_hp.h(i), old_kv.k(i), old_kv.v(i))
            ++ i
        }
    }

    // ---------------------------------------------------------------------------------------------

    operator fun set (key: K, value: V): V?
    {
        if (capacity * MAX_LOAD < items)
            resize()

        return put(key.hashCode(), key, value)
    }

    // ---------------------------------------------------------------------------------------------

    // more complex version using arraycopy -- not really faster
    private inline fun remove_index2 (_index: Int, _limit: Int)
    {
        assert(0 <= _index && _index < capacity)
        assert(0 <= _limit && _limit < capacity)
        assert(_index != _limit)

        val index = _index * 2
        val limit = if (_limit == 0) hp.size else _limit * 2
        val limit2 = limit / 2

        if (index < limit) {
            System.arraycopy(hp, index + 2, hp, index, limit - index - 2)

            var i = _index
            while (i < limit2 - 1) {
                setp(i, p(i) - 1)
                ++i
            }

            System.arraycopy(kv, index + 2, kv, index, limit - index - 2)
            erase(limit2 - 1)
        }
        else {
            System.arraycopy(hp, index + 2, hp, index, hp.size - index - 2)

            var i = _index
            while (i < capacity - 1) {
                setp(i, p(i) - 1)
                ++i
            }

            System.arraycopy(kv, index + 2, kv, index, kv.size - index - 2)

            hp[hp.size - 2] = hp[0]
            hp[hp.size - 1] = hp[1] - 1
            System.arraycopy(hp, 2, hp, 0, limit - 2)

            var j = 0
            while (j < limit2 - 1) {
                setp(j, p(j) - 1)
                ++j
            }

            kv[kv.size - 2] = kv[0]
            kv[kv.size - 1] = kv[1]
            System.arraycopy(kv, 2, kv, 0, limit - 2)

            erase(limit2 - 1)
        }
    }

    // ---------------------------------------------------------------------------------------------

    // Unused simpler version (but slightly slower).
    private inline fun remove_index (index: Int, limit: Int)
    {
        assert(0 <= index && index < capacity)
        assert(0 <= limit && limit < capacity)
        assert(index != limit)

        var i = index
        while (true)
        {
            val j = i.up()
            if (j == limit) {
                erase(i)
                break
            }

            copy(j, i)
            setp(i, p(i) - 1)
            i = j
        }
    }

    // ---------------------------------------------------------------------------------------------

    fun remove (k: K): V?
    {
        val h = k.hashCode()
        var i = h and (capacity - 1)
        var p = 0 // probe
        var index = 0
        var out: V? = null

        while (p <= max_probe)
        {
            if (p(i) < p)
                break

            if (h == h(i) && k == k(i))
            {
                index = i
                out = v(i)
                break
            }

            i = i.up()
            ++ p
        }

        if (out == null) return out
        -- items

        while (true) {
            i = i.up()
            if (p(i) <= 0) break
        }

        remove_index(index, i)
        return out
    }

    // ---------------------------------------------------------------------------------------------

    private inline fun <U: Any> gen_iterator(crossinline f: (K, V) -> U)
        = object: Iterator<U>
    {
        var i = 0

        init { advance() }

        private fun advance() {
            while (i < capacity && p(i) == EMPTY) ++ i
        }

        override fun hasNext()
            = i < capacity

        override fun next(): U
        {
            if (i == capacity)
                throw NoSuchElementException()

            val out = f(k(i), v(i))
            ++ i
            advance()
            return out
        }
    }

    // ---------------------------------------------------------------------------------------------

    override fun iterator(): Iterator<Pair<K, V>>
        = gen_iterator { k, v -> Pair(k, v) }

    // ---------------------------------------------------------------------------------------------

    fun iter_keys(): Iterator<K>
        = gen_iterator { k, v -> k }

    // ---------------------------------------------------------------------------------------------

    fun iter_values(): Iterator<V>
        = gen_iterator { k, v -> v }

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
