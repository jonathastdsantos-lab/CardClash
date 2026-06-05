package com.example.ui.components

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InteractiveTutorialBottomSheet(
    onDismissRequest: () -> Unit,
    onAwardCoins: (Int) -> Unit
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    var isDismissing by remember { mutableStateOf(false) }

    // Animates sheet slide-down before triggering the parent lifecycle toggle (fixes InputDispatcher crashes)
    val safeDismiss = {
        if (!isDismissing) {
            isDismissing = true
            scope.launch {
                try {
                    sheetState.hide()
                } finally {
                    onDismissRequest()
                }
            }
        }
    }
    
    // Tutorial navigation steps state
    var currentStep by remember { mutableStateOf(1) }
    val totalSteps = 6

    // Timing Game state inside Step 4
    val infiniteTransition = rememberInfiniteTransition(label = "tuto_power_bar")
    val currentPowerCycle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(850, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "tuto_cycle"
    )
    
    var showSuccessSlap by remember { mutableStateOf(false) }
    var showFailedSlap by remember { mutableStateOf(false) }
    var isSlapDone by remember { mutableStateOf(false) }
    var chosenSlapPower by remember { mutableStateOf(0f) }

    ModalBottomSheet(
        onDismissRequest = { safeDismiss() },
        sheetState = sheetState,
        containerColor = StadiumObsidian,
        dragHandle = {
            BottomSheetDefaults.DragHandle(color = NeonEmerald.copy(alpha = 0.5f))
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp)
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            
            // Header Title with Step Indicators
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ACADEMIA DE CRACKS 🏆",
                    color = NeonEmerald,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp
                )
                
                // Active indicators pills
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    for (i in 1..totalSteps) {
                        val isActive = i == currentStep
                        val isDone = i < currentStep
                        Box(
                            modifier = Modifier
                                .size(width = if (isActive) 16.dp else 8.dp, height = 8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(
                                    if (isActive) NeonEmerald 
                                    else if (isDone) NeonCyan.copy(alpha = 0.7f) 
                                    else StadiumBorder
                                )
                        )
                    }
                }
            }

            // Divider Green Glow Line
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(Color.Transparent, NeonEmerald, Color.Transparent)
                        )
                    )
            )

            // Animated content container switching per step
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize()
                    .heightIn(min = 280.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                when (currentStep) {
                    1 -> TutorialIntroView()
                    2 -> TutorialAlbumView()
                    3 -> TutorialStoreView()
                    4 -> TutorialSlapGameView(
                        currentPower = currentPowerCycle,
                        showSuccess = showSuccessSlap,
                        showFailed = showFailedSlap,
                        onSlapClick = {
                            chosenSlapPower = currentPowerCycle
                            isSlapDone = true
                            if (currentPowerCycle in 0.6f..0.8f) {
                                showSuccessSlap = true
                                showFailedSlap = false
                            } else {
                                showFailedSlap = true
                                showSuccessSlap = false
                            }
                        },
                        onResetClick = {
                            showSuccessSlap = false
                            showFailedSlap = false
                            isSlapDone = false
                        }
                    )
                    5 -> TutorialTradesAndPredictsView()
                    6 -> TutorialRewardCompletedView(
                        onCollectCoins = {
                            onAwardCoins(1000)
                            Toast.makeText(context, "🎁 +1000 Moedas creditadas por se formar na Academia!", Toast.LENGTH_LONG).show()
                            safeDismiss()
                        }
                    )
                }
            }

            // Standard Navigation Buttons (Back & Next)
            if (currentStep < totalSteps) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Back button
                    if (currentStep > 1) {
                        TextButton(
                            onClick = { 
                                currentStep-- 
                            },
                            colors = ButtonDefaults.textButtonColors(contentColor = Color.White.copy(alpha = 0.7f))
                        ) {
                            Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Voltar", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Voltar", fontSize = 13.sp)
                        }
                    } else {
                        Spacer(modifier = Modifier.width(1.dp))
                    }

                    // Next / Action Button
                    val nextButtonEnabled = (currentStep != 4 || showSuccessSlap)
                    
                    Button(
                        onClick = {
                            if (currentStep < totalSteps) currentStep++
                        },
                        enabled = nextButtonEnabled,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NeonEmerald,
                            contentColor = Color.Black,
                            disabledContainerColor = StadiumBorder,
                            disabledContentColor = Color.White.copy(alpha = 0.3f)
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .height(44.dp)
                            .padding(horizontal = 4.dp)
                    ) {
                        val btnText = if (currentStep == 4) "Desbloquear Recompensa!" else "Bora, Próximo!"
                        Text(
                            text = btnText,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = "Avançar",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TutorialIntroView() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .background(NeonEmerald.copy(alpha = 0.2f), shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text("👋", fontSize = 36.sp)
        }
        
        Text(
            text = "E AÍ, CRAQUE DO BAFO!",
            color = Color.White,
            fontWeight = FontWeight.Black,
            fontSize = 18.sp,
            textAlign = TextAlign.Center
        )

        Text(
            text = "Bem-vindo ao CardClash, seu novo vício digital! Esqueça aquelas figurinhas estáticas sem graça de papel que mofam na gaveta.",
            color = Color.White.copy(alpha = 0.9f),
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
            lineHeight = 18.sp
        )

        Text(
            text = "Aqui você vai colecionar craques lendários do futebol brasileiro e internacional, customizar suas equipes e desafiar oponentes na internet e por GPS com as mecânicas nostálgicas do tradicional jogo de bater bafo (as icônicas conchinhas!).",
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            lineHeight = 16.sp
        )
    }
}

@Composable
fun TutorialAlbumView() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .background(NeonCyan.copy(alpha = 0.15f), shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.PhotoAlbum,
                contentDescription = null,
                tint = NeonEmerald,
                modifier = Modifier.size(32.dp)
            )
        }

        Text(
            text = "1. SEU ÁLBUM DE ELITE (COLEÇÃO)",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            textAlign = TextAlign.Center
        )

        Card(
            colors = CardDefaults.cardColors(containerColor = StadiumConcrete),
            border = BorderStroke(1.dp, StadiumBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("💡", fontSize = 18.sp)
                    Text(
                        "Click em um Card:", 
                        color = NeonCyan, 
                        fontWeight = FontWeight.Bold, 
                        fontSize = 13.sp
                    )
                }
                Text(
                    text = "Aperte em qualquer card do Álbum para abrir os detalhes. Veja as estatísticas do jogador de ritmo, drible e overall em detalhes.",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("⚡", fontSize = 18.sp)
                    Text(
                        "Evolução Dinâmica (Upgrade):", 
                        color = NeonCyan, 
                        fontWeight = FontWeight.Bold, 
                        fontSize = 13.sp
                    )
                }
                Text(
                    text = "Abaixo do card nos detalhes há o botão de EVOLUIR. Gaste suas moedas para fazer um upgrade no seu jogador! Você pode upar até +3 níveis. Cada upgrade aumenta grandemente os status e adiciona raridades especiais como ESPECIAL, ASSINADA e a relíquia ANIMADA!",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )
            }
        }
    }
}

@Composable
fun TutorialStoreView() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .background(BrightGold.copy(alpha = 0.15f), shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.ShoppingBag,
                contentDescription = null,
                tint = BrightGold,
                modifier = Modifier.size(32.dp)
            )
        }

        Text(
            text = "2. A LOJA & OS PACOTINHOS",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            textAlign = TextAlign.Center
        )

        Card(
            colors = CardDefaults.cardColors(containerColor = StadiumConcrete),
            border = BorderStroke(1.dp, StadiumBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("🛍️", fontSize = 18.sp)
                    Text(
                        "Pacotes de Craques:", 
                        color = BrightGold, 
                        fontWeight = FontWeight.Bold, 
                        fontSize = 13.sp
                    )
                }
                Text(
                    text = "Gaste suas moedas comprando Pacotes de Bronze, Prata, Ouro ou Lendários! Quanto melhor o pacote, maior a probabilidade de dropar uma carta do seu time do coração com status elevados.",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("🎟️", fontSize = 18.sp)
                    Text(
                        "Passe de Elite & Patrocínio Grátis:", 
                        color = BrightGold, 
                        fontWeight = FontWeight.Bold, 
                        fontSize = 13.sp
                    )
                }
                Text(
                    text = "Aqui a gente cuida da sua diversão. Você pode apoiar o game ou simular pagamentos gratuitos do Passe Elite e recarregar moedas de graça! Além de poder assistir a anúncios patrocinados rápidos de 5 segundos para ganhar rodadas de moedas extras.",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )
            }
        }
    }
}

