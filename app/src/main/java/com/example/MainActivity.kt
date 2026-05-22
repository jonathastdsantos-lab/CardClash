package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
                    CollectionScreen(
                        inventory = inventory,
                        onToggleBattleDeck = { cardId -> viewModel.toggleDeckStatus(cardId) }
                    )
                }

                AppTab.LOJA -> {
                    PackOpenerScreen(
                        coins = profile.coins,
                        isOpeningAnim = isPackOpening,
                        openedCards = openedCards,
                        onBuyPack = { type, price -> viewModel.buyPack(type, price) },
                        onDismissPack = { viewModel.dismissPackOpening() }
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

        Spacer(modifier = Modifier.weight(1f))

        Button(
            colors = ButtonDefaults.buttonColors(containerColor = StadiumGlow),
            onClick = onClearCache,
            modifier = Modifier.fillMaxWidth(0.8f)
        ) {
            Text("Limpar Cache Local", color = Color.White)
        }
    }
}
