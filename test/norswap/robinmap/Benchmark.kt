package norswap.robinmap
import java.time.Duration
import java.util.ArrayList
import java.util.HashMap
import java.util.HashSet
import kotlin.system.measureNanoTime

// -------------------------------------------------------------------------------------------------

fun main (args: Array<String>)
{
    var results = emptyList<Result>()
    fori(0, 3) {
        results = run_test(true, true)
        println(".")
    }

    results.forEach {
        println("-----\n$it")
    }
}

// -------------------------------------------------------------------------------------------------

val N = 131072 // 2^17
val E = 17

val INSERTS = (0.75 * N - 10).toInt()

fun duration(nanos: Long)
    = Duration.ofNanos(nanos).toNanos()

// -------------------------------------------------------------------------------------------------

data class Result (val name: String, val avg: Long, val max: Long, val sum: Long)

// -------------------------------------------------------------------------------------------------

fun run_test(hash: Boolean, robin: Boolean): List<Result>
{
    var out = emptyList<Result>()

    if (hash) {
        out += listOf(
            //loading_hash(16),
            loading_hash(N),
            batch_hash(0.1),
            batch_hash(0.7),
            ripple_hash(0.1))
    }

    if (robin) {
        out+= listOf(
            //loading_robin(4),
            loading_robin(E),
            batch_robin(0.1),
            batch_robin(0.7),
            ripple_robin(0.1))
    }

    return out
}

// -------------------------------------------------------------------------------------------------

fun loading_hash(init: Int): Result
{
    val map = HashMap<Int, Int>(init)
    val times = ArrayList<Long>()


    fori (0, INSERTS) {
        val k = RANDOM.nextInt()
        val v = RANDOM.nextInt()
        times.add(measureNanoTime {
            map.put(k, v)
        })
    }

    val avg = duration(times.average().toLong())
    val max = duration(times.max()!!)
    val sum = duration(times.sum())

    return Result("loading_hash", avg, max, sum)
}

// -------------------------------------------------------------------------------------------------

fun loading_robin(init: Int): Result
{
    val map = RobinMapFlat<Int, Int>(init)
    val times = ArrayList<Long>()


    fori (0, INSERTS) {
        val k = RANDOM.nextInt()
        val v = RANDOM.nextInt()
        times.add(measureNanoTime {
            map[k] = v
        })
    }

    val avg = duration(times.average().toLong())
    val max = duration(times.max()!!)
    val sum = duration(times.sum())

    return Result("loading_robin", avg, max, sum)
}

// -------------------------------------------------------------------------------------------------

fun batch_hash(lfr: Double): Result
{
    val map = HashMap<Int, Int>()
    val times = ArrayList<Long>()

    while (map.size < INSERTS)
    {
        val k = RANDOM.nextInt()
        val v = RANDOM.nextInt()
        times.add(measureNanoTime {
            map.put(k, v)
        })
    }

    val keys = RandomRemove(map.keys)

    fori (0, 50)
    {
        fori (0, (lfr * N).toInt())
        {
            val k = keys.random()
            times.add(measureNanoTime {
                map.remove(k)
            })
        }
        fori (0, (lfr * N).toInt())
        {
            val k2 = RANDOM.nextInt()
            val v2 = RANDOM.nextInt()
            var v: Int? = null
            times.add(measureNanoTime {
                v = map.put(k2, v2)
            })
            if (v == null) keys.add(k2)
        }
    }

    val avg = duration(times.average().toLong())
    val max = duration(times.max()!!)
    val sum = duration(times.sum())

    return Result("batch_hash", avg, max, sum)
}

// -------------------------------------------------------------------------------------------------

fun batch_robin(lfr: Double): Result
{
    val map = RobinMapFlat<Int, Int>()
    val times = ArrayList<Long>()

    while (map.size < INSERTS)
    {
        val k = RANDOM.nextInt()
        val v = RANDOM.nextInt()
        times.add(measureNanoTime {
            map[k] = v
        })
    }

    val keys = RandomRemove(map.keys())

    fori (0, 50)
    {
        fori (0, (lfr * N).toInt())
        {
            val k = keys.random()
            times.add(measureNanoTime {
                map.remove(k)
            })
        }
        fori (0, (lfr * N).toInt())
        {
            val k2 = RANDOM.nextInt()
            val v2 = RANDOM.nextInt()
            var v: Int? = null
            times.add(measureNanoTime {
                v = map.set(k2, v2)
            })
            if (v == null) keys.add(k2)
        }
    }

    val avg = duration(times.average().toLong())
    val max = duration(times.max()!!)
    val sum = duration(times.sum())

    return Result("batch_robin", avg, max, sum)
}

// -------------------------------------------------------------------------------------------------

fun ripple_hash (lfr: Double): Result
{
    val map = HashMap<Int, Int>()
    val times = ArrayList<Long>()

    while (map.size < INSERTS)
    {
        val k = RANDOM.nextInt()
        val v = RANDOM.nextInt()
        times.add(measureNanoTime {
            map.put(k, v)
        })
    }

    val keys = RandomRemove(map.keys)

    fori (0, 50)
    {
        fori (0, (lfr * N).toInt())
        {
            val k = keys.random()
            times.add(measureNanoTime {
                map.remove(k)
            })

            val k2 = RANDOM.nextInt()
            val v2 = RANDOM.nextInt()
            var v: Int? = null
            times.add(measureNanoTime {
                v = map.put(k2, v2)
            })
            if (v == null) keys.add(k2)
        }
    }

    val avg = duration(times.average().toLong())
    val max = duration(times.max()!!)
    val sum = duration(times.sum())

    return Result("ripple_hash", avg, max, sum)
}

// -------------------------------------------------------------------------------------------------

fun ripple_robin(lfr: Double): Result
{
    val map = RobinMapFlat<Int, Int>()
    val times = ArrayList<Long>()

    while (map.size < INSERTS)
    {
        val k = RANDOM.nextInt()
        val v = RANDOM.nextInt()
        times.add(measureNanoTime {
            map[k] = v
        })
    }

    val keys = RandomRemove(map.keys())

    fori (0, 50)
    {
        fori (0, (lfr * N).toInt())
        {
            val k = keys.random()
            times.add(measureNanoTime {
                map.remove(k)
            })

            val k2 = RANDOM.nextInt()
            val v2 = RANDOM.nextInt()
            var v: Int? = null
            times.add(measureNanoTime {
                v = (map.set(k2, v2))
            })
            if (v == null) keys.add(k2)
        }
    }

    val avg = duration(times.average().toLong())
    val max = duration(times.max()!!)
    val sum = duration(times.sum())

    return Result("ripple_robin", avg, max, sum)
}

// -------------------------------------------------------------------------------------------------
