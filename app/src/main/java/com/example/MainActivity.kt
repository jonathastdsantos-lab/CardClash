package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.*
import com.example.ui.components.InteractiveTutorialBottomSheet
import com.example.ui.components.PreLaunchLandingDialog
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: FutViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                val profile by viewModel.userProfile.collectAsStateWithLifecycle()
                
                if (profile == null) {
                    LoginScreen(
                        viewModel = viewModel,
                        onRegisterSuccess = { name, team, age, provider ->
                            viewModel.registerUser(name, team, age, provider)
                        }
                    )
                } else {
                    DashboardContent(viewModel, profile!!)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardContent(viewModel: FutViewModel, profile: UserProfile) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var showTutorialSheet by remember { mutableStateOf(false) }
    var showPreLaunchCampaignDialog by remember { mutableStateOf(false) }

    // Auto-trigger tutorial on first registration (Level 1 and exactly 1200 starting coins)
    LaunchedEffect(profile.id) {
        if (profile.level == 1 && profile.coins == 1200) {
            showTutorialSheet = true
        }
    }

    // ViewModel subscriptions
    val activeTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val inventory by viewModel.inventory.collectAsStateWithLifecycle()
    val tradeOffers by viewModel.tradeOffers.collectAsStateWithLifecycle()
    val liveMatches by viewModel.liveMatches.collectAsStateWithLifecycle()
    val battleState by viewModel.battleState.collectAsStateWithLifecycle()
    val quizState by viewModel.quizState.collectAsStateWithLifecycle()

    // GPS & Invites subscriptions
    val isGpsPermitted by viewModel.isGpsPermitted.collectAsStateWithLifecycle()
    val isSearchingNearby by viewModel.isSearchingNearby.collectAsStateWithLifecycle()
    val nearbyPlayers by viewModel.nearbyPlayers.collectAsStateWithLifecycle()
    val battleGroupsList by viewModel.battleGroupsList.collectAsStateWithLifecycle()
    val activeGroup by viewModel.activeGroup.collectAsStateWithLifecycle()
    val friendsList by viewModel.friendsListFlow.collectAsStateWithLifecycle()
    val duelInvites by viewModel.duelInvitesFlow.collectAsStateWithLifecycle()

    val isPackOpening by viewModel.isPackOpeningAnimActive.collectAsStateWithLifecycle()
    val openedCards by viewModel.openingPackResult.collectAsStateWithLifecycle()

    val billingState by viewModel.billingState.collectAsStateWithLifecycle()
    val hasElitePass by viewModel.hasElitePass.collectAsStateWithLifecycle()
    val isSimulatingAd by viewModel.isSimulatingAd.collectAsStateWithLifecycle()
    val adCountdown by viewModel.adCountdown.collectAsStateWithLifecycle()

    // Host to trigger visual goal snackbar alerts
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.goalNotification.collectLatest { pair ->
            val (title, text) = pair
            snackbarHostState.showSnackbar(
                message = "$title\n$text",
                duration = SnackbarDuration.Short
            )
        }
    }

    // Intercept Back presses inside sub-tabs to switch back to Album (COLECAO) before exiting the app
    if (activeTab != AppTab.COLECAO) {
        BackHandler {
            viewModel.selectTab(AppTab.COLECAO)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = StadiumObsidian,
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(16.dp),
                snackbar = { data ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = StadiumGlow),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, NeonEmerald, shape = RoundedCornerShape(12.dp)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(15.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(text = "🚨", fontSize = 24.sp)
                            Column {
                                Text(
                                    text = data.visuals.message.split("\n").getOrNull(0) ?: "Notificação",
                                    color = NeonEmerald,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = data.visuals.message.split("\n").getOrNull(1) ?: "",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    lineHeight = 14.sp
                                )
                            }
                        }
                    }
                }
            )
        },
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = StadiumConcrete),
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(end = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // User Badge info with Avatar - Immersive UI
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(Color(0xFF3F4935), shape = CircleShape)
                                    .border(1.dp, NeonEmerald.copy(alpha = 0.2f), shape = CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .background(
                                            brush = Brush.sweepGradient(
                                                colors = listOf(NeonEmerald, Color(0xFF386924))
                                            ),
                                            shape = CircleShape
                                        )
                                )
                            }
                            Column {
                                Text(
                                    text = "NÍVEL ${profile.level}".uppercase(),
                                    color = NeonEmerald,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = profile.name,
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        // Left/middle help chip to launch Interactive Tutorial Onboarding
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier
                                .background(NeonEmerald.copy(alpha = 0.12f), shape = RoundedCornerShape(12.dp))
                                .border(1.dp, NeonEmerald.copy(alpha = 0.35f), shape = RoundedCornerShape(12.dp))
                                .clickable {
                                    showTutorialSheet = true
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                .testTag("tutorial_launcher_chip")
                        ) {
                            Text(
                                text = "🎮 Como Jogar",
                                color = NeonEmerald,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Coins Balance with custom $ pill
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .background(StadiumConcrete, shape = RoundedCornerShape(16.dp))
                                .border(1.dp, StadiumBorder, shape = RoundedCornerShape(16.dp))
                                .clickable {
                                    viewModel.addManualCoins(500)
                                    Toast.makeText(context, "+500 Moedas creditadas!", Toast.LENGTH_SHORT).show()
                                }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                .testTag("quick_coins_wallet")
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .background(Color(0xFFFDB931), shape = CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "$",
                                    color = Color.Black,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                            Text(
                                text = "${profile.coins}",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = StadiumConcrete,
                tonalElevation = 6.dp,
                windowInsets = WindowInsets.navigationBars
            ) {
                // Bottom tab item selectors
                TabItem(
                    selected = activeTab == AppTab.COLECAO,
                    onClick = { viewModel.selectTab(AppTab.COLECAO) },
                    icon = Icons.Default.PhotoAlbum,
                    label = "Álbum",
                    tag = "tab_album"
                )
                TabItem(
                    selected = activeTab == AppTab.LOJA,
                    onClick = { viewModel.selectTab(AppTab.LOJA) },
                    icon = Icons.Default.ShoppingBag,
                    label = "Loja",
                    tag = "tab_loja"
                )
                TabItem(
                    selected = activeTab == AppTab.BAFO,
                    onClick = { viewModel.selectTab(AppTab.BAFO) },
                    icon = Icons.Default.SportsEsports,
                    label = "Bafo",
                    tag = "tab_bafo"
                )
                TabItem(
                    selected = activeTab == AppTab.TROCAS,
                    onClick = { viewModel.selectTab(AppTab.TROCAS) },
                    icon = Icons.Default.People,
                    label = "Trocas",
                    tag = "tab_trocas"
                )
                TabItem(
                    selected = activeTab == AppTab.PLACARES,
                    onClick = { viewModel.selectTab(AppTab.PLACARES) },
                    icon = Icons.Default.Sports,
                    label = "Rodada",
                    tag = "tab_placares"
                )
                TabItem(
                    selected = activeTab == AppTab.MINIJOGOS,
                    onClick = { viewModel.selectTab(AppTab.MINIJOGOS) },
                    icon = Icons.Default.HelpCenter,
                    label = "Quiz",
                    tag = "tab_quiz"
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Main active tab selection switcher
            when (activeTab) {
                AppTab.COLECAO -> {
                    val liveMatches by viewModel.liveMatches.collectAsStateWithLifecycle()
                    CollectionScreen(
                        inventory = inventory,
                        liveMatches = liveMatches,
                        onToggleBattleDeck = { cardId -> viewModel.toggleDeckStatus(cardId) },
                        onUpgradeCard = { cardId, cb -> viewModel.upgradePlayerCard(cardId, cb) },
                        onCustomizeCard = { cardId, customName, customPhotoUrl, customClubAndCountry, cb -> 
                            viewModel.customizePlayerCard(cardId, customName, customPhotoUrl, customClubAndCountry, cb)
                        },
                        onAutoFillCardFromIA = { cardId, playerName, cb ->
                            viewModel.autoFillPlayerCardFromIA(cardId, playerName, cb)
                        },
                        onUpdateCardSticker = { cardId, sticker, cb ->
                            viewModel.updateCardSticker(cardId, sticker, cb)
                        },
                        onOpenPreLaunchCampaign = {
                            showPreLaunchCampaignDialog = true
                        }
                    )
                }

                AppTab.LOJA -> {
                    PackOpenerScreen(
                        coins = profile.coins,
                        isOpeningAnim = isPackOpening,
                        openedCards = openedCards,
                        onBuyPack = { type, price -> viewModel.buyPack(type, price) },
                        onDismissPack = { viewModel.dismissPackOpening() },
                        availableCoinPacks = viewModel.availableCoinPacks,
                        availablePremiumPacks = viewModel.availablePremiumPacks,
                        billingState = billingState,
                        hasElitePass = hasElitePass,
                        isSimulatingAd = isSimulatingAd,
                        adCountdown = adCountdown,
                        onStartBilling = { pack -> viewModel.startBillingSimulation(pack) },
                        onStartElitePassBilling = { viewModel.startElitePassBillingSimulation() },
                        onCancelBilling = { viewModel.cancelBillingSimulation() },
                        onSelectPaymentAndProcess = { pack, method -> viewModel.selectPaymentAndProcess(pack, method) },
                        onProcessElitePassPurchase = { method -> viewModel.processElitePassPurchase(method) },
                        onPlayAd = { viewModel.playSimulatedAd() },
                        onStartPremiumPackBilling = { prod -> viewModel.startPremiumPackBillingSimulation(prod) },
                        onSelectPremiumPackPaymentAndProcess = { prod, method -> viewModel.selectPremiumPackPaymentAndProcess(prod, method) },
                        onCompletePremiumPackPurchaseAndOpen = { prod -> viewModel.completePremiumPackPurchaseAndOpen(prod) }
                    )
                }

                AppTab.BAFO -> {
                    BafoBattleScreen(
                        battleState = battleState,
                        isGpsPermitted = isGpsPermitted,
                        isSearchingNearby = isSearchingNearby,
                        nearbyPlayers = nearbyPlayers,
                        battleGroupsList = battleGroupsList,
                        activeGroup = activeGroup,
                        friendsList = friendsList,
                        duelInvites = duelInvites,
                        onGrantGps = { granted -> viewModel.grantGpsPermission(granted) },
                        onSearchPlayers = { viewModel.searchNearbyPlayers() },
                        onCreateGroup = { name -> viewModel.createNewGroup(name) },
                        onJoinGroup = { group -> viewModel.joinGroup(group) },
                        onLeaveGroup = { viewModel.leaveGroup() },
                        onSendFriendInvite = { nick -> viewModel.sendFriendInvite(nick) },
                        onAcceptInvite = { invite -> viewModel.acceptDuelInvite(invite) },
                        onDeclineInvite = { invite -> viewModel.declineDuelInvite(invite) },
                        onSimulateIncoming = { viewModel.simulateIncomingInvite() },
                        onFightUser = { opp -> viewModel.fightAgainstName(opp) },
                        onEnterLobby = { viewModel.enterBattleLobby() },
                        onSelectWager = { card, diff -> viewModel.selectBattleWager(card, diff) },
                        onUpdatePower = { p -> viewModel.updateSlapPower(p) },
                        onExecuteSlap = { viewModel.executeSlap() },
                        onEndBattle = { viewModel.endBattle() }
                    )
                }

                AppTab.TROCAS -> {
                    TradeHubScreen(
                        tradeOffers = tradeOffers,
                        inventory = inventory,
                        onAcceptTrade = { offer, cb -> viewModel.acceptTrade(offer, cb) },
                        onCreateTrade = { offerId, requestId, cb -> viewModel.createTradeOffer(offerId, requestId, cb) },
                        onCancelTrade = { id -> viewModel.cancelTrade(id) }
                    )
                }

                AppTab.PLACARES -> {
                    MatchCenterScreen(
                        liveMatches = liveMatches,
                        favoriteTeam = profile.favoriteTeam,
                        onPredictResult = { matchId, pOption ->
                            // Correct predictions give rewards! Simulate outcome after 4s
                            scope.launch {
                                delay(4000)
                                viewModel.addManualCoins(250)
                                Toast.makeText(context, "Parabéns! Seu palpite foi CERTEIRO: +250 Moedas!", Toast.LENGTH_LONG).show()
                            }
                        }
                    )
                }

                AppTab.MINIJOGOS -> {
                    GamesScreen(
                        quizState = quizState,
                        onStartQuiz = { viewModel.startQuiz() },
                        onAnswerQuestion = { idx -> viewModel.answerQuizQuestion(idx) },
                        onProceed = { corrCount, currIdx -> viewModel.proceedFromAnswer(corrCount, currIdx) },
                        onFinishQuiz = { viewModel.finishQuiz() }
                    )
                }

                AppTab.PROFILE -> {
                    // Profile/Settings view
                    ProfileScreen(profile = profile, onClearCache = {
                        Toast.makeText(context, "Sessão segurada com êxito!", Toast.LENGTH_SHORT).show()
                    })
                }
            }

            if (showTutorialSheet) {
                InteractiveTutorialBottomSheet(
                    onDismissRequest = { showTutorialSheet = false },
                    onAwardCoins = { amount -> viewModel.addManualCoins(amount) }
                )
            }

            if (showPreLaunchCampaignDialog) {
                PreLaunchLandingDialog(
                    viewModel = viewModel,
                    onDismiss = { showPreLaunchCampaignDialog = false }
                )
            }
        }
    }
}

@Composable
fun RowScope.TabItem(
    selected: Boolean,
    onClick: () -> Unit,
    icon: ImageVector,
    label: String,
    tag: String
) {
    NavigationBarItem(
        selected = selected,
        onClick = onClick,
        icon = { Icon(imageVector = icon, contentDescription = label) },
        label = { Text(text = label, fontSize = 9.sp) },
        colors = NavigationBarItemDefaults.colors(
            selectedIconColor = NeonCyan,
            unselectedIconColor = Color.White.copy(alpha = 0.4f),
            selectedTextColor = NeonEmerald,
            unselectedTextColor = Color.White.copy(alpha = 0.4f),
            indicatorColor = Color(0xFF3F4935)
        ),
        modifier = Modifier.testTag(tag)
    )
}

@Composable
fun ProfileScreen(profile: UserProfile, onClearCache: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(StadiumObsidian)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.AccountCircle,
            contentDescription = null,
            tint = NeonCyan,
            modifier = Modifier.size(80.dp)
        )

        Text(
            text = profile.name,
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold
        )

        Text(
            text = "Time do Coração: ${profile.favoriteTeam}",
            color = NeonEmerald,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = StadiumConcrete)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("DADOS DO PRODUTO SEGURO", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Text("Idade Cadastrada: ${profile.age} anos", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                Text("Método de Autenticação: ${profile.loginProvider}", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                Text("Parceria Livre: Selo de Proteção e Economia Fechada ativo.", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
            }
        }

        // AI Prompt Card Generator
        var selectedLeague by remember { mutableStateOf("Brasileirão 2026") }
        val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
        val context = LocalContext.current

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, NeonCyan.copy(alpha = 0.4f), shape = RoundedCornerShape(12.dp)),
            colors = CardDefaults.cardColors(containerColor = StadiumConcrete)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(imageVector = Icons.Default.FlashOn, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(16.dp))
                    Text("Sincronização / Prompt de IA", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                Text(
                    text = "Obtenha um prompt otimizado para que qualquer IA monte o elenco do ${profile.favoriteTeam} com fotos estáveis corretas e atributos realistas para o seu álbum.",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 11.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val leagues = listOf("Brasileirão 2026", "Champions 25", "Copa do Mundo")
                    leagues.forEach { league ->
                        val isSel = selectedLeague == league
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(if (isSel) NeonCyan else StadiumGlow, shape = RoundedCornerShape(6.dp))
                                .border(0.5.dp, if (isSel) NeonCyan else Color.Transparent, shape = RoundedCornerShape(6.dp))
                                .clickable { selectedLeague = league }
                                .padding(vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = league,
                                color = if (isSel) Color.Black else Color.White.copy(alpha = 0.8f),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                val promptText = remember(selectedLeague, profile.favoriteTeam) {
                    """
                    Atue como um especialista em futebol e engenheiro de prompts de IA de alta performance. Gostaria que você gerasse um arquivo em formato de código de catálogo de jogadores atualizados especificamente para a liga "$selectedLeague" para o time "${profile.favoriteTeam}" (ou preencha com dados reais atualizados deste torneio se aplicável).

                    Gere um array estático de dados contendo no mínimo os 11 jogadores principais e reservas relevantes respeitando as seguintes regras rígidas:
                    1. Forneça o nome oficial correto, posição exata (ATA, MEI, VOL, DEF, GOL) baseada nas táticas modernas.
                    2. Forneça pontuações gerais realistas (overall de 60 a 99) e estatísticas detalhadas de Rítmo (pac), Finalização (sho), Passagem (pas), Condução (dri), Defesa (def), e Físico (phy).
                    3. Vincule URLs de fotos reais públicas do jogador oriundas de Unsplash ou de representações esportivas oficiais (URLs iniciadas em https://images.unsplash.com/ ou de CDN estável de rostos).
                    4. Retorne o código formatado estritamente como instâncias da classe Kotlin PlayerCard em formato válido:
                    
                    Exemplo de Saída Esperada de Código:
                    PlayerCard(
                        id = 1,
                        name = "Pelé",
                        clubAndCountry = "Santos / Brasil",
                        position = Position.ATA,
                        overall = 99,
                        stats = PlayerStats(pac = 97, sho = 99, pas = 95, dri = 98, def = 60, phy = 88),
                        rarity = Rarity.LENDARIA,
                        initialHexColor = "#FFD700",
                        photoUrl = "https://images.unsplash.com/photo-1544005313-94ddf0286df2?w=150",
                        clubLogoUrl = "https://images.unsplash.com/photo-1508098682722"
                    )
                    """.trimIndent()
                }

                Button(
                    onClick = {
                        clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(promptText))
                        Toast.makeText(context, "Prompt copiado! Insira-o no Gemini para receber o catálogo atualizado.", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Copiar Prompt de IA", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            colors = ButtonDefaults.buttonColors(containerColor = StadiumGlow),
            onClick = onClearCache,
            modifier = Modifier.fillMaxWidth(0.8f)
        ) {
            Text("Limpar Cache Local", color = Color.White)
        }
    }
}
