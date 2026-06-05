package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.CardCatalog
import com.example.data.model.PlayerCard
import com.example.data.model.PlayerStats
import com.example.data.model.Position
import com.example.data.model.Rarity
import com.example.ui.theme.*
import com.example.ui.viewmodel.FutViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreLaunchLandingDialog(
    viewModel: FutViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var activeTab by remember { mutableStateOf(0) } // 0 = Bracket, ...

    // Waitlist and prelaunch states from viewmodel
    val waitlistEmail by viewModel.waitlistEmail.collectAsStateWithLifecycle()
    val predictedChampion by viewModel.predictedChampion.collectAsStateWithLifecycle()
    val isWaitlistRegistered by viewModel.isWaitlistRegistered.collectAsStateWithLifecycle()
    val waitlistQueuePosition by viewModel.waitlistQueuePosition.collectAsStateWithLifecycle()

    // Collect prelaunch sponsor and group states
    val waitlistGroupName by viewModel.waitlistGroupName.collectAsStateWithLifecycle()
    val waitlistGroupCode by viewModel.waitlistGroupCode.collectAsStateWithLifecycle()
    val waitlistGroupType by viewModel.waitlistGroupType.collectAsStateWithLifecycle()
    val waitlistGroupSize by viewModel.waitlistGroupSize.collectAsStateWithLifecycle()
    val waitlistGroupPosition by viewModel.waitlistGroupPosition.collectAsStateWithLifecycle()
    val waitlistGroupMembers by viewModel.waitlistGroupMembers.collectAsStateWithLifecycle()
    val waitlistGroupLeaderboard by viewModel.waitlistGroupLeaderboard.collectAsStateWithLifecycle()
    val redeemedSponsorCodes by viewModel.redeemedSponsorCodes.collectAsStateWithLifecycle()

    // Local inputs
    var inputEmail by remember { mutableStateOf(waitlistEmail) }
    var selectedPrediction by remember { mutableStateOf(predictedChampion.ifBlank { "Brasil 🇧🇷" }) }
    var expandedDropdown by remember { mutableStateOf(false) }

    var sponsorCodeInput by remember { mutableStateOf("") }
    var teamNameInput by remember { mutableStateOf("") }
    var teamTypeSelected by remember { mutableStateOf("Amigos") }
    var joinTeamCodeInput by remember { mutableStateOf("") }

    val countriesList = listOf(
        "Brasil 🇧🇷",
        "Espanha 🇪🇸",
        "França 🇫🇷",
        "Inglaterra 🏴󠁧󠁢󠁥󠁮󠁧󠁿",
        "Argentina 🇦🇷",
        "Uruguai 🇺🇾",
        "Portugal 🇵🇹",
        "Alemanha 🇩🇪"
    )

    // Dedicated countdown logic: From June 5, 2026 to June 11, 2026
    val targetCutoffMillis = 1781136000000L // June 11, 2026 UTC
    val currentMillis = 1780690819000L // Simulated June 5, 2026 (from metadata)
    val remainingMillis = (targetCutoffMillis - currentMillis).coerceAtLeast(0)
    val remainingDays = remainingMillis / (1000 * 60 * 60 * 24)
    val remainingHours = (remainingMillis % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60)

    val simulatedRegistrantsCount = 14854

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier
            .fillMaxWidth(0.95f)
            .fillMaxHeight(0.92f)
            .border(2.dp, NeonCyan, shape = RoundedCornerShape(24.dp)),
        containerColor = StadiumObsidian,
        title = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .background(NeonCyan.copy(alpha = 0.15f), shape = RoundedCornerShape(40.dp))
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "🏆 COPA DO MUNDO 2026",
                        color = NeonCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.1.sp
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "CAMPANHA DE PRÉ-LANÇAMENTO",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(10.dp))

                // Scrollable Sub-tabs for detailed marketing tasks
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val tabLabels = listOf(
                        "📍 Bracket & Copa" to Icons.Default.EmojiEvents,
                        "📋 Escalação Ideal" to Icons.Default.List,
                        "🏆 Card do Jogo" to Icons.Default.Star,
                        "❓ Quiz de Stories" to Icons.Default.Help,
                        "💬 Grupos VIP 1000" to Icons.Default.Group,
                        "🎬 Estúdio TikTok/Reels" to Icons.Default.Videocam,
                        "📣 Embaixadores" to Icons.Default.Campaign,
                        "🥤 Patrocínio Local" to Icons.Default.LocalDrink,
                        "👥 Equipes & Grupos" to Icons.Default.People
                    )
                    tabLabels.forEachIndexed { index, pair ->
                        val isSelected = activeTab == index
                        val btnColor = if (isSelected) NeonCyan else StadiumConcrete
                        val txtColor = if (isSelected) Color.Black else Color.White
                        
                        Box(
                            modifier = Modifier
                                .background(btnColor, shape = RoundedCornerShape(8.dp))
                                .clickable { activeTab = index }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = pair.second,
                                    contentDescription = null,
                                    tint = txtColor,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = pair.first,
                                    color = txtColor,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        },
        text = {
            val scrollState = rememberScrollState()
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                when (activeTab) {
                    0 -> {
                        // === TAB 0: BRACKET & COPA WAITLIST (Original enhanced) ===
                        // Slogan / positioning statement section
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = StadiumConcrete),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .background(
                                        brush = Brush.verticalGradient(
                                            colors = listOf(StadiumConcrete, StadiumGlow)
                                        )
                                    )
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "\"Quando a Copa acabar,\nsua coleção continua.\"",
                                    color = NeonEmerald,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 26.sp
                                )
                                Text(
                                    text = "Inscreva-se na lista de espera antes de 11/06 para garantir seu Card de Fundador \"Origem\". Palpite no campeão para ganhar pacotes extras!",
                                    color = Color.White.copy(alpha = 0.75f),
                                    fontSize = 12.sp,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 16.sp
                                )
                            }
                        }

                        // Countdown Timer Section
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = StadiumConcrete),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(Color(0xFFE91E63).copy(alpha = 0.15f), shape = CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Timer,
                                        contentDescription = null,
                                        tint = Color(0xFFFF4081)
                                    )
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "PROMOÇÃO DE FUNDADOR EXPIRA EM:",
                                        color = Color.White.copy(alpha = 0.6f),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.5.sp
                                    )
                                    Text(
                                        text = "Restam $remainingDays dias e $remainingHours horas para se qualificar!",
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                            }
                        }

                        // Founder Card Origem Preview Section
                        Text(
                            text = "🎁 CARD DE FUNDADOR EXCLUSIVO",
                            color = NeonCyan,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Start
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val founderCard = CardCatalog.getCardById(30)
                            if (founderCard != null) {
                                Box(
                                    modifier = Modifier
                                        .width(110.dp)
                                        .height(175.dp)
                                ) {
                                    FutCardView(
                                        card = founderCard,
                                        upgradeLevel = 0,
                                        stickerEmoji = "⭐"
                                    )
                                }
                            }

                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "Card de Fundador \"Origem\" ⚡",
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Raridade: LENDÁRIA/ANIMADA\nNível Inicial: 95 MEI\n\nEssa obra de arte digital só é garantida gratuitamente para quem se cadastrar na lista antes de 11 de junho de 2026.",
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp
                                )
                            }
                        }

                        Divider(color = Color.White.copy(alpha = 0.1f))

                        // Free Prediction Bracket Waitlist Form
                        if (!isWaitlistRegistered) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.EmojiEvents,
                                        contentDescription = null,
                                        tint = NeonCyan,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = "BRACKET GRÁTIS — PALPITE DO CAMPEÃO",
                                        color = NeonCyan,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }

                                Text(
                                    text = "Digite seu e-mail e selecione o Campeão da Copa de 2026. Acertar o palpite concede pacotes lendários adicionais no lançamento do app (22/07/2026)!",
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp
                                )

                                OutlinedTextField(
                                    value = inputEmail,
                                    onValueChange = { inputEmail = it },
                                    placeholder = { Text("Seu melhor e-mail...", fontSize = 12.sp) },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Email,
                                            contentDescription = null,
                                            tint = Color.White.copy(alpha = 0.5f)
                                        )
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        focusedBorderColor = NeonCyan,
                                        unfocusedBorderColor = Color.White.copy(alpha = 0.2f)
                                    ),
                                    singleLine = true
                                )

                                Box(modifier = Modifier.fillMaxWidth()) {
                                    OutlinedTextField(
                                        value = selectedPrediction,
                                        onValueChange = {},
                                        readOnly = true,
                                        label = { Text("Palpitar Campeão da Copa") },
                                        trailingIcon = {
                                            IconButton(onClick = { expandedDropdown = true }) {
                                                Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = "Seta")
                                            }
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { expandedDropdown = true },
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White,
                                            focusedBorderColor = NeonCyan,
                                            unfocusedBorderColor = Color.White.copy(alpha = 0.2f)
                                        )
                                    )
                                    DropdownMenu(
                                        expanded = expandedDropdown,
                                        onDismissRequest = { expandedDropdown = false },
                                        modifier = Modifier
                                            .fillMaxWidth(0.85f)
                                            .background(StadiumGlow)
                                    ) {
                                        countriesList.forEach { country ->
                                            DropdownMenuItem(
                                                text = { Text(country, color = Color.White) },
                                                onClick = {
                                                    selectedPrediction = country
                                                    expandedDropdown = false
                                                }
                                            )
                                        }
                                    }
                                }

                                Button(
                                    onClick = {
                                        viewModel.registerInWaitlist(inputEmail, selectedPrediction) { success, msg ->
                                            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(46.dp),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = "GARANTIR CARD DE FUNDADOR E PALPITAR",
                                        color = Color.Black,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        } else {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = StadiumConcrete),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .border(1.dp, NeonEmerald, shape = RoundedCornerShape(12.dp))
                                        .padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "✨ INSCRIÇÃO CONCLUÍDA!",
                                        color = NeonEmerald,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(StadiumGlow, shape = RoundedCornerShape(8.dp))
                                            .padding(12.dp),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(text = "E-mail: $waitlistEmail", color = Color.White, fontSize = 12.sp)
                                        Text(text = "Palpite da Copa: $predictedChampion", color = NeonCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        Text(text = "Selo de Fundador: ATIVO!", color = NeonEmerald, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }
                        }
                    }

                    1 -> {
                        // === TAB 1: ESCALAÇÃO IDEAL ===
                        var selectedMatchLineup by remember { mutableStateOf("Marrocos 🇲🇦") }

                        val lineupPlayers = when (selectedMatchLineup) {
                            "Marrocos 🇲🇦" -> listOf(
                                Position.ATA to "Neymar Jr.", Position.ATA to "Vini Jr.", Position.ATA to "Rodrygo",
                                Position.MEI to "B. Guimarães", Position.MEI to "Paquetá", Position.MEI to "Casemiro",
                                Position.DFS to "Arana", Position.DFS to "Marquinhos", Position.DFS to "Militão", Position.DFS to "Danilo",
                                Position.GOL to "Alisson"
                            )
                            "Haiti 🇭🇹" -> listOf(
                                Position.ATA to "Neymar Jr.", Position.ATA to "Vini Jr.", Position.ATA to "Endrick",
                                Position.MEI to "B. Guimarães", Position.MEI to "João Gomes", Position.MEI to "Andreas P.",
                                Position.DFS to "Arana", Position.DFS to "Beraldo", Position.DFS to "Militão", Position.DFS to "Yan Couto",
                                Position.GOL to "Bento"
                            )
                            else -> listOf( // "Escócia 🏴󠁧󠁢󠁳󠁣󠁴󠁿"
                                Position.ATA to "Raphinha", Position.ATA to "Vini Jr.", Position.ATA to "Rodrygo",
                                Position.MEI to "B. Guimarães", Position.MEI to "G. Martinelli", Position.MEI to "Douglas Luiz",
                                Position.DFS to "Wendell", Position.DFS to "Gabriel M.", Position.DFS to "Marquinhos", Position.DFS to "Danilo",
                                Position.GOL to "Alisson"
                            )
                        }

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = StadiumConcrete),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "📋 Post de Pré-Jogo: Escalação Ideal",
                                    color = NeonCyan,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Text(
                                    text = "Antes de cada jogo do Brasil, publicamos a escalação ideal de craques em formato de card para gerar debates, retweets e novos pré-cadastros na lista.",
                                    color = Color.White.copy(alpha = 0.75f),
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp
                                )

                                // Match selector Row
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    listOf("Marrocos 🇲🇦", "Haiti 🇭🇹", "Escócia 🏴󠁧󠁢󠁳󠁣󠁴󠁿").forEach { opponent ->
                                        val isSelected = selectedMatchLineup == opponent
                                        Box(
                                            modifier = Modifier
                                                .background(if (isSelected) NeonCyan else StadiumGlow, shape = RoundedCornerShape(6.dp))
                                                .clickable { selectedMatchLineup = opponent }
                                                .weight(1f)
                                                .padding(vertical = 6.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = opponent,
                                                color = if (isSelected) Color.Black else Color.White,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                // Visual Tactical Football pitch emulation
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(290.dp)
                                        .background(Color(0xFF0F3B1A), shape = RoundedCornerShape(12.dp))
                                        .border(1.dp, Color.White.copy(alpha = 0.15f), shape = RoundedCornerShape(12.dp))
                                        .padding(8.dp)
                                ) {
                                    // Custom pitch markings overlays using simple lines
                                    Column(
                                        modifier = Modifier.fillMaxSize(),
                                        verticalArrangement = Arrangement.SpaceBetween,
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        // 1. Attack Row (4-3-3: 3 forwards)
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceEvenly
                                        ) {
                                            lineupPlayers.filter { it.first == Position.ATA }.forEach { pair ->
                                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(34.dp)
                                                            .background(Color(0xFFFF5252).copy(alpha = 0.15f), shape = CircleShape)
                                                            .border(1.dp, Color(0xFFFF5252), shape = CircleShape),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Icon(imageVector = Icons.Default.DirectionsRun, contentDescription = null, tint = Color(0xFFFF5252), modifier = Modifier.size(16.dp))
                                                    }
                                                    Text(pair.second, color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                                    Text("ATA", color = Color(0xFFFF8A80), fontSize = 7.sp, fontWeight = FontWeight.SemiBold)
                                                }
                                            }
                                        }

                                        // 2. Midfield Row (3 midfielders)
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceEvenly
                                        ) {
                                            lineupPlayers.filter { it.first == Position.MEI }.forEach { pair ->
                                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(34.dp)
                                                            .background(Color(0xFFFFD740).copy(alpha = 0.15f), shape = CircleShape)
                                                            .border(1.dp, Color(0xFFFFD740), shape = CircleShape),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Icon(imageVector = Icons.Default.Radar, contentDescription = null, tint = Color(0xFFFFD740), modifier = Modifier.size(16.dp))
                                                    }
                                                    Text(pair.second, color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                                    Text("MEI", color = Color(0xFFFFE57F), fontSize = 7.sp, fontWeight = FontWeight.SemiBold)
                                                }
                                            }
                                        }

                                        // 3. Defense Row (4 defenders)
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceEvenly
                                        ) {
                                            lineupPlayers.filter { it.first == Position.DFS }.forEach { pair ->
                                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(34.dp)
                                                            .background(Color(0xFF40C4FF).copy(alpha = 0.15f), shape = CircleShape)
                                                            .border(1.dp, Color(0xFF40C4FF), shape = CircleShape),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Icon(imageVector = Icons.Default.Shield, contentDescription = null, tint = Color(0xFF40C4FF), modifier = Modifier.size(16.dp))
                                                    }
                                                    Text(pair.second, color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                                    Text("DFS", color = Color(0xFF82B1FF), fontSize = 7.sp, fontWeight = FontWeight.SemiBold)
                                                }
                                            }
                                        }

                                        // 4. Goalkeeper Row (1 goalie)
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            lineupPlayers.filter { it.first == Position.GOL }.forEach { pair ->
                                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(34.dp)
                                                            .background(Color(0xFF69F0AE).copy(alpha = 0.15f), shape = CircleShape)
                                                            .border(1.dp, Color(0xFF69F0AE), shape = CircleShape),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Icon(imageVector = Icons.Default.PanTool, contentDescription = null, tint = Color(0xFF69F0AE), modifier = Modifier.size(16.dp))
                                                    }
                                                    Text(pair.second, color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                                    Text("GOL", color = Color(0xFFB9F6CA), fontSize = 7.sp, fontWeight = FontWeight.SemiBold)
                                                }
                                            }
                                        }
                                    }
                                }

                                Divider(color = Color.White.copy(alpha = 0.1f))

                                Button(
                                    onClick = {
                                        val templateText = """
                                            📋 ESCALAÇÃO IDEAL DO BRASIL vs $selectedMatchLineup 🇧🇷
                                            Os 11 guerreiros recomendados pelo CardClash estão escalados na formação de gala 4-3-3!
                                            
                                            ⚽ Ataque: ${lineupPlayers.filter { it.first == Position.ATA }.map { it.second }.joinToString(" - ")}
                                            🛡️ Defesa sólida de Copa do Mundo.
                                            
                                            💥 Quando a Copa de 2026 fechar, nossa coleção segue viva com os melhores do Brasileirão e copas continentais! Garanta já o seu Card de Fundador "Origem": CardClash no ar dia 22/07!
                                            👉 Participe do pré-cadastro oficial! #CardClash #SelecaoBrasileira #Copa2026
                                        """.trimIndent()

                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        clipboard.setPrimaryClip(ClipData.newPlainText("lineup_post", templateText))
                                        Toast.makeText(context, "Post de Escalação Ideal de $selectedMatchLineup copiado para transferência! Publique nos Stories ou X!", Toast.LENGTH_LONG).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.Black)
                                        Text("COPIAR ARTE E TEXTO DO POST 📲", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    2 -> {
                        // === TAB 2: ESCALAÇÃO DO CARD DO JOGO (MOTM / Destaques pós-apito) ===
                        var selectedMatchMOTM by remember { mutableStateOf("Marrocos 🇲🇦") }

                        val motmCardsMap = mapOf(
                            "Marrocos 🇲🇦" to PlayerCard(
                                id = 301,
                                name = "Bruno G. (MOTM)",
                                clubAndCountry = "Brasil / Estilo Geral",
                                position = Position.MEI,
                                overall = 93,
                                stats = PlayerStats(82, 85, 93, 89, 88, 92),
                                rarity = Rarity.LENDARIA,
                                initialHexColor = "#FFD700",
                                photoUrl = "https://images.unsplash.com/photo-1544005313-94ddf0286df2?w=150&auto=format&fit=crop&q=80",
                                league = "Copa do Mundo"
                            ),
                            "Haiti 🇭🇹" to PlayerCard(
                                id = 302,
                                name = "Rodrygo (MOTM)",
                                clubAndCountry = "Brasil / Genérico",
                                position = Position.ATA,
                                overall = 92,
                                stats = PlayerStats(91, 91, 88, 93, 35, 70),
                                rarity = Rarity.ANIMADA,
                                initialHexColor = "#FF007F",
                                photoUrl = "https://images.unsplash.com/photo-1544005313-94ddf0286df2?w=150&auto=format&fit=crop&q=80",
                                league = "Copa do Mundo"
                            ),
                            "Escócia 🏴󠁧󠁢󠁳󠁣󠁴󠁿" to PlayerCard(
                                id = 303,
                                name = "Vini Jr. (MOTM)",
                                clubAndCountry = "Brasil / Genérico",
                                position = Position.ATA,
                                overall = 95,
                                stats = PlayerStats(98, 92, 86, 96, 38, 80),
                                rarity = Rarity.LENDARIA,
                                initialHexColor = "#00E5FF",
                                photoUrl = "https://images.unsplash.com/photo-1544005313-94ddf0286df2?w=150&auto=format&fit=crop&q=80",
                                league = "Copa do Mundo"
                            )
                        )

                        val motmDetailsMap = mapOf(
                            "Marrocos 🇲🇦" to Triple(
                                "Placar Final: Brasil 2 x 1 Marrocos",
                                "Comandou o meio-campo com inteligência, obteve 94% de aproveitamento nos passes, cobriu toda a extensão defensiva e deu a assistência decisiva no gol que sacramentou a vitória da partida!",
                                "⭐️ BRUNO GUIMARÃES"
                            ),
                            "Haiti 🇭🇹" to Triple(
                                "Placar Final: Brasil 5 x 0 Haiti",
                                "Grande show no ataque! Marcou dois gols assombrosos de fora da área, distribuiu dribles estonteantes na lateral e garantiu o selo 'Man of the Match' em meio à goleada histórica brasileira!",
                                "⚡ RODRYGO"
                            ),
                            "Escócia 🏴󠁧󠁢󠁳󠁣󠁴󠁿" to Triple(
                                "Placar Final: Brasil 3 x 1 Escócia",
                                "Velocidade letal! O lateral simplesmente não conseguiu segurar. Fechou o placar com uma arrancada fantástica e golaço no final do segundo tempo, provando seu nível incomparável!",
                                "👑 VINICIUS JR."
                            )
                        )

                        val currentMOTMCard = motmCardsMap[selectedMatchMOTM]!!
                        val currentMatchLog = motmDetailsMap[selectedMatchMOTM]!!

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = StadiumConcrete),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "🏆 Card do Jogo (Man of the Match)",
                                    color = NeonCyan,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Text(
                                    text = "Instantes após o encerramento do jogo, o Card do Jogo destaca o herói brasileiro da partida real. Garantimos na publicação: 'Ele estará ativo e colecionável em sua conta!'",
                                    color = Color.White.copy(alpha = 0.75f),
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp
                                )

                                // Select matchup
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    listOf("Marrocos 🇲🇦", "Haiti 🇭🇹", "Escócia 🏴󠁧󠁢󠁳󠁣󠁴󠁿").forEach { opponent ->
                                        val isSelected = selectedMatchMOTM == opponent
                                        Box(
                                            modifier = Modifier
                                                .background(if (isSelected) NeonCyan else StadiumGlow, shape = RoundedCornerShape(6.dp))
                                                .clickable { selectedMatchMOTM = opponent }
                                                .weight(1f)
                                                .padding(vertical = 6.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                opponent,
                                                color = if (isSelected) Color.Black else Color.White,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }

                                // Interactive Glowing Card showcase
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(StadiumGlow, shape = RoundedCornerShape(12.dp))
                                        .border(1.dp, NeonCyan.copy(alpha = 0.3f), shape = RoundedCornerShape(12.dp))
                                        .padding(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .width(110.dp)
                                                .height(175.dp)
                                        ) {
                                            FutCardView(
                                                card = currentMOTMCard,
                                                upgradeLevel = 2,
                                                stickerEmoji = "🎖️"
                                            )
                                        }

                                        Column(
                                            modifier = Modifier.weight(1f),
                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Text(
                                                text = currentMatchLog.third,
                                                color = NeonCyan,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Black
                                            )
                                            Text(
                                                text = currentMatchLog.first,
                                                color = Color.White,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = currentMatchLog.second,
                                                color = Color.White.copy(alpha = 0.7f),
                                                fontSize = 10.sp,
                                                lineHeight = 13.sp
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Box(
                                                modifier = Modifier
                                                    .background(NeonEmerald.copy(alpha = 0.15f), shape = RoundedCornerShape(4.dp))
                                                    .border(1.dp, NeonEmerald, shape = RoundedCornerShape(4.dp))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = "GARANTIDO NO APP REAL EM 22/07!",
                                                    color = NeonEmerald,
                                                    fontSize = 8.sp,
                                                    fontWeight = FontWeight.Black
                                                )
                                            }
                                        }
                                    }
                                }

                                Button(
                                    onClick = {
                                        val matchSummary = """
                                            🚨 ACALMA O CORAÇÃO! Apito final e temos o homem do jogo! 🇧🇷 
                                            Com atuação mágica no triunfo brasileiro, o meio-campista `${currentMOTMCard.name}` foi consagrado o Card do Jogo da rodada Copa 2026!
                                            
                                            🔥 Esse card supremo está garantido para distribuição oficial na plataforma! Quando a Copa do Mundo fechar, a nossa diversão colecionável segue ativa com as divisões e ligas brasileiras.
                                            👉 Cadastre-se na lista de espera, jogue a demo do bafo na web e segure sua vaga! #CardClash #Copa2026 #Brasil
                                        """.trimIndent()

                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        clipboard.setPrimaryClip(ClipData.newPlainText("motm_post", matchSummary))
                                        Toast.makeText(context, "Roteiro viral de post 'Card do Jogo' com bônus copiado! Compartilhe!", Toast.LENGTH_LONG).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.Black)
                                        Text("COMPARTILHAR POST DE DESTAQUE VIRAL 📲", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    3 -> {
                        // === TAB 3: QUIZ DIÁRIO INSTAGRAM STORIES ===
                        var activeQuizIndex by remember { mutableStateOf(0) } // 0 = Sponsor, 1 = Score Guess
                        var quiz1AnswerSelected by remember { mutableStateOf<Int?>(null) }
                        var quiz2AnswerSelected by remember { mutableStateOf<Int?>(null) }
                        var hasAnsweredQuiz1 by remember { mutableStateOf(false) }
                        var hasAnsweredQuiz2 by remember { mutableStateOf(false) }

                        val quiz1Options = listOf(
                            "Samsung (Parceiro de Hardware) 📱",
                            "Coca-Cola (Códigos Físicos & Grátis) 🥤",
                            "Pepsi Co (Parceiro de Lanchonete) 🥤",
                            "Nike (Parceria Relevante de Botas) 👟"
                        )
                        val quiz1Correct = 1 // Coca-Cola

                        val quiz2Options = listOf(
                            "Brasil 1 x 0 Escócia",
                            "Brasil 2 x 0 Escócia",
                            "Brasil 3 x 1 Escócia ( late winner Vini Jr!) 🏴󠁧󠁢󠁳󠁣󠁴󠁿",
                            "Brasil 0 x 1 Escócia"
                        )
                        val quiz2Correct = 2 // Brasil 3x1

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = StadiumConcrete),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "❓ Quiz Diário: Simulador de Stories",
                                    color = NeonCyan,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Text(
                                    text = "Os colecionadores respondem diariamente ao Quiz no Instagram Stories para testar seus conhecimentos. Acertar o Quiz acelera a sua fila!",
                                    color = Color.White.copy(alpha = 0.75f),
                                    fontSize = 11.sp,
                                    lineHeight = 14.sp
                                )

                                // Current waitlist positioning badge
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(StadiumGlow, shape = RoundedCornerShape(10.dp))
                                        .border(1.dp, if (isWaitlistRegistered) NeonEmerald.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.1f), shape = RoundedCornerShape(10.dp))
                                        .padding(12.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = if (isWaitlistRegistered) "STATUS DO SEU PRÉ-CADASTRO" else "AUMENTE SUAS CHANCES",
                                            color = Color.White.copy(alpha = 0.5f),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = if (isWaitlistRegistered) "E-mail: $waitlistEmail" else "Cadastre seu e-mail no primeiro tab para resgatar posições!",
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Text(
                                                text = "Sua Posição na Fila:",
                                                color = Color.White,
                                                fontSize = 12.sp
                                            )
                                            Text(
                                                text = "#$waitlistQueuePosition",
                                                color = if (isWaitlistRegistered) NeonEmerald else NeonCyan,
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Black
                                            )
                                        }
                                        if (isWaitlistRegistered) {
                                            Text(
                                                text = when {
                                                    waitlistQueuePosition <= 1500 -> "🏆 LOTE DE ELITE DIAMANTE (Acesso Primário Confirmado)"
                                                    waitlistQueuePosition <= 5000 -> "⚡ LOTE ACELERADO OURO"
                                                    waitlistQueuePosition <= 10000 -> "✨ LOTE DE PRATA"
                                                    else -> "🥉 LOTE BRONZE (Suba acertando os quizzes!)"
                                                },
                                                color = if (waitlistQueuePosition <= 5000) NeonEmerald else NeonCyan,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                modifier = Modifier.padding(top = 4.dp)
                                            )
                                        }
                                    }
                                }

                                // Interactive instagram story mockup container
                                Box(
                                    modifier = Modifier
                                        .width(220.dp)
                                        .height(340.dp)
                                        .border(2.dp, Color(0xFFC13584), shape = RoundedCornerShape(16.dp)) // Instagram gradient color border
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(
                                            Brush.verticalGradient(
                                                colors = listOf(Color(0xFFC13584), Color(0xFFE1306C), Color(0xFFF77737))
                                            )
                                        )
                                        .padding(12.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.fillMaxSize(),
                                        verticalArrangement = Arrangement.SpaceBetween,
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        // Header Stories Bar
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(3.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            repeat(3) { index ->
                                                val progress = if (index < activeQuizIndex) 1f else if (index == activeQuizIndex) 0.5f else 0f
                                                LinearProgressIndicator(
                                                    progress = { progress },
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .height(2.dp)
                                                        .clip(RoundedCornerShape(1.dp)),
                                                    color = Color.White,
                                                    trackColor = Color.White.copy(alpha = 0.3f)
                                                )
                                            }
                                        }

                                        // Story Poster details
                                        Row(
                                            modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(24.dp)
                                                    .background(Color.White, shape = CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text("⚽", fontSize = 12.sp)
                                            }
                                            Column {
                                                Text("cardclash_oficial", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Black)
                                                Text("Copa 2026 Campanha • 12h", color = Color.White.copy(alpha = 0.6f), fontSize = 8.sp)
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(10.dp))

                                        // Quiz Card representation inside stories
                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = CardDefaults.cardColors(containerColor = Color.White),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(10.dp),
                                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                // Quiz icon
                                                Box(
                                                    modifier = Modifier
                                                        .background(Color(0xFFE1306C).copy(alpha = 0.1f), shape = CircleShape)
                                                        .padding(6.dp)
                                                ) {
                                                    Icon(imageVector = Icons.Default.Quiz, contentDescription = null, tint = Color(0xFFE1306C), modifier = Modifier.size(20.dp))
                                                }

                                                Text(
                                                    text = if (activeQuizIndex == 0) {
                                                        "Qual marca patrocinará nossos cupons e pacotes físicos de recarga grátis no app?"
                                                    } else {
                                                        "Qual o placar final do jogão do Brasil contra a Escócia na Copa de 2026?"
                                                    },
                                                    color = Color.Black,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    textAlign = TextAlign.Center,
                                                    lineHeight = 13.sp
                                                )

                                                // Options blocks
                                                Column(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                                ) {
                                                    val currentOptions = if (activeQuizIndex == 0) quiz1Options else quiz2Options
                                                    val selectedAns = if (activeQuizIndex == 0) quiz1AnswerSelected else quiz2AnswerSelected
                                                    val correctAnsIndex = if (activeQuizIndex == 0) quiz1Correct else quiz2Correct
                                                    val hasAnsweredCurrent = if (activeQuizIndex == 0) hasAnsweredQuiz1 else hasAnsweredQuiz2

                                                    currentOptions.forEachIndexed { opIdx, opText ->
                                                        val isCorrect = opIdx == correctAnsIndex
                                                        val isWrongSelected = opIdx == selectedAns && !isCorrect
                                                        
                                                        val blockBg = when {
                                                            hasAnsweredCurrent && isCorrect -> Color(0xFF25D366).copy(alpha = 0.15f)
                                                            hasAnsweredCurrent && isWrongSelected -> Color(0xFFFF5252).copy(alpha = 0.15f)
                                                            opIdx == selectedAns -> Color(0xFFC13584).copy(alpha = 0.1f)
                                                            else -> Color.Gray.copy(alpha = 0.05f)
                                                        }
                                                        val blockBorder = when {
                                                            hasAnsweredCurrent && isCorrect -> Color(0xFF25D366)
                                                            hasAnsweredCurrent && isWrongSelected -> Color(0xFFFF5252)
                                                            opIdx == selectedAns -> Color(0xFFC13584)
                                                            else -> Color.Black.copy(alpha = 0.1f)
                                                        }

                                                        Box(
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                                .background(blockBg, shape = RoundedCornerShape(8.dp))
                                                                .border(1.dp, blockBorder, shape = RoundedCornerShape(8.dp))
                                                                .clickable(enabled = !hasAnsweredCurrent) {
                                                                    if (activeQuizIndex == 0) {
                                                                        quiz1AnswerSelected = opIdx
                                                                        hasAnsweredQuiz1 = true
                                                                        if (opIdx == quiz1Correct) {
                                                                            viewModel.boostWaitlistPosition(1500)
                                                                        }
                                                                    } else {
                                                                        quiz2AnswerSelected = opIdx
                                                                        hasAnsweredQuiz2 = true
                                                                        if (opIdx == quiz2Correct) {
                                                                            viewModel.boostWaitlistPosition(2000)
                                                                        }
                                                                    }
                                                                }
                                                                .padding(horizontal = 8.dp, vertical = 6.dp)
                                                        ) {
                                                            Row(
                                                                modifier = Modifier.fillMaxWidth(),
                                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                                verticalAlignment = Alignment.CenterVertically
                                                            ) {
                                                                Text(
                                                                    text = opText,
                                                                    color = Color.Black,
                                                                    fontSize = 9.sp,
                                                                    fontWeight = FontWeight.Bold
                                                                )
                                                                if (hasAnsweredCurrent) {
                                                                    if (isCorrect) {
                                                                        Text("✅", fontSize = 9.sp)
                                                                    } else if (isWrongSelected) {
                                                                        Text("❌", fontSize = 9.sp)
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(10.dp))

                                        // Navigation dots or status update success
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            val currentAnswer = if (activeQuizIndex == 0) quiz1AnswerSelected else quiz2AnswerSelected
                                            val correctInd = if (activeQuizIndex == 0) quiz1Correct else quiz2Correct
                                            val answered = if (activeQuizIndex == 0) hasAnsweredQuiz1 else hasAnsweredQuiz2

                                            if (answered) {
                                                val scoreSuccess = currentAnswer == correctInd
                                                Text(
                                                    text = if (scoreSuccess) "✨ +${if (activeQuizIndex == 0) "1.500" else "2.000"} POSIÇÕES!" else "❌ Resposta incorreta! Tente a outra!",
                                                    color = if (scoreSuccess) NeonEmerald else Color.White,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Black
                                                )
                                                Text(
                                                    text = if (scoreSuccess) "Você subiu na fila do app!" else "Continue acompanhando os Stories diários!",
                                                    color = Color.White.copy(alpha = 0.8f),
                                                    fontSize = 8.sp,
                                                    textAlign = TextAlign.Center
                                                )
                                            } else {
                                                Text(
                                                    text = "Clique na sua opção para votar!",
                                                    color = Color.White,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                }

                                // Simulator action controls
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            activeQuizIndex = 0
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = if (activeQuizIndex == 0) NeonCyan else StadiumGlow),
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("Quiz 1: Patrocinador", color = if (activeQuizIndex == 0) Color.Black else Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    }

                                    Button(
                                        onClick = {
                                            activeQuizIndex = 1
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = if (activeQuizIndex == 1) NeonCyan else StadiumGlow),
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("Quiz 2: Placar", color = if (activeQuizIndex == 1) Color.Black else Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    4 -> {
                        // === TAB 4: GRUPOS VIP ("Primeiros 1.000 Colecionadores") ===
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = StadiumConcrete),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = "💬 Comunidade VIP: Primeiro Lote",
                                    color = NeonCyan,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    text = "Formamos os grupos de fundadores para aproximar colecionadores e garantir troca viral de IDs e cupons de marcas locais parceiras.",
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp
                                )

                                // Live Scarcity Progress Bar
                                val spotsOccupied = 842
                                val totalSpots = 1000
                                val progressRatio = spotsOccupied.toFloat() / totalSpots.toFloat()

                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(StadiumGlow, shape = RoundedCornerShape(8.dp))
                                        .padding(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Grupo de Elite \"Primeiros 1.000\"",
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "$spotsOccupied / $totalSpots vagas",
                                            color = NeonEmerald,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Black
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    LinearProgressIndicator(
                                        progress = { progressRatio },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(8.dp)
                                            .clip(RoundedCornerShape(4.dp)),
                                        color = NeonEmerald,
                                        trackColor = Color.White.copy(alpha = 0.1f)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "🚨 FOMO: Apenas ${totalSpots - spotsOccupied} posições restam para garantir bônus!",
                                        color = Color(0xFFFF4081),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            // Handle WhatsApp Join
                                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                            clipboard.setPrimaryClip(ClipData.newPlainText("whatsapp_group", "https://chat.whatsapp.com/invite/CardClash1000VipsSimulated"))
                                            Toast.makeText(context, "Link do grupo VIP de WhatsApp copiado para transferência! Bem-vindo colecionador #$spotsOccupied!", Toast.LENGTH_LONG).show()
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(imageVector = Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.Black)
                                            Text("WhatsApp", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    Button(
                                        onClick = {
                                            // Handle Discord Join
                                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                            clipboard.setPrimaryClip(ClipData.newPlainText("discord_group", "https://discord.gg/cardclash2026simulated"))
                                            Toast.makeText(context, "Link do servidor oficial Discord copiado para transferência! Você se juntará a $spotsOccupied outros!", Toast.LENGTH_LONG).show()
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5865F2)),
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(imageVector = Icons.Default.Forum, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                                            Text("Discord", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    5 -> {
                        // === TAB 5: ESTÚDIO TIKTOK/REELS (Videos of 8 favorites) ===
                        var selectedVideoCountry by remember { mutableStateOf("Brasil 🇧🇷") }
                        var filterEffect by remember { mutableStateOf("Glow Clássico ✨") }

                        val countryStatsMap = mapOf(
                            "Brasil 🇧🇷" to PlayerCard(31, "Neymar Jr. (Original)", "Seleção Brasileira / Genérico", Position.MEI, 93, PlayerStats(78, 88, 93, 94, 30, 60), Rarity.ESPECIAL, "#D4AF37", "https://images.unsplash.com/photo-1544005313-94ddf0286df2?w=150&auto=format&fit=crop&q=80", league = "Copa do Mundo"),
                            "Espanha 🇪🇸" to PlayerCard(32, "Lamine Yamal (Estilo)", "Espanha / Original", Position.ATA, 91, PlayerStats(93, 85, 87, 92, 28, 64), Rarity.ANIMADA, "#FF0000", league = "Copa do Mundo"),
                            "França 🇫🇷" to PlayerCard(33, "Mbappé (Estilizado)", "França / Original", Position.ATA, 93, PlayerStats(97, 90, 80, 92, 36, 78), Rarity.LENDARIA, "#00BFFF", league = "Copa do Mundo"),
                            "Inglaterra 🏴󠁧󠁢󠁥󠁮󠁧󠁿" to PlayerCard(34, "Bellingham (Estilizado)", "Inglaterra / Original", Position.MEI, 90, PlayerStats(80, 83, 83, 88, 78, 82), Rarity.ASSINADA, "#E6E6FA", league = "Copa do Mundo"),
                            "Argentina 🇦🇷" to PlayerCard(35, "Messi (Estilizado)", "Argentina / Original", Position.MEI, 94, PlayerStats(82, 91, 92, 94, 33, 65), Rarity.LENDARIA, "#FF69B4", league = "Copa do Mundo"),
                            "Uruguai 🇺🇾" to PlayerCard(36, "Fede Valverde (Original)", "Uruguai / Estilo", Position.MEI, 89, PlayerStats(87, 82, 85, 84, 80, 89), Rarity.ESPECIAL, "#32CD32", league = "Copa do Mundo"),
                            "Portugal 🇵🇹" to PlayerCard(37, "Cristiano R. (Original)", "Portugal / Estilo", Position.ATA, 91, PlayerStats(80, 92, 81, 84, 35, 80), Rarity.ASSINADA, "#FFA500", league = "Copa do Mundo"),
                            "Alemanha 🇩🇪" to PlayerCard(38, "Musiala (Original)", "Alemanha / Estilo", Position.MEI, 89, PlayerStats(84, 79, 83, 91, 55, 70), Rarity.ESPECIAL, "#FFFFFF", league = "Copa do Mundo")
                        )

                        val currentPreviewCard = countryStatsMap[selectedVideoCountry] ?: countryStatsMap["Brasil 🇧🇷"]!!

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = StadiumConcrete),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "🎬 Estúdio de Vídeo: 9:16 Shorts/TikTok",
                                    color = NeonCyan,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Text(
                                    text = "Produza materiais virais de divulgação rápida destacando as nossas 8 favoritas da Copa de 2026. A logo do app é incorporada no rodapé como marca d'água.",
                                    color = Color.White.copy(alpha = 0.75f),
                                    fontSize = 11.sp,
                                    lineHeight = 14.sp
                                )

                                // Horizontal Country Selector
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    countriesList.forEach { country ->
                                        val isSelected = selectedVideoCountry == country
                                        Box(
                                            modifier = Modifier
                                                .background(if (isSelected) NeonCyan else StadiumGlow, shape = RoundedCornerShape(6.dp))
                                                .clickable { selectedVideoCountry = country }
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text(country, color = if (isSelected) Color.Black else Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }

                                // 9:16 Vertical Video Frame Emulator
                                Box(
                                    modifier = Modifier
                                        .width(200.dp)
                                        .height(355.dp) // 9:16 aspect ratio
                                        .border(2.dp, NeonCyan, shape = RoundedCornerShape(16.dp))
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(StadiumGlow)
                                        .padding(12.dp),
                                    contentAlignment = Alignment.TopCenter
                                ) {
                                    Column(
                                        modifier = Modifier.fillMaxSize(),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        // Top Sound effect tag
                                        Box(
                                            modifier = Modifier
                                                .background(Color.Black.copy(alpha = 0.6f), shape = RoundedCornerShape(20.dp))
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Icon(imageVector = Icons.Default.MusicNote, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(10.dp))
                                                Text("Áudio Viral: Phonk de Copa (Simulado)", color = Color.White, fontSize = 8.sp)
                                            }
                                        }

                                        // Card center view
                                        Box(
                                            modifier = Modifier
                                                .width(130.dp)
                                                .height(200.dp)
                                        ) {
                                            FutCardView(
                                                card = currentPreviewCard,
                                                upgradeLevel = 2,
                                                stickerEmoji = "🎖️"
                                            )
                                        }

                                        // Footer Watermark & Branding Area
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.spacedBy(1.dp)
                                        ) {
                                            Text(
                                                text = "⚽ CARDCLASH ⚽",
                                                color = Color.White,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Black,
                                                letterSpacing = 1.sp
                                            )
                                            Text(
                                                text = "\"Quando a Copa acabar, sua coleção continua.\"",
                                                color = NeonCyan,
                                                fontSize = 7.sp,
                                                fontWeight = FontWeight.Bold,
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            val narrationScript = """
                                                🚨 ATENÇÃO COLECIONADORES! Veja essa carta absurda do `${currentPreviewCard.name}` (Overall ${currentPreviewCard.overall}) na Copa 2026! 🏆 
                                                Adquira seu Card Especial de Fundador 'Origem' totalmente de graça! 
                                                🔗 Link na Bio: Quando a Copa acabar, nossa coleção continua! #CardClash #Futebol #Copa2026
                                            """.trimIndent()
                                            
                                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                            clipboard.setPrimaryClip(ClipData.newPlainText("tiktok_script", narrationScript))
                                            Toast.makeText(context, "Roteiro viral e hashtags de divulgação copiados com sucesso! Cole no TikTok/Reels!", Toast.LENGTH_LONG).show()
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = StadiumGlow),
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("Copiar Roteiro 📋", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }

                                    Button(
                                        onClick = {
                                            Toast.makeText(context, "Simulação de renderização concluída! O vídeo de 9:16 da seleção de $selectedVideoCountry com a marca d'água foi saved com sucesso!", Toast.LENGTH_SHORT).show()
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("Renderizar e Salvar 🎬", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    6 -> {
                        // === TAB 6: EMBAIXADORES / MICROINFLUENCERS PORTAL ===
                        var creatorHandle by remember { mutableStateOf("") }
                        var selectionFollowers by remember { mutableStateOf("5k a 20k seguidores") }
                        var isRegisteredCreator by remember { mutableStateOf(false) }

                        val calculatedPackCommission = when (selectionFollowers) {
                            "Menos de 5k seguidores" -> 15
                            "5k a 20k seguidores" -> 50
                            "20k a 100k seguidores" -> 150
                            else -> 500
                        }

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = StadiumConcrete),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = "📣 Portal de Microinfluenciadores de Futebol",
                                    color = NeonCyan,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )

                                Text(
                                    text = "Oferecemos a criadores e influenciadores de futebol um kit exclusivo contendo a Carta de Fundador Embaixador personalizada ao seu handle, com códigos promocionais dedicados para seus seguidores.",
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontSize = 11.sp,
                                    lineHeight = 14.sp
                                )

                                if (!isRegisteredCreator) {
                                    Column(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        // Handle input
                                        OutlinedTextField(
                                            value = creatorHandle,
                                            onValueChange = { creatorHandle = it },
                                            placeholder = { Text("Ex: @futebol_raiz_2026", fontSize = 12.sp) },
                                            label = { Text("Seu handle do Instagram/TikTok") },
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedTextColor = Color.White,
                                                unfocusedTextColor = Color.White,
                                                focusedBorderColor = NeonCyan,
                                                unfocusedBorderColor = Color.White.copy(alpha = 0.2f)
                                            ),
                                            singleLine = true
                                        )

                                        // Followers dropdown picker simulate
                                        Text("Alcance Estimado / Canal:", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        Row(
                                            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            listOf("Menos de 5k", "5k a 20k", "20k a 100k", "100k+").forEach { range ->
                                                val isRangeSelected = selectionFollowers.startsWith(range)
                                                Box(
                                                    modifier = Modifier
                                                        .background(if (isRangeSelected) NeonCyan else StadiumGlow, shape = RoundedCornerShape(6.dp))
                                                        .clickable { selectionFollowers = "$range seguidores" }
                                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                                ) {
                                                    Text(range, color = if (isRangeSelected) Color.Black else Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(4.dp))

                                        // Live calculation preview
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(StadiumGlow, shape = RoundedCornerShape(8.dp))
                                                .padding(10.dp)
                                        ) {
                                            Column {
                                                Text("🎁 Benefícios Garantidos de Parceiro:", color = NeonEmerald, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                Text("• Carta de Fundador Embaixador customizada", color = Color.White, fontSize = 10.sp)
                                                Text("• Lote inicial de $calculatedPackCommission Pacotes Premium para sortear à comunidade", color = Color.White, fontSize = 10.sp)
                                                Text("• Cupom exclusivo: CLASH-${creatorHandle.replace("@", "").uppercase().ifBlank { "NOME" }} (1 grátis dia)", color = Color.White, fontSize = 10.sp)
                                            }
                                        }

                                        Button(
                                            onClick = {
                                                if (creatorHandle.isBlank()) {
                                                    Toast.makeText(context, "Por favor, digite seu handle ou nome de criador!", Toast.LENGTH_SHORT).show()
                                                } else {
                                                    isRegisteredCreator = true
                                                    Toast.makeText(context, "Parceria estimada com sucesso! O kit promocional foi gerado.", Toast.LENGTH_LONG).show()
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text("SOU CRIADOR & QUERO MEU KIT", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                } else {
                                    // Interactive Custom holographic Ambassador card preview
                                    Column(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            text = "Seu Card de Fundador Embaixador está pronto! 🚀",
                                            color = NeonEmerald,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )

                                        val ambCard = PlayerCard(
                                            id = 999,
                                            name = if (creatorHandle.length > 15) creatorHandle.take(15) else creatorHandle,
                                            clubAndCountry = "Embaixador CardClash",
                                            position = Position.ATA,
                                            overall = 99,
                                            stats = PlayerStats(99, 99, 99, 99, 99, 99),
                                            rarity = Rarity.LENDARIA,
                                            initialHexColor = "#FFD700",
                                            photoUrl = "https://images.unsplash.com/photo-1544005313-94ddf0286df2?w=150&auto=format&fit=crop&q=80",
                                            league = "Copa do Mundo"
                                        )

                                        Box(
                                            modifier = Modifier
                                                .width(130.dp)
                                                .height(205.dp)
                                                .border(2.dp, NeonCyan, shape = RoundedCornerShape(12.dp))
                                        ) {
                                            FutCardView(
                                                card = ambCard,
                                                upgradeLevel = 3,
                                                stickerEmoji = "👑"
                                            )
                                        }

                                        Text(
                                            text = "Holográfica premium exclusiva. Seu cupom de criador ativo para distribuição é:\n👉 CLASH-${creatorHandle.replace("@", "").uppercase()}",
                                            color = Color.White.copy(alpha = 0.8f),
                                            fontSize = 11.sp,
                                            textAlign = TextAlign.Center
                                        )

                                        Button(
                                            onClick = {
                                                isRegisteredCreator = false
                                                creatorHandle = ""
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = StadiumGlow),
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text("Voltar ao Cadastro", color = Color.White, fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    7 -> {
                        // === TAB 7: PATROCÍNIO LOCAL (GURANÁ DAORA & PIPOPA) ===
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = StadiumConcrete),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .background(NeonCyan.copy(alpha = 0.15f), shape = RoundedCornerShape(8.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.LocalDrink, contentDescription = null, tint = NeonCyan)
                                    }
                                    Column {
                                        Text(
                                            text = "🥤 Parceria Oficial: Guaraná Daora & Snacks Pipopo",
                                            color = NeonCyan,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                        Text(
                                            text = "Bebida & Snack Oficial do CardClash",
                                            color = Color.White.copy(alpha = 0.6f),
                                            fontSize = 10.sp
                                        )
                                    }
                                }

                                Text(
                                    text = "Negociamos uma parceria com as marcas locais mais queridas do Brasil! Encontre códigos promocionais impressos sob a tampa das garrafas de Guaraná Daora (600ml e 2L) ou no interior das embalagens marcadas de Salgadinhos Pipopo CrocChamps e pule na fila de pré-lançamento!",
                                    color = Color.White.copy(alpha = 0.85f),
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp
                                )

                                // Interactive display showing where the codes are found
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    // Bottle indicator
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .background(StadiumGlow, shape = RoundedCornerShape(10.dp))
                                            .border(1.dp, NeonCyan.copy(alpha = 0.3f), shape = RoundedCornerShape(10.dp))
                                            .padding(8.dp)
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text("🟢 Sob a Tampa", color = NeonEmerald, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text("Guaraná Daora 2L", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text("Código de 8 dígitos", color = Color.White.copy(alpha = 0.6f), fontSize = 8.sp)
                                        }
                                    }

                                    // Snack box indicator
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .background(StadiumGlow, shape = RoundedCornerShape(10.dp))
                                            .border(1.dp, NeonCyan.copy(alpha = 0.3f), shape = RoundedCornerShape(10.dp))
                                            .padding(8.dp)
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text("🟠 No Pacote", color = NeonCyan, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text("Salgadinhos Pipopo", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text("CrocChamps Sabores", color = Color.White.copy(alpha = 0.6f), fontSize = 8.sp)
                                        }
                                    }
                                }

                                Divider(color = Color.White.copy(alpha = 0.1f), thickness = 1.dp)

                                // Input code section
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "Insira o Código do Produto físico para Resgatar:",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )

                                    OutlinedTextField(
                                        value = sponsorCodeInput,
                                        onValueChange = { sponsorCodeInput = it },
                                        placeholder = { Text("Ex: DAORA-HEXA-2026", fontSize = 12.sp) },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White,
                                            focusedBorderColor = NeonCyan,
                                            unfocusedBorderColor = Color.White.copy(alpha = 0.2f)
                                        ),
                                        singleLine = true
                                    )

                                    Button(
                                        onClick = {
                                            viewModel.redeemSponsorCode(sponsorCodeInput) { success, msg ->
                                                if (success) {
                                                    sponsorCodeInput = ""
                                                }
                                                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("VALIDAR OU RESGATAR CÓDIGO ⚡", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                if (redeemedSponsorCodes.isNotEmpty()) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(NeonCyan.copy(alpha = 0.05f), shape = RoundedCornerShape(8.dp))
                                            .border(1.dp, NeonCyan.copy(alpha = 0.2f), shape = RoundedCornerShape(8.dp))
                                            .padding(10.dp)
                                    ) {
                                        Column {
                                            Text("✔️ Seus Códigos Resgatados:", color = NeonEmerald, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            redeemedSponsorCodes.forEach { code ->
                                                Text("• $code (Fila otimizada)", color = Color.White.copy(alpha = 0.8f), fontSize = 10.sp)
                                            }
                                        }
                                    }
                                }

                                // Interactive Quick Simulation buttons representing buying soda/snack
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(StadiumGlow, shape = RoundedCornerShape(10.dp))
                                        .padding(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "🧪 Simulator: Testar Códigos Promocionais Ativos",
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Button(
                                            onClick = { sponsorCodeInput = "DAORA-HEXA-2026" },
                                            colors = ButtonDefaults.buttonColors(containerColor = StadiumConcrete),
                                            shape = RoundedCornerShape(6.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("Guaraná Daora 🥤", color = Color.White, fontSize = 8.sp)
                                        }
                                        Button(
                                            onClick = { sponsorCodeInput = "PIPOPO-CHAMPS" },
                                            colors = ButtonDefaults.buttonColors(containerColor = StadiumConcrete),
                                            shape = RoundedCornerShape(6.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("Snack Pipopo 🍿", color = Color.White, fontSize = 8.sp)
                                        }
                                        Button(
                                            onClick = { sponsorCodeInput = "BATER-BAFO-LOCAL" },
                                            colors = ButtonDefaults.buttonColors(containerColor = StadiumConcrete),
                                            shape = RoundedCornerShape(6.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("Comércio Local 🏪", color = Color.White, fontSize = 8.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    8 -> {
                        // === TAB 8: EQUIPES & GRUPOS (WAITLIST GROUPS) ===
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = StadiumConcrete),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .background(NeonCyan.copy(alpha = 0.15f), shape = RoundedCornerShape(8.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.People, contentDescription = null, tint = NeonCyan)
                                    }
                                    Column {
                                        Text(
                                            text = "👥 Equipes & Grupos de Colecionadores",
                                            color = NeonCyan,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                        Text(
                                            text = "\"O grupo todo sobe junto no ranking da Fila!\"",
                                            color = Color.White.copy(alpha = 0.6f),
                                            fontSize = 11.sp
                                        )
                                    }
                                }

                                if (waitlistGroupName.isBlank()) {
                                    // User NOT in a group: offer creation and joining
                                    Column(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Text(
                                            text = "Cadastre-se junto com sua galera (turma, trabalho, amigos) e todo o grupo ganha bônus no ranking de espera com o ID compartilhável!",
                                            color = Color.White.copy(alpha = 0.8f),
                                            fontSize = 11.sp,
                                            lineHeight = 15.sp
                                        )

                                        // Area A: Criar Grupo
                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = CardDefaults.cardColors(containerColor = StadiumGlow),
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(12.dp),
                                                verticalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Text("🛠️ Criar Novo Grupo", color = NeonCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                
                                                OutlinedTextField(
                                                    value = teamNameInput,
                                                    onValueChange = { teamNameInput = it },
                                                    placeholder = { Text("Ex: Pelada de Quarta, Galera do T.I.", fontSize = 12.sp) },
                                                    label = { Text("Nome do Grupo") },
                                                    modifier = Modifier.fillMaxWidth(),
                                                    colors = OutlinedTextFieldDefaults.colors(
                                                        focusedTextColor = Color.White,
                                                        unfocusedTextColor = Color.White,
                                                        focusedBorderColor = NeonCyan,
                                                        unfocusedBorderColor = Color.White.copy(alpha = 0.2f)
                                                    ),
                                                    singleLine = true
                                                )

                                                // Category Selection
                                                Text("Tipo de Grupo:", color = Color.White, fontSize = 10.sp)
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                ) {
                                                    listOf("Turma 🎓", "Trabalho 💻", "Amigos 🍻", "Família 🏆").forEach { option ->
                                                        val optionClean = option.split(" ")[0]
                                                        val isSelected = teamTypeSelected == optionClean
                                                        Box(
                                                            modifier = Modifier
                                                                .weight(1f)
                                                                .background(if (isSelected) NeonCyan else StadiumConcrete, shape = RoundedCornerShape(6.dp))
                                                                .clickable { teamTypeSelected = optionClean }
                                                                .padding(vertical = 6.dp),
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            Text(option, color = if (isSelected) Color.Black else Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                                        }
                                                    }
                                                }

                                                Button(
                                                    onClick = {
                                                        viewModel.createWaitlistGroup(teamNameInput, teamTypeSelected) { success, msg ->
                                                            if (success) {
                                                                teamNameInput = ""
                                                            }
                                                            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                                        }
                                                    },
                                                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                                                    modifier = Modifier.fillMaxWidth(),
                                                    shape = RoundedCornerShape(8.dp)
                                                ) {
                                                    Text("CRIAR MEU GRUPO DE ESPERA", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }

                                        // Area B: Entrar em Grupo Existente
                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = CardDefaults.cardColors(containerColor = StadiumGlow),
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(12.dp),
                                                verticalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Text("🤝 Entrar com ID de Amigo", color = NeonCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                
                                                OutlinedTextField(
                                                    value = joinTeamCodeInput,
                                                    onValueChange = { joinTeamCodeInput = it },
                                                    placeholder = { Text("Ex: CLASH-TURMA-1234", fontSize = 12.sp) },
                                                    modifier = Modifier.fillMaxWidth(),
                                                    colors = OutlinedTextFieldDefaults.colors(
                                                        focusedTextColor = Color.White,
                                                        unfocusedTextColor = Color.White,
                                                        focusedBorderColor = NeonCyan,
                                                        unfocusedBorderColor = Color.White.copy(alpha = 0.2f)
                                                    ),
                                                    singleLine = true
                                                )

                                                Button(
                                                    onClick = {
                                                        viewModel.joinWaitlistGroup(joinTeamCodeInput) { success, msg ->
                                                            if (success) {
                                                                joinTeamCodeInput = ""
                                                            }
                                                            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                                        }
                                                    },
                                                    colors = ButtonDefaults.buttonColors(containerColor = NeonEmerald),
                                                    modifier = Modifier.fillMaxWidth(),
                                                    shape = RoundedCornerShape(8.dp)
                                                ) {
                                                    Text("ENTRAR NO GRUPO DE AMIGOS", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }

                                        // Clickable Public Community Groups to test!
                                        Column(
                                            modifier = Modifier.fillMaxWidth().background(StadiumGlow, shape = RoundedCornerShape(8.dp)).padding(8.dp),
                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Text("🌐 Grupos Comunitários Ativos (Clique para simular):", color = Color.White.copy(alpha = 0.7f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                            listOf(
                                                "CLASH-TRABALHO-DEV" to "Devs do Hexa 💻",
                                                "CLASH-TURMA-EFE" to "Educação Física UFRJ ⚽",
                                                "CLASH-AMIGOS-PEL" to "Pelada de Quarta 🍻"
                                            ).forEach { pair ->
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .clickable { joinTeamCodeInput = pair.first }
                                                        .padding(vertical = 4.dp),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text("👉 ${pair.second}", color = NeonCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                    Text("CÓD: ${pair.first}", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    // User IS in a group
                                    Column(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(NeonCyan.copy(alpha = 0.1f), shape = RoundedCornerShape(12.dp))
                                                .border(2.dp, NeonCyan, shape = RoundedCornerShape(12.dp))
                                                .padding(14.dp)
                                        ) {
                                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(text = "SUA EQUIPE ATIVA", color = NeonCyan, fontSize = 10.sp, fontWeight = FontWeight.Black)
                                                    Box(
                                                        modifier = Modifier
                                                            .background(NeonEmerald, shape = RoundedCornerShape(12.dp))
                                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                                    ) {
                                                        Text("Na Fila: #$waitlistGroupPosition", color = Color.Black, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                                    }
                                                }

                                                Text(text = waitlistGroupName, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                                                Text(text = "Tipo: $waitlistGroupType • ID: $waitlistGroupCode", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)

                                                Spacer(modifier = Modifier.height(4.dp))

                                                // Member counts & list
                                                Text("👥 Integrantes ($waitlistGroupSize):", color = NeonCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                Row(
                                                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    waitlistGroupMembers.forEach { member ->
                                                        val isMe = member == "Você"
                                                        Box(
                                                            modifier = Modifier
                                                                .background(if (isMe) NeonCyan else StadiumGlow, shape = RoundedCornerShape(8.dp))
                                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                                        ) {
                                                            Text(member, color = if (isMe) Color.Black else Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                        }
                                                    }
                                                }

                                                Spacer(modifier = Modifier.height(6.dp))

                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    Button(
                                                        onClick = {
                                                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                                            clipboard.setPrimaryClip(ClipData.newPlainText("group_id", waitlistGroupCode))
                                                            Toast.makeText(context, "ID do Grupo copiado! Compartilhe com sua galera para eles entrarem!", Toast.LENGTH_SHORT).show()
                                                        },
                                                        colors = ButtonDefaults.buttonColors(containerColor = StadiumConcrete),
                                                        modifier = Modifier.weight(1f),
                                                        shape = RoundedCornerShape(8.dp)
                                                    ) {
                                                        Text("Copiar ID 📋", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                    }

                                                    Button(
                                                        onClick = {
                                                            viewModel.leaveWaitlistGroup()
                                                            Toast.makeText(context, "Você saiu do grupo.", Toast.LENGTH_SHORT).show()
                                                        },
                                                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.7f)),
                                                        modifier = Modifier.weight(0.8f),
                                                        shape = RoundedCornerShape(8.dp)
                                                    ) {
                                                        Text("Sair", color = Color.White, fontSize = 10.sp)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                Divider(color = Color.White.copy(alpha = 0.1f), thickness = 1.dp)

                                // Global waitlist groups leaderboard
                                Text(
                                    text = "📊 Top Leaderboard de Grupos na Fila",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = StadiumGlow),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(10.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        waitlistGroupLeaderboard.forEach { grp ->
                                            val isUserGrp = grp.code == waitlistGroupCode
                                            val bgSelected = if (isUserGrp) NeonCyan.copy(alpha = 0.12f) else Color.Transparent
                                            val borderSelected = if (isUserGrp) BorderStroke(1.dp, NeonCyan) else null

                                            Card(
                                                modifier = Modifier.fillMaxWidth(),
                                                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                                                border = borderSelected,
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Row(
                                                    modifier = Modifier
                                                        .background(bgSelected)
                                                        .padding(horizontal = 8.dp, vertical = 6.dp)
                                                        .fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                    ) {
                                                        Text(
                                                            text = "#${grp.queuePosition}",
                                                            color = if (isUserGrp) NeonCyan else Color.White.copy(alpha = 0.6f),
                                                            fontSize = 11.sp,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                        Column {
                                                            Text(
                                                                text = grp.name,
                                                                color = Color.White,
                                                                fontSize = 11.sp,
                                                                fontWeight = FontWeight.Bold
                                                            )
                                                            Text(
                                                                text = "${grp.type} • ID: ${grp.code}",
                                                                color = Color.White.copy(alpha = 0.5f),
                                                                fontSize = 8.sp
                                                            )
                                                        }
                                                    }

                                                    Box(
                                                        modifier = Modifier
                                                            .background(StadiumConcrete, shape = RoundedCornerShape(10.dp))
                                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                                    ) {
                                                        Text("👥 ${grp.size} memb", color = Color.White, fontSize = 9.sp)
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
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = StadiumGlow),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Text(text = "VOLTAR", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = null
    )
}
