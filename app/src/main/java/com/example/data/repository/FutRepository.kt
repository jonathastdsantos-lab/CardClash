package com.example.data.repository

import com.example.data.local.FutDao
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlin.random.Random

class FutRepository(private val futDao: FutDao) {

    val userProfile: Flow<UserProfile?> = futDao.getUserProfile()
    val inventory: Flow<List<UserInventory>> = futDao.getInventory()
    val battleLogs: Flow<List<BattleLog>> = futDao.getBattleLogs()
    val tradeOffers: Flow<List<TradeOffer>> = futDao.getTradeOffers()
    val liveMatches: Flow<List<LiveMatch>> = futDao.getLiveMatches()

    suspend fun saveProfile(profile: UserProfile) {
        futDao.insertOrUpdateProfile(profile)
        // If first launch, give starter cards
        val currentInv = futDao.getInventory().firstOrNull() ?: emptyList()
        if (currentInv.isEmpty()) {
            giveStarterPack()
        }
    }

    suspend fun addCoins(amount: Int) {
        val profile = futDao.getUserProfile().firstOrNull()
        if (profile != null) {
            val newCoins = (profile.coins + amount).coerceAtLeast(0)
            futDao.updateCoins(newCoins)
        }
    }

    suspend fun addXp(amount: Int) {
        val profile = futDao.getUserProfile().firstOrNull()
        if (profile != null) {
            val currentXp = profile.xp + amount
            val xpNeeded = profile.level * 100
            val (newLevel, newXp) = if (currentXp >= xpNeeded) {
                Pair(profile.level + 1, currentXp - xpNeeded)
            } else {
                Pair(profile.level, currentXp)
            }
            futDao.updateXpAndLevel(newXp, newLevel)
        }
    }

    suspend fun claimDailyStreakReward(): Int? {
        val profile = futDao.getUserProfile().firstOrNull() ?: return null
        val now = System.currentTimeMillis()
        val oneDayMillis = 24 * 60 * 60 * 1000L

        // Simple threshold check: wait 18 hours before claiming again
        val timeSinceLastClaim = now - profile.lastDailyClaimTimestamp
        if (timeSinceLastClaim < 18 * 60 * 60 * 1000L) {
            return null // Not ready
        }

        val newStreak = if (timeSinceLastClaim > 40 * oneDayMillis) {
            1 // Streak broken, restart
        } else {
            profile.dailyStreak + 1
        }

        val baseRewardCoins = 150
        val bonus = newStreak * 50
        val totalReward = baseRewardCoins + bonus

        futDao.updateDailyStreak(newStreak, now)
        addCoins(totalReward)

        // Give a free common pack too!
        openPack(packType = "GRATIS", cost = 0)

        return totalReward
    }

    // Give 5 initial cards
    private suspend fun giveStarterPack() {
        val starterCardIds = listOf(18, 19, 20, 21, 22) // Our 5 bronze star players
        for (id in starterCardIds) {
            val current = futDao.getInventoryItem(id)
            val newQty = (current?.quantity ?: 0) + 1
            futDao.insertInventoryItem(UserInventory(cardId = id, quantity = newQty, inBattleDeck = true))
        }
    }

