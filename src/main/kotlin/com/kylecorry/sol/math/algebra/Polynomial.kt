package com.kylecorry.sol.math.algebra

import com.kylecorry.sol.math.SolMath
import com.kylecorry.sol.math.sumOfFloat

data class PolynomialTerm(
    val coefficient: Float,
    val exponent: Int
)

class Polynomial(terms: List<PolynomialTerm>) {

    val terms = terms
        .groupBy { it.exponent }
        .map { (exponent, groupedTerms) ->
            val coefficient = groupedTerms.sumOfFloat { it.coefficient }
            PolynomialTerm(coefficient, exponent)
        }
        .filter { !SolMath.isZero(it.coefficient) }
        .sortedBy { -it.exponent }

    fun evaluate(x: Float): Float {
        return terms.sumOfFloat { it.coefficient * SolMath.power(x, it.exponent) }
    }

    fun derivative(): Polynomial {
        val derivedTerms = terms.mapNotNull {
            if (it.exponent == 0) {
                null
            } else {
                PolynomialTerm(it.coefficient * it.exponent, it.exponent - 1)
            }
        }
        return Polynomial(derivedTerms)
    }

    fun integral(c: Float = 0f): Polynomial {
        val integratedTerms = terms.map {
            PolynomialTerm(
                coefficient = it.coefficient / (it.exponent + 1),
                exponent = it.exponent + 1
            )
        }
        return Polynomial(integratedTerms + listOf(PolynomialTerm(c, 0)))
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Polynomial) return false

        if (terms.size != other.terms.size) return false
        for (i in terms.indices) {
            if (terms[i] != other.terms[i]) return false
        }

        return true
    }

    override fun hashCode(): Int {
        return terms.hashCode()
    }

    override fun toString(): String {
        if (terms.isEmpty()) {
            return "0"
        }
        return terms.joinToString(" + ") { term ->
            val coefficientString = when {
                term.exponent == 0 -> "${term.coefficient}"
                term.coefficient == 1f -> ""
                term.coefficient == -1f -> "-"
                else -> "${term.coefficient}"
            }.removeSuffix(".0")
            val exponentStr = when (term.exponent) {
                0 -> ""
                1 -> "x"
                else -> "x^${term.exponent}"
            }
            "$coefficientString$exponentStr"
        }.replace("+ -", "- ")
    }

    companion object {
        fun of(vararg terms: PolynomialTerm): Polynomial {
            return Polynomial(terms.toList())
        }

        /**
         * Creates a polynomial from coefficients.
         * The index of the coefficient corresponds to the exponent.
         */
        fun fromCoefficients(vararg coefficients: Float): Polynomial {
            val terms = coefficients.mapIndexed { index, coefficient ->
                PolynomialTerm(coefficient, index)
            }
            return Polynomial(terms)
        }

        /**
         * Creates a polynomial from a string equation.
         * Example inputs: "x^2", "x + 4x^2 + 5", "-2x", "x - 2x^3"
         */
        fun of(equation: String): Polynomial {
            val terms = equation
                .replace(" ", "")
                .replace("-", "+-")
                .replace("^", "")
                .split("+")
                .filter { it.isNotEmpty() }
                .map { termString ->
                    if ("x" !in termString) {
                        return@map PolynomialTerm(termString.toFloat(), 0)
                    }
                    val parts = termString.split("x")
                    val coefficientPart = parts[0]
                        .replace("(", "")
                        .replace(")", "")
                    val coefficient = when {
                        parts[0] == "" -> 1f
                        parts[0] == "-" -> -1f
                        parts[0].contains("/") -> {
                            val fractionParts = coefficientPart.split("/")
                            if (fractionParts.size != 2) {
                                throw IllegalArgumentException("Invalid fraction in term: $termString")
                            }
                            val numerator = fractionParts[0].toFloat()
                            val denominator = fractionParts[1].toFloat()
                            numerator / denominator
                        }

                        else -> parts[0].toFloat()
                    }
                    val exponent = if (parts.size > 1 && parts[1].isNotEmpty()) {
                        parts[1].toInt()
                    } else {
                        1
                    }
                    PolynomialTerm(coefficient, exponent)
                }
            return Polynomial(terms)
        }
    }
}