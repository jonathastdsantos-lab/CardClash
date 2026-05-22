package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
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
import com.example.data.model.*
import com.example.ui.components.FutCardShape
import com.example.ui.components.FutCardView
import com.example.ui.theme.*

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CollectionScreen(
    inventory: List<UserInventory>,
    onToggleBattleDeck: (Int) -> Unit
) {
    var rarityFilter by remember { mutableStateOf<Rarity?>(null) }
    var positionFilter by remember { mutableStateOf<Position?>(null) }
    var ownedOnly by remember { mutableStateOf(false) }

    // Map database inventory index by Card ID
    val inventoryMap = remember(inventory) {
        inventory.associateBy { it.cardId }
    }

    // Filter cards
    val filteredCards = remember(rarityFilter, positionFilter, ownedOnly, inventoryMap) {
        CardCatalog.cards.filter { card ->
            val matchesRarity = rarityFilter == null || card.rarity == rarityFilter
            val matchesPosition = positionFilter == null || card.position == positionFilter
            val inv = inventoryMap[card.id]
            val hasCard = inv != null && inv.quantity > 0
            val matchesOwned = !ownedOnly || hasCard
            matchesRarity && matchesPosition && matchesOwned
        }
    }

    // Completion percentage calculation
    val totalAvailable = CardCatalog.cards.size
    val uniqueOwned = CardCatalog.cards.count { card ->
        val inv = inventoryMap[card.id]
        inv != null && inv.quantity > 0
    }
    val completionPercent = if (totalAvailable > 0) (uniqueOwned * 100) / totalAvailable else 0

    // Selected card modal details state
    var selectedDetailCard by remember { mutableStateOf<PlayerCard?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(StadiumObsidian)
            .statusBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Upper Progress Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .border(1.dp, StadiumBorder, shape = RoundedCornerShape(12.dp)),
            colors = CardDefaults.cardColors(containerColor = StadiumConcrete)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Álbum de Figurinhas",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "$uniqueOwned/$totalAvailable Cards",
                        color = NeonCyan,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                LinearProgressIndicator(
                    progress = { completionPercent / 100f },
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                    color = NeonEmerald,
                    trackColor = StadiumGlow
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Progresso Geral",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 12.sp
                    )
                    Text(
                        text = "$completionPercent%",
                        color = NeonEmerald,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Filters Container Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .border(1.dp, StadiumBorder, shape = RoundedCornerShape(12.dp)),
            colors = CardDefaults.cardColors(containerColor = StadiumConcrete)
        ) {
            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(imageVector = Icons.Default.FilterList, contentDescription = null, tint = NeonEmerald, modifier = Modifier.size(16.dp))
                    Text("Filtros Rápidos", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                // Horizontal list of Rarities filters
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // "Todos" chip
                    FilterChip(
                        selected = rarityFilter == null,
                        onClick = { rarityFilter = null },
                        label = { Text("Tudo", fontSize = 10.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = NeonEmerald,
                            selectedLabelColor = Color.Black
                        )
                    )

                    Rarity.values().forEach { rarity ->
                        val isSel = rarityFilter == rarity
                        val rarityColor = when(rarity) {
                            Rarity.BRONZE -> ColorBronze
                            Rarity.PRATA -> ColorSilver
                            Rarity.OURO -> ColorGold
                            Rarity.ESPECIAL -> ColorEspecial
                            Rarity.LENDARIA -> ColorLendaria
                            Rarity.ASSINADA -> ColorAssinada
                            Rarity.ANIMADA -> ColorAnimada
                        }
                        FilterChip(
                            selected = isSel,
                            onClick = { rarityFilter = if (isSel) null else rarity },
                            label = { Text(rarity.name, fontSize = 10.sp, color = if (isSel) Color.Black else rarityColor) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = rarityColor,
                                selectedLabelColor = Color.Black
                            )
                        )
                    }
                }

                // Stats ownership filter checkbox
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable { ownedOnly = !ownedOnly }
                        .padding(vertical = 4.dp)
                ) {
                    Checkbox(
                        checked = ownedOnly,
                        onCheckedChange = { ownedOnly = it },
                        colors = CheckboxDefaults.colors(checkedColor = NeonEmerald)
                    )
                    Text("Mostrar apenas cards que eu possuo", color = Color.White, fontSize = 11.sp)
                }
            }
        }

        // LazyGrid
        if (filteredCards.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Nenhum card correspondente encontrado!\nTente ajustar seus filtros na barra acima.",
                    color = Color.White.copy(alpha = 0.5f),
                    textAlign = TextAlign.Center,
                    fontSize = 14.sp
                )
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(145.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
                    .padding(horizontal = 12.dp)
                    .testTag("collection_grid"),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(filteredCards) { card ->
                    val invItem = inventoryMap[card.id]
                    val qty = invItem?.quantity ?: 0
                    val inDeck = invItem?.inBattleDeck ?: false

                    if (qty > 0) {
                        // Fully unlocked glossy card
                        FutCardView(
                            card = card,
                            quantity = qty,
                            inDeck = inDeck,
                            onClick = { selectedDetailCard = card }
                        )
                    } else {
                        // Dark locked silhouetted card placeholder
                        Box(
                            modifier = Modifier
                                .width(145.dp)
                                .height(220.dp)
                                .background(StadiumGlow.copy(alpha = 0.6f), shape = FutCardShape)
                                .border(1.dp, Color.White.copy(alpha = 0.1f), shape = FutCardShape)
                                .clickable { selectedDetailCard = card },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = card.name,
                                    color = Color.White.copy(alpha = 0.35f),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = "BLOQUEADO",
                                    color = Color.White.copy(alpha = 0.2f),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Detail Pop-up drawer logic
    selectedDetailCard?.let { card ->
        val inv = inventoryMap[card.id]
        val qty = inv?.quantity ?: 0
        val inDeck = inv?.inBattleDeck ?: false

        AlertDialog(
            onDismissRequest = { selectedDetailCard = null },
            containerColor = StadiumConcrete,
            title = {
                Text(text = card.name, color = Color.White, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Raridade: ${card.rarity.name}", color = NeonCyan, fontWeight = FontWeight.SemiBold)
                    Text("Time/País: ${card.clubAndCountry}", color = Color.White.copy(alpha = 0.8f))
                    Text("Posição: ${card.position.name}", color = Color.White.copy(alpha = 0.8f))
                    Text("Inventário pessoal: Você possui $qty unidade(s).", color = Color.White)

                    if (qty > 0) {
                        Divider(color = Color.White.copy(alpha = 0.12f))
                        Text(
                            text = "Adicione esta figurinha ao seu 'Baralho de Batalha' para utilizá-la em disputas competitivas de Bafo!",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 11.sp
                        )
                    }
                }
            },
            confirmButton = {
                if (qty > 0) {
                    Button(
                        colors = ButtonDefaults.buttonColors(containerColor = if (inDeck) Color.Red else NeonEmerald),
                        onClick = {
                            onToggleBattleDeck(card.id)
                            selectedDetailCard = null
                        }
                    ) {
                        Text(
                            text = if (inDeck) "Remover da Batalha" else "Usar na Batalha",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedDetailCard = null }) {
                    Text("Fechar", color = Color.White)
                }
            }
        )
    }
}
