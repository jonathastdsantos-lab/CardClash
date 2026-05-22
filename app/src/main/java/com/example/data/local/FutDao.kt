package com.example.data.local

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface FutDao {
    // --- USER PROFILE ---
    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    fun getUserProfile(): Flow<UserProfile?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProfile(profile: UserProfile)

    @Query("UPDATE user_profile SET coins = :newCoins WHERE id = 1")
    suspend fun updateCoins(newCoins: Int)

    @Query("UPDATE user_profile SET xp = :newXp, level = :newLevel WHERE id = 1")
    suspend fun updateXpAndLevel(newXp: Int, newLevel: Int)

    @Query("UPDATE user_profile SET dailyStreak = :newStreak, lastDailyClaimTimestamp = :lastClaim WHERE id = 1")
    suspend fun updateDailyStreak(newStreak: Int, lastClaim: Long)

    // --- USER INVENTORY ---
    @Query("SELECT * FROM user_inventory")
    fun getInventory(): Flow<List<UserInventory>>

    @Query("SELECT * FROM user_inventory WHERE cardId = :cardId")
    suspend fun getInventoryItem(cardId: Int): UserInventory?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInventoryItem(item: UserInventory)

    @Delete
    suspend fun deleteInventoryItem(item: UserInventory)

    @Query("UPDATE user_inventory SET inBattleDeck = :inDeck WHERE cardId = :cardId")
    suspend fun updateBattleDeckStatus(cardId: Int, inDeck: Boolean)

    // --- BATTLE LOGS ---
    @Query("SELECT * FROM battle_log ORDER BY timestamp DESC")
    fun getBattleLogs(): Flow<List<BattleLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBattleLog(log: BattleLog)

    // --- TRADE OFFERS ---
    @Query("SELECT * FROM trade_offer ORDER BY timestamp DESC")
    fun getTradeOffers(): Flow<List<TradeOffer>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTradeOffer(offer: TradeOffer)

    @Query("UPDATE trade_offer SET status = :status WHERE id = :offerId")
    suspend fun updateTradeOfferStatus(offerId: String, status: String)

    @Query("DELETE FROM trade_offer WHERE id = :offerId")
    suspend fun deleteTradeOffer(offerId: String)

    // --- LIVE MATCHES ---
    @Query("SELECT * FROM live_match ORDER BY id ASC")
    fun getLiveMatches(): Flow<List<LiveMatch>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMatches(matches: List<LiveMatch>)

    @Query("UPDATE live_match SET homeScore = :homeScore, awayScore = :awayScore, minute = :minute, isLive = :isLive, scorers = :scorers WHERE id = :matchId")
    suspend fun updateMatchState(matchId: Int, homeScore: Int, awayScore: Int, minute: Int, isLive: Boolean, scorers: String)

    @Query("UPDATE live_match SET scoreTriggered = :triggered WHERE id = :matchId")
    suspend fun markMatchScoreTriggered(matchId: Int, triggered: Boolean)
}
