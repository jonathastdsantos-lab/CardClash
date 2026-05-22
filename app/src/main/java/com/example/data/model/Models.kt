package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class Rarity {
    BRONZE,
    PRATA,
    OURO,
    ESPECIAL,
    LENDARIA,
    ASSINADA,
    ANIMADA
}

enum class Position {
    ATA, // Atacante (Forward)
    MEI, // Meio-campo (Midfield)
    DFS, // Defensor (Defense)
    GOL  // Goleiro (Goalkeeper)
}

data class PlayerStats(
    val pac: Int, // Ritmo (Pace)
    val sho: Int, // Chute (Shot)
    val pas: Int, // Passe (Pass)
    val dri: Int, // Drible (Dribbling)
    val def: Int, // Defesa (Defense)
    val phy: Int  // Físico (Physical)
)

data class PlayerCard(
    val id: Int,
    val name: String,
    val clubAndCountry: String,
    val position: Position,
    val overall: Int,
    val stats: PlayerStats,
    val rarity: Rarity,
    val initialHexColor: String, // Custom visual tint for the card graphics
    val photoUrl: String? = null,
    val clubLogoUrl: String? = null
)

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val id: Int = 1, // Only single profile row
    val name: String,
    val favoriteTeam: String,
    val age: Int,
    val coins: Int = 1000,
    val xp: Int = 0,
    val level: Int = 1,
    val dailyStreak: Int = 0,
    val lastDailyClaimTimestamp: Long = 0,
    val loginProvider: String // "google", "facebook", "guest"
)

@Entity(tableName = "user_inventory")
data class UserInventory(
    @PrimaryKey val cardId: Int,
    val quantity: Int = 0,
    val isFavorite: Boolean = false,
    val inBattleDeck: Boolean = false,
    val upgradeLevel: Int = 0, // Card evolution level (0 to 3)
    val customPhotoUrl: String? = null,
    val customName: String? = null
)

@Entity(tableName = "battle_log")
data class BattleLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val opponentName: String,
    val wageredCardId: Int,
    val opponentCardId: Int,
    val won: Boolean,
    val xpEarned: Int,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "trade_offer")
data class TradeOffer(
    @PrimaryKey val id: String, // String ID for online/local simulation matching
    val posterName: String,
    val offerCardId: Int,
    val requestCardId: Int,
    val status: String, // "PENDING", "ACCEPTED", "DECLINED"
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "live_match")
data class LiveMatch(
    @PrimaryKey val id: Int,
    val homeTeam: String,
    val awayTeam: String,
    val homeScore: Int,
    val awayScore: Int,
    val minute: Int,
    val isLive: Boolean,
    val scoreTriggered: Boolean = false,
    val scorers: String // Comma separated player names
)
