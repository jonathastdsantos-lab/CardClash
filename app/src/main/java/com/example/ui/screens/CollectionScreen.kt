package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.components.FutCardShape
import com.example.ui.components.FutCardView
import com.example.ui.theme.*

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun CollectionScreen(
    inventory: List<UserInventory>,
    liveMatches: List<LiveMatch> = emptyList(),
    onToggleBattleDeck: (Int) -> Unit,
    onUpgradeCard: (Int, (Boolean, String) -> Unit) -> Unit,
    onCustomizeCard: (Int, String?, String?, (Boolean, String) -> Unit) -> Unit
) {
    val context = LocalContext.current
    var rarityFilter by remember { mutableStateOf<Rarity?>(null) }
    var positionFilter by remember { mutableStateOf<Position?>(null) }
    var ownedOnly by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    // Map database inventory index by Card ID
    val inventoryMap = remember(inventory) {
        inventory.associateBy { it.cardId }
    }

    // Interactive simulated "LIVE" override state for immersion
    val liveSimulatedCards = remember { mutableStateMapOf<Int, Boolean>() }

    // Selected card modal details state
    var selectedDetailCard by remember { mutableStateOf<PlayerCard?>(null) }

    // Nested match details dialog state
    var selectedMatchDetails by remember { mutableStateOf<String?>(null) }

    // Filter cards
    val filteredCards = remember(rarityFilter, positionFilter, ownedOnly, searchQuery, inventoryMap) {
        CardCatalog.cards.filter { card ->
            val matchesRarity = rarityFilter == null || card.rarity == rarityFilter
            val matchesPosition = positionFilter == null || card.position == positionFilter
            val inv = inventoryMap[card.id]
            val hasCard = inv != null && inv.quantity > 0
            val matchesOwned = !ownedOnly || hasCard
            
            val displayName = inv?.customName ?: card.name
            val matchesSearch = displayName.contains(searchQuery, ignoreCase = true) || 
                                card.clubAndCountry.contains(searchQuery, ignoreCase = true)
            matchesRarity && matchesPosition && matchesOwned && matchesSearch
        }
    }

    // Completion percentage calculation
    val totalAvailable = CardCatalog.cards.size
    val uniqueOwned = CardCatalog.cards.count { card ->
        val inv = inventoryMap[card.id]
        inv != null && inv.quantity > 0
    }
    val completionPercent = if (totalAvailable > 0) (uniqueOwned * 100) / totalAvailable else 0

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

        // Search Field Card
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Buscar por nome da figurinha ou time...", color = Color.White.copy(alpha = 0.5f), fontSize = 13.sp) },
            leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(18.dp)) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .background(StadiumConcrete, shape = RoundedCornerShape(12.dp))
                .border(1.dp, StadiumBorder, shape = RoundedCornerShape(12.dp)),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = NeonCyan,
                unfocusedBorderColor = Color.Transparent
            ),
            singleLine = true
        )

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
                    val upgradeLevel = invItem?.upgradeLevel ?: 0

                    // Dynamic upgrade boost logic
                    val boostedCard = remember(card, upgradeLevel, invItem) {
                        var c = card
                        if (invItem?.customName != null) {
                            c = c.copy(name = invItem.customName)
                        }
                        if (invItem?.customPhotoUrl != null) {
                            c = c.copy(photoUrl = invItem.customPhotoUrl)
                        }
                        if (upgradeLevel > 0) {
                            val boost = upgradeLevel * 3
                            val rawStats = card.stats
                            val upgradedStats = PlayerStats(
                                pac = (rawStats.pac + boost).coerceAtMost(99),
                                sho = (rawStats.sho + boost).coerceAtMost(99),
                                pas = (rawStats.pas + boost).coerceAtMost(99),
                                dri = (rawStats.dri + boost).coerceAtMost(99),
                                def = (rawStats.def + boost).coerceAtMost(99),
                                phy = (rawStats.phy + boost).coerceAtMost(99)
                            )
                            val upgradedRarity = when (upgradeLevel) {
                                1 -> if (card.rarity < Rarity.OURO) Rarity.OURO else card.rarity
                                2 -> if (card.rarity < Rarity.LENDARIA) Rarity.LENDARIA else card.rarity
                                else -> Rarity.ANIMADA
                            }
                            c = c.copy(
                                overall = (card.overall + boost).coerceAtMost(99),
                                stats = upgradedStats,
                                rarity = upgradedRarity
                            )
                        }
                        c
                    }

                    if (qty > 0) {
                        // Fully unlocked boosted card
                        FutCardView(
                            card = boostedCard,
                            quantity = qty,
                            inDeck = inDeck,
                            onClick = { selectedDetailCard = boostedCard }
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

    // Detail Pop-up layout with dynamic stats controls and upgrade evolutions
    selectedDetailCard?.let { card ->
        val inv = inventoryMap[card.id]
        val qty = inv?.quantity ?: 0
        val inDeck = inv?.inBattleDeck ?: false
        val upgradeLevel = inv?.upgradeLevel ?: 0

        // Check if player is currently in a live match
        val isGenuineLive = liveMatches.any { it.isLive && (card.clubAndCountry.contains(it.homeTeam, ignoreCase = true) || card.clubAndCountry.contains(it.awayTeam, ignoreCase = true)) }
        val isLiveOverridden = liveSimulatedCards[card.id] == true
        val isPlayerLiveActive = isGenuineLive || isLiveOverridden

        AlertDialog(
            onDismissRequest = { selectedDetailCard = null },
            containerColor = StadiumConcrete,
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = card.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        Text(text = card.clubAndCountry, color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
                    }
                    if (isPlayerLiveActive) {
                        Surface(
                            color = Color.Red,
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier.padding(horizontal = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .background(Color.White, shape = CircleShape)
                                )
                                Text(
                                    text = "AO VIVO",
                                    color = Color.White,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }
                }
            },
            text = {
                val scrollState = rememberScrollState()
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(scrollState)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Raridade: ${card.rarity.name}", color = NeonCyan, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        Text("Posição: ${card.position.name}", color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp)
                    }

                    Text("Quantidade no Inventário: $qty", color = Color.White, fontSize = 13.sp)

                    // Stats bars layout
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Estatísticas Atualizadas:", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)

                        // PAC
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("RIT: ${card.stats.pac}", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp, modifier = Modifier.width(44.dp))
                            LinearProgressIndicator(
                                progress = { card.stats.pac / 100f },
                                color = NeonCyan,
                                trackColor = StadiumGlow,
                                modifier = Modifier.weight(1f).height(6.dp).clip(RoundedCornerShape(3.dp))
                            )
                        }
                        // SHO
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("FIN: ${card.stats.sho}", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp, modifier = Modifier.width(44.dp))
                            LinearProgressIndicator(
                                progress = { card.stats.sho / 100f },
                                color = NeonCyan,
                                trackColor = StadiumGlow,
                                modifier = Modifier.weight(1f).height(6.dp).clip(RoundedCornerShape(3.dp))
                            )
                        }
                        // PAS
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("PAS: ${card.stats.pas}", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp, modifier = Modifier.width(44.dp))
                            LinearProgressIndicator(
                                progress = { card.stats.pas / 100f },
                                color = NeonCyan,
                                trackColor = StadiumGlow,
                                modifier = Modifier.weight(1f).height(6.dp).clip(RoundedCornerShape(3.dp))
                            )
                        }
                        // DRI
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("CON: ${card.stats.dri}", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp, modifier = Modifier.width(44.dp))
                            LinearProgressIndicator(
                                progress = { card.stats.dri / 100f },
                                color = NeonCyan,
                                trackColor = StadiumGlow,
                                modifier = Modifier.weight(1f).height(6.dp).clip(RoundedCornerShape(3.dp))
                            )
                        }
                    }

                    // EVOLUTIONS UPGRADE MECHANISM (Earned coins application)
                    if (qty > 0) {
                        Divider(color = Color.White.copy(alpha = 0.12f))
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Evolução do Card (EA FC Style)",
                                    color = NeonEmerald,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = "⭐ Nível $upgradeLevel de 3",
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontSize = 11.sp
                                )
                            }

                            if (upgradeLevel < 3) {
                                Button(
                                    onClick = {
                                        onUpgradeCard(card.id) { isSuccess, message ->
                                            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                                            if (isSuccess) {
                                                selectedDetailCard = null
                                            }
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = NeonEmerald),
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Evoluir para Nível ${upgradeLevel + 1} (Custo: 500 🪙)", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(NeonEmerald.copy(alpha = 0.1f), shape = RoundedCornerShape(6.dp))
                                        .padding(8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "🏆 Evolução Máxima Atingida!",
                                        color = NeonEmerald,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }

                    // PERSONALIZAÇÃO DA IDENTIDADE (OPÇÃO 1 - CUSTOM PHOTO & NAME)
                    if (qty > 0) {
                        Divider(color = Color.White.copy(alpha = 0.12f))
                        var isCustomizing by remember { mutableStateOf(false) }
                        
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { isCustomizing = !isCustomizing }
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(imageVector = Icons.Default.Star, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(16.dp))
                                Text("Personalizar Foto e Nome (Opção 1)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.5f),
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        if (isCustomizing) {
                            var editedName by remember { mutableStateOf(inv?.customName ?: "") }
                            var editedPhotoUrl by remember { mutableStateOf(inv?.customPhotoUrl ?: "") }
                            
                            Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                            ) {
                                Text(
                                    text = "Insira um link de imagem pública da internet (ou use algum de nossos presets incríveis abaixo) para mudar o visual do card!",
                                    color = Color.White.copy(alpha = 0.6f),
                                    fontSize = 11.sp
                                )

                                OutlinedTextField(
                                    value = editedName,
                                    onValueChange = { editedName = it },
                                    label = { Text("Nome Personalizado", fontSize = 11.sp) },
                                    placeholder = { Text("Ex: Neymar, Lionel... (ou vazio)", fontSize = 11.sp) },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        focusedBorderColor = NeonCyan,
                                        unfocusedBorderColor = Color.White.copy(alpha = 0.2f)
                                    ),
                                    singleLine = true
                                )

                                OutlinedTextField(
                                    value = editedPhotoUrl,
                                    onValueChange = { editedPhotoUrl = it },
                                    label = { Text("URL da Foto do Jogador", fontSize = 11.sp) },
                                    placeholder = { Text("https://img.unsplash.com/... (ou vazio)", fontSize = 11.sp) },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        focusedBorderColor = NeonCyan,
                                        unfocusedBorderColor = Color.White.copy(alpha = 0.2f)
                                    ),
                                    singleLine = true
                                )

                                // Clickable Presets Row for super high-quality sports silhouettes and generic football aesthetic portraits:
                                Text(
                                    text = "🌟 Modelos de Fotos de Preset:",
                                    color = NeonCyan,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                                
                                val presets = listOf(
                                    Triple("Dynamic Red", "https://images.unsplash.com/photo-1518063319789-7217e6706b04?w=150", Color(0xFFE11D48)),
                                    Triple("Golden Boot", "https://images.unsplash.com/photo-1544005313-94ddf0286df2?w=150", Color(0xFFFFD700)),
                                    Triple("Neon Blue", "https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?w=150", Color(0xFF00FFFF)),
                                    Triple("Sleek Dark", "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=150", Color(0xFF94A3B8)),
                                    Triple("Stadium Green", "https://images.unsplash.com/photo-1508098682722-e99c43a406b2?w=150", Color(0xFF10B981))
                                )

                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    presets.forEach { (title, url, colorValue) ->
                                        Box(
                                            modifier = Modifier
                                                .background(StadiumGlow, shape = RoundedCornerShape(6.dp))
                                                .border(0.5.dp, Color.White.copy(alpha = 0.2f), shape = RoundedCornerShape(6.dp))
                                                .clickable { 
                                                    editedPhotoUrl = url
                                                }
                                                .padding(horizontal = 6.dp, vertical = 4.dp)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Box(modifier = Modifier.size(6.dp).background(colorValue, CircleShape))
                                                Text(text = title, color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            editedName = ""
                                            editedPhotoUrl = ""
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.3f)),
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text("Limpar", color = Color.White, fontSize = 11.sp)
                                    }

                                    Button(
                                        onClick = {
                                            onCustomizeCard(card.id, editedName, editedPhotoUrl) { success, msg ->
                                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                                if (success) {
                                                    selectedDetailCard = null
                                                }
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                                        modifier = Modifier.weight(1.5f),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text("Salvar Identidade", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }

                    // RECENT 5-MATCH FORM TIMELINE (Clickable pills)
                    Divider(color = Color.White.copy(alpha = 0.12f))
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Histórico da Forma Recente (Últimos 5 Jogos):",
                            color = Color.White.copy(alpha = 0.9f),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // W W D L W
                            val formList = listOf(
                                Pair("V", "Vitória por 3x1 contra o Palmeiras. ${card.name} fez 1 gol de falta de longa distância aos 72' e deu assistência decisiva aos 15'!"),
                                Pair("V", "Vitória por 2x0 sobre o Flamengo. ${card.name} controlou o meio de campo, efetuando 6 desarmes fundamentais no segundo tempo!"),
                                Pair("E", "Empate emocionante por 2x2 com o Vasco. ${card.name} contribuiu criando 4 chances de ataque deslumbrantes!"),
                                Pair("D", "Derrota por 1x0 contra o São Paulo. Jogo duro com marcação pesada individual em ${card.name}, que ainda finalizou na trave!"),
                                Pair("V", "Vitória magnífica de 1x0 sobre o Santos. ${card.name} marcou o gol da vitória em cobrança cirúrgica de pênalti aos 89'!")
                            )

                            formList.forEachIndexed { index, pair ->
                                val (outcome, details) = pair
                                val color = when(outcome) {
                                    "V" -> NeonEmerald
                                    "E" -> Color.Gray
                                    else -> Color.Red
                                }
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .background(color = color.copy(alpha = 0.2f), shape = CircleShape)
                                        .border(1.5.dp, color, CircleShape)
                                        .clickable { selectedMatchDetails = "Jogo ${index + 1}: $details" },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = outcome, color = color, fontWeight = FontWeight.Black, fontSize = 11.sp)
                                }
                            }
                        }
                        Text(
                            text = "Toque em um círculo para visualizar os detalhes e notas da partida.",
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 10.sp
                        )
                    }

                    // LIVE SIMULATOR CONTROLLER
                    Divider(color = Color.White.copy(alpha = 0.12f))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { liveSimulatedCards[card.id] = !isLiveOverridden }
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(imageVector = Icons.Default.FlashOn, contentDescription = null, tint = Color.Yellow, modifier = Modifier.size(16.dp))
                            Text("Simular Jogador Ao Vivo", color = Color.White, fontSize = 12.sp)
                        }
                        Switch(
                            checked = isLiveOverridden,
                            onCheckedChange = { liveSimulatedCards[card.id] = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.Red, checkedTrackColor = Color.Red.copy(alpha = 0.3f))
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
                            color = if (inDeck) Color.White else Color.Black,
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

    // Secondary match details dialog
    selectedMatchDetails?.let { details ->
        AlertDialog(
            onDismissRequest = { selectedMatchDetails = null },
            containerColor = Color(0xFF141D2D),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Resumo da Partida do Histórico", color = NeonCyan, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Text(text = details, color = Color.White, fontSize = 13.sp, lineHeight = 18.sp)
            },
            confirmButton = {
                Button(
                    onClick = { selectedMatchDetails = null },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
                ) {
                    Text("Entendido", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}