    // Opens a pack of cards and saves it to inventory.
    // Returns the players drawn in this pack.
    suspend fun openPack(packType: String, cost: Int): List<PlayerCard> {
        val profile = futDao.getUserProfile().firstOrNull()
        if (packType != "GRATIS" && (profile == null || profile.coins < cost)) {
            return emptyList()
        }

        if (cost > 0) {
            addCoins(-cost)
        }

        val (numCards, minRarities) = when (packType) {
            "BRONZE" -> Pair(3, listOf(Rarity.BRONZE))
            "PRATA" -> Pair(3, listOf(Rarity.PRATA, Rarity.BRONZE))
            "OURO" -> Pair(4, listOf(Rarity.OURO, Rarity.PRATA, Rarity.BRONZE))
            "PREMIUM" -> Pair(4, listOf(Rarity.ESPECIAL, Rarity.OURO, Rarity.PRATA))
            "LENDARIO" -> Pair(5, listOf(Rarity.LENDARIA, Rarity.ASSINADA, Rarity.ANIMADA, Rarity.ESPECIAL))
            else -> Pair(2, listOf(Rarity.BRONZE)) // GRATIS
        }

        val drawnCards = mutableListOf<PlayerCard>()
        val allCards = CardCatalog.cards

        for (i in 0 until numCards) {
            // First card guarantees one of the minRarities
            val pool = if (i == 0) {
                allCards.filter { it.rarity in minRarities }
            } else {
                // Normal draw weights: 55% Bronze, 25% Prata, 12% Ouro, 5% Especial, 3% Lendária/Assinada/Animada
                val weight = Random.nextInt(100)
                when {
                    weight < 55 -> allCards.filter { it.rarity == Rarity.BRONZE }
                    weight < 80 -> allCards.filter { it.rarity == Rarity.PRATA }
                    weight < 92 -> allCards.filter { it.rarity == Rarity.OURO }
                    weight < 97 -> allCards.filter { it.rarity == Rarity.ESPECIAL }
                    else -> allCards.filter { it.rarity in listOf(Rarity.LENDARIA, Rarity.ASSINADA, Rarity.ANIMADA) }
                }
            }

            // Fallback if subset is empty
            val resolvedPool = pool.ifEmpty { allCards }
            val chosen = resolvedPool[Random.nextInt(resolvedPool.size)]
            drawnCards.add(chosen)

            // Save to database inventory
            val existing = futDao.getInventoryItem(chosen.id)
            val newQty = (existing?.quantity ?: 0) + 1
            futDao.insertInventoryItem(UserInventory(cardId = chosen.id, quantity = newQty, inBattleDeck = existing?.inBattleDeck ?: false))
        }

        return drawnCards
    }

    // Toggle deck status
    suspend fun toggleBattleDeck(cardId: Int) {
        val existing = futDao.getInventoryItem(cardId) ?: return
        if (existing.quantity > 0) {
            futDao.updateBattleDeckStatus(cardId, !existing.inBattleDeck)
        }
    }

    // --- ESCROW TRANSACTION: CARD TRADE ---
    // Safely execute trading without duplicate exploits (Atomic transaction)
    suspend fun executeTradeOffer(offer: TradeOffer): Boolean {
        val existingOffer = futDao.getTradeOffers().firstOrNull()?.find { it.id == offer.id } ?: return false
        if (existingOffer.status != "PENDING") return false

        // Check user has the needed card to accept this trade
        val myCardInv = futDao.getInventoryItem(offer.requestCardId)
        if (myCardInv == null || myCardInv.quantity <= 0) {
            return false // User does not own the requested card
        }

        // Deduct my requested card
        val newMyQty = myCardInv.quantity - 1
        if (newMyQty <= 0) {
            futDao.deleteInventoryItem(myCardInv)
        } else {
            futDao.insertInventoryItem(myCardInv.copy(quantity = newMyQty))
        }

        // Give me the offered card
        val offerCardInv = futDao.getInventoryItem(offer.offerCardId)
        val newOfferQty = (offerCardInv?.quantity ?: 0) + 1
        futDao.insertInventoryItem(UserInventory(cardId = offer.offerCardId, quantity = newOfferQty, inBattleDeck = offerCardInv?.inBattleDeck ?: false))

        // Update trade status in database
        futDao.updateTradeOfferStatus(offer.id, "ACCEPTED")
        addXp(25) // Social interaction reward
        return true
    }

