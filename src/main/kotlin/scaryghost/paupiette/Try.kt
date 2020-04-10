package scaryghost.paupiette

fun <T> Try(block: () -> T): Try<T> = try {
    Success(block()).flatten()
} catch (t: Throwable) {
    Failure(t)
}

sealed class Try<out T> {
    abstract fun flatten(): Try<T>
}

data class Success<out T>(val value: T) : Try<T>() {
    override fun flatten(): Try<T> {
        return when(value) {
            null -> this
            is Success<*> -> value.flatten() as Try<T>
            is Failure<*> -> value.flatten() as Try<T>
            else -> this
        }
    }
}

data class Failure<out T>(val exception: Throwable) : Try<T>() {
    override fun flatten(): Try<T> = this
}