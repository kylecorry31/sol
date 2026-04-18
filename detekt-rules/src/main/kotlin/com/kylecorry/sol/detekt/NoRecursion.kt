package com.kylecorry.sol.detekt

import io.gitlab.arturbosch.detekt.api.CodeSmell
import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.Debt
import io.gitlab.arturbosch.detekt.api.Entity
import io.gitlab.arturbosch.detekt.api.Issue
import io.gitlab.arturbosch.detekt.api.Rule
import io.gitlab.arturbosch.detekt.api.Severity
import io.gitlab.arturbosch.detekt.api.internal.RequiresTypeResolution
import org.jetbrains.kotlin.descriptors.CallableDescriptor
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.calls.model.ResolvedCall

@RequiresTypeResolution
class NoRecursion(config: Config) : Rule(config) {

    override val issue = Issue(
        id = javaClass.simpleName,
        severity = Severity.Defect,
        description = "Recursive functions are not allowed.",
        debt = Debt.FIVE_MINS,
    )

    private val functionStack = ArrayDeque<CallableDescriptor>()

    override fun visitNamedFunction(function: KtNamedFunction) {
        val functionDescriptor =
            bindingContext[BindingContext.DECLARATION_TO_DESCRIPTOR, function] as? CallableDescriptor

        if (functionDescriptor == null) {
            super.visitNamedFunction(function)
            return
        }

        functionStack.addLast(functionDescriptor.original)
        super.visitNamedFunction(function)
        functionStack.removeLast()
    }

    override fun visitCallExpression(expression: KtCallExpression) {
        val currentFunction = functionStack.lastOrNull()
        val calledFunction = resolveCallDescriptor(expression)

        if (currentFunction != null && calledFunction == currentFunction) {
            report(
                CodeSmell(
                    issue = issue,
                    entity = Entity.from(expression),
                    message = "Function '${currentFunction.name}' calls itself recursively.",
                ),
            )
        }

        super.visitCallExpression(expression)
    }

    private fun resolveCallDescriptor(expression: KtCallExpression): CallableDescriptor? {
        val resolvedCall = callUtilMethod.invoke(null, expression, bindingContext) as? ResolvedCall<*>
        return resolvedCall?.resultingDescriptor?.original
    }

    private companion object {
        val callUtilMethod =
            Class.forName("org.jetbrains.kotlin.resolve.calls.callUtil.CallUtilKt")
                .getMethod("getResolvedCall", KtElement::class.java, BindingContext::class.java)
    }
}
