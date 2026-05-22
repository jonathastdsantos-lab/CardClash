package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onRegisterSuccess: (name: String, team: String, age: Int, provider: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
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
        Column(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Icon Header
            Icon(
                imageVector = Icons.Default.SportsSoccer,
                contentDescription = "Fut Cards",
                tint = NeonEmerald,
                modifier = Modifier.size(72.dp)
            )

            Text(
                text = "FUT CARDS",
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

            Spacer(modifier = Modifier.height(8.dp))

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

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
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
    }
}
