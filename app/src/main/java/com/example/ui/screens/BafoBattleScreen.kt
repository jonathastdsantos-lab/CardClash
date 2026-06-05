package com.example.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PlayerCard
import com.example.ui.components.FutCardShape
import com.example.ui.components.FutCardView
import com.example.ui.theme.*
import com.example.ui.viewmodel.BattleState
import com.example.ui.viewmodel.NearbyUser
import com.example.ui.viewmodel.BattleGroup
import com.example.ui.viewmodel.FriendInvite
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BafoBattleScreen(
    battleState: BattleState,
    isGpsPermitted: Boolean,
    isSearchingNearby: Boolean,
    nearbyPlayers: List<NearbyUser>,
    battleGroupsList: List<BattleGroup>,
    activeGroup: BattleGroup?,
    friendsList: List<String>,
    duelInvites: List<FriendInvite>,
    onGrantGps: (Boolean) -> Unit,
    onSearchPlayers: () -> Unit,
    onCreateGroup: (String) -> Unit,
    onJoinGroup: (BattleGroup) -> Unit,
    onLeaveGroup: () -> Unit,
    onSendFriendInvite: (String) -> Unit,
    onAcceptInvite: (FriendInvite) -> Unit,
    onDeclineInvite: (FriendInvite) -> Unit,
    onSimulateIncoming: () -> Unit,
    onFightUser: (String) -> Unit,
    onEnterLobby: () -> Unit,
    onSelectWager: (PlayerCard, Int) -> Unit,
    onUpdatePower: (Float) -> Unit,
    onExecuteSlap: () -> Unit,
    onEndBattle: () -> Unit
) {
    // Launcher for genuine GPS sensor permissions
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        onGrantGps(isGranted)
    }

    // Cyclic timer to simulate timing bar motion in Active Play mode
    val infiniteTransition = rememberInfiniteTransition(label = "power_bar")
    val currentPowerCycle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cycle"
    )

    // Pulsing alpha for simulated active radar pulse
    val radarPulse by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOutCirc),
            repeatMode = RepeatMode.Reverse
        ),
        label = "radar"
    )

    // Option 2: Competitive Announcer Soundboard Haptic Simulator State
    var activeComicSticker by remember { mutableStateOf<String?>(null) }
    var activeCommentaryText by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()
    val localHaptic = androidx.compose.ui.platform.LocalHapticFeedback.current

    val playAnnouncerSound = { text: String, sticker: String, hapticTimes: Int ->
        activeCommentaryText = text
        activeComicSticker = sticker
        coroutineScope.launch {
            repeat(hapticTimes) {
                try {
                    localHaptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                } catch (e: Exception) {
                    // Fail-safe in emulator environments
                }
                kotlinx.coroutines.delay(100)
            }
            kotlinx.coroutines.delay(1800)
            if (activeComicSticker == sticker) {
                activeComicSticker = null
            }
            if (activeCommentaryText == text) {
                activeCommentaryText = null
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(StadiumObsidian)
            .statusBarsPadding()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Main Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.SportsEsports,
                contentDescription = null,
                tint = NeonEmerald,
                modifier = Modifier.size(28.dp)
            )
            Text(
                text = "Arena de Batalha: Bafo",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }

        HorizontalDivider(color = StadiumBorder)

        when (battleState) {
            is BattleState.Idle -> {
                // Battle Idle: Split into Solo AI practice, GPS Radar (Nearby), and Friends invites
                var selectedIdleMode by remember { mutableStateOf(0) } // 0 = Praticar (Solo), 1 = GPS (Local), 2 = Amigos (Conexões)

                SingleStatesSelector(
                    selectedIndex = selectedIdleMode,
                    options = listOf("Praticar 🤖", "GPS Radar 📍", "Amigos 👥"),
                    onSelect = { selectedIdleMode = it }
                )

                Spacer(modifier = Modifier.height(4.dp))

                when (selectedIdleMode) {
                    0 -> {
                        // SOLO PRACTICE MODE (EXISTING BAFO FLOW)
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .weight(1f)
                                .verticalScroll(rememberScrollState()),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ElectricBolt,
                                contentDescription = null,
                                tint = NeonCyan,
                                modifier = Modifier.size(72.dp)
                            )

                            Text(
                                text = "Pratique o clássico jogo do bafinho!",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )

                            Text(
                                text = "Dispute as suas figurinhas do 'Baralho de Batalha' contra a IA de simulação rápida. Solte vento no medidor no instante ideal de pressão para virar as figurinhas da mesa!",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 12.sp,
                                lineHeight = 16.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )

                            Card(
                                colors = CardDefaults.cardColors(containerColor = StadiumConcrete),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, StadiumBorder, shape = RoundedCornerShape(12.dp))
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = BrightGold)
                                    Text(
                                        text = "Apenas cards duplicados ou marcados no 'Baralho de Batalha' (Seção Álbum) correm risco nas derrotas. Coleção principal protegida!",
                                        color = Color.White.copy(alpha = 0.85f),
                                        fontSize = 10.sp,
                                        lineHeight = 13.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Button(
                                colors = ButtonDefaults.buttonColors(containerColor = NeonEmerald),
                                onClick = onEnterLobby,
                                modifier = Modifier
                                    .fillMaxWidth(0.85f)
                                    .height(48.dp)
                                    .testTag("enter_battle_btn")
                            ) {
                                Text("ENTRAR NO LOBBY SOLO", color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    1 -> {
                        // GPS RADAR & LOCAL BATTLE GROUPS
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .weight(1f),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            if (!isGpsPermitted) {
                                // Request Location block
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .verticalScroll(rememberScrollState()),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Place,
                                        contentDescription = null,
                                        tint = Color.LightGray,
                                        modifier = Modifier.size(64.dp)
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "Encontre Jogadores por Perto!",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Para disputar figurinhas com outros colecionadores através do GPS Radar ou criar grupos de batalha próximos, precisamos da permissão de localização do seu aparelho.",
                                        color = Color.White.copy(alpha = 0.6f),
                                        fontSize = 12.sp,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(horizontal = 24.dp)
                                    )
                                    Spacer(modifier = Modifier.height(20.dp))
                                    Button(
                                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                                        onClick = { permissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION) },
                                        modifier = Modifier.fillMaxWidth(0.8f).height(48.dp).testTag("gps_permit_btn")
                                    ) {
                                        Text("ATIVAR SENSOR GPS", color = Color.Black, fontWeight = FontWeight.Bold)
                                    }
                                }
                            } else {
                                // GPS is Active. Display radar scanner, user list and groups.
                                LazyColumn(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(14.dp)
                                ) {
                                    // GPS Status Header
                                    item {
                                        Card(
                                            colors = CardDefaults.cardColors(containerColor = StadiumConcrete),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(12.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(8.dp)
                                                            .clip(CircleShape)
                                                            .background(NeonEmerald)
                                                    )
                                                    Text(
                                                        text = "GPS Ativo: Maracanã, Rio de Janeiro (-22.912, -43.230)",
                                                        color = Color.White.copy(alpha = 0.8f),
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.SemiBold
                                                    )
                                                }
                                                IconButton(
                                                    onClick = onSearchPlayers,
                                                    modifier = Modifier.size(24.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Refresh,
                                                        contentDescription = "Search Radar",
                                                        tint = NeonCyan,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    // Display Radar searching state
                                    if (isSearchingNearby) {
                                        item {
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 24.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.spacedBy(10.dp)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(54.dp)
                                                        .border(2.dp, NeonCyan.copy(alpha = radarPulse), shape = CircleShape),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Place,
                                                        contentDescription = null,
                                                        tint = NeonCyan,
                                                        modifier = Modifier.size(32.dp)
                                                    )
                                                }
                                                Text(
                                                    text = "Escaneando arredores por WiFi / GPS...",
                                                    color = Color.White.copy(alpha = 0.5f),
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    } else {
                                        // Nearby Players List
                                        item {
                                            Text(
                                                text = "JOGADORES PRÓXIMOS (RAIO 500M)",
                                                color = NeonCyan,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                letterSpacing = 1.sp
                                            )
                                        }

                                        if (nearbyPlayers.isEmpty()) {
                                            item {
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(vertical = 12.dp)
                                                        .border(1.dp, StadiumBorder, shape = RoundedCornerShape(8.dp))
                                                        .padding(14.dp),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = "Nenhum colecionador por perto. Toque no ícone de recarregar acima para radar de varredura.",
                                                        color = Color.White.copy(alpha = 0.4f),
                                                        fontSize = 11.sp,
                                                        textAlign = TextAlign.Center
                                                    )
                                                }
                                            }
                                        } else {
                                            items(nearbyPlayers) { player ->
                                                Card(
                                                    colors = CardDefaults.cardColors(containerColor = StadiumConcrete),
                                                    shape = RoundedCornerShape(10.dp),
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .border(1.dp, StadiumBorder, shape = RoundedCornerShape(10.dp))
                                                ) {
                                                    Row(
                                                        modifier = Modifier.padding(10.dp),
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.SpaceBetween
                                                    ) {
                                                        Column {
                                                            Text(
                                                                text = player.nickname,
                                                                color = Color.White,
                                                                fontWeight = FontWeight.Bold,
                                                                fontSize = 13.sp
                                                            )
                                                            Row(
                                                                verticalAlignment = Alignment.CenterVertically,
                                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                            ) {
                                                                Text("Nível ${player.level}", color = NeonEmerald, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                                Text("•", color = Color.White.copy(alpha = 0.3f), fontSize = 10.sp)
                                                                Text("${player.distanceMetres}m de distância", color = Color.White.copy(alpha = 0.6f), fontSize = 10.sp)
                                                            }
                                                        }
                                                        Button(
                                                            colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                                                            onClick = { onFightUser(player.nickname) },
                                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                                            modifier = Modifier.height(34.dp)
                                                        ) {
                                                            Text("DESAFIAR ⚔️", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                        }
                                                    }
                                                }
                                            }
                                        }

                                        // GROUPS SECTION
                                        item {
                                            Spacer(modifier = Modifier.height(10.dp))
                                            Text(
                                                text = "GRUPOS DE BATALHA COMUNITÁRIOS (FUT-BANDO) 👥",
                                                color = NeonCyan,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                letterSpacing = 1.sp
                                            )
                                        }

                                        // Check if already inside an active group
                                        if (activeGroup != null) {
                                            item {
                                                Card(
                                                    colors = CardDefaults.cardColors(containerColor = StadiumGlow),
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .border(1.dp, NeonEmerald, shape = RoundedCornerShape(12.dp)),
                                                    shape = RoundedCornerShape(12.dp)
                                                ) {
                                                    Column(
                                                        modifier = Modifier.padding(14.dp),
                                                        verticalArrangement = Arrangement.spacedBy(10.dp)
                                                    ) {
                                                        Row(
                                                            modifier = Modifier.fillMaxWidth(),
                                                            horizontalArrangement = Arrangement.SpaceBetween,
                                                            verticalAlignment = Alignment.CenterVertically
                                                        ) {
                                                            Text(
                                                                text = activeGroup.name,
                                                                color = NeonEmerald,
                                                                fontSize = 15.sp,
                                                                fontWeight = FontWeight.Black
                                                            )
                                                            Text(
                                                                text = "${activeGroup.memberCount} Online",
                                                                color = Color.White,
                                                                fontSize = 10.sp,
                                                                fontWeight = FontWeight.Bold,
                                                                modifier = Modifier
                                                                    .background(StadiumConcrete, shape = RoundedCornerShape(8.dp))
                                                                    .padding(horizontal = 6.dp, vertical = 3.dp)
                                                            )
                                                        }

                                                        Text(
                                                            text = "Membros Ativos neste Ponto de Encontro:",
                                                            color = Color.White.copy(alpha = 0.6f),
                                                            fontSize = 11.sp,
                                                            fontWeight = FontWeight.Bold
                                                        )

                                                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                                            activeGroup.members.forEach { member ->
                                                                Row(
                                                                    verticalAlignment = Alignment.CenterVertically,
                                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                                ) {
                                                                    Box(
                                                                        modifier = Modifier
                                                                            .size(6.dp)
                                                                            .clip(CircleShape)
                                                                            .background(NeonEmerald)
                                                                    )
                                                                    Text(
                                                                        text = if (member == "Você" || member == "Jogador") "Você ($member)" else member,
                                                                        color = Color.White,
                                                                        fontSize = 12.sp,
                                                                        fontWeight = FontWeight.SemiBold
                                                                    )
                                                                    Spacer(modifier = Modifier.weight(1f))
                                                                    if (member != "Você" && member != "Jogador" && member != "Eu") {
                                                                        Text(
                                                                            text = "DESAFIAR",
                                                                            color = NeonCyan,
                                                                            fontSize = 10.sp,
                                                                            fontWeight = FontWeight.Bold,
                                                                            modifier = Modifier
                                                                                .clickable { onFightUser(member) }
                                                                                .border(0.5.dp, NeonCyan, shape = RoundedCornerShape(4.dp))
                                                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                                                        )
                                                                    }
                                                                }
                                                            }
                                                        }

                                                        Spacer(modifier = Modifier.height(4.dp))

                                                        Button(
                                                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.15f)),
                                                            onClick = onLeaveGroup,
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                                .border(1.dp, Color.Red.copy(alpha = 0.5f), shape = RoundedCornerShape(8.dp)),
                                                            contentPadding = PaddingValues(vertical = 8.dp)
                                                        ) {
                                                            Text("DISSOLVER / SAIR DO GRUPO", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                                        }
                                                    }
                                                }
                                            }
                                        } else {
                                            // Available rooms list helper
                                            if (battleGroupsList.isEmpty()) {
                                                item {
                                                    Text(
                                                        text = "Nenhum ponto local disponível.",
                                                        color = Color.Gray,
                                                        fontSize = 11.sp
                                                    )
                                                }
                                            } else {
                                                items(battleGroupsList) { grp ->
                                                    Card(
                                                        colors = CardDefaults.cardColors(containerColor = StadiumConcrete),
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .padding(vertical = 1.dp)
                                                    ) {
                                                        Row(
                                                            modifier = Modifier.padding(12.dp),
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            horizontalArrangement = Arrangement.SpaceBetween
                                                        ) {
                                                            Column(modifier = Modifier.weight(1f)) {
                                                                Text(text = grp.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                                                Text(text = "Fundado por - @${grp.creatorNickname}", color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp)
                                                            }
                                                            Button(
                                                                colors = ButtonDefaults.buttonColors(containerColor = NeonEmerald),
                                                                onClick = { onJoinGroup(grp) },
                                                                contentPadding = PaddingValues(horizontal = 14.dp),
                                                                modifier = Modifier.height(32.dp)
                                                            ) {
                                                                Text("ENTRAR (${grp.memberCount})", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                            }
                                                        }
                                                    }
                                                }
                                            }

                                            // Register a group card
                                            item {
                                                Spacer(modifier = Modifier.height(6.dp))
                                                var newGroupName by remember { mutableStateOf("") }
                                                Card(
                                                    colors = CardDefaults.cardColors(containerColor = StadiumConcrete),
                                                    modifier = Modifier.fillMaxWidth().border(1.dp, StadiumBorder, RoundedCornerShape(12.dp))
                                                ) {
                                                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                                        Text("Criar Ponto de Batalha (Sinal GPS) 📡", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                        OutlinedTextField(
                                                            value = newGroupName,
                                                            onValueChange = { newGroupName = it },
                                                            placeholder = { Text("Nome do local, ex: Arena do Campinho", fontSize = 11.sp) },
                                                            modifier = Modifier.fillMaxWidth(),
                                                            colors = OutlinedTextFieldDefaults.colors(
                                                                focusedBorderColor = NeonCyan,
                                                                unfocusedBorderColor = StadiumBorder,
                                                                focusedTextColor = Color.White,
                                                                unfocusedTextColor = Color.White
                                                            ),
                                                            singleLine = true,
                                                            textStyle = LocalTextStyle.current.copy(fontSize = 12.sp)
                                                        )
                                                        Button(
                                                            colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                                                            onClick = {
                                                                if (newGroupName.isNotBlank()) {
                                                                    onCreateGroup(newGroupName)
                                                                    newGroupName = ""
                                                                }
                                                            },
                                                            modifier = Modifier.fillMaxWidth().height(36.dp),
                                                            enabled = newGroupName.isNotBlank(),
                                                            shape = RoundedCornerShape(8.dp)
                                                        ) {
                                                            Text("FUNDAR GRUPO DE ENCONTRO", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    2 -> {
                        // FRIENDS LIST & CUSTOM DUEL NICKNAME INVITES
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .weight(1f)
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            var friendNicknameToInvite by remember { mutableStateOf("") }

                            Card(
                                colors = CardDefaults.cardColors(containerColor = StadiumConcrete),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, StadiumBorder, shape = RoundedCornerShape(12.dp)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Text(
                                        text = "Convidar Amigo por Nickname ⚔️",
                                        color = Color.White,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )

                                    OutlinedTextField(
                                        value = friendNicknameToInvite,
                                        onValueChange = { friendNicknameToInvite = it },
                                        placeholder = { Text("Apelido do usuário (ex: NeymarZinho10)", fontSize = 12.sp) },
                                        modifier = Modifier.fillMaxWidth().testTag("friend_nickname_input"),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = NeonCyan,
                                            unfocusedBorderColor = StadiumBorder,
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White
                                        ),
                                        singleLine = true,
                                        textStyle = LocalTextStyle.current.copy(fontSize = 12.sp)
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Button(
                                            colors = ButtonDefaults.buttonColors(containerColor = NeonEmerald),
                                            onClick = {
                                                if (friendNicknameToInvite.isNotBlank()) {
                                                    onSendFriendInvite(friendNicknameToInvite)
                                                    friendNicknameToInvite = ""
                                                }
                                            },
                                            modifier = Modifier.weight(1f).height(40.dp).testTag("send_invite_btn"),
                                            enabled = friendNicknameToInvite.isNotBlank()
                                        ) {
                                            Text("ENVIAR CONVITE", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }

                                        Button(
                                            colors = ButtonDefaults.buttonColors(containerColor = StadiumGlow),
                                            onClick = onSimulateIncoming,
                                            modifier = Modifier.height(40.dp)
                                        ) {
                                            Text("REDUZIR ESPERA (SIMULADOR 🔔)", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }

                            // Active incoming or outgoing duel requests listing
                            val incoming = duelInvites.filter { it.targetNickname == "Você" }
                            val outgoing = duelInvites.filter { it.senderNickname == "Você" }

                            Text(
                                text = "CONVITES DE DUELO RECEBIDOS (${incoming.size})",
                                color = NeonCyan,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )

                            if (incoming.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(0.5.dp, StadiumBorder, RoundedCornerShape(8.dp))
                                        .padding(12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("Nenhum convite recebido no momento.", color = Color.Gray, fontSize = 11.sp)
                                }
                            } else {
                                incoming.forEach { invite ->
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = StadiumConcrete),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(10.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column {
                                                Text(text = invite.senderNickname, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                                Text(text = "Duelo de Bafo solicitado!", color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp)
                                            }
                                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                IconButton(
                                                    onClick = { onAcceptInvite(invite) },
                                                    modifier = Modifier.size(32.dp).background(NeonEmerald, CircleShape)
                                                ) {
                                                    Icon(imageVector = Icons.Default.Check, contentDescription = "Accept", tint = Color.Black, modifier = Modifier.size(16.dp))
                                                }
                                                IconButton(
                                                    onClick = { onDeclineInvite(invite) },
                                                    modifier = Modifier.size(32.dp).background(Color.Red.copy(alpha = 0.2f), CircleShape)
                                                ) {
                                                    Icon(imageVector = Icons.Default.Close, contentDescription = "Decline", tint = Color.Red, modifier = Modifier.size(16.dp))
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // Outgoing invites tracking state
                            if (outgoing.isNotEmpty()) {
                                Text(
                                    text = "CONVITES ENVIADOS",
                                    color = NeonCyan,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )

                                outgoing.forEach { invite ->
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = StadiumConcrete),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(10.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(text = "@${invite.targetNickname}", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                            if (invite.status == "ACCEPTED") {
                                                Button(
                                                    colors = ButtonDefaults.buttonColors(containerColor = NeonEmerald),
                                                    onClick = { onAcceptInvite(invite) },
                                                    contentPadding = PaddingValues(horizontal = 10.dp),
                                                    modifier = Modifier.height(28.dp)
                                                ) {
                                                    Text("DUELAR JÁ! ✅", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                }
                                            } else {
                                                Text(
                                                    text = "AGUARDANDO...",
                                                    color = BrightGold,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Black,
                                                    modifier = Modifier
                                                        .border(0.5.dp, BrightGold, shape = RoundedCornerShape(4.dp))
                                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // Fast friends listing shortcut
                            Text(
                                text = "ATALHO DE AMIGOS RÁPIDOS",
                                color = NeonCyan,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )

                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(friendsList) { friend ->
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = StadiumConcrete),
                                        modifier = Modifier
                                            .width(110.dp)
                                            .clickable { onFightUser(friend) }
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(10.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(24.dp)
                                                    .background(StadiumGlow, CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text("⚽", fontSize = 11.sp)
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(text = friend, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                                            Text(text = "Duelo ⚔️", color = NeonCyan, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            is BattleState.ChooseWager -> {
                // Select which card to risk
                if (battleState.myBattleCards.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Seu Baralho de Batalha está Vazio!",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Para poder batalhar, vá na aba 'COLEÇÃO', selecione qualquer figurinha que você possui e defina 'USAR NA BATALHA'!",
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 24.dp)
                            )
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "PASSO 1: Selecione a FIGURINHA que colocará em ESCROW para a disputa:",
                            color = NeonCyan,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("wager_cards_row")
                        ) {
                            items(battleState.myBattleCards) { card ->
                                FutCardView(
                                    card = card,
                                    onClick = {
                                        // Pick and match with custom difficulty tier
                                        onSelectWager(card, if (card.overall > 85) 90 else 75)
                                    }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "💡 Clique em uma figurinha acima para confirmar o depósito em custódia. O app reterá temporariamente durante o jogo. Se você ganhar, leva o card do adversário!",
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 11.sp,
                            lineHeight = 14.sp
                        )
                    }
                }
            }

            is BattleState.ActivePlay -> {
                // Game Loop Core Screen
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Scorecard row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Você", color = Color.White, fontWeight = FontWeight.Bold)
                            Text("Card: ${battleState.myWager.name}", color = NeonEmerald, fontSize = 11.sp)
                            Text(
                                if (battleState.myCardsFlipped) "VIRADO ✅" else "FACE DOWN ❌",
                                color = if (battleState.myCardsFlipped) NeonEmerald else Color.LightGray,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Text("VS", color = Color.White, fontWeight = FontWeight.Black, fontSize = 18.sp)

                        Column(horizontalAlignment = Alignment.End) {
                            Text(battleState.oppName, color = Color.White, fontWeight = FontWeight.Bold)
                            Text("Card: ${battleState.oppWager.name}", color = NeonCyan, fontSize = 11.sp)
                            Text(
                                if (battleState.oppCardsFlipped) "VIRADO ✅" else "FACE DOWN ❌",
                                color = if (battleState.oppCardsFlipped) NeonCyan else Color.LightGray,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Cards Layout stacked in center
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // Opponent card (slightly rotated offset)
                        Box(
                            modifier = Modifier
                                .offset(x = 10.dp, y = (-10).dp)
                                .background(Color.Black.copy(alpha = 0.5f), shape = FutCardShape)
                        ) {
                            FutCardView(card = battleState.oppWager)
                        }

                        // My card (overlapping on bottom)
                        Box(
                            modifier = Modifier
                                .offset(x = (-10).dp, y = 10.dp)
                                .background(Color.Black.copy(alpha = 0.5f), shape = FutCardShape)
                        ) {
                            FutCardView(card = battleState.myWager)
                        }

                        // Option 2: Active Narrator comic sticker overlay
                        if (activeComicSticker != null) {
                            Box(
                                modifier = Modifier
                                    .background(Color.Black.copy(alpha = 0.9f), shape = RoundedCornerShape(12.dp))
                                    .border(1.5.dp, NeonEmerald, shape = RoundedCornerShape(12.dp))
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                                    .align(Alignment.Center)
                                    .rotate(-8f)
                                    .scale(1.15f),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = activeComicSticker!!,
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    // Option 2: Active Soundboard Board
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "🔊 Mesa de Voz do Narrador (Som & Haptic)",
                            color = NeonCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally)
                        ) {
                            val soundboardCollection = listOf(
                                Triple("📢 TOMA!", "Narrador: SE PREPARA QUE LÁ VEM CONCHADA ESTILO RETRÔ!", "POW! 👋💥"),
                                Triple("📢 AMASSOU!", "Narrador: ESSA APERTADA VAI DOER ATÉ EM 2026!", "AMASSADO! 🔨💥"),
                                Triple("📢 GOOL!", "Narrador: MINHA NOSSA SENHORA! ELIMIDOU O OPONENTE DA MESA!", "CRAQUE! 🏆👑"),
                                Triple("📢 BAFO!", "Narrador: BATEU DE CONCHA NO MEIO DA FIGURINHA!", "VIRA-TUDO! ⚡🔥")
                            )
                            soundboardCollection.forEach { (lbl, comment, sticker) ->
                                Button(
                                    onClick = {
                                        playAnnouncerSound(comment, sticker, 2)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = StadiumGlow),
                                    modifier = Modifier
                                        .height(28.dp)
                                        .weight(1f)
                                        .border(0.5.dp, Color.White.copy(alpha = 0.15f), shape = RoundedCornerShape(14.dp)),
                                    contentPadding = PaddingValues(0.dp),
                                    shape = RoundedCornerShape(14.dp)
                                ) {
                                    Text(text = lbl, color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Black)
                                }
                            }
                        }
                    }

                    // Option 2: Commentary visual speech bubble
                    if (activeCommentaryText != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.95f)
                                .background(Color(0xFF0F172A), shape = RoundedCornerShape(8.dp))
                                .border(1.dp, NeonCyan.copy(alpha = 0.4f), shape = RoundedCornerShape(8.dp))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = activeCommentaryText!!,
                                color = NeonCyan,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Instruction status banner
                    Text(
                        text = battleState.currentTurnText,
                        color = Color.White,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    // Power bar timing game
                    if (battleState.isMySlap) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Target sweet spot zone highlighter
                            Text(
                                text = "Zona Ideal de Batida: 60% a 80% da barra!",
                                color = BrightGold,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )

                            // Drawn linear timing progress bar
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.9f)
                                    .height(24.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(StadiumConcrete)
                                    .border(1.dp, Color.White.copy(alpha = 0.2f), shape = RoundedCornerShape(12.dp))
                            ) {
                                // Redsweet spot Highlight marker
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .fillMaxWidth(0.2f)
                                        .align(Alignment.CenterStart)
                                        .offset(x = 145.dp) // Offset matching roughly 60% to 80% range in compact devices
                                        .background(BrightGold.copy(alpha = 0.61f))
                                )

                                // Real oscillating power slider
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .fillMaxWidth(currentPowerCycle)
                                        .background(
                                            Brush.horizontalGradient(
                                                colors = listOf(NeonEmerald, NeonCyan)
                                            )
                                        )
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Button(
                                colors = ButtonDefaults.buttonColors(containerColor = NeonEmerald),
                                onClick = {
                                    onUpdatePower(currentPowerCycle)
                                    onExecuteSlap()
                                    // Option 2: Automated Haptic Voice effects and Comic overlay on Hit action!
                                    val power = currentPowerCycle
                                    if (power in 0.6f..0.8f) {
                                        playAnnouncerSound("Narrador: BATEU CONCHINHA PERFEITA! QUE CONTROLE ESPETACULAR!", "PERFEITO! 👑💥", 3)
                                    } else if (power < 0.45f) {
                                        playAnnouncerSound("Narrador: VENTOU! O ar deu carona por baixo e a figurinha nem mexeu!", "VENTOU... 🌀💨", 1)
                                    } else {
                                        playAnnouncerSound("Narrador: PAULADA! Amassou o chão do estádio e doeu a mão!", "PAULADA! 🔨🔥", 2)
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth(0.85f)
                                    .height(48.dp)
                                    .testTag("slap_hit_btn")
                            ) {
                                Text("BATER CONCHINHA! 👋💥", color = Color.Black, fontWeight = FontWeight.Black)
                            }
                        }
                    } else {
                        // AI indicator spinning progress
                        CircularProgressIndicator(color = NeonCyan)
                    }
                }
            }

            is BattleState.Result -> {
                // Battle Finish Screen
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    Text(
                        text = if (battleState.won) "VITÓRIA GLORIOSA! 🏆🎉" else "CARD PERDIDO!",
                        color = if (battleState.won) NeonEmerald else Color.Red,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally)
                    ) {
                        FutCardView(card = battleState.myWager)
                        FutCardView(card = battleState.oppWager)
                    }

                    Text(
                        text = battleState.prizeText,
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = if (battleState.won) "Sua habilidade foi decisiva! O card foi transferido instantaneamente da custódia do app para seu álbum completo."
                        else "Você errou a pressão. A figurinha apostada foi entregue ao oponente de acordo com as diretrizes do bafo competitivo.",
                        color = Color.White.copy(alpha = 0.65f),
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp),
                        lineHeight = 14.sp
                    )

                    Button(
                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                        onClick = onEndBattle,
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .height(48.dp)
                            .testTag("finish_battle_btn")
                    ) {
                        Text("CONCLUIR DISPUTA", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun SingleStatesSelector(
    selectedIndex: Int,
    options: List<String>,
    onSelect: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(StadiumConcrete)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        options.forEachIndexed { index, title ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (selectedIndex == index) StadiumGlow else Color.Transparent)
                    .clickable { onSelect(index) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = title,
                    color = if (selectedIndex == index) NeonCyan else Color.White.copy(alpha = 0.6f),
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }
    }
}
