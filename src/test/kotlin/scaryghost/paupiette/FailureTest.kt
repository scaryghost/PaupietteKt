package scaryghost.paupiette

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import kotlin.reflect.KClass

fun <T : Throwable> assertFailure(actual: Try<*>, expectedExceptionClass: KClass<T>) {
    actual::class shouldBe Failure::class
    (actual as Failure).exception::class shouldBe expectedExceptionClass
}

class FailureTest : StringSpec({
    val original = Failure<Int>(ThreadDeath())

    "map returns Failure holding same exception instance" {
        val result = original.map{ listOf<Float>() }

        (result as Failure).exception shouldBeSameInstanceAs original.exception
    }

    "flatMap returns Failure holding same exception instance" {
        val result = original.flatMap{ Failure<String>(AssertionError()) }

        (result as Failure).exception shouldBeSameInstanceAs original.exception
    }

    "filter always returns same instance" {
        val result = original.filter{ true }

        result shouldBeSameInstanceAs original
    }
})