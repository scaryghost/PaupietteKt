package scaryghost.paupiette

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.next
import io.kotest.property.arbitrary.string

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

        result::class shouldBe Success::class
        (result as Success).value shouldBe expected
    }

    "map convert from type T to U" {
        val expected = Arb.string().next()
        val result = Success(Arb.int().next()).map{ expected }

        result::class shouldBe Success::class
        (result as Success).value shouldBe expected
    }
})