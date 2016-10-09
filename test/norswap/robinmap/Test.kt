package norswap.robinmap

import java.math.BigInteger
import java.util.HashMap
import java.util.Random

// -------------------------------------------------------------------------------------------------

fun main (args: Array<String>)
{
    while (true)
        doSomething()
}

// -------------------------------------------------------------------------------------------------

val m1 = HashMap<String, Int>()
val m2 = RobinMapFlat<String, Int>()

// -------------------------------------------------------------------------------------------------

fun check (b: Boolean)
{
    if (!b) {
        println("--------")
        println("${m1.isEmpty()} / ${m2.empty}")
        println("${m1.size} / ${m2.size}")
        println(m1)
        println(m2)
        println("--------")
        throw AssertionError()
    }
}

// -------------------------------------------------------------------------------------------------

fun verify()
{
    check(m1.size == m2.size)

    for (k in m1.keys) {
        if (m1[k] != m2[k])
        {
            println("/////////")
            println(k)
            println(m1[k])
            println(m2[k])
            println("/////////")
        }
        check(m1[k] == m2[k])
    }
}

// -------------------------------------------------------------------------------------------------

fun doSomething()
{
    if (RANDOM.nextBoolean()) // add
    {
        val n = when (RANDOM.nextInt(3)) {
            0    -> { println("adding 1")   ; 1 }
            1    -> { println("adding 10")  ; 10 }
            else -> { println("adding 100") ; 100 }
        }

        for (i in 1..n) {
            val str = random_string()
            val int = RANDOM.nextInt()
            m1[str] = int
            m2[str] = int
        }
    }
    else // remove
    {
        val n = when (RANDOM.nextInt(3)) {
            0    -> { println("remove 1")  ; 1 }
            1    -> { println("remove 9")  ; 9 }
            else -> { println("remove 90") ; 90 }
        }

        for (i in 1..n) {
            if (m1.isEmpty()) {
                check(m2.empty)
                break
            }

            val j = RANDOM.nextInt(m1.size)
            val k = m1.keys.asSequence().drop(j).iterator().next()
            m1.remove(k)
            m2.remove(k)
            check (m2[k] == null)
        }

    }

    println(m2.size)
    verify()
}

// -------------------------------------------------------------------------------------------------
