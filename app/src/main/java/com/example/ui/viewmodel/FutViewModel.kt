package com.example.ui.viewmodel

import android.app.Application
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

class FutViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: FutRepository
    private var liveSimJob: Job? = null
    private var quizTimerJob: Job? = null

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

            _battleState.value = BattleState.Result(
                myWager = state.myWager,
                oppWager = state.oppWager,
                won = won,
                prizeText = if (won) "Você ganhou o card: ${state.oppWager.name} + 100 Moedas" else "Você perdeu o card: ${state.myWager.name}"
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
                    _goalNotification.emit(notif)
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
