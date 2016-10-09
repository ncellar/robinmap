@file:Suppress("NOTHING_TO_INLINE")
package norswap.robinmap

// -------------------------------------------------------------------------------------------------

/**
 * Shorthand for [StringBuilder.append].
 */
operator inline fun StringBuilder.plusAssign(o: Any?) { append(o) }

// -------------------------------------------------------------------------------------------------

/**
 * Read/write property for the last item of the array.
 */
var <T> Array<T>.last: T
    get() = this[size - 1]
    set(t: T) { this[size - 1] = t }

// -------------------------------------------------------------------------------------------------

/**
 * Use to make type assertions to enables smart casts: `tassert(x as List<T>)`.
 */
inline fun tassert(e: Any) {}

// -------------------------------------------------------------------------------------------------