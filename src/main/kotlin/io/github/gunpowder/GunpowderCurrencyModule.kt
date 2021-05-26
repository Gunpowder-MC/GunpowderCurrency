/*
 * MIT License
 *
 * Copyright (c) 2020 GunpowderMC
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.github.gunpowder

import io.github.gunpowder.api.GunpowderMod
import io.github.gunpowder.api.GunpowderModule
import io.github.gunpowder.api.ext.getOrNull
import io.github.gunpowder.api.module.currency.modelhandlers.BalanceHandler as APIBalanceHandler
import io.github.gunpowder.commands.BalanceCommand
import io.github.gunpowder.commands.PayCommand
import io.github.gunpowder.configs.CurrencyConfig
import io.github.gunpowder.modelhandlers.BalanceHandler
import io.github.gunpowder.models.BalanceTable
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import java.util.*
import java.util.function.Supplier

class GunpowderCurrencyModule : GunpowderModule {
    override val name = "currency"
    override val toggleable = true
    val gunpowder = GunpowderMod.instance
    val bonusTimeout = mutableMapOf<UUID, Int>()

    override fun registerCommands() {
        gunpowder.registry.registerCommand(BalanceCommand::register)
        gunpowder.registry.registerCommand(PayCommand::register)
    }

    override fun registerConfigs() {
        gunpowder.registry.registerConfig("gunpowder-currency.yaml", CurrencyConfig::class.java, "gunpowder-currency.yaml")
    }

    override fun registerEvents() {
        val hourly = gunpowder.registry.getConfig(CurrencyConfig::class.java).hourlyBonus.toBigDecimal()

        if (hourly > (0).toBigDecimal()) {
            ServerTickEvents.START_SERVER_TICK.register(ServerTickEvents.StartTick { server ->
                server.playerManager.playerList.forEach {
                    var x = bonusTimeout.getOrDefault(it.uuid, 0)
                    if (x++ > 72000) {  // 1 hour played
                        BalanceHandler.modifyUser(it.uuid) { balance ->
                            balance.balance += hourly
                            balance
                        }
                        x = 0
                    }
                    bonusTimeout[it.uuid] = x
                }
            })
        }
    }

    override fun onInitialize() {
        gunpowder.registry.registerTable(BalanceTable)
        gunpowder.registry.registerModelHandler(APIBalanceHandler::class.java) { BalanceHandler }
    }

}