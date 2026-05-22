package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.theme.*

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TradeHubScreen(
    tradeOffers: List<TradeOffer>,
    inventory: List<UserInventory>,
    onAcceptTrade: (TradeOffer, (Boolean) -> Unit) -> Unit,
    onCreateTrade: (Int, Int, (Boolean) -> Unit) -> Unit,
    onCancelTrade: (String) -> Unit
) {
    var showCreateTradeDialog by remember { mutableStateOf(false) }
    var selectedOfferCardId by remember { mutableStateOf<Int?>(null) }
    var selectedRequestCardId by remember { mutableStateOf<Int?>(null) }
    var activeTradeConfirmationOffer by remember { mutableStateOf<TradeOffer?>(null) }

    var feedbackMessage by remember { mutableStateOf<String?>(null) }

    // Map inventories for fast lookup
    val inventoryMap = remember(inventory) {
        inventory.associateBy { it.cardId }
    }

    val ownedCardsForOffer = remember(inventory) {
        inventory.filter { it.quantity > 0 }.mapNotNull {
            CardCatalog.getCardById(it.cardId)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(StadiumObsidian)
            .statusBarsPadding()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Hub Header Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(imageVector = Icons.Default.People, contentDescription = null, tint = NeonEmerald, modifier = Modifier.size(28.dp))
                Text("Feira de Trocas", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
            }

            Button(
                colors = ButtonDefaults.buttonColors(containerColor = NeonEmerald),
                shape = RoundedCornerShape(8.dp),
                onClick = { showCreateTradeDialog = true },
                modifier = Modifier.testTag("open_trade_creator_btn")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                    Text("Criar Troca", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        feedbackMessage?.let { msg ->
            Card(
                colors = CardDefaults.cardColors(containerColor = StadiumGlow),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = msg, color = NeonEmerald, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    TextButton(onClick = { feedbackMessage = null }) {
                        Text("OK", color = Color.White)
                    }
                }
            }
        }

        Text(
            text = "Troque cartas duplicadas com outros colecionadores. Todas as propostas são retidas de forma atômica e segura.",
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 11.sp,
            lineHeight = 15.sp
        )

        // Offers Listings LazyColumn
        if (tradeOffers.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().weight(1f), contentAlignment = Alignment.Center) {
                Text("Nenhuma proposta de troca ativa no mural de hoje!\nCrie a primeira clicando no botão acima.", color = Color.White.copy(alpha = 0.5f), textAlign = TextAlign.Center)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().weight(1f).testTag("trades_list"),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(tradeOffers) { offer ->
                    val offerCard = CardCatalog.getCardById(offer.offerCardId)
                    val requestCard = CardCatalog.getCardById(offer.requestCardId)

                    if (offerCard != null && requestCard != null) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, StadiumBorder, shape = RoundedCornerShape(12.dp)),
                            colors = CardDefaults.cardColors(containerColor = StadiumConcrete)
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                // Author details
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = "Proposta de: ${offer.posterName}", color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp)
                                    if (offer.status == "PENDING") {
                                        Box(
                                            modifier = Modifier
                                                .background(NeonCyan.copy(alpha = 0.15f), shape = RoundedCornerShape(4.dp))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Icon(imageVector = Icons.Default.Verified, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(10.dp))
                                                Text("AGUARDANDO", color = NeonCyan, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    } else {
                                        Text(text = offer.status, color = NeonEmerald, fontSize = 9.sp, fontWeight = FontWeight.Black)
                                    }
                                }

                                // Swap trade visual cards
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Give Card column
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("ELE OFERECE:", color = NeonCyan, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                        Text(text = offerCard.name, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold)
                                        Text(text = "OVER ${offerCard.overall} | ${offerCard.rarity.name}", color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp)
                                    }

                                    Icon(imageVector = Icons.Default.ArrowForward, contentDescription = "Por", tint = NeonEmerald, modifier = Modifier.padding(horizontal = 6.dp))

                                    // Want Card column
                                    Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                                        Text("ELE BUSCA:", color = BrightGold, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                        Text(text = requestCard.name, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.End)
                                        Text(text = "OVER ${requestCard.overall} | ${requestCard.rarity.name}", color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp, textAlign = TextAlign.End)
                                    }
                                }

                                // Footer confirm actions
                                if (offer.status == "PENDING") {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End
                                    ) {
                                        if (offer.posterName.contains("Você")) {
                                            // Cancel my own offer
                                            TextButton(
                                                onClick = { onCancelTrade(offer.id) }
                                            ) {
                                                Text("CANCELAR MEU CARD EM CUSTÓDIA", color = Color.Red, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }
                                        } else {
                                            // Accept other people's trades
                                            val ownsRequestedCard = (inventoryMap[offer.requestCardId]?.quantity ?: 0) > 0
                                            Button(
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = if (ownsRequestedCard) NeonEmerald else Color.Gray.copy(alpha = 0.15f)
                                                ),
                                                enabled = ownsRequestedCard,
                                                shape = RoundedCornerShape(8.dp),
                                                onClick = { activeTradeConfirmationOffer = offer },
                                                modifier = Modifier.testTag("accept_trade_btn_${offer.id}")
                                            ) {
                                                Text(
                                                    text = if (ownsRequestedCard) "Aceitar Troca" else "Sem estoque para trocar",
                                                    color = if (ownsRequestedCard) Color.Black else Color.White.copy(alpha = 0.35f),
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
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

    // CREATE PROPOSAL MODAL DIALOGUE
    if (showCreateTradeDialog) {
        AlertDialog(
            onDismissRequest = { showCreateTradeDialog = false },
            containerColor = StadiumConcrete,
            title = { Text("Publicar Proposta de Troca", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Importante: Propostas seguras. O card oferecido será temporariamente retido na custódia segura do app até que alguém aceite.",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 11.sp
                    )

                    // 1. SELECT OFFER
                    Text("1. Escolha o card que você deseja dar (Oferecer):", color = NeonCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    if (ownedCardsForOffer.isEmpty()) {
                        Text("Você não possui cards adicionais duplicados livres em inventory!", color = Color.Red, fontSize = 11.sp)
                    } else {
                        var dropdown1Expanded by remember { mutableStateOf(false) }
                        val offerName = selectedOfferCardId?.let { CardCatalog.getCardById(it)?.name } ?: "Selecione o card..."

                        Box {
                            Button(
                                onClick = { dropdown1Expanded = true },
                                colors = ButtonDefaults.buttonColors(containerColor = StadiumGlow),
                                modifier = Modifier.fillMaxWidth().testTag("select_offer_card_btn")
                            ) {
                                Text(offerName, color = Color.White)
                            }
                            DropdownMenu(
                                expanded = dropdown1Expanded,
                                onDismissRequest = { dropdown1Expanded = false },
                                modifier = Modifier.background(StadiumGlow)
                            ) {
                                ownedCardsForOffer.forEach { card ->
                                    DropdownMenuItem(
                                        text = { Text("${card.name} (OVER ${card.overall} | ${card.rarity.name})", color = Color.White) },
                                        onClick = {
                                            selectedOfferCardId = card.id
                                            dropdown1Expanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // 2. SELECT WANT
                    Text("2. Escolha o card que você quer receber (Pedir):", color = BrightGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    var dropdown2Expanded by remember { mutableStateOf(false) }
                    val wantName = selectedRequestCardId?.let { CardCatalog.getCardById(it)?.name } ?: "Selecione o card..."

                    Box {
                        Button(
                            onClick = { dropdown2Expanded = true },
                            colors = ButtonDefaults.buttonColors(containerColor = StadiumGlow),
                            modifier = Modifier.fillMaxWidth().testTag("select_request_card_btn")
                        ) {
                            Text(wantName, color = Color.White)
                        }
                        DropdownMenu(
                            expanded = dropdown2Expanded,
                            onDismissRequest = { dropdown2Expanded = false },
                            modifier = Modifier.background(StadiumGlow)
                        ) {
                            CardCatalog.cards.forEach { card ->
                                DropdownMenuItem(
                                    text = { Text("${card.name} (OVER ${card.overall} | ${card.rarity.name})", color = Color.White) },
                                    onClick = {
                                        selectedRequestCardId = card.id
                                        dropdown2Expanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                val ok = selectedOfferCardId != null && selectedRequestCardId != null
                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = if (ok) NeonEmerald else Color.Gray),
                    enabled = ok,
                    onClick = {
                        onCreateTrade(selectedOfferCardId!!, selectedRequestCardId!!) { res ->
                            if (res) {
                                feedbackMessage = "Proposta criada com sucesso! Card depositado na custódia interna."
                            } else {
                                feedbackMessage = "Falha ao criar proposta. Verifique se o card possui estoque disponível."
                            }
                        }
                        showCreateTradeDialog = false
                    },
                    modifier = Modifier.testTag("submit_trade_proposal_btn")
                ) {
                    Text("Publicar Proposta", color = Color.Black)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateTradeDialog = false }) {
                    Text("Cancelar", color = Color.White)
                }
            }
        )
    }

    // CONFIRM DOUBLE-CHECK TRADE DIALOGUE
    activeTradeConfirmationOffer?.let { offer ->
        val offerCard = CardCatalog.getCardById(offer.offerCardId)
        val requestCard = CardCatalog.getCardById(offer.requestCardId)

        if (offerCard != null && requestCard != null) {
            AlertDialog(
                onDismissRequest = { activeTradeConfirmationOffer = null },
                containerColor = StadiumConcrete,
                icon = { Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = NeonEmerald) },
                title = { Text("Confirmar Troca Atômica", color = Color.White, fontWeight = FontWeight.Bold) },
                text = {
                    Text(
                        text = "Você está prestes a trocar com ${offer.posterName}.\n\nAo confirmar:\n" +
                               "1. Você ENVIARÁ seu card [${requestCard.name}].\n" +
                               "2. Você RECEBERÁ o card [${offerCard.name}].\n" +
                               "Essa ação é permanente e irrevogável.",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 13.sp
                    )
                },
                confirmButton = {
                    Button(
                        colors = ButtonDefaults.buttonColors(containerColor = NeonEmerald),
                        onClick = {
                            onAcceptTrade(offer) { res ->
                                if (res) {
                                    feedbackMessage = "Troca efetuada com sucesso! Cards creditados atonicamente."
                                } else {
                                    feedbackMessage = "Erro ao processar. Você possui saldo de cartas suficiente para esta proposta?"
                                }
                            }
                            activeTradeConfirmationOffer = null
                        }
                    ) {
                        Text("Confirmar Termos", color = Color.Black)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { activeTradeConfirmationOffer = null }) {
                        Text("Recusar", color = Color.White)
                    }
                }
            )
        }
    }
}
