package scaryghost.paupiette


import io.kotest.core.spec.style.StringSpec
import io.kotest.data.forAll
import io.kotest.data.row
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.next

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
})