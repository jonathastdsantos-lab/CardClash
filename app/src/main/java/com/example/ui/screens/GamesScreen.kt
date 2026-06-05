package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HelpCenter
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Timer
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
import com.example.ui.theme.*
import com.example.ui.viewmodel.QuizState

@Composable
fun GamesScreen(
    quizState: QuizState,
    onStartQuiz: () -> Unit,
    onAnswerQuestion: (Int) -> Unit,
    onProceed: (correctCount: Int, currentIdx: Int) -> Unit,
    onFinishQuiz: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(StadiumObsidian)
            .statusBarsPadding()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Mode Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(imageVector = Icons.Default.HelpCenter, contentDescription = null, tint = NeonEmerald, modifier = Modifier.size(28.dp))
            Text(
                text = "Quiz da Arquibancada",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }

        HorizontalDivider(color = StadiumBorder)

        when (quizState) {
            is QuizState.Idle -> {
                // Intro Lobby
                Column(
                    modifier = Modifier.fillMaxSize().weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)
                ) {
                    Icon(imageVector = Icons.Default.HelpCenter, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(72.dp))

                    Text(
                        text = "Mostre que você é craque de bola!",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "Acerte perguntas sobre a história do futebol brasileiro e internacional. Cada resposta correta rende 50 Moedas grátis para você gastar abrindo pacotes de figurinhas!",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        colors = ButtonDefaults.buttonColors(containerColor = NeonEmerald),
                        onClick = onStartQuiz,
                        modifier = Modifier.fillMaxWidth(0.8f).height(48.dp).testTag("start_quiz_btn")
                    ) {
                        Text("INICIAR QUIZ DE HOJE", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }

            is QuizState.Loading -> {
                // Pre-game Count Down UI
                Column(
                    modifier = Modifier.fillMaxSize().weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)
                ) {
                    Text(text = "Preparando a rodada...", color = Color.White.copy(alpha = 0.6f), fontSize = 14.sp)
                    Text(
                        text = if (quizState.countdown > 0) quizState.countdown.toString() else "ROLA A BOLA!",
                        color = NeonEmerald,
                        fontSize = 72.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            is QuizState.Active -> {
                // Interactive Question Display
                val q = quizState.question
                val activeScrollState = rememberScrollState()

                Column(
                    modifier = Modifier.fillMaxSize().weight(1f),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .verticalScroll(activeScrollState),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                    // Timer bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(imageVector = Icons.Default.Timer, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(16.dp))
                            Text(text = "Tempo Restante:", color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp)
                        }
                        Text(
                            text = "${quizState.secondsRemaining}s",
                            color = if (quizState.secondsRemaining < 5) Color.Red else NeonCyan,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    LinearProgressIndicator(
                        progress = { quizState.secondsRemaining / 15f },
                        color = if (quizState.secondsRemaining < 5) Color.Red else NeonCyan,
                        trackColor = StadiumGlow,
                        modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp))
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Question title
                    Text(
                        text = q.text,
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 22.sp,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Column of choices
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        q.options.forEachIndexed { idx, option ->
                            val isChosen = quizState.selectedIndex == idx
                            val isCorrect = q.correctAnswerIndex == idx
                            val borderTint = when {
                                quizState.isAnswered && isCorrect -> NeonEmerald
                                quizState.isAnswered && isChosen -> Color.Red
                                isChosen -> NeonCyan
                                else -> Color.White.copy(alpha = 0.2f)
                            }
                            val bgTint = when {
                                quizState.isAnswered && isCorrect -> NeonEmerald.copy(alpha = 0.15f)
                                quizState.isAnswered && isChosen -> Color.Red.copy(alpha = 0.15f)
                                isChosen -> NeonCyan.copy(alpha = 0.1f)
                                else -> StadiumConcrete
                            }

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, borderTint, shape = RoundedCornerShape(10.dp))
                                    .clickable(!quizState.isAnswered) { onAnswerQuestion(idx) }
                                    .testTag("quiz_option_$idx"),
                                colors = CardDefaults.cardColors(containerColor = bgTint)
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${('A' + idx)}.",
                                        fontWeight = FontWeight.Black,
                                        color = if (isChosen) NeonCyan else Color.White,
                                        modifier = Modifier.width(24.dp)
                                    )
                                    Text(text = option, color = Color.White, fontSize = 13.sp)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Educational Trivia Explainer
                    AnimatedVisibility(
                        visible = quizState.isAnswered,
                        enter = fadeIn() + expandVertically()
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = StadiumGlow),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Icon(imageVector = Icons.Default.Lightbulb, contentDescription = null, tint = BrightGold, modifier = Modifier.size(16.dp))
                                    Text("Você sabia?", color = BrightGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                                Text(text = q.funFact, color = Color.White.copy(alpha = 0.85f), fontSize = 10.sp, lineHeight = 13.sp)
                            }
                        }
                    }

                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Confirmation action buttons
                    if (quizState.isAnswered) {
                        Button(
                            colors = ButtonDefaults.buttonColors(containerColor = NeonEmerald),
                            onClick = {
                                // Calculate correct answers and index on active state transition
                                val isCorrect = quizState.selectedIndex == q.correctAnswerIndex
                                onProceed(if (isCorrect) 1 else 0, 0) // QuizState transitions next questions dynamically inside ViewModel
                            },
                            modifier = Modifier.fillMaxWidth().height(48.dp).testTag("quiz_next_btn")
                        ) {
                            Text("PRÓXIMA PERGUNTA", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            is QuizState.Completed -> {
                // Finish stage
                Column(
                    modifier = Modifier.fillMaxSize().weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)
                ) {
                    Text(
                        text = "QUIZ CONCLUÍDO! 🏁⚽",
                        color = NeonEmerald,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black
                    )

                    Text(
                        text = "Você acertou ${quizState.correctCount} perguntas!",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Card(
                        modifier = Modifier.fillMaxWidth(0.85f),
                        colors = CardDefaults.cardColors(containerColor = StadiumConcrete)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "🪙 RECOMPENSA FINANCEIRA:", color = Color.White.copy(alpha = 0.61f), fontSize = 11.sp)
                            Text(text = "+${quizState.rewardCoins} Moedas", color = BrightGold, fontWeight = FontWeight.Black)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                        onClick = onFinishQuiz,
                        modifier = Modifier.fillMaxWidth(0.8f).height(48.dp).testTag("finish_quiz_btn")
                    ) {
                        Text("VOLTAR AOS MEUS CARDS", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
