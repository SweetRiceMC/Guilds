/*
 * MIT License
 *
 * Copyright (c) 2022 Glare
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
package me.glaremasters.guilds.placeholders

import me.clip.placeholderapi.PlaceholderAPI
import me.clip.placeholderapi.expansion.PlaceholderExpansion
import me.glaremasters.guilds.Guilds
import me.glaremasters.guilds.challenges.ChallengeHandler
import me.glaremasters.guilds.guild.GuildHandler
import me.glaremasters.guilds.guild.GuildTier
import me.glaremasters.guilds.utils.EconomyUtils
import org.bukkit.entity.Player
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.util.*

class PlaceholderAPI(
    private val guildHandler: GuildHandler,
    private val challengeHandler: ChallengeHandler
) : PlaceholderExpansion() {
    val date = SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
    override fun getIdentifier(): String {
        return "guilds"
    }

    override fun persist(): Boolean {
        return true
    }

    override fun getAuthor(): String {
        return "Glare"
    }

    override fun getVersion(): String {
        return "2.1"
    }

    override fun onPlaceholderRequest(player: Player?, params: String): String {
        if (player == null) {
            return ""
        }
        val api = Guilds.getApi() ?: return ""
        val arg = PlaceholderAPI.setBracketPlaceholders(player, params)
        // Check formatted here because this needs to return before we check the guild
        if (arg.toLowerCase() == "formatted") {
            return guildHandler.getFormattedPlaceholder(player)
        }

        // %guilds_top_wins_name_#1%
        if (arg.startsWith("top_wins_name_")) {
            val updated = try {
                arg.replace("top_wins_name_", "").toInt()
            } catch (ex: NumberFormatException) {
                return ""
            }

            val guild = try {
                api.guildHandler.guilds.sortedBy { it.guildScore.wins }.reversed()[updated - 1]
            } catch (ex: IndexOutOfBoundsException) {
                return ""
            }

            return guild.name
        }

        // %guilds_top_wins_amount_#%
        if (arg.startsWith("top_wins_amount_")) {
            val updated = try {
                arg.replace("top_wins_amount_", "").toInt()
            } catch (ex: NumberFormatException) {
                return ""
            }

            val guild = try {
                api.guildHandler.guilds.sortedBy { it.guildScore.wins }.reversed()[updated - 1]
            } catch (ex: IndexOutOfBoundsException) {
                return ""
            }

            return guild.guildScore.wins.toString()
        }

        // %guilds_top_losses_name_1%
        if (arg.startsWith("top_losses_name_")) {
            val updated = try {
                arg.replace("top_losses_name_", "").toInt()
            } catch (ex: NumberFormatException) {
                return ""
            }

            val guild = try {
                api.guildHandler.guilds.sortedBy { it.guildScore.loses }.reversed()[updated - 1]
            } catch (ex: IndexOutOfBoundsException) {
                return ""
            }

            return guild.name
        }

        // %guilds_top_losses_amount_#%
        if (arg.startsWith("top_losses_amount_")) {
            val updated = try {
                arg.replace("top_losses_amount_", "").toInt()
            } catch (ex: NumberFormatException) {
                return ""
            }

            val guild = try {
                api.guildHandler.guilds.sortedBy { it.guildScore.loses }.reversed()[updated - 1]
            } catch (ex: IndexOutOfBoundsException) {
                return ""
            }

            return guild.guildScore.loses.toString()
        }

        // %guilds_top_wlr_name_#%
        if (arg.startsWith("top_wlr_name_")) {
            val updated = try {
                arg.replace("top_wlr_name_", "").toInt()
            } catch (ex: NumberFormatException) {
                return ""
            }

            val guild = try {
                api.guildHandler.guilds.sortedBy { (it.guildScore.wins / it.guildScore.loses) }.reversed()[updated - 1]
            } catch (ex: IndexOutOfBoundsException) {
                return ""
            }

            return guild.name
        }

        // %guilds_top_wlr_amount_#%
        if (arg.startsWith("top_wlr_amount_")) {
            val updated = try {
                arg.replace("top_wlr_amount_", "").toInt()
            } catch (ex: NumberFormatException) {
                return ""
            }

            val guild = try {
                api.guildHandler.guilds.sortedBy { (it.guildScore.wins / it.guildScore.loses) }.reversed()[updated - 1]
            } catch (ex: IndexOutOfBoundsException) {
                return ""
            }

            return (guild.guildScore.wins / guild.guildScore.loses).toString()
        }
        if (arg.startsWith("tier_")) {
            val s = arg.removePrefix("tier_")
            if (s.contains("_")) {
                val tier = guildHandler.getGuildTier(s.substring(0, s.indexOf("_")).toIntOrNull() ?: -1)
                if (tier != null)
                    handleTierPlaceholder(tier, s.removePrefix("${tier}_"))?.also { return it }
            }
        }
        val guild = api.getGuild(player) ?: return ""
        val nextTier = guildHandler.getGuildTier(guild.tier.level + 1)
        val member = guild.getMember(player.uniqueId)
        return when (arg.lowercase()) {
            "id" -> guild.id
            "name" -> guild.name
            "master" -> guild.guildMaster.asOfflinePlayer.name
            "member_count" -> guild.members.size
            "prefix" -> guild.prefix
            "members_online" -> guild.onlineMembers.size
            "status" -> guild.status.name
            "role" -> member.role.name
            "tier" -> guild.tier.level
            "balance" -> EconomyUtils.format(guild.balance)
            "balance_raw" -> guild.balance
            "frd" -> guild.prosperity
            "prosperity" -> guild.prosperity
            "code_amount" -> guild.codes.size
            "max_members" -> guild.tier.maxMembers
            "max_balance" -> EconomyUtils.format(guild.tier.maxBankBalance)
            "max_balance_raw" -> guild.tier.maxBankBalance
            "challenge_wins" -> guild.guildScore.wins
            "challenge_loses" -> guild.guildScore.loses
            "motd" -> guild.motd ?: ""
            "join_time" -> date.format(Date(member.joinDate))
            "create_time" -> date.format(Date(guild.creationDate))
            "vault_count" -> guild.vaults.size.toString()
            "next_tier" -> nextTier?.level?.toString() ?: "MAX"
            "challenge_guild" -> challengeHandler.getChallenge(guild)?.run {
                if (guildHandler.isSameGuild(guild, challenger))
                    defender.name
                else challenger.name
            } ?: ""
            else -> when {
                arg.startsWith("is_member_") -> {
                    val s = arg.removePrefix("is_member_")
                    guild.members.any { it.name?.equals(s, true) ?: false }
                }
                arg.startsWith("tier_") -> {
                    val s = arg.removePrefix("tier_")
                    handleTierPlaceholder(guild.tier, s)
                }
                arg.startsWith("next_tier_") -> {
                    if (nextTier == null) return "MAX"
                    val s = arg.removePrefix("next_tier_")
                    handleTierPlaceholder(nextTier, s)
                }
                else -> null
            }
        }?.toString() ?: ""
    }
    private fun handleTierPlaceholder(tier: GuildTier, s: String): String? = when(s) {
        "level" -> tier.level
        "name" -> tier.name
        "cost" -> tier.cost
        "frd" -> tier.prosperity
        "prosperity" -> tier.prosperity
        "max_allies" -> tier.maxAllies
        "max_members" -> tier.maxMembers
        "max_balance" -> EconomyUtils.format(tier.maxBankBalance)
        "max_balance_raw" -> tier.maxBankBalance
        "damage_multiplier" -> tier.damageMultiplier
        "exp_multiplier" -> tier.mobXpMultiplier
        "mob_xp_multiplier" -> tier.mobXpMultiplier
        "vault_amount" -> tier.vaultAmount
        "vault_count" -> tier.vaultAmount
        else -> null
    }?.toString()

}
