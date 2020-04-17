package scaryghost.paupiette

import io.kotest.core.spec.style.StringSpec
import io.kotest.data.forAll
import io.kotest.data.row
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.next
import java.nio.BufferOverflowException

class TryTest : StringSpec({
    "Failure.flatten returns self" {
        val result = Failure<Int>(IllegalArgumentException())
        result.flatten() shouldBe result
    }

    "Success.flatten returns self when un-nested" {
        forAll(
            row(Success(Arb.int().next())),
            row(Success(null))
        ) { result ->
            result.flatten() shouldBe result
        }
    }

    "Success.flatten returns un-nested value" {
        forAll(
            row(Success(Arb.int().next())),
            row(Failure<Int>(NullPointerException()))
        ) { original ->
            val nested = Success(Success(Success(Success(original))))
            nested.flatten() shouldBe original
        }
    }

    "Failure.filter always returns itself" {
        forAll(
            row({_: Int -> true}),
            row({_: Int -> false})
        ) { predicate ->
            val original = Failure<Int>(BufferOverflowException())

            original.filter(predicate) shouldBe original
        }

        val limit = Arb.int().next()
        val original = Success(limit)
        val result = original.filter{
            it <= Arb.int(limit .. Int.MAX_VALUE).next()
        }

        result shouldBe original
    }
})