package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
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
    onCustomizeCard: (Int, String?, String?, String?, (Boolean, String) -> Unit) -> Unit,
    onAutoFillCardFromIA: (Int, String, (Boolean, String, com.example.data.api.PlayerUpdateInfo?) -> Unit) -> Unit,
    onUpdateCardSticker: (Int, String?, (Boolean, String) -> Unit) -> Unit,
    onOpenPreLaunchCampaign: () -> Unit
) {
    val context = LocalContext.current
    var rarityFilter by remember { mutableStateOf<Rarity?>(null) }
    var positionFilter by remember { mutableStateOf<Position?>(null) }
    var ownedOnly by remember { mutableStateOf(false) }
    var upadasOnly by remember { mutableStateOf(false) }
    var stickerOnly by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var isFiltersExpanded by remember { mutableStateOf(false) }
    var selectedLeagueFilter by remember { mutableStateOf<String?>("Brasileirão 2026") }

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
    val filteredCards = remember(rarityFilter, positionFilter, ownedOnly, upadasOnly, stickerOnly, searchQuery, selectedLeagueFilter, inventoryMap) {
        CardCatalog.cards.filter { card ->
            val matchesRarity = rarityFilter == null || card.rarity == rarityFilter
            val matchesPosition = positionFilter == null || card.position == positionFilter
            val matchesLeague = selectedLeagueFilter == null || card.league == selectedLeagueFilter
            val inv = inventoryMap[card.id]
            val hasCard = inv != null && inv.quantity > 0
            val matchesOwned = !ownedOnly || hasCard
            
            val matchesUpadas = !upadasOnly || (inv != null && inv.upgradeLevel > 0)
            val matchesSticker = !stickerOnly || (inv != null && !inv.stickerEmoji.isNullOrBlank())
            
            val displayName = inv?.customName ?: card.name
            val matchesSearch = displayName.contains(searchQuery, ignoreCase = true) || 
                                card.clubAndCountry.contains(searchQuery, ignoreCase = true)
            matchesRarity && matchesPosition && matchesOwned && matchesUpadas && matchesSticker && matchesSearch && matchesLeague
        }
    }

    // Completion percentage calculation
    val totalAvailable = remember(selectedLeagueFilter) {
        if (selectedLeagueFilter == null) CardCatalog.cards.size 
        else CardCatalog.cards.count { it.league == selectedLeagueFilter }
    }
    val uniqueOwned = remember(selectedLeagueFilter, inventoryMap) {
        if (selectedLeagueFilter == null) {
            CardCatalog.cards.count { card ->
                val inv = inventoryMap[card.id]
                inv != null && inv.quantity > 0
            }
        } else {
            CardCatalog.cards.count { card ->
                card.league == selectedLeagueFilter && (inventoryMap[card.id]?.quantity ?: 0) > 0
            }
        }
    }
    val completionPercent = if (totalAvailable > 0) (uniqueOwned * 100) / totalAvailable else 0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(StadiumObsidian)
            .statusBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 🏆 Copa 2026 pre-launch campaign promo bar
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .border(1.dp, NeonCyan.copy(alpha = 0.5f), shape = RoundedCornerShape(8.dp))
                .clickable { onOpenPreLaunchCampaign() },
            colors = CardDefaults.cardColors(containerColor = StadiumConcrete)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(text = "🏆", fontSize = 16.sp)
                Text(
                    text = "Copa 2026: \"Quando a Copa acabar, sua coleção continua.\" Participe do Bracket grátis!",
                    color = NeonCyan,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Text(text = "PARTICIPAR ➔", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Black)
            }
        }

        // Upper Progress Card - Sleek, space-saving design
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .border(1.dp, StadiumBorder, shape = RoundedCornerShape(12.dp)),
            colors = CardDefaults.cardColors(containerColor = StadiumConcrete)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = if (selectedLeagueFilter == null) "Álbum Geral" else selectedLeagueFilter!!,
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Box(
                            modifier = Modifier
                                .background(NeonEmerald.copy(alpha = 0.15f), shape = RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "$uniqueOwned / $totalAvailable",
                                color = NeonEmerald,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                    Text(
                        text = "$completionPercent% Completo",
                        color = NeonCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                LinearProgressIndicator(
                    progress = { completionPercent / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(5.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = NeonEmerald,
                    trackColor = StadiumGlow
                )
            }
        }

        // Horizontal league/championship filter scroll
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val leaguesList = listOf(
                "Brasileirão 2026",
                "Champions 25",
                "Premier League",
                "La Liga",
                "MLS",
                "Copa do Mundo",
                null
            )
            leaguesList.forEach { league ->
                val isSelected = selectedLeagueFilter == league
                val displayName = league ?: "Ver Tudo"
                val buttonColor = if (isSelected) NeonCyan else StadiumConcrete
                val textColor = if (isSelected) Color.Black else Color.White
                val borderColor = if (isSelected) NeonCyan else StadiumBorder
                
                Box(
                    modifier = Modifier
                        .background(buttonColor, shape = RoundedCornerShape(8.dp))
                        .border(1.dp, borderColor, shape = RoundedCornerShape(8.dp))
                        .clickable { selectedLeagueFilter = league }
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = displayName,
                            color = textColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        if (league == "Brasileirão 2026") {
                            Box(
                                modifier = Modifier
                                    .background(NeonEmerald, shape = RoundedCornerShape(4.dp))
                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = "ATIVO",
                                    color = Color.Black,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }
                }
            }
        }

        // Search Field Card - Compact vertical space
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

        // Filters Container Card - Collapsible to fully clear area for cards
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .border(1.dp, StadiumBorder, shape = RoundedCornerShape(12.dp)),
            colors = CardDefaults.cardColors(containerColor = StadiumConcrete)
        ) {
            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isFiltersExpanded = !isFiltersExpanded }
                        .padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(imageVector = Icons.Default.FilterList, contentDescription = null, tint = NeonEmerald, modifier = Modifier.size(16.dp))
                        Text("Filtros Rápidos", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        
                        // Show active count badge if filters are collapsed but there are active filters
                        val activeCount = (if (rarityFilter != null) 1 else 0) +
                                (if (positionFilter != null) 1 else 0) +
                                (if (ownedOnly) 1 else 0) +
                                (if (upadasOnly) 1 else 0) +
                                (if (stickerOnly) 1 else 0)
                        if (activeCount > 0 && !isFiltersExpanded) {
                            Box(
                                modifier = Modifier
                                    .background(NeonEmerald, shape = CircleShape)
                                    .size(18.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = activeCount.toString(),
                                    color = Color.Black,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = if (isFiltersExpanded) "Recolher" else "Expandir",
                            color = NeonCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Icon(
                            imageVector = if (isFiltersExpanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                            contentDescription = if (isFiltersExpanded) "Recolher Filtros" else "Expandir Filtros",
                            tint = NeonCyan,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                if (isFiltersExpanded) {
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

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Option 2: Upadas Only
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { upadasOnly = !upadasOnly }
                                .background(if (upadasOnly) NeonEmerald.copy(alpha = 0.1f) else Color.Transparent, shape = RoundedCornerShape(6.dp))
                                .border(0.5.dp, if (upadasOnly) NeonEmerald else Color.White.copy(alpha = 0.1f), shape = RoundedCornerShape(6.dp))
                                .padding(end = 8.dp)
                        ) {
                            Checkbox(
                                checked = upadasOnly,
                                onCheckedChange = { upadasOnly = it },
                                colors = CheckboxDefaults.colors(checkedColor = NeonEmerald)
                            )
                            Text("Evoluídas ⚡", color = if (upadasOnly) NeonEmerald else Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }

                        // Option 2: Sticker Only
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { stickerOnly = !stickerOnly }
                                .background(if (stickerOnly) NeonCyan.copy(alpha = 0.1f) else Color.Transparent, shape = RoundedCornerShape(6.dp))
                                .border(0.5.dp, if (stickerOnly) NeonCyan else Color.White.copy(alpha = 0.1f), shape = RoundedCornerShape(6.dp))
                                .padding(end = 8.dp)
                        ) {
                            Checkbox(
                                checked = stickerOnly,
                                onCheckedChange = { stickerOnly = it },
                                colors = CheckboxDefaults.colors(checkedColor = NeonCyan)
                            )
                            Text("Adesivos 🎨", color = if (stickerOnly) NeonCyan else Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
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
                columns = GridCells.Adaptive(135.dp),
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
                        if (invItem?.customClubAndCountry != null) {
                            c = c.copy(clubAndCountry = invItem.customClubAndCountry)
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
                            upgradeLevel = upgradeLevel,
                            stickerEmoji = invItem?.stickerEmoji,
                            onClick = { selectedDetailCard = boostedCard }
                        )
                    } else {
                        // Dark locked silhouetted card placeholder with same footprint
                        Box(
                            modifier = Modifier
                                .width(140.dp)
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
        val boughtWithMoney = inv?.boughtWithMoney == true

        // Check if player is currently in a live match
        val isGenuineLive = liveMatches.any { it.isLive && (card.clubAndCountry.contains(it.homeTeam, ignoreCase = true) || card.clubAndCountry.contains(it.awayTeam, ignoreCase = true)) }
        val isLiveOverridden = liveSimulatedCards[card.id] == true
        val isPlayerLiveActive = isGenuineLive || isLiveOverridden

        // Lifted state variables for card customization to avoid Compose remembering bugs inside conditional blocks
        var isCustomizing by remember(card.id) { mutableStateOf(false) }
        var editedName by remember(card.id, inv?.customName) { mutableStateOf(inv?.customName ?: "") }
        var editedPhotoUrl by remember(card.id, inv?.customPhotoUrl) { mutableStateOf(inv?.customPhotoUrl ?: "") }
        var editedClubAndCountry by remember(card.id, inv?.customClubAndCountry) { mutableStateOf(inv?.customClubAndCountry ?: "") }
        var isIAFilling by remember { mutableStateOf(false) }

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
                    // Option 2: Customized Preview (Realtime render)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        FutCardView(
                            card = card,
                            upgradeLevel = upgradeLevel,
                            stickerEmoji = inv?.stickerEmoji,
                            inDeck = inDeck
                        )
                    }

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

                    // PERSONALIZAÇÃO DA IDENTIDADE
                    if (qty > 0) {
                        Divider(color = Color.White.copy(alpha = 0.12f))
                        
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
                                Text("Personalizar Identidade (Opção 1 & 2)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.5f),
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        if (isCustomizing) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                            ) {
                                Text(
                                    text = "Auto-preencha de forma automática com Inteligência Artificial, ou digite manualmente e selecione fotos customizadas!",
                                    color = Color.White.copy(alpha = 0.6f),
                                    fontSize = 11.sp
                                )

                                // IA AUTOFILL CALL SECTION
                                Button(
                                    onClick = {
                                        isIAFilling = true
                                        onAutoFillCardFromIA(card.id, card.name) { success, msg, info ->
                                            isIAFilling = false
                                            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                            if (success && info != null) {
                                                editedName = info.name
                                                editedClubAndCountry = info.clubAndCountry
                                                if (!info.photoUrl.isNullOrBlank()) {
                                                    editedPhotoUrl = info.photoUrl
                                                }
                                            }
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp),
                                    enabled = !isIAFilling
                                ) {
                                    if (isIAFilling) {
                                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.Black, strokeWidth = 2.dp)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Consultando Dados de 2026...", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    } else {
                                        Icon(imageVector = Icons.Default.FlashOn, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Auto-Preencher com IA (Opção 2)", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }
                                }

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
                                    value = editedClubAndCountry,
                                    onValueChange = { editedClubAndCountry = it },
                                    label = { Text("Clube / Seleção", fontSize = 11.sp) },
                                    placeholder = { Text("Ex: Real Madrid / França (ou vazio)", fontSize = 11.sp) },
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

                                // Option 2: Digital Sticker Selection Grid
                                Text(
                                    text = "🎨 Escolher Adesivo Digital (Opção 2 - Premium)",
                                    color = NeonCyan,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                                
                                var selectedSticker by remember(card.id, inv?.stickerEmoji) { mutableStateOf(inv?.stickerEmoji) }
                                
                                val availableStickers = listOf(
                                    Pair("X", null),
                                    Pair("🔥 Fogo", "🔥"),
                                    Pair("⭐ Star", "⭐"),
                                    Pair("👑 Coroa", "👑"),
                                    Pair("⚡ Raio", "⚡"),
                                    Pair("💎 Diamond", "💎"),
                                    Pair("🛡️ Escudo", "🛡️"),
                                    Pair("⚽ Bola", "⚽")
                                )

                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    availableStickers.forEach { (stickerText, emoji) ->
                                        val isThisSelected = selectedSticker == emoji
                                        Box(
                                            modifier = Modifier
                                                .background(
                                                    if (isThisSelected) NeonEmerald.copy(alpha = 0.2f) else StadiumGlow,
                                                    shape = RoundedCornerShape(6.dp)
                                                )
                                                .border(
                                                    width = 1.dp,
                                                    color = if (isThisSelected) NeonEmerald else Color.White.copy(alpha = 0.15f),
                                                    shape = RoundedCornerShape(6.dp)
                                                )
                                                .clickable {
                                                    selectedSticker = emoji
                                                    onUpdateCardSticker(card.id, emoji) { success, msg ->
                                                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                                    }
                                                }
                                                .padding(horizontal = 8.dp, vertical = 6.dp)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                if (emoji != null) {
                                                    Text(text = emoji, fontSize = 12.sp)
                                                }
                                                Text(
                                                    text = stickerText,
                                                    color = if (isThisSelected) NeonEmerald else Color.White,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                }

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
                                            editedClubAndCountry = ""
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.3f)),
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text("Limpar", color = Color.White, fontSize = 11.sp)
                                    }

                                    Button(
                                        onClick = {
                                            onCustomizeCard(card.id, editedName, editedPhotoUrl, editedClubAndCountry) { success, msg ->
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
                    if (boughtWithMoney) {
                        Surface(
                            color = Color(0xFF6A1BFF).copy(alpha = 0.2f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF8B5CF6)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.padding(vertical = 4.dp, horizontal = 12.dp)
                        ) {
                            Text(
                                text = "🔒 Protegido (Compra Real)",
                                color = Color(0xFFA78BFA),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            )
                        }
                    } else {
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
