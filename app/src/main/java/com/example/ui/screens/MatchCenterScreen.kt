package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Sports
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LiveMatch
import com.example.ui.theme.*
import kotlin.random.Random

@Composable
fun MatchCenterScreen(
    liveMatches: List<LiveMatch>,
    favoriteTeam: String,
    onPredictResult: (Int, String) -> Unit
) {
    var selectedPredictionMatchId by remember { mutableStateOf<Int?>(null) }
    var userPredictionsMap = remember { mutableStateMapOf<Int, String>() }
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(StadiumObsidian)
            .statusBarsPadding()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Mode Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(imageVector = Icons.Default.Sports, contentDescription = null, tint = NeonEmerald, modifier = Modifier.size(28.dp))
            Text(
                text = "Central da Rodada",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }

        // Live alert info
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
                Icon(imageVector = Icons.Default.NotificationsActive, contentDescription = null, tint = NeonCyan)
                Text(
                    text = "Acompanhe ao vivo! Receba moedas grátis se der palpites certeiros ou se um jogador que você coleciona balançar as redes na partida real!",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 11.sp,
                    lineHeight = 14.sp
                )
            }
        }

        Text(
            text = "PLACARDS AO VIVO DA RODADA",
            color = NeonCyan,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )

        // Matches Column list
        liveMatches.forEach { match ->
            val userPrediction = userPredictionsMap[match.id]

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, StadiumBorder, shape = RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = StadiumConcrete)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Score row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Home Team
                        Text(
                            text = match.homeTeam,
                            color = if (match.homeTeam == favoriteTeam) NeonEmerald else Color.White,
                            fontWeight = if (match.homeTeam == favoriteTeam) FontWeight.ExtraBold else FontWeight.Bold,
                            fontSize = 15.sp,
                            modifier = Modifier.width(100.dp)
                        )

                        // score indicators
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = match.homeScore.toString(),
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black
                            )
                            Text(text = "-", color = Color.White.copy(alpha = 0.5f))
                            Text(
                                text = match.awayScore.toString(),
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black
                            )
                        }

                        // Away Team
                        Text(
                            text = match.awayTeam,
                            color = if (match.awayTeam == favoriteTeam) NeonEmerald else Color.White,
                            fontWeight = if (match.awayTeam == favoriteTeam) FontWeight.ExtraBold else FontWeight.Bold,
                            fontSize = 15.sp,
                            textAlign = TextAlign.End,
                            modifier = Modifier.width(100.dp)
                        )
                    }

                    // live minute badge
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (match.isLive) {
                            Box(
                                modifier = Modifier
                                    .background(Color.Red, shape = RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(text = "${match.minute}' AO VIVO", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Black)
                            }
                        } else {
                            Text(
                                text = if (match.minute >= 90) "FINALIZADO" else "AGUARDANDO INÍCIO",
                                color = Color.White.copy(alpha = 0.4f),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // scorers details
                    if (match.scorers.isNotEmpty()) {
                        HorizontalDivider(color = StadiumBorder)
                        Text(
                            text = "⚽ Gols: ${match.scorers}",
                            color = Color.White.copy(alpha = 0.65f),
                            fontSize = 10.sp,
                            lineHeight = 13.sp
                        )
                    }

                    // Palpite Action BTN
                    if (!match.isLive && match.minute == 0) {
                        HorizontalDivider(color = StadiumBorder)
                        if (userPrediction == null) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        userPredictionsMap[match.id] = "HOME"
                                        onPredictResult(match.id, "HOME")
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = StadiumGlow),
                                    modifier = Modifier.weight(1f).height(32.dp),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text("Vitória ${match.homeTeam}", color = Color.White, fontSize = 10.sp)
                                }

                                Button(
                                    onClick = {
                                        userPredictionsMap[match.id] = "AWAY"
                                        onPredictResult(match.id, "AWAY")
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = StadiumGlow),
                                    modifier = Modifier.weight(1f).height(32.dp),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text("Vitória ${match.awayTeam}", color = Color.White, fontSize = 10.sp)
                                }
                            }
                        } else {
                            Text(
                                text = "Seu palpite: Vitória do ${if (userPrediction == "HOME") match.homeTeam else match.awayTeam} (Validando...)",
                                color = NeonCyan,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Simulated standings Table
        Text(
            text = "TABELA DO CAMPEONATO (ESTÍMULO)",
            color = NeonCyan,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, StadiumBorder, shape = RoundedCornerShape(12.dp)),
            colors = CardDefaults.cardColors(containerColor = StadiumConcrete)
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                TableHeader()
                HorizontalDivider(color = StadiumBorder)
                TableRow(pos = 1, team = "Flamengo", pts = 22, games = 10, won = 7, diff = 12)
                TableRow(pos = 2, team = "Palmeiras", pts = 20, games = 10, won = 6, diff = 9)
                TableRow(pos = 3, team = "São Paulo", pts = 18, games = 10, won = 5, diff = 6)
                TableRow(pos = 4, team = "Cruzeiro", pts = 17, games = 10, won = 5, diff = 4)
                TableRow(pos = 5, team = "Vasco", pts = 14, games = 10, won = 4, diff = -1)
            }
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}

@Composable
fun TableHeader() {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text("Pos / Clube", color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("P", color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(14.dp))
            Text("J", color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(14.dp))
            Text("SG", color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(14.dp))
        }
    }
}

@Composable
fun TableRow(pos: Int, team: String, pts: Int, games: Int, won: Int, diff: Int) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(text = "$pos. $team", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 11.sp)
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(text = pts.toString(), color = NeonEmerald, fontWeight = FontWeight.Black, fontSize = 11.sp, modifier = Modifier.width(14.dp))
            Text(text = games.toString(), color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp, modifier = Modifier.width(14.dp))
            Text(text = (if (diff >= 0) "+$diff" else diff.toString()), color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp, modifier = Modifier.width(14.dp))
        }
    }
}
