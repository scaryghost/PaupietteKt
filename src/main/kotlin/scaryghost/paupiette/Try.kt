package scaryghost.paupiette

fun <T> Try(block: () -> T): Try<T> = try {
    Success(block()).flatten()
} catch (t: Throwable) {
    Failure(t)
}

sealed class Try<out T> {
    abstract val isFailure: Boolean
    abstract val isSuccess: Boolean

    abstract fun filter(p: (T) -> Boolean): Try<T>
    abstract fun flatten(): Try<T>
    abstract fun <U> map(f: (T) -> U): Try<U>
    abstract fun <U> flatMap(f: (T) -> Try<U>): Try<U>

    inline fun <reified U> recover(f: (Throwable) -> U): Try<U> {
        return when (this) {
            is Success<*> -> Success(this.value as U)
            is Failure<*> -> Success(f(this.exception))
        }
    }
    inline fun <reified U> recoverWith(f: (Throwable) -> Try<U>): Try<U> {
        return when (this) {
            is Success<*> -> Try { this.value as U }
            is Failure<*> -> f(this.exception)
        }
    }
}

data class Success<out T>(val value: T) : Try<T>() {
    override val isFailure: Boolean
        get() = false
    override val isSuccess: Boolean
        get() = true

    override fun <U> map(f: (T) -> U): Try<U> = Success(f(value))

    override fun <U> flatMap(f: (T) -> Try<U>): Try<U> = f(value)

    override fun filter(p: (T) -> Boolean): Try<T> {
        return when(val result = Try { p(value) }) {
            is Success<Boolean> -> {
                if (result.value) this
                else Failure(NoSuchElementException("Predicate does not hold for: $value"))
            }
            is Failure<Boolean> -> {
                val wrappedException = NoSuchElementException("Predicate does not hold for: $value")
                wrappedException.initCause(result.exception)
                Failure(wrappedException)
            }
        }
    }

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
    override val isFailure: Boolean
        get() = true
    override val isSuccess: Boolean
        get() = false

    override fun flatten(): Try<T> = this

    override fun filter(p: (T) -> Boolean): Try<T> = this

    override fun <U> map(f: (T) -> U): Try<U> = Failure(exception)

    override fun <U> flatMap(f: (T) -> Try<U>): Try<U> = Failure(exception)
}