package scaryghost.paupiette

import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.data.forAll
import io.kotest.data.row
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.next
import io.kotest.property.arbitrary.string
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

    "recover is not safe" {
        shouldThrow<NullPointerException> {
            original.recover<Int> { throw NullPointerException() }
        }
    }

    "recover returns Success containing result from applying f" {
        forAll(
            row(Arb.string().next()),
            row(Arb.int().next())
        ) { expected ->
            val result = original.recover { expected }

            (result as Success).value shouldBe expected
        }
    }

    "recoverWith is Safe" {
        shouldNotThrowAny {
            original.recoverWith {
                Try { 1 / 0 }
            }
        }
    }

    "recoverWith returns result from applying f" {
        forAll(
            row(Success(Arb.string().next())),
            row(Success(Arb.int().next())),
            row(Failure<Any>(IllegalArgumentException()))
        ) { expected ->
            val result = original.recoverWith { expected }

            result shouldBeSameInstanceAs expected
        }
    }
})