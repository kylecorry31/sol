package com.kylecorry.sol.detekt

import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.RuleSet
import io.gitlab.arturbosch.detekt.api.RuleSetProvider

class SolRuleSetProvider : RuleSetProvider {
    override val ruleSetId: String = "sol"

    override fun instance(config: Config): RuleSet {
        return RuleSet(ruleSetId, listOf(NoRecursion(config)))
    }
}