    // Create a new trade offer
    suspend fun postTradeOffer(offerCardId: Int, requestCardId: Int): Boolean {
        // Verify user currently has this card to post
        val inv = futDao.getInventoryItem(offerCardId)
        if (inv == null || inv.quantity <= 0) return false

        // Atomically deposit the offer card in escrow (by deducting 1 unit)
        val newQty = inv.quantity - 1
        if (newQty <= 0) {
            futDao.deleteInventoryItem(inv)
        } else {
            futDao.insertInventoryItem(inv.copy(quantity = newQty))
        }

        val offerId = "trade_" + Random.nextInt(100000, 999999).toString()
        val offer = TradeOffer(
            id = offerId,
            posterName = "Você (Anônimo)",
            offerCardId = offerCardId,
            requestCardId = requestCardId,
            status = "PENDING"
        )
        futDao.insertTradeOffer(offer)
        return true
    }

    // Cancel offer (refund card)
    suspend fun cancelTradeOffer(offerId: String): Boolean {
        val offers = futDao.getTradeOffers().firstOrNull() ?: emptyList()
        val found = offers.find { it.id == offerId } ?: return false
        if (found.status != "PENDING") return false

        // Refund offered card to inventory
        val existing = futDao.getInventoryItem(found.offerCardId)
        val newQty = (existing?.quantity ?: 0) + 1
        futDao.insertInventoryItem(UserInventory(cardId = found.offerCardId, quantity = newQty, inBattleDeck = existing?.inBattleDeck ?: false))

        futDao.deleteTradeOffer(offerId)
        return true
    }

    // Seed mock offers to create a live, social community hub feel!
    suspend fun seedMockOffersIfEmpty() {
        val current = futDao.getTradeOffers().firstOrNull() ?: emptyList()
        if (current.isEmpty()) {
            val mocks = listOf(
                TradeOffer("m1", "Lucas_Vasco", 14, 18, "PENDING"),       // Offers Lucas Moura, wants Cano
                TradeOffer("m2", "Gabi10_Mengo", 13, 20, "PENDING"),      // Offers Calleri, wants Mastriani
                TradeOffer("m3", "Verdao_Soberano", 15, 12, "PENDING"),   // Offers Raphael Veiga, wants Raphinha
                TradeOffer("m4", "Ney_Deus", 10, 4, "PENDING")            // Offers Rodrygo, wants Neymar Jr (extremely rare swap)
            )
            for (m in mocks) {
                futDao.insertTradeOffer(m)
            }
        }
    }

    // --- ESCROW TRANSACTION: BATTLE RESULTS ---
    suspend fun completeBattle(wageredCardId: Int, opponentCardId: Int, won: Boolean, opponentName: String) {
        val inv = futDao.getInventoryItem(wageredCardId) ?: return

        if (won) {
            // Give user the opponent's card (won card)
            val oppInvItem = futDao.getInventoryItem(opponentCardId)
            val newQty = (oppInvItem?.quantity ?: 0) + 1
            futDao.insertInventoryItem(UserInventory(cardId = opponentCardId, quantity = newQty, inBattleDeck = oppInvItem?.inBattleDeck ?: false))
            addXp(40)
            addCoins(100)
        } else {
            // Deduct the wagered card
            val newQty = inv.quantity - 1
            if (newQty <= 0) {
                futDao.deleteInventoryItem(inv)
            } else {
                futDao.insertInventoryItem(inv.copy(quantity = newQty))
            }
            addXp(10)
        }

        // Add to battle logs
        val log = BattleLog(
            opponentName = opponentName,
            wageredCardId = wageredCardId,
            opponentCardId = opponentCardId,
            won = won,
            xpEarned = if (won) 40 else 10
        )
        futDao.insertBattleLog(log)
    }

