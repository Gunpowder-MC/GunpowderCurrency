package io.github.gunpowder.entities

import java.math.BigDecimal
import java.util.*

data class StoredBalance(
    val uuid: UUID,
    var balance: BigDecimal
)