@Composable
fun TutorialSlapGameView(
    currentPower: Float,
    showSuccess: Boolean,
    showFailed: Boolean,
    onSlapClick: () -> Unit,
    onResetClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .background(NeonCyan.copy(alpha = 0.15f), shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.SportsEsports,
                contentDescription = null,
                tint = NeonCyan,
                modifier = Modifier.size(28.dp)
            )
        }

        Text(
            text = "3. O INSTANTÂNEO JOGO DO BAFO 🤜💥",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )

        Text(
            text = "A Regra Suprema: Use o medidor oscilador no menu **BAFO**! Seu objetivo é soltar a batida no ponto ideal (entre 60% e 80% da força) para criar a pressão de ar ideal e virar a carta!",
            color = Color.White.copy(alpha = 0.75f),
            fontSize = 11.sp,
            textAlign = TextAlign.Center,
            lineHeight = 14.sp
        )

        // The Mini interactive Simulator widget
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (showSuccess) Color(0xFF1B2F17) 
                                 else if (showFailed) Color(0xFF2E1717) 
                                 else StadiumConcrete
            ),
            border = BorderStroke(
                width = 1.3.dp, 
                color = if (showSuccess) NeonEmerald 
                        else if (showFailed) Color.Red 
                        else StadiumBorder
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Info
                Text(
                    text = if (showSuccess) "🏆 SENSACIONAL! BATIDA DE FILÓSOFO!" 
                           else if (showFailed) "❌ IH, AMASSOU A FIGURINHA!" 
                           else "EXERCÍCIO PRÁTICO: Mire na zona amarela de 60% - 80%!",
                    color = if (showSuccess) NeonEmerald 
                            else if (showFailed) Color.Red 
                            else BrightGold,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center
                )

                // High Contrast Oscillating Progress Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.95f)
                        .height(20.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(StadiumObsidian)
                        .border(1.dp, StadiumBorder, shape = RoundedCornerShape(10.dp))
                ) {
                    // Yellow sweet spot
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(0.2f)
                            .align(Alignment.CenterEnd)
                            .padding(end = 50.dp) // Placed matching 60%..80% visual zone
                            .background(BrightGold.copy(alpha = 0.5f))
                    )

                    // Target power fill
                    val displayPower = if (showSuccess || showFailed) currentPower else currentPower
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(if (showSuccess || showFailed) currentPower else currentPower)
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(NeonEmerald, NeonCyan)
                                )
                            )
                    )
                }

                // Interactive CTA Trigger
                if (!showSuccess && !showFailed) {
                    Button(
                        onClick = onSlapClick,
                        colors = ButtonDefaults.buttonColors(containerColor = NeonEmerald),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth(0.8f).height(38.dp)
                    ) {
                        Text("👋 BATER CONCHINHA!", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                } else {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (showSuccess) {
                            Text(
                                "Excelente! Você dominou o timing. Desbloqueou o avanço para retirar seu prêmio de 1000 Moedas!",
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = 10.sp,
                                modifier = Modifier.weight(1f),
                                lineHeight = 13.sp
                            )
                        } else {
                            Text(
                                "Foi muito forte ou fraco! Treina de novo para acertar na mosca e liberar o prêmio.",
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = 10.sp,
                                modifier = Modifier.weight(1f),
                                lineHeight = 13.sp
                            )
                            Button(
                                onClick = onResetClick,
                                colors = ButtonDefaults.buttonColors(containerColor = StadiumBorder),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Text("Tentar De Novo 🔄", color = Color.White, fontSize = 10.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TutorialTradesAndPredictsView() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(62.dp)
                .background(NeonCyan.copy(alpha = 0.15f), shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.People,
                contentDescription = null,
                tint = NeonCyan,
                modifier = Modifier.size(30.dp)
            )
        }

        Text(
            text = "4. MERCADO & RODADAS DE FUTEBOL",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            textAlign = TextAlign.Center
        )

        Card(
            colors = CardDefaults.cardColors(containerColor = StadiumConcrete),
            border = BorderStroke(1.dp, StadiumBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "🤝 Troque e Faça Amigos:",
                    color = NeonEmerald,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
                Text(
                    text = "No menu de TROCAS existem anúncios criados por outros colecionadores. Se tiver a figurinha desejada por eles, faça a troca na hora de modo totalmente seguro!",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 11.sp,
                    lineHeight = 14.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "⚽ Palpites da Rodada (Faturamento):",
                    color = NeonEmerald,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
                Text(
                    text = "Na aba RODADA, acompanhe os jogos reais ao vivo! Dê chutes no placar vencedor (Palpites). Chutes certeiros rendem +250 moedas na hora! No menu de QUIZ jogue para testar seus saberes e lucrar muito.",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 11.sp,
                    lineHeight = 14.sp
                )
            }
        }
    }
}

@Composable
fun TutorialRewardCompletedView(onCollectCoins: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_reward")
    val scaleFactor by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(90.dp)
                .background(BrightGold.copy(alpha = 0.2f), shape = CircleShape)
                .border(2.dp, BrightGold, shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text("👑", fontSize = 44.sp)
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "ACADEMIA CONCLUÍDA!",
                color = BrightGold,
                fontWeight = FontWeight.Black,
                fontSize = 18.sp,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Você agora é oficialmente um Mestre do Bafo!",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = StadiumGlow),
            border = BorderStroke(1.dp, NeonEmerald.copy(alpha = 0.4f)),
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .padding(vertical = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "BÔNUS DE REGISTRO SEGURO 🪙🏆",
                    color = NeonEmerald,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
                Text(
                    text = "Resgate seu prêmio exclusivo de +1000 Moedas para gastar abrindo pacotes agora mesmo na Loja do Copa Arena!",
                    color = Color.White,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 15.sp
                )
            }
        }

        Button(
            onClick = onCollectCoins,
            colors = ButtonDefaults.buttonColors(containerColor = BrightGold, contentColor = Color.Black),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .height(48.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "RECLAMAR MEU PRÊMIO 🤑💎",
                    fontWeight = FontWeight.Black,
                    fontSize = 12.sp
                )
            }
        }
    }
}
