package com.kylecorry.sol.detekt

import io.gitlab.arturbosch.detekt.rules.KotlinCoreEnvironmentTest
import io.gitlab.arturbosch.detekt.test.compileAndLintWithContext
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals

@KotlinCoreEnvironmentTest
class NoRecursionTest(private val env: KotlinCoreEnvironment) {

    private val subject = NoRecursion(io.gitlab.arturbosch.detekt.api.Config.empty)

    @Test
    fun `reports direct recursion`() {
        val findings = subject.compileAndLintWithContext(
            env,
            """
                fun factorial(n: Int): Int {
                    return if (n <= 1) 1 else n * factorial(n - 1)
                }
            """.trimIndent(),
        )

        assertEquals(1, findings.size)
    }

    @Test
    fun `does not report non-recursive function calls`() {
        val findings = subject.compileAndLintWithContext(
            env,
            """
                fun helper(n: Int): Int {
                    return n - 1
                }

                fun factorial(n: Int): Int {
                    return if (n <= 1) 1 else n * helper(n)
                }
            """.trimIndent(),
        )

        assertEquals(0, findings.size)
    }

    @Test
    fun `does not report overload delegation`() {
        val findings = subject.compileAndLintWithContext(
            env,
            """
                object Example {
                    fun toRadians(angle: Double): Double {
                        return Math.toRadians(angle)
                    }

                    fun toRadians(angle: Float): Float {
                        return toRadians(angle.toDouble()).toFloat()
                    }
                }
            """.trimIndent(),
        )

        assertEquals(0, findings.size)
    }
}