    // --- SEED LIVE MATCHES AND LIVE SIMULATION ---
    suspend fun seedLiveMatchesIfEmpty() {
        val current = futDao.getLiveMatches().firstOrNull() ?: emptyList()
        if (current.isEmpty()) {
            val initial = listOf(
                LiveMatch(1, "Flamengo", "Vasco", 0, 0, 0, false, false, ""),
                LiveMatch(2, "Palmeiras", "Corinthians", 0, 0, 0, false, false, ""),
                LiveMatch(3, "São Paulo", "Santos", 0, 0, 0, false, false, ""),
                LiveMatch(4, "Grêmio", "Internacional", 0, 0, 0, false, false, "")
            )
            futDao.insertMatches(initial)
        }
    }

    // Simulate match timer and stats incrementing!
    // Every 5-10 seconds this runs, it increments minutes of live matches, and scores arbitrary goals.
    // If a goal is scored, we can notify the view!
    suspend fun simulateLiveTick(): Pair<String, String>? {
        val matches = futDao.getLiveMatches().firstOrNull() ?: return null
        val liveMatchesList = matches.toMutableList()
        if (liveMatchesList.isEmpty()) return null
        var notification: Pair<String, String>? = null

        // Pick one random match to update or toggle live state
        val idx = Random.nextInt(liveMatchesList.size)
        val selected = liveMatchesList[idx]

        if (!selected.isLive) {
            // Kick off match with 50% chance
            if (Random.nextBoolean()) {
                val updated = selected.copy(isLive = true, minute = 1, homeScore = 0, awayScore = 0, scorers = "", scoreTriggered = false)
                futDao.insertMatches(listOf(updated))
                notification = Pair("Partida Iniciada!", "${updated.homeTeam} x ${updated.awayTeam} começou agora!")
            }
        } else {
            // Increment minute and maybe score
            val newMinute = selected.minute + Random.nextInt(5, 12)
            if (newMinute >= 90) {
                // End match
                val updated = selected.copy(isLive = false, minute = 90)
                futDao.insertMatches(listOf(updated))
                notification = Pair("Fim de Jogo!", "${updated.homeTeam} ${updated.homeScore} x ${updated.awayScore} ${updated.awayTeam}")
            } else {
                var newHomeScore = selected.homeScore
                var newAwayScore = selected.awayScore
                var newScorers = selected.scorers

                // 25% goal chance
                if (Random.nextInt(4) == 0) {
                    val isHome = Random.nextBoolean()
                    val scorerName = pickScorerForTeam(if (isHome) selected.homeTeam else selected.awayTeam)
                    if (isHome) newHomeScore++ else newAwayScore++
                    newScorers = if (newScorers.isEmpty()) scorerName else "$newScorers, $scorerName ($newMinute')"
                    notification = Pair("GOOOL do ${if (isHome) selected.homeTeam else selected.awayTeam}! ⚽", "$scorerName marca aos $newMinute'! (${selected.homeTeam} $newHomeScore x $newAwayScore ${selected.awayTeam})")
                }

                val updated = selected.copy(
                    minute = newMinute,
                    homeScore = newHomeScore,
                    awayScore = newAwayScore,
                    scorers = newScorers
                )
                futDao.insertMatches(listOf(updated))
            }
        }
        return notification
    }

    private fun pickScorerForTeam(team: String): String {
        return when (team) {
            "Flamengo" -> listOf("Pedro", "Neymar Jr", "Bruno Henrique", "Gerson").random()
            "Vasco"    -> listOf("Payet", "Vegetti", "Coutinho", "Lucas Piton").random()
            "Palmeiras"-> listOf("Estêvão", "Raphael Veiga", "Rony", "Felipe Anderson").random()
            "Corinthians"-> listOf("Yuri Alberto", "Depay", "Garro", "Romero").random()
            "São Paulo"-> listOf("Calleri", "Lucas Moura", "Luciano", "Ferreirinha").random()
            "Santos"   -> listOf("Otero", "Guilherme", "Willian", "Pelé").random()
            "Grêmio"   -> listOf("Soteldo", "Cristaldo", "Diego Costa", "Pavón").random()
            else       -> listOf("Valdivia", "Fred", "Dadá Maravilha", "Socrates").random()
        }
    }
}
