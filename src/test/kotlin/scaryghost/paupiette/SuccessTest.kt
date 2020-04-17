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
import java.lang.IllegalArgumentException

class SuccessTest : StringSpec({
    "filter returns Failure if predicate faults" {
        val rootException = ArithmeticException()
        val result = Success(Arb.int().next())
                .filter{ throw rootException }

        assertFailure(result, NoSuchElementException::class)
        (result as Failure).exception.cause shouldBe rootException
    }

    "filter returns Failure if predicate fails" {
        val result = Success(Arb.int().next())
                .filter{ false }

        assertFailure(result, NoSuchElementException::class)
    }

    "filter returns same instance if predicate succeeds" {
        val original = Success(Arb.int().next())
        val result = original.filter{ true }

        result shouldBeSameInstanceAs original
    }

    "map throws exception from function" {
        shouldThrow<NullPointerException> {
            Success(Arb.int().next()).map{ throw NullPointerException() }
        }
    }

    "map converts value to new value" {
        val arbInt = Arb.int()
        val expected = arbInt.next()
        val result = Success(arbInt.next()).map{ expected }

        (result as Success).value shouldBe expected
    }

    "map convert from type T to U" {
        val expected = Arb.string().next()
        val result = Success(Arb.int().next()).map{ expected }

        (result as Success).value shouldBe expected
    }

    "recover does not apply function" {
        val arbString = Arb.string()
        val expected = arbString.next()

        shouldNotThrowAny {
            Success(expected).recover<String> {
                throw IllegalArgumentException()
            }
        }

        val result = Success(expected).recover {arbString.next()}
        (result as Success).value shouldBe expected
    }

    "recover throws exception if U cast fails" {
        shouldThrow<ClassCastException> {
            Success(Arb.string().next()).recover {
                Arb.int().next()
            }
        }
    }

    "recoverWith does not apply function" {
        val arbString = Arb.string()

        forAll(
            row(Success(arbString.next())),
            row(Failure<String>(NullPointerException()))
        ) { fallbackResult ->
            val expected = arbString.next()

            val result = Success(expected).recoverWith { fallbackResult }
            (result as Success).value shouldBe expected
        }
    }

    "recoverWith returns Failure if U cast fails" {
        val result = Success(Arb.string().next())
                .recoverWith<Int> {
                    Try { Arb.int().next() }
                }

        assertFailure(result, ClassCastException::class)
    }


    "transform calls s" {
        val expected = Success(Arb.int().next())

        val result = Success(Arb.string().next()).transform(
                { expected }, { Failure(RuntimeException()) }
        )
        result shouldBeSameInstanceAs expected
    }
})