package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import androidx.compose.ui.res.painterResource
import com.example.R
import com.example.ui.viewmodel.FutViewModel
import com.example.ui.components.PreLaunchLandingDialog

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun LoginScreen(
    viewModel: FutViewModel,
    onRegisterSuccess: (name: String, team: String, age: Int, provider: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var showPreLaunchDialog by remember { mutableStateOf(false) }
    var ageText by remember { mutableStateOf("") }
    var favoriteTeam by remember { mutableStateOf("Flamengo") }
    var expandedDropdown by remember { mutableStateOf(false) }
    var agreedTerms by remember { mutableStateOf(true) }
    var showingAgeWarning by remember { mutableStateOf(false) }

    val teamsList = listOf(
        "Flamengo", "Palmeiras", "São Paulo", "Corinthians", 
        "Vasco", "Santos", "Grêmio", "Internacional", "Cruzeiro", "Atlético-MG"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(StadiumObsidian, StadiumConcrete)
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        val scrollState = rememberScrollState()
        Column(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .verticalScroll(scrollState)
                .padding(vertical = 24.dp, horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Custom Concept C Brand Logo (Chama / Bafo)
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .shadow(12.dp, shape = RoundedCornerShape(22.dp))
                    .clip(RoundedCornerShape(22.dp))
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color(0xFFFF7A3D), Color(0xFFE8431A), Color(0xFF7D1208))
                        )
                    )
                    .testTag("iconC"),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_launcher_foreground),
                    contentDescription = "CardClash Logo",
                    modifier = Modifier.fillMaxSize().padding(10.dp)
                )
            }

            Text(
                text = "CARDCLASH",
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Crie seu álbum, negocie figurinhas e desafie oponentes em batalhas de bafo!",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 12.dp)
            )

            // 🏆 Copa 2026 pre-launch waitlist banner
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.5.dp, NeonCyan, shape = RoundedCornerShape(12.dp))
                    .clickable { showPreLaunchDialog = true }
                    .testTag("waitlist_banner_card"),
                colors = CardDefaults.cardColors(containerColor = StadiumConcrete.copy(alpha = 0.9f))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(text = "🏆", fontSize = 28.sp)
                    Column {
                        Text(
                            text = "CAMPANHA COPA DO MUNDO 2026",
                            color = NeonCyan,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "\"Quando a Copa acabar, sua coleção continua.\"",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = "Acesse o Bracket Grátis e ganhe o Card especial 'Fundador Origem'! 👀 Clique aqui.",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 9.sp,
                            lineHeight = 11.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Text Field Inputs
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nome de Colecionador") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("name_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonEmerald,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                    focusedLabelColor = NeonEmerald,
                    unfocusedLabelColor = Color.White.copy(alpha = 0.6f)
                )
            )

            OutlinedTextField(
                value = ageText,
                onValueChange = { ageText = it },
                label = { Text("Idade") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth().testTag("age_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonEmerald,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                    focusedLabelColor = NeonEmerald,
                    unfocusedLabelColor = Color.White.copy(alpha = 0.6f)
                )
            )

            // Team Dropdown Selection
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = favoriteTeam,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Time do Coração") },
                    trailingIcon = {
                        IconButton(onClick = { expandedDropdown = true }) {
                            Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = "Dropdown")
                        }
                    },
                    modifier = Modifier.fillMaxWidth().clickable { expandedDropdown = true },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonEmerald,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.3f)
                    )
                )
                DropdownMenu(
                    expanded = expandedDropdown,
                    onDismissRequest = { expandedDropdown = false },
                    modifier = Modifier.fillMaxWidth(0.85f).background(StadiumGlow)
                ) {
                    teamsList.forEach { team ->
                        DropdownMenuItem(
                            text = { Text(team, color = Color.White) },
                            onClick = {
                                favoriteTeam = team
                                expandedDropdown = false
                            }
                        )
                    }
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().clickable { agreedTerms = !agreedTerms }
            ) {
                Checkbox(
                    checked = agreedTerms,
                    onCheckedChange = { agreedTerms = it },
                    colors = CheckboxDefaults.colors(checkedColor = NeonEmerald)
                )
                Text(
                    text = "Verifico ter mais de 13 anos e aceito os termos do álbum seguro (Sem azar ou apostas financeiras).",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 11.sp,
                    lineHeight = 14.sp
                )
            }

            if (showingAgeWarning) {
                Text(
                    text = "A idade de cadastro precisa ser um número maior ou igual a 13!",
                    color = Color.Red,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Auths options
            Text(
                text = "ENTRAR COM CONTA SOCIAL (SIMULADO)",
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                maxItemsInEachRow = 2
            ) {
                Button(
                    onClick = {
                        val age = ageText.toIntOrNull() ?: 18
                        if (name.isBlank() || age < 13) {
                            showingAgeWarning = true
                        } else {
                            showingAgeWarning = false
                            onRegisterSuccess(name, favoriteTeam, age, "GOOGLE")
                        }
                    },
                    modifier = Modifier.weight(1f).height(46.dp).testTag("google_login_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEA4335)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Google", color = Color.White, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = {
                        val age = ageText.toIntOrNull() ?: 18
                        if (name.isBlank() || age < 13) {
                            showingAgeWarning = true
                        } else {
                            showingAgeWarning = false
                            onRegisterSuccess(name, favoriteTeam, age, "FACEBOOK")
                        }
                    },
                    modifier = Modifier.weight(1f).height(46.dp).testTag("fb_login_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1877F2)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Facebook", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }

            TextButton(
                onClick = {
                    val age = ageText.toIntOrNull() ?: 18
                    if (name.isBlank() || age < 13) {
                        showingAgeWarning = true
                    } else {
                        showingAgeWarning = false
                        onRegisterSuccess(name.ifBlank { "Convidado" }, favoriteTeam, age, "GUEST")
                    }
                },
                modifier = Modifier.testTag("guest_login_btn")
            ) {
                Text("Entrar como Visitante Temporário", color = NeonCyan, fontSize = 12.sp)
            }
        }

        if (showPreLaunchDialog) {
            PreLaunchLandingDialog(
                viewModel = viewModel,
                onDismiss = { showPreLaunchDialog = false }
            )
        }
    }
}
