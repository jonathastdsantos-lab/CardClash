package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.*
import com.example.data.repository.FutRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.random.Random

enum class AppTab {
    PROFILE,
    COLECAO,
    LOJA,
    BAFO,
    TROCAS,
    PLACARES,
    MINIJOGOS
}

sealed interface BattleState {
    object Idle : BattleState
    data class ChooseWager(val myBattleCards: List<PlayerCard>) : BattleState
    data class ActivePlay(
        val myWager: PlayerCard,
        val oppWager: PlayerCard,
        val oppName: String,
        val currentPower: Float = 0f,
        val optimalTriggerZone: ClosedFloatingPointRange<Float> = 0.6f..0.8f,
        val isMySlap: Boolean = true,
        val myCardsFlipped: Boolean = false,
        val oppCardsFlipped: Boolean = false,
        val currentTurnText: String
    ) : BattleState
    data class Result(
        val myWager: PlayerCard,
        val oppWager: PlayerCard,
        val won: Boolean,
        val prizeText: String
    ) : BattleState
}

data class QuizQuestion(
    val text: String,
    val options: List<String>,
    val correctAnswerIndex: Int,
    val funFact: String
)

sealed interface QuizState {
    object Idle : QuizState
    data class Loading(val countdown: Int = 3) : QuizState
    data class Active(
        val question: QuizQuestion,
        val selectedIndex: Int? = null,
        val isAnswered: Boolean = false,
        val secondsRemaining: Int = 15
    ) : QuizState
    data class Completed(
        val correctCount: Int,
        val rewardCoins: Int
    ) : QuizState
}

data class NearbyUser(
    val nickname: String,
    val distanceMetres: Int,
    val level: Int,
    val status: String // "Online", "Batalhando"
)

data class BattleGroup(
    val id: String,
    val name: String,
    val creatorNickname: String,
    val memberCount: Int,
    val members: List<String>
)

data class FriendInvite(
    val id: String,
    val senderNickname: String,
    val targetNickname: String,
    val status: String, // "PENDING", "ACCEPTED"
    val timestamp: Long = System.currentTimeMillis()
)

data class CoinPack(
    val id: String,          // SKU no Google Play Console (ex: "coin_pack_small")
    val name: String,
    val coinsAmount: Int,
    val priceBrl: String,
    val bonusLabel: String? = null
)

data class PremiumPackProduct(
    val id: String,
    val name: String,
    val description: String,
    val priceBrl: String,
    val packType: String,
    val badge: String? = null
)

sealed class BillingSimulationState {
    data class ChoosePaymentMethod(val pack: CoinPack) : BillingSimulationState()
    data class Processing(val pack: CoinPack, val paymentMethod: String) : BillingSimulationState()
    data class Success(val pack: CoinPack, val coinsGranted: Int) : BillingSimulationState()
    data class ChoosePremiumPackPayment(val product: PremiumPackProduct) : BillingSimulationState()
    data class ProcessingPremiumPack(val product: PremiumPackProduct, val paymentMethod: String) : BillingSimulationState()
    data class PremiumPackSuccess(val product: PremiumPackProduct) : BillingSimulationState()
    data class BuyingElitePass(val priceBrl: String) : BillingSimulationState()
    data class ElitePassSuccess(val bonusCoins: Int) : BillingSimulationState()
}

data class WaitlistGroup(
    val code: String,
    val name: String,
    val type: String, // "Turma", "Trabalho", "Amigos"
    val size: Int,
    val queuePosition: Int
)

class FutViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: FutRepository
    private var liveSimJob: Job? = null
    private var quizTimerJob: Job? = null

    // --- PRE-LAUNCH CAMPANHA COPA 2026 LISTA DE ESPERA ---
    private val prefs = getApplication<Application>().getSharedPreferences("prelaunch_prefs", Context.MODE_PRIVATE)

    private val _waitlistEmail = MutableStateFlow(prefs.getString("waitlist_email", "") ?: "")
    val waitlistEmail: StateFlow<String> = _waitlistEmail.asStateFlow()

    private val _predictedChampion = MutableStateFlow(prefs.getString("predicted_champion", "") ?: "")
    val predictedChampion: StateFlow<String> = _predictedChampion.asStateFlow()

    private val _isWaitlistRegistered = MutableStateFlow(prefs.getBoolean("is_waitlist_registered", false))
    val isWaitlistRegistered: StateFlow<Boolean> = _isWaitlistRegistered.asStateFlow()

    private val _waitlistQueuePosition = MutableStateFlow(prefs.getInt("waitlist_queue_position", 14854))
    val waitlistQueuePosition: StateFlow<Int> = _waitlistQueuePosition.asStateFlow()

    // --- PRE-LAUNCH SPONSOR & GROUPS ---
    private val _waitlistGroupName = MutableStateFlow(prefs.getString("waitlist_group_name", "") ?: "")
    val waitlistGroupName: StateFlow<String> = _waitlistGroupName.asStateFlow()

    private val _waitlistGroupCode = MutableStateFlow(prefs.getString("waitlist_group_code", "") ?: "")
    val waitlistGroupCode: StateFlow<String> = _waitlistGroupCode.asStateFlow()

    private val _waitlistGroupType = MutableStateFlow(prefs.getString("waitlist_group_type", "") ?: "")
    val waitlistGroupType: StateFlow<String> = _waitlistGroupType.asStateFlow()

    private val _waitlistGroupSize = MutableStateFlow(prefs.getInt("waitlist_group_size", 0))
    val waitlistGroupSize: StateFlow<Int> = _waitlistGroupSize.asStateFlow()

    private val _waitlistGroupPosition = MutableStateFlow(prefs.getInt("waitlist_group_position", 0))
    val waitlistGroupPosition: StateFlow<Int> = _waitlistGroupPosition.asStateFlow()

    private val _waitlistGroupMembers = MutableStateFlow<List<String>>(
        prefs.getString("waitlist_group_members", "")?.split(",")?.filter { it.isNotBlank() } ?: emptyList()
    )
    val waitlistGroupMembers: StateFlow<List<String>> = _waitlistGroupMembers.asStateFlow()

    private val _waitlistGroupLeaderboard = MutableStateFlow<List<WaitlistGroup>>(
        listOf(
            WaitlistGroup("CLASH-TRABALHO-DEV", "Devs do Hexa 💻", "Trabalho", 48, 12),
            WaitlistGroup("CLASH-TURMA-EFE", "Educação Física UFRJ ⚽", "Turma", 35, 18),
            WaitlistGroup("CLASH-AMIGOS-PEL", "Pelada de Quarta 🍻", "Amigos", 24, 42),
            WaitlistGroup("CLASH-FAMILIA-SIL", "Família Silva 🏆", "Família", 18, 89)
        )
    )
    val waitlistGroupLeaderboard: StateFlow<List<WaitlistGroup>> = _waitlistGroupLeaderboard.asStateFlow()

    private val _redeemedSponsorCodes = MutableStateFlow<Set<String>>(
        prefs.getStringSet("redeemed_sponsor_codes", emptySet()) ?: emptySet()
    )
    val redeemedSponsorCodes: StateFlow<Set<String>> = _redeemedSponsorCodes.asStateFlow()

    fun boostWaitlistPosition(reduction: Int) {
        viewModelScope.launch {
            val currentPos = _waitlistQueuePosition.value
            val newPos = (currentPos - reduction).coerceAtLeast(112) // Ensure it stops before priority #1
            prefs.edit().putInt("waitlist_queue_position", newPos).apply()
            _waitlistQueuePosition.value = newPos
        }
    }

    fun createWaitlistGroup(name: String, type: String, callback: (Boolean, String) -> Unit) {
        if (name.isBlank()) {
            callback(false, "Insira um nome válido para o seu grupo!")
            return
        }
        val randCode = "CLASH-${type.substring(0, 3).uppercase()}-${Random.nextInt(1000, 9999)}"
        viewModelScope.launch {
            prefs.edit()
                .putString("waitlist_group_name", name)
                .putString("waitlist_group_code", randCode)
                .putString("waitlist_group_type", type)
                .putInt("waitlist_group_size", 1)
                .putInt("waitlist_group_position", 120)
                .putString("waitlist_group_members", "Você")
                .apply()

            _waitlistGroupName.value = name
            _waitlistGroupCode.value = randCode
            _waitlistGroupType.value = type
            _waitlistGroupSize.value = 1
            _waitlistGroupPosition.value = 120
            _waitlistGroupMembers.value = listOf("Você")

            updateGroupLeaderboardWithUser(randCode, name, type, 1, 120)
            boostWaitlistPosition(1000)

            callback(true, "Grupo '$name' criado com sucesso! Compartilhe o ID: $randCode. Você avançou 1.000 posições na fila!")
        }
    }

    fun joinWaitlistGroup(code: String, callback: (Boolean, String) -> Unit) {
        val cleanCode = code.trim().uppercase()
        if (cleanCode.isBlank()) {
            callback(false, "Por favor, insira um ID de grupo!")
            return
        }
        if (cleanCode == _waitlistGroupCode.value) {
            callback(false, "Você já está participando de seu próprio grupo!")
            return
        }

        viewModelScope.launch {
            val matchingDummy = _waitlistGroupLeaderboard.value.find { it.code == cleanCode }
            val groupName = matchingDummy?.name ?: "Turma de Colecionadores"
            val groupType = matchingDummy?.type ?: "Amigos"
            val baseSize = matchingDummy?.size ?: 5
            val newSize = baseSize + 1
            val newPosition = (matchingDummy?.queuePosition ?: 150) - 1

            val mockMembers = when (cleanCode) {
                "CLASH-TRABALHO-DEV", "CLASH-TRA-1234", "CLASH-TRA-9999" -> listOf("Você", "Rodrigo_Dev", "Amanda_Front", "Thiago_M", "Leo_Infra")
                "CLASH-TURMA-EFE", "CLASH-TUR-1234", "CLASH-TUR-9999" -> listOf("Você", "Prof_Marcos", "Leticia_EDF", "Gustavo_Fit")
                "CLASH-AMIGOS-PEL", "CLASH-AMI-1234", "CLASH-AMI-9999" -> listOf("Você", "Ze_Pelada", "Kiko_Goleiro", "Chico_Meio")
                "CLASH-FAMILIA-SIL", "CLASH-FAM-1234", "CLASH-FAM-9999" -> listOf("Você", "Tio_Beto", "Priscila", "Vovô_Chico")
                else -> listOf("Você", "Amigo_Fiel", "Craque_Bairro")
            }

            prefs.edit()
                .putString("waitlist_group_name", groupName)
                .putString("waitlist_group_code", cleanCode)
                .putString("waitlist_group_type", groupType)
                .putInt("waitlist_group_size", newSize)
                .putInt("waitlist_group_position", newPosition)
                .putString("waitlist_group_members", mockMembers.joinToString(","))
                .apply()

            _waitlistGroupName.value = groupName
            _waitlistGroupCode.value = cleanCode
            _waitlistGroupType.value = groupType
            _waitlistGroupSize.value = newSize
            _waitlistGroupPosition.value = newPosition
            _waitlistGroupMembers.value = mockMembers

            updateGroupLeaderboardWithUser(cleanCode, groupName, groupType, newSize, newPosition)
            boostWaitlistPosition(1800)

            callback(true, "Você se juntou ao grupo '$groupName'! Sua equipe tem agora $newSize participantes e você subiu 1.800 posições!")
        }
    }

    fun leaveWaitlistGroup() {
        viewModelScope.launch {
            prefs.edit()
                .remove("waitlist_group_name")
                .remove("waitlist_group_code")
                .remove("waitlist_group_type")
                .remove("waitlist_group_size")
                .remove("waitlist_group_position")
                .remove("waitlist_group_members")
                .apply()

            _waitlistGroupName.value = ""
            _waitlistGroupCode.value = ""
            _waitlistGroupType.value = ""
            _waitlistGroupSize.value = 0
            _waitlistGroupPosition.value = 0
            _waitlistGroupMembers.value = emptyList()

            _waitlistGroupLeaderboard.value = listOf(
                WaitlistGroup("CLASH-TRABALHO-DEV", "Devs do Hexa 💻", "Trabalho", 48, 12),
                WaitlistGroup("CLASH-TURMA-EFE", "Educação Física UFRJ ⚽", "Turma", 35, 18),
                WaitlistGroup("CLASH-AMIGOS-PEL", "Pelada de Quarta 🍻", "Amigos", 24, 42),
                WaitlistGroup("CLASH-FAMILIA-SIL", "Família Silva 🏆", "Família", 18, 89)
            )
        }
    }

    private fun updateGroupLeaderboardWithUser(code: String, name: String, type: String, size: Int, pos: Int) {
        val list = _waitlistGroupLeaderboard.value.toMutableList()
        val index = list.indexOfFirst { it.code == code }
        if (index != -1) {
            list[index] = WaitlistGroup(code, name, type, size, pos)
        } else {
            list.add(WaitlistGroup(code, name, type, size, pos))
        }
        _waitlistGroupLeaderboard.value = list.sortedBy { it.queuePosition }
    }

    fun redeemSponsorCode(code: String, callback: (Boolean, String) -> Unit) {
        val cleanCode = code.trim().uppercase()
        if (cleanCode.isBlank()) {
            callback(false, "Por favor, digite um código de patrocínio!")
            return
        }
        if (_redeemedSponsorCodes.value.contains(cleanCode)) {
            callback(false, "Este código já foi resgatado por você!")
            return
        }

        viewModelScope.launch {
            when (cleanCode) {
                "DAORA-HEXA-2026" -> {
                    boostWaitlistPosition(3500)
                    repository.saveInventoryItem(
                        UserInventory(
                            cardId = 32, // Yamal
                            quantity = 1,
                            isFavorite = true
                        )
                    )
                    addRedeemedCode(cleanCode)
                    callback(true, "Código do Guaraná Daora resgatado! Você pulou 3.500 posições na fila de espera e desbloqueou a Carta Premium de Lamine Yamal no Álbum! 🥤⚽")
                }
                "PIPOPO-CHAMPS" -> {
                    boostWaitlistPosition(2500)
                    repository.saveInventoryItem(
                        UserInventory(
                            cardId = 33, // Mbappé
                            quantity = 1,
                            isFavorite = false
                        )
                    )
                    addRedeemedCode(cleanCode)
                    callback(true, "Código de Pipopo CrocChamps resgatado! Você pulou 2.500 posições e faturou a Carta Especial de Mbappé! 🍿⚡")
                }
                "BATER-BAFO-LOCAL" -> {
                    boostWaitlistPosition(1500)
                    addRedeemedCode(cleanCode)
                    callback(true, "Cupom do Comércio de Bairro resgatado! Você pulou 1.500 posições na fila! 🏪💨")
                }
                "COPA2026", "CARDCLASH" -> {
                    boostWaitlistPosition(1000)
                    addRedeemedCode(cleanCode)
                    callback(true, "Parceiro de Snack resgatado! Esse cupom oficial concedeu 1.000 posições de bônus! ⚽🍟")
                }
                else -> {
                    callback(
                        false,
                        "Código inválido! Os códigos físicos de bebidas ficam sob a tampa das garrafas de Guaraná Daora e os de Snacks ficam no verso do pacote CrocChamps Pipopo!"
                    )
                }
            }
        }
    }

    private fun addRedeemedCode(code: String) {
        val newSet = _redeemedSponsorCodes.value.toMutableSet()
        newSet.add(code)
        prefs.edit().putStringSet("redeemed_sponsor_codes", newSet).apply()
        _redeemedSponsorCodes.value = newSet
    }

    fun registerInWaitlist(email: String, prediction: String, callback: (Boolean, String) -> Unit) {
        if (email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            callback(false, "Por favor, insira um e-mail válido para a lista de espera!")
            return
        }
        if (prediction.isBlank()) {
            callback(false, "Por favor, escolha uma equipe como campeã da Copa!")
            return
        }

        viewModelScope.launch {
            prefs.edit()
                .putString("waitlist_email", email)
                .putString("predicted_champion", prediction)
                .putBoolean("is_waitlist_registered", true)
                .apply()

            _waitlistEmail.value = email
            _predictedChampion.value = prediction
            _isWaitlistRegistered.value = true

            // Current simulated UTC time is June 5, 2026, which is BEFORE June 11, 2026.
            // Check programmatically:
            val cutoff = 1781136000000L // 11 June 2026 UTC
            val currentTime = System.currentTimeMillis()
            if (currentTime < cutoff) {
                // Let's grant the user the exclusive Card de Fundador "Origem" (ID 30) directly!
                repository.saveInventoryItem(
                    UserInventory(
                        cardId = 30,
                        quantity = 1,
                        isFavorite = true,
                        inBattleDeck = true
                    )
                )
                callback(true, "Parabéns! Inscrição confirmada na lista de espera! Você garantiu e resgatou o raro Card Especial de Fundador 'Origem' (#30) no seu Álbum!")
            } else {
                callback(true, "Inscrito com sucesso na lista de espera! Você palpitou no $prediction. Seu Card de Fundador 'Origem' infelizmente expirou (período acabou dia 11/06/2026).")
            }
        }
    }

    // --- MONETIZATION STATE FLOWS ---
    private val _hasElitePass = MutableStateFlow(false)
    val hasElitePass: StateFlow<Boolean> = _hasElitePass.asStateFlow()

    private val _isSimulatingAd = MutableStateFlow(false)
    val isSimulatingAd: StateFlow<Boolean> = _isSimulatingAd.asStateFlow()

    private val _adCountdown = MutableStateFlow(0)
    val adCountdown: StateFlow<Int> = _adCountdown.asStateFlow()

    private val _billingState = MutableStateFlow<BillingSimulationState?>(null)
    val billingState: StateFlow<BillingSimulationState?> = _billingState.asStateFlow()

    val availableCoinPacks = listOf(
        CoinPack("coin_pack_small", "Pilha de Moedas", 2500, "R$ 4,90"),
        CoinPack("coin_pack_medium", "Saco de Moedas", 10000, "R$ 14,90", "+10% BÔNUS"),
        CoinPack("coin_pack_large", "Baú de Moedas", 35000, "R$ 39,90", "+25% BÔNUS"),
        CoinPack("coin_pack_vault", "Cofre de Moedas", 100000, "R$ 99,90", "+40% BÔNUS")
    )

    val availablePremiumPacks = listOf(
        PremiumPackProduct("prem_pack_rare", "Pacote Ouro Real", "Garante 4 cards com ótimas chances de Ouro, Especial e Lendária, sem moedas!", "R$ 4,90", "OURO", "🔥 POPULAR"),
        PremiumPackProduct("prem_pack_special", "Pacote Elite Seleção", "Contém 4 cards com chances aumentadas de craques da Seleção e Especiais!", "R$ 7,90", "PREMIUM", "✨ BRASIL"),
        PremiumPackProduct("prem_pack_legendary", "Megapacote Lendas Reais", "Garante 5 cards com pelo menos 1 Lendária, Assinada ou Animada de Elite de valor máximo!", "R$ 11,90", "LENDARIO", "👑 SUPREMO")
    )

    // Override for customized Bafo opponents via invites/nearby
    var customOpponentName: String? = null

    // Location / Near Match status
    private val _isGpsPermitted = MutableStateFlow(false)
    val isGpsPermitted: StateFlow<Boolean> = _isGpsPermitted.asStateFlow()

    private val _isSearchingNearby = MutableStateFlow(false)
    val isSearchingNearby: StateFlow<Boolean> = _isSearchingNearby.asStateFlow()

    private val _nearbyPlayers = MutableStateFlow<List<NearbyUser>>(emptyList())
    val nearbyPlayers: StateFlow<List<NearbyUser>> = _nearbyPlayers.asStateFlow()

    // Groups State
    private val _battleGroupsList = MutableStateFlow<List<BattleGroup>>(
        listOf(
            BattleGroup("g1", "Copa Bafo Pinheiros 🏆", "Astro_Figurinha", 3, listOf("Astro_Figurinha", "Bruninho9", "Fla_King")),
            BattleGroup("g2", "Mesa Vila Belmiro Clash", "SantosFera", 2, listOf("SantosFera", "Galo_Doido"))
        )
    )
    val battleGroupsList: StateFlow<List<BattleGroup>> = _battleGroupsList.asStateFlow()

    private val _activeGroup = MutableStateFlow<BattleGroup?>(null)
    val activeGroup: StateFlow<BattleGroup?> = _activeGroup.asStateFlow()

    // Friends & Invites
    private val _friendsListFlow = MutableStateFlow<List<String>>(
        listOf("Coutinho99", "GamerCarioca", "MarcosGamer")
    )
    val friendsListFlow: StateFlow<List<String>> = _friendsListFlow.asStateFlow()

    private val _duelInvitesFlow = MutableStateFlow<List<FriendInvite>>(
        listOf(
            FriendInvite("i1", "Coutinho99", "Você", "PENDING")
        )
    )
    val duelInvitesFlow: StateFlow<List<FriendInvite>> = _duelInvitesFlow.asStateFlow()

    val userProfile: StateFlow<UserProfile?>
    val inventory: StateFlow<List<UserInventory>>
    val battleLogs: StateFlow<List<BattleLog>>
    val tradeOffers: StateFlow<List<TradeOffer>>
    val liveMatches: StateFlow<List<LiveMatch>>

    // UI state
    private val _selectedTab = MutableStateFlow(AppTab.PROFILE)
    val selectedTab: StateFlow<AppTab> = _selectedTab.asStateFlow()

    // Packs sequence
    private val _openingPackResult = MutableStateFlow<List<PlayerCard>?>(null)
    val openingPackResult: StateFlow<List<PlayerCard>?> = _openingPackResult.asStateFlow()

    private val _isPackOpeningAnimActive = MutableStateFlow(false)
    val isPackOpeningAnimActive: StateFlow<Boolean> = _isPackOpeningAnimActive.asStateFlow()

    // Interactive custom game elements
    private val _battleState = MutableStateFlow<BattleState>(BattleState.Idle)
    val battleState: StateFlow<BattleState> = _battleState.asStateFlow()

    private val _quizState = MutableStateFlow<QuizState>(QuizState.Idle)
    val quizState: StateFlow<QuizState> = _quizState.asStateFlow()

    // Goal alerting
    private val _goalNotification = MutableSharedFlow<Pair<String, String>>(extraBufferCapacity = 1)
    val goalNotification: SharedFlow<Pair<String, String>> = _goalNotification.asSharedFlow()

    // Filter selectors in Collection
    val filterRarity = MutableStateFlow<Rarity?>(null)
    val filterPosition = MutableStateFlow<Position?>(null)
    val filterOnlyOwned = MutableStateFlow(false)

    init {
        val database = AppDatabase.getDatabase(application)
        repository = FutRepository(database.futDao())

        userProfile = repository.userProfile.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

        inventory = repository.inventory.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        battleLogs = repository.battleLogs.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        tradeOffers = repository.tradeOffers.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        liveMatches = repository.liveMatches.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        // Seed and start match ticker
        viewModelScope.launch {
            repository.seedLiveMatchesIfEmpty()
            repository.seedMockOffersIfEmpty()
            startMatchSimulator()
        }
    }

    fun selectTab(tab: AppTab) {
        _selectedTab.value = tab
    }

    // --- USER PROFILE CREATION ---
    fun registerUser(name: String, favoriteTeam: String, age: Int, provider: String) {
        viewModelScope.launch {
            val profile = UserProfile(
                name = name,
                favoriteTeam = favoriteTeam,
                age = age,
                coins = 1200, // Starting bonus
                xp = 0,
                level = 1,
                loginProvider = provider
            )
            repository.saveProfile(profile)
            _selectedTab.value = AppTab.COLECAO
        }
    }

    // --- DIRECT ACTIONS ---
    fun addManualCoins(amount: Int) {
        viewModelScope.launch {
            repository.addCoins(amount)
        }
    }

    fun startBillingSimulation(pack: CoinPack) {
        _billingState.value = BillingSimulationState.ChoosePaymentMethod(pack)
    }

    fun startElitePassBillingSimulation() {
        _billingState.value = BillingSimulationState.BuyingElitePass("R$ 19,90")
    }

    fun cancelBillingSimulation() {
        _billingState.value = null
    }

    fun startPremiumPackBillingSimulation(product: PremiumPackProduct) {
        _billingState.value = BillingSimulationState.ChoosePremiumPackPayment(product)
    }

    fun selectPremiumPackPaymentAndProcess(product: PremiumPackProduct, method: String) {
        _billingState.value = BillingSimulationState.ProcessingPremiumPack(product, method)
        viewModelScope.launch {
            delay(1500)
            _billingState.value = BillingSimulationState.PremiumPackSuccess(product)
        }
    }

    fun completePremiumPackPurchaseAndOpen(product: PremiumPackProduct) {
        _billingState.value = null
        buyPack(product.packType, 0) // Triggers unboxing animation with cost = 0 (free of coin deduct!)
    }

    fun selectPaymentAndProcess(pack: CoinPack, method: String) {
        _billingState.value = BillingSimulationState.Processing(pack, method)
        viewModelScope.launch {
            delay(1500) // Simulate processing time with animation
            repository.addCoins(pack.coinsAmount)
            _billingState.value = BillingSimulationState.Success(pack, pack.coinsAmount)
        }
    }

    fun processElitePassPurchase(method: String) {
        _billingState.value = BillingSimulationState.Processing(
            CoinPack("elite_pass", "Passe Elite", 0, "R$ 19,90"),
            method
        )
        viewModelScope.launch {
            delay(1500) // Simulate processing time with animation
            _hasElitePass.value = true
            repository.addCoins(5000) // Give substantial starting coins as bonus
            repository.addElitePassBonusCard(2) // Give Ronaldinho (id = 2) as a protected Premium card!
            _billingState.value = BillingSimulationState.ElitePassSuccess(5000)
        }
    }

    fun playSimulatedAd() {
        if (_isSimulatingAd.value) return
        _isSimulatingAd.value = true
        _adCountdown.value = 5 // Playful 5 seconds ad simulation instead of long 30s
        viewModelScope.launch {
            while (_adCountdown.value > 0) {
                delay(1000)
                _adCountdown.value = _adCountdown.value - 1
            }
            // Finished! Award the user
            repository.addCoins(150)
            _isSimulatingAd.value = false
            _goalNotification.emit(Pair("Moedas de Recompensa! 🎥", "Você assistiu ao anúncio premiado e recebeu +150 moedas!"))
        }
    }

    fun upgradePlayerCard(cardId: Int, callback: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val profile = repository.userProfile.firstOrNull() ?: return@launch
            val cost = 500
            if (profile.coins < cost) {
                callback(false, "Moedas insuficientes! É necessário $cost moedas para evoluir um card.")
                return@launch
            }
            val currentUnit = repository.getInventoryItem(cardId) ?: return@launch
            if (currentUnit.quantity <= 0) {
                callback(false, "Você não possui este card!")
                return@launch
            }
            val nextLevel = currentUnit.upgradeLevel + 1
            if (nextLevel > 3) {
                callback(false, "Este card já atingiu o nível máximo de Evolução!")
                return@launch
            }
            // Deduct coins and update inventory
            repository.addCoins(-cost)
            val updatedItem = currentUnit.copy(upgradeLevel = nextLevel)
            repository.saveInventoryItem(updatedItem)
            repository.addXp(50)
            callback(true, "Sucesso! Card evoluído para Nível $nextLevel! Atributos e bônus aumentados!")
        }
    }

    fun customizePlayerCard(cardId: Int, customName: String?, customPhotoUrl: String?, customClubAndCountry: String?, callback: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val currentUnit = repository.getInventoryItem(cardId) ?: return@launch
            if (currentUnit.quantity <= 0) {
                callback(false, "Você não possui este card!")
                return@launch
            }
            val updatedItem = currentUnit.copy(
                customName = if (customName.isNullOrBlank()) null else customName,
                customPhotoUrl = if (customPhotoUrl.isNullOrBlank()) null else customPhotoUrl,
                customClubAndCountry = if (customClubAndCountry.isNullOrBlank()) null else customClubAndCountry
            )
            repository.saveInventoryItem(updatedItem)
            callback(true, "Card personalizado com sucesso!")
        }
    }

    fun updateCardSticker(cardId: Int, stickerEmoji: String?, callback: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val currentUnit = repository.getInventoryItem(cardId) ?: return@launch
            if (currentUnit.quantity <= 0) {
                callback(false, "Você não possui este card!")
                return@launch
            }
            val updatedItem = currentUnit.copy(
                stickerEmoji = stickerEmoji
            )
            repository.saveInventoryItem(updatedItem)
            callback(true, if (stickerEmoji != null) "Adesivo '$stickerEmoji' colado com sucesso!" else "Adesivo removido!")
        }
    }

    fun autoFillPlayerCardFromIA(cardId: Int, playerName: String, callback: (Boolean, String, com.example.data.api.PlayerUpdateInfo?) -> Unit) {
        viewModelScope.launch {
            val currentUnit = repository.getInventoryItem(cardId) ?: return@launch
            if (currentUnit.quantity <= 0) {
                callback(false, "Você não possui este card!", null)
                return@launch
            }
            try {
                val info = com.example.data.api.GeminiApiClient.fetchPlayerUpdates(playerName)
                if (info != null) {
                    callback(true, "Dados do jogador '$playerName' carregados com Inteligência Artificial!", info)
                } else {
                    callback(false, "Falha ao obter dados automáticos do jogador '$playerName'. Usando fallback de teste local de IA ou verifique sua API Key.", null)
                }
            } catch (e: Exception) {
                callback(false, "Erro ao carregar dados por IA: ${e.message}", null)
            }
        }
    }

    fun claimDailyReward(onSuccess: (Int) -> Unit, onError: () -> Unit) {
        viewModelScope.launch {
            val prize = repository.claimDailyStreakReward()
            if (prize != null) {
                onSuccess(prize)
            } else {
                onError()
            }
        }
    }

    // --- PACK OPENING & UNBOXING STAGE ---
    fun buyPack(packType: String, cost: Int) {
        viewModelScope.launch {
            _isPackOpeningAnimActive.value = true
            _openingPackResult.value = null
            delay(1500) // Beautiful pack shaking sound/visual lag
            val cards = repository.openPack(packType, cost)
            _openingPackResult.value = cards
        }
    }

    fun dismissPackOpening() {
        _openingPackResult.value = null
        _isPackOpeningAnimActive.value = false
    }

    // Toggle deck status for battles
    fun toggleDeckStatus(cardId: Int) {
        viewModelScope.launch {
            repository.toggleBattleDeck(cardId)
        }
    }

    // --- TRADING HUB ---
    fun acceptTrade(offer: TradeOffer, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val ok = repository.executeTradeOffer(offer)
            onResult(ok)
        }
    }

    fun createTradeOffer(offerCardId: Int, requestCardId: Int, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val ok = repository.postTradeOffer(offerCardId, requestCardId)
            onResult(ok)
        }
    }

    fun cancelTrade(offerId: String) {
        viewModelScope.launch {
            repository.cancelTradeOffer(offerId)
        }
    }

    // --- BAFO BATTLE MODULE (Habilidade) ---
    fun enterBattleLobby() {
        viewModelScope.launch {
            val inv = inventory.value
            val readyCards = inv.filter { it.quantity > 0 && it.inBattleDeck }.mapNotNull {
                CardCatalog.getCardById(it.cardId)
            }
            _battleState.value = BattleState.ChooseWager(readyCards)
        }
    }

    fun selectBattleWager(myCard: PlayerCard, opponentTierMax: Int) {
        viewModelScope.launch {
            // Find a suitable rival card from Catalog based on tier
            // (Lower tier matches common opponent, Ouro matches elite opponent)
            val pool = CardCatalog.cards.filter {
                if (opponentTierMax >= 85) it.overall >= 82 else it.overall <= 83
            }
            val opponentCard = pool.randomOrNull() ?: CardCatalog.cards.random()

            val names = listOf("Bruninho9", "Carioca_Furia", "Tricolor_Gamer", "Palestra_1914", "Fla_King", "Galo_Doido")
            val opponentName = customOpponentName ?: names.random()
            customOpponentName = null

            _battleState.value = BattleState.ActivePlay(
                myWager = myCard,
                oppWager = opponentCard,
                oppName = opponentName,
                isMySlap = true,
                currentTurnText = "Sua vez! Encha o medidor com batidinhas de concha (Toque Rápido) e solte no alvo!"
            )
        }
    }

    fun updateSlapPower(delta: Float) {
        val current = _battleState.value
        if (current is BattleState.ActivePlay && current.isMySlap) {
            val newVal = (current.currentPower + delta).coerceIn(0f, 1f)
            _battleState.value = current.copy(currentPower = newVal)
        }
    }

    // User executes the slap! Based purely on hitting the target zone on power bar
    fun executeSlap() {
        val state = _battleState.value as? BattleState.ActivePlay ?: return
        if (!state.isMySlap) return

        // Set isMySlap = false immediately to reject consecutive instant calls
        _battleState.value = state.copy(isMySlap = false)

        viewModelScope.launch {
            // Check if power landed in sweet spot
            val hitOptimal = state.currentPower in state.optimalTriggerZone
            val flipSuccess = hitOptimal || (Random.nextFloat() < (state.currentPower * 0.4f)) // Skill gives higher likelihood

            _battleState.value = state.copy(
                isMySlap = false,
                myCardsFlipped = flipSuccess,
                currentTurnText = if (flipSuccess) "BOMBASTICO! Você virou o card!" else "Quase! O vento levantou mas o card não virou..."
            )

            delay(2000)

            // Play Opponent's turn
            val currentStateAfterMyTurn = _battleState.value as? BattleState.ActivePlay ?: return@launch
            _battleState.value = currentStateAfterMyTurn.copy(
                isMySlap = false,
                currentPower = 0f,
                currentTurnText = "Vez de ${state.oppName}... Ele está preparando a batida!"
            )

            delay(2500)

            // Opponent outcome
            val currentStateBeforeOppTurn = _battleState.value as? BattleState.ActivePlay ?: return@launch
            val oppHit = Random.nextFloat() > 0.4f // 60% standard win rate
            _battleState.value = currentStateBeforeOppTurn.copy(
                oppCardsFlipped = oppHit,
                currentTurnText = if (oppHit) "${state.oppName} conseguiu virar!" else "${state.oppName} errou a força da batida!"
            )

            delay(2000)

            // Evaluate battle winner
            // If user virou and opponent didn't, user wins. Ties solved via final reflex power.
            val won = if (flipSuccess && !oppHit) {
                true
            } else if (!flipSuccess && oppHit) {
                false
            } else {
                // TIE BREAKER: based on user's final power closeness to 0.75
                Math.abs(state.currentPower - 0.7f) < 0.2f
            }

            // Save transactionally in repository
            repository.completeBattle(
                wageredCardId = state.myWager.id,
                opponentCardId = state.oppWager.id,
                won = won,
                opponentName = state.oppName
            )

            if (_hasElitePass.value) {
                // Double XP and Coins rewards for Elite Pass holders!
                if (won) {
                    repository.addXp(40)
                    repository.addCoins(100)
                } else {
                    repository.addXp(10)
                }
                _goalNotification.emit(Pair("Bônus Passe Elite! 👑", "Seus prêmios da partida foram DOBRADOS!"))
            }

            val finalPrizeText = if (won) {
                if (_hasElitePass.value) {
                    "Você ganhou o card: ${state.oppWager.name} + 200 Moedas (2x Passe Elite)"
                } else {
                    "Você ganhou o card: ${state.oppWager.name} + 100 Moedas"
                }
            } else {
                if (_hasElitePass.value) {
                    "Você perdeu o card: ${state.myWager.name} (+20 XP Passe Elite)"
                } else {
                    "Você perdeu o card: ${state.myWager.name}"
                }
            }

            _battleState.value = BattleState.Result(
                myWager = state.myWager,
                oppWager = state.oppWager,
                won = won,
                prizeText = finalPrizeText
            )
        }
    }

    fun endBattle() {
        _battleState.value = BattleState.Idle
    }

    // --- MINI-GAMES STATE ENGINE (QUIZ) ---
    private val quizPool = listOf(
        QuizQuestion(
            text = "Quem é o maior artilheiro da história das Copas do Mundo pelo Brasil?",
            options = listOf("Neymar Jr", "Pelé", "Ronaldo Fenômeno", "Romário"),
            correctAnswerIndex = 2,
            funFact = "Ronaldo Fenômeno marcou 15 gols em Copas do Mundo, sendo superado apenas pelo alemão Miroslav Klose!"
        ),
        QuizQuestion(
            text = "Qual time tem mais títulos de Copa Libertadores no Brasil?",
            options = listOf("Flamengo / Palmeiras / São Paulo / Grêmio / Santos", "Corinthians", "Cruzeiro", "Atlético-MG"),
            correctAnswerIndex = 0,
            funFact = "Cinco clubes brasileiros lideram a lista nacional de maiores campeões com 3 taças cada um!"
        ),
        QuizQuestion(
            text = "Qual jovem craque brasileiro revelado pelo Palmeiras foi vendido recentemente ao Real Madrid?",
            options = listOf("Estêvão", "Endrick", "Vini Jr", "Rodrygo"),
            correctAnswerIndex = 1,
            funFact = "Endrick se juntou aos 'Galácticos' do Real Madrid assim que completou 18 anos, repetindo o sucesso de Neymar e Vinicius Jr."
        ),
        QuizQuestion(
            text = "Qual clássico opõe as maiores equipes do futebol de Porto Alegre?",
            options = listOf("Fla-Flu", "Grenal", "Dérbi Paulista", "San-São"),
            correctAnswerIndex = 1,
            funFact = "O clássico Grenal entre Grêmio e Internacional é um dos confrontos de maior rivalidade do futebol sul-americano!"
        )
    )

    private var quizCorrectCount = 0
    private var quizCurrentQuestionIndex = 0

    fun startQuiz() {
        viewModelScope.launch {
            _quizState.value = QuizState.Loading()
            quizCorrectCount = 0
            quizCurrentQuestionIndex = 0
            repeat(3) { i ->
                delay(1000)
                _quizState.value = QuizState.Loading(2 - i)
            }
            nextQuizQuestion()
        }
    }

    private fun nextQuizQuestion() {
        if (quizCurrentQuestionIndex >= quizPool.size) {
            // Completed! Give coins
            val totalCoinsWon = quizCorrectCount * 50
            _quizState.value = QuizState.Completed(quizCorrectCount, totalCoinsWon)
            viewModelScope.launch {
                repository.addCoins(totalCoinsWon)
                repository.addXp(30)
            }
            return
        }

        val question = quizPool[quizCurrentQuestionIndex]
        _quizState.value = QuizState.Active(
            question = question,
            selectedIndex = null,
            isAnswered = false,
            secondsRemaining = 15
        )

        // Spawn interactive countdown loop
        quizTimerJob?.cancel()
        quizTimerJob = viewModelScope.launch {
            var left = 15
            while (left > 0) {
                delay(1000)
                left--
                val curr = _quizState.value
                if (curr is QuizState.Active && !curr.isAnswered) {
                    _quizState.value = curr.copy(secondsRemaining = left)
                } else {
                    break
                }
            }
            // Timeout behavior
            val finalState = _quizState.value
            if (finalState is QuizState.Active && !finalState.isAnswered) {
                submitQuizAnswer(-1) // Incorrect by timeout
            }
        }
    }

    fun answerQuizQuestion(selectedIndex: Int) {
        val curr = _quizState.value as? QuizState.Active ?: return
        if (curr.isAnswered) return
        _quizState.value = curr.copy(selectedIndex = selectedIndex, isAnswered = true)
    }

    fun proceedFromAnswer(correctCount: Int, currentQuestionIndex: Int) {
        val curr = _quizState.value as? QuizState.Active ?: return
        val wasCorrect = curr.selectedIndex == curr.question.correctAnswerIndex
        if (wasCorrect) {
            quizCorrectCount++
        }
        quizCurrentQuestionIndex++
        nextQuizQuestion()
    }

    private fun submitQuizAnswer(ansIdx: Int) {
        val curr = _quizState.value as? QuizState.Active ?: return
        _quizState.value = curr.copy(selectedIndex = ansIdx, isAnswered = true)
    }

    fun finishQuiz() {
        _quizState.value = QuizState.Idle
    }

    // --- REALTIME MATCH ENGINE METRIC SIMULATOR ---
    private fun startMatchSimulator() {
        liveSimJob?.cancel()
        liveSimJob = viewModelScope.launch {
            while (true) {
                delay(9000) // Simulates events every 9 seconds inside the platform
                val notif = repository.simulateLiveTick()
                if (notif != null) {
                    val isPlayingBafo = _selectedTab.value == AppTab.BAFO || _battleState.value != BattleState.Idle
                    if (!isPlayingBafo) {
                        _goalNotification.emit(notif)
                    }
                }
            }
        }
    }

    // --- GPS LOCATION RADAR, BATTLE GROUPS & FRIENDS SYSTEM ---
    fun grantGpsPermission(granted: Boolean) {
        _isGpsPermitted.value = granted
        if (granted) {
            searchNearbyPlayers()
        } else {
            _nearbyPlayers.value = emptyList()
        }
    }

    fun searchNearbyPlayers() {
        if (!_isGpsPermitted.value) return
        viewModelScope.launch {
            _isSearchingNearby.value = true
            _nearbyPlayers.value = emptyList()
            delay(1500)
            _nearbyPlayers.value = listOf(
                NearbyUser("Lukinha_Fla", 45, 8, "Online"),
                NearbyUser("SantosFera", 110, 5, "Online"),
                NearbyUser("Vascaino_Bafo", 290, 7, "Online"),
                NearbyUser("CorinthianoGamer", 410, 10, "Batalhando")
            )
            _isSearchingNearby.value = false
        }
    }

    fun createNewGroup(groupName: String) {
        val crNickname = userProfile.value?.name ?: "Jogador"
        val newGrp = BattleGroup(
            id = "g_" + Random.nextInt(10000),
            name = groupName,
            creatorNickname = crNickname,
            memberCount = 1,
            members = listOf(crNickname)
        )
        _battleGroupsList.value = _battleGroupsList.value + newGrp
        _activeGroup.value = newGrp
    }

    fun joinGroup(group: BattleGroup) {
        val myName = userProfile.value?.name ?: "Jogador"
        if (group.members.contains(myName)) {
            _activeGroup.value = group
            return
        }
        val updatedMembers = group.members + myName
        val updatedGroup = group.copy(
            members = updatedMembers,
            memberCount = updatedMembers.size
        )
        _battleGroupsList.value = _battleGroupsList.value.map {
            if (it.id == group.id) updatedGroup else it
        }
        _activeGroup.value = updatedGroup
    }

    fun leaveGroup() {
        val current = _activeGroup.value ?: return
        val myName = userProfile.value?.name ?: "Jogador"
        val updatedMembers = current.members.filter { it != myName }
        if (updatedMembers.isEmpty()) {
            _battleGroupsList.value = _battleGroupsList.value.filter { it.id != current.id }
        } else {
            val updatedGroup = current.copy(
                members = updatedMembers,
                memberCount = updatedMembers.size
            )
            _battleGroupsList.value = _battleGroupsList.value.map {
                if (it.id == current.id) updatedGroup else it
            }
        }
        _activeGroup.value = null
    }

    fun sendFriendInvite(nickname: String) {
        if (nickname.isBlank()) return
        val currentInvites = _duelInvitesFlow.value
        if (currentInvites.any { it.targetNickname.equals(nickname, ignoreCase = true) }) return // already sent
        
        val newInvite = FriendInvite(
            id = "i_" + Random.nextInt(10000),
            senderNickname = "Você",
            targetNickname = nickname,
            status = "PENDING"
        )
        _duelInvitesFlow.value = _duelInvitesFlow.value + newInvite

        // Simulation: after 3 seconds, friend accepts the duel, and sends a notification!
        viewModelScope.launch {
            delay(3500)
            // update invite status to "ACCEPTED"
            _duelInvitesFlow.value = _duelInvitesFlow.value.map {
                if (it.id == newInvite.id) it.copy(status = "ACCEPTED") else it
            }
            _goalNotification.emit(
                Pair(
                    "CONVITE ACEITO! 👋💥",
                    "$nickname aceitou seu convite! Vá para o Lobby de duelo pendente."
                )
            )
        }
    }

    fun acceptDuelInvite(invite: FriendInvite) {
        // Set opponent and go directly into choose wager
        customOpponentName = invite.senderNickname
        _duelInvitesFlow.value = _duelInvitesFlow.value.filter { it.id != invite.id }
        enterBattleLobby()
    }

    fun declineDuelInvite(invite: FriendInvite) {
        _duelInvitesFlow.value = _duelInvitesFlow.value.filter { it.id != invite.id }
    }

    fun simulateIncomingInvite() {
        val possibleSenders = listOf("Estêvão_Fan", "Depay_Class", "NeymarJr_Real", "GansoOitavo", "Gabigol_Cru")
        val randomName = possibleSenders.random()
        val newInvite = FriendInvite(
            id = "i_" + Random.nextInt(10000),
            senderNickname = randomName,
            targetNickname = "Você",
            status = "PENDING"
        )
        _duelInvitesFlow.value = _duelInvitesFlow.value + newInvite

        viewModelScope.launch {
            _goalNotification.emit(
                Pair("Desafio Recebido! 💥", "$randomName convidou você para jogar Bafo!")
            )
        }
    }

    fun fightAgainstName(opponentName: String) {
        customOpponentName = opponentName
        enterBattleLobby()
    }

    override fun onCleared() {
        super.onCleared()
        liveSimJob?.cancel()
        quizTimerJob?.cancel()
    }
}
