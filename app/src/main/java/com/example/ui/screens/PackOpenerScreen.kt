package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PlayerCard
import com.example.ui.components.FutCardShape
import com.example.ui.components.FutCardView
import com.example.ui.theme.*
import com.example.ui.viewmodel.CoinPack
import com.example.ui.viewmodel.BillingSimulationState

@Composable
fun PackOpenerScreen(
    coins: Int,
    isOpeningAnim: Boolean,
    openedCards: List<PlayerCard>?,
    onBuyPack: (String, Int) -> Unit,
    onDismissPack: () -> Unit,
    availableCoinPacks: List<CoinPack> = emptyList(),
    billingState: BillingSimulationState? = null,
    hasElitePass: Boolean = false,
    isSimulatingAd: Boolean = false,
    adCountdown: Int = 0,
    onStartBilling: (CoinPack) -> Unit = {},
    onStartElitePassBilling: () -> Unit = {},
    onCancelBilling: () -> Unit = {},
    onSelectPaymentAndProcess: (CoinPack, String) -> Unit = { _, _ -> },
    onProcessElitePassPurchase: (String) -> Unit = {},
    onPlayAd: () -> Unit = {}
) {
    var selectedPackTypeForInfo by remember { mutableStateOf<String?>(null) }
    val scrollState = rememberScrollState()

    // Shake animation configs
    val infiniteTransition = rememberInfiniteTransition(label = "pack_shake")
    val shakeRotation by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(120, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shake"
    )

    // Current unreleased card index inside the pack unboxing
    var currentRevealIndex by remember { mutableStateOf(0) }

    LaunchedEffect(openedCards) {
        if (openedCards != null) {
            currentRevealIndex = 0
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(StadiumObsidian)
            .statusBarsPadding()
    ) {
        if (!isOpeningAnim && openedCards == null) {
            // --- SHOP LIST MODE ---
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Shop Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.ShoppingBag, contentDescription = null, tint = NeonEmerald, modifier = Modifier.size(28.dp))
                        Text(
                            text = "Mercado de Pacotes",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    // Coins indicator
                    Card(
                        colors = CardDefaults.cardColors(containerColor = StadiumConcrete),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Paid, contentDescription = "Moedas", tint = BrightGold, modifier = Modifier.size(18.dp))
                            Text(text = "$coins Moedas", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Black)
                        }
                    }
                }

                Text(
                    text = "Ganhe moedas completando o quiz de futebol ou disputando bafo, e gaste-as aqui para completar seu álbum com estrelas nacionais!",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )

                // Packs inventory cards
                PackStoreItem(
                    name = "Pacote de Bronze",
                    description = "Contém 3 cards garantindo pelo menos raridade Bronze. Ideal para novatos.",
                    cost = 150,
                    tintColor = ColorBronze,
                    onBuy = { onBuyPack("BRONZE", 150) },
                    onInfo = { selectedPackTypeForInfo = "BRONZE" },
                    userCoins = coins
                )

                PackStoreItem(
                    name = "Pacote de Prata",
                    description = "Contém 3 cards garantindo pelo menos 1 card Prata ou superior.",
                    cost = 350,
                    tintColor = ColorSilver,
                    onBuy = { onBuyPack("PRATA", 350) },
                    onInfo = { selectedPackTypeForInfo = "PRATA" },
                    userCoins = coins
                )

                PackStoreItem(
                    name = "Pacote de Ouro",
                    description = "Contém 4 cards com ótimas chances de craques Ouro e nacionais.",
                    cost = 800,
                    tintColor = ColorGold,
                    onBuy = { onBuyPack("OURO", 800) },
                    onInfo = { selectedPackTypeForInfo = "OURO" },
                    userCoins = coins
                )

                PackStoreItem(
                    name = "Pacote Especial Premium",
                    description = "Contém 4 cards com chances aumentadas de cards da Seleção Brasileira e Estrelas Jovens.",
                    cost = 1500,
                    tintColor = ColorEspecial,
                    onBuy = { onBuyPack("PREMIUM", 1500) },
                    onInfo = { selectedPackTypeForInfo = "PREMIUM" },
                    userCoins = coins
                )

                PackStoreItem(
                    name = "Pacote Lendas Atômicas",
                    description = "Contém 5 cards. Garante cartas Lendárias, Assinadas ou Animadas exclusivas de alto valor!",
                    cost = 3000,
                    tintColor = ColorLendaria,
                    onBuy = { onBuyPack("LENDARIO", 3000) },
                    onInfo = { selectedPackTypeForInfo = "LENDARIO" },
                    userCoins = coins
                )

                HorizontalDivider(color = Color.White.copy(alpha = 0.12f), thickness = 1.dp, modifier = Modifier.padding(vertical = 8.dp))

                // Real Money Coin Packs Store
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.Paid, contentDescription = null, tint = BrightGold, modifier = Modifier.size(24.dp))
                    Text(
                        text = "Loja de Moedas & Vantagens",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                
                Text(
                    text = "Adquira moedas com a simulação real de faturamento in-app (Google Play Billing), ganhe moedas grátis assistindo a anúncios premiados ou ative o Passe de Elite Temporada 1!",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )

                ElitePassStoreItem(
                    hasElitePass = hasElitePass,
                    onPurchase = onStartElitePassBilling
                )

                RewardedAdStoreItem(
                    isSimulating = isSimulatingAd,
                    adCountdown = adCountdown,
                    onPlay = onPlayAd
                )

                availableCoinPacks.forEach { pack ->
                    CoinPackStoreItem(
                        pack = pack,
                        onPurchase = { onStartBilling(pack) }
                    )
                }

                Spacer(modifier = Modifier.height(48.dp))
            }
        } else {
            // --- PACK UNBOXING ANIMATIONS ACTIVE STAGE ---
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.95f)),
                contentAlignment = Alignment.Center
            ) {
                if (openedCards == null) {
                    // Shaking animated box sequence
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(20.dp),
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Card(
                            modifier = Modifier
                                .size(180.dp)
                                .rotate(shakeRotation)
                                .border(2.dp, NeonEmerald, shape = RoundedCornerShape(16.dp)),
                            colors = CardDefaults.cardColors(containerColor = StadiumGlow),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(imageVector = Icons.Default.ShoppingBag, contentDescription = null, tint = BrightGold, modifier = Modifier.size(54.dp))
                                    Text("FUT CARDS", color = Color.White, fontWeight = FontWeight.Black, fontSize = 16.sp)
                                    Text("ABRINDO...", color = NeonCyan, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                }
                            }
                        }

                        Text(
                            text = "Aguarde... Rasgando o lacre do pacote!",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    // Reveal Card step-by-step unboxing screen
                    val activeCard = openedCards.getOrNull(currentRevealIndex)
                    if (activeCard != null) {
                        Column(
                            modifier = Modifier.fillMaxSize().padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "VOCÊ TIROU! 💥",
                                color = NeonEmerald,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 2.sp
                            )
                            Text(
                                text = "Carta ${currentRevealIndex + 1} de ${openedCards.size}",
                                color = Color.White.copy(alpha = 0.5f),
                                fontSize = 11.sp,
                                modifier = Modifier.padding(top = 2.dp)
                            )

                            Spacer(modifier = Modifier.height(30.dp))

                            // Highlighted glowing wrapper
                            Box(
                                modifier = Modifier
                                    .padding(8.dp)
                                    .border(
                                        width = 1.dp,
                                        brush = Brush.radialGradient(
                                            colors = listOf(Color.White.copy(alpha = 0.4f), Color.Transparent)
                                        ),
                                        shape = FutCardShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                FutCardView(card = activeCard)
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            Text(
                                text = activeCard.name,
                                color = Color.White,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.ExtraBold,
                                textAlign = TextAlign.Center
                            )

                            Text(
                                text = "${activeCard.rarity.name} | OVER: ${activeCard.overall}",
                                color = NeonCyan,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 2.dp)
                            )

                            Spacer(modifier = Modifier.height(40.dp))

                            if (currentRevealIndex < openedCards.size - 1) {
                                Button(
                                    colors = ButtonDefaults.buttonColors(containerColor = NeonEmerald),
                                    onClick = { currentRevealIndex++ },
                                    modifier = Modifier.fillMaxWidth(0.7f).height(48.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text("REVELAR PRÓXIMA", color = Color.Black, fontWeight = FontWeight.Bold)
                                        Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = Color.Black)
                                    }
                                }
                            } else {
                                Button(
                                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                                    onClick = { onDismissPack() },
                                    modifier = Modifier.fillMaxWidth(0.7f).height(48.dp).testTag("save_collection_btn")
                                ) {
                                    Text("GUARDAR NA COLEÇÃO", color = Color.Black, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Odds Dialogue explanation
    selectedPackTypeForInfo?.let { pack ->
        val oddsText = when(pack) {
            "BRONZE" -> "100% Bronze ou superior comum (Sem raridades elite garantidos)."
            "PRATA" -> "Garantia: 1 Card Prata. Taxas: 70% Prata, 20% Bronze, 10% Ouro."
            "OURO" -> "Garantia: 1 Card Ouro. Taxas: 60% Ouro, 30% Prata, 8% Especial, 2% Lendária."
            "PREMIUM" -> "Garantia: 1 Especial/Ouro. Taxas: 50% Especial, 35% Ouro, 10% Prata, 5% Lendária/Assinada."
            "LENDARIO" -> "Apenas Cards de Alto Nível: 100% de probabilidade em Lendária, Assinada, Animada ou Especial."
            else -> ""
        }
        AlertDialog(
            onDismissRequest = { selectedPackTypeForInfo = null },
            containerColor = StadiumConcrete,
            icon = { Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = NeonCyan) },
            title = { Text("Probabilidades Transparentes", color = Color.White) },
            text = { Text(oddsText, color = Color.White.copy(alpha = 0.82f)) },
            confirmButton = {
                TextButton(onClick = { selectedPackTypeForInfo = null }) {
                    Text("Entendido", color = NeonEmerald)
                }
            }
        )
    }

    // --- SIMULATED GOOGLE PLAY BILLING OVERLAY DIALOGS ---
    billingState?.let { state ->
        AlertDialog(
            onDismissRequest = { if (state !is BillingSimulationState.Processing) onCancelBilling() },
            containerColor = StadiumConcrete,
            modifier = Modifier.border(1.dp, NeonCyan.copy(alpha = 0.3f), shape = RoundedCornerShape(16.dp)),
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.Wallet, contentDescription = null, tint = BrightGold)
                    Text(
                        text = "Google Play Billing",
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    when (state) {
                        is BillingSimulationState.ChoosePaymentMethod -> {
                            val pack = state.pack
                            Text(
                                text = "Você está comprando: ${pack.name}",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "Valor: ${pack.priceBrl}",
                                color = BrightGold,
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp,
                                textAlign = TextAlign.Center
                            )
                            HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                            Text(
                                text = "Selecione o método de pagamento simulado:",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center
                            )
                            
                            val methods = listOf(
                                "Pix (Chave Copa)" to Icons.Default.QrCode,
                                "Cartão de Crédito (Visa *8829)" to Icons.Default.CreditCard,
                                "Saldo Google Play" to Icons.Default.Paid,
                                "Mercado Pago" to Icons.Default.AccountBalance
                            )
                            
                            methods.forEach { (name, icon) ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(StadiumObsidian, shape = RoundedCornerShape(8.dp))
                                        .border(1.dp, StadiumBorder, shape = RoundedCornerShape(8.dp))
                                        .clickable { onSelectPaymentAndProcess(pack, name) }
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(imageVector = icon, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(18.dp))
                                    Text(text = name, color = Color.White, fontWeight = FontWeight.Medium, fontSize = 13.sp)
                                }
                            }
                        }
                        
                        is BillingSimulationState.BuyingElitePass -> {
                            Text(
                                text = "Você está ativando o Passe de Elite Temporada 1!",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "Valor único: ${state.priceBrl}",
                                color = ColorEspecial,
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp,
                                textAlign = TextAlign.Center
                            )
                            HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                            
                            val methods = listOf(
                                "Pix (Chave Copa)" to Icons.Default.QrCode,
                                "Cartão de Crédito (Visa *8829)" to Icons.Default.CreditCard,
                                "Saldo Google Play" to Icons.Default.Paid,
                                "Mercado Pago" to Icons.Default.AccountBalance
                            )
                            
                            methods.forEach { (name, icon) ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(StadiumObsidian, shape = RoundedCornerShape(8.dp))
                                        .border(1.dp, StadiumBorder, shape = RoundedCornerShape(8.dp))
                                        .clickable { onProcessElitePassPurchase(name) }
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(imageVector = icon, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(18.dp))
                                    Text(text = name, color = Color.White, fontWeight = FontWeight.Medium, fontSize = 13.sp)
                                }
                            }
                        }
                        
                        is BillingSimulationState.Processing -> {
                            CircularProgressIndicator(color = NeonCyan, modifier = Modifier.size(48.dp))
                            Text(
                                text = "Processando transação com a carteira ${state.paymentMethod}...",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                        
                        is BillingSimulationState.Success -> {
                            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = NeonEmerald, modifier = Modifier.size(54.dp))
                            Text(
                                text = "Compra Efetuada com Sucesso!",
                                color = NeonEmerald,
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp
                            )
                            Text(
                                text = "Creditado: +${state.coinsGranted} Moedas",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Google Play confirmou o consumo do produto consumível com êxito.",
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 11.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                        
                        is BillingSimulationState.ElitePassSuccess -> {
                            Icon(imageVector = Icons.Default.Star, contentDescription = null, tint = ColorEspecial, modifier = Modifier.size(54.dp))
                            Text(
                                text = "Passe de Elite Ativado!",
                                color = ColorEspecial,
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp
                            )
                            Text(
                                text = "Bônus Creditado: +${state.bonusCoins} Moedas",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Agora você possui o dobro de XP nas partidas e o selo ativo!",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 11.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            },
            confirmButton = {
                if (state is BillingSimulationState.Success || state is BillingSimulationState.ElitePassSuccess) {
                    TextButton(onClick = onCancelBilling) {
                        Text("Fechar", color = NeonCyan)
                    }
                } else if (state !is BillingSimulationState.Processing) {
                    TextButton(onClick = onCancelBilling) {
                        Text("Cancelar", color = Color.White.copy(alpha = 0.6f))
                    }
                }
            }
        )
    }

    // --- REWARDED ADS SIMULATED DIALOG ---
    if (isSimulatingAd) {
        AlertDialog(
            onDismissRequest = {}, // Cannot dismiss during short ad simulation
            containerColor = Color.Black.copy(alpha = 0.95f),
            modifier = Modifier.fillMaxWidth().border(1.dp, NeonEmerald, shape = RoundedCornerShape(16.dp)),
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, tint = NeonEmerald)
                    Text("Anúncio Premiado em Exibição", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .background(StadiumConcrete, shape = RoundedCornerShape(12.dp))
                            .border(1.dp, Color.White.copy(alpha = 0.1f), shape = RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            CircularProgressIndicator(color = NeonEmerald, modifier = Modifier.size(36.dp))
                            Text(
                                text = "${adCountdown}s restantes",
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 20.sp
                            )
                        }
                    }
                    Text(
                        text = "Assista até o fim para receber as moedas gratuitas. Simulação patrocinada pela Google AdMob.",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center
                    )
                }
            },
            confirmButton = {} // No dismiss button for realism
        )
    }
}

@Composable
fun PackStoreItem(
    name: String,
    description: String,
    cost: Int,
    tintColor: Color,
    onBuy: () -> Unit,
    onInfo: () -> Unit,
    userCoins: Int
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, tintColor.copy(alpha = 0.3f), shape = RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = StadiumConcrete)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(modifier = Modifier.size(12.dp).background(tintColor, shape = RoundedCornerShape(2.dp)))
                    Text(text = name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }

                IconButton(onClick = onInfo) {
                    Icon(imageVector = Icons.Default.Info, contentDescription = "Mostrar Probabilidades", tint = NeonCyan.copy(alpha = 0.8f))
                }
            }

            Text(text = description, color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp, lineHeight = 15.sp)

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Cost Tag
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(imageVector = Icons.Default.Paid, contentDescription = null, tint = BrightGold, modifier = Modifier.size(16.dp))
                    Text(text = "$cost m.", color = BrightGold, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }

                // Buy BTN
                val isEnough = userCoins >= cost
                Button(
                    onClick = onBuy,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isEnough) NeonEmerald else Color.Gray.copy(alpha = 0.2f),
                        disabledContainerColor = Color.Gray.copy(alpha = 0.2f)
                    ),
                    enabled = isEnough,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("buy_pack_${name.replace(" ", "_").lowercase()}")
                ) {
                    Text(
                        text = if (isEnough) "Comprar Pacote" else "Dinheiro Insuficiente",
                        color = if (isEnough) Color.Black else Color.White.copy(alpha = 0.5f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun CoinPackStoreItem(
    pack: CoinPack,
    onPurchase: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, NeonCyan.copy(alpha = 0.25f), shape = RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = StadiumConcrete)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(imageVector = Icons.Default.Paid, contentDescription = null, tint = BrightGold, modifier = Modifier.size(20.dp))
                    Text(
                        text = pack.name,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    pack.bonusLabel?.let { label ->
                        Box(
                            modifier = Modifier
                                .background(NeonEmerald, shape = RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = label,
                                color = Color.Black,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
                Text(
                    text = "Garante +${pack.coinsAmount} moedas instantaneamente",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 11.sp
                )
            }
            
            Button(
                onClick = onPurchase,
                colors = ButtonDefaults.buttonColors(containerColor = BrightGold),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = pack.priceBrl,
                    color = Color.Black,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}

@Composable
fun ElitePassStoreItem(
    hasElitePass: Boolean,
    onPurchase: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                border = BorderStroke(
                    1.dp,
                    if (hasElitePass) NeonCyan else ColorEspecial
                ),
                shape = RoundedCornerShape(12.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (hasElitePass) StadiumConcrete else StadiumGlow
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = if (hasElitePass) {
                            listOf(StadiumConcrete, StadiumConcrete)
                        } else {
                            listOf(ColorEspecial.copy(alpha = 0.15f), NeonEmerald.copy(alpha = 0.05f))
                        }
                    )
                )
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = if (hasElitePass) NeonCyan else ColorEspecial,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Passe de Elite Temporada 1",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp
                        )
                    }
                    Text(
                        text = if (hasElitePass) {
                            "Status: ATIVO! 👑 Ganhe o dobro de XP nas partidas e desfrute de privilégios premium."
                        } else {
                            "Desbloqueie: +5.000 moedas bônus imediato, o dobro de XP no Bafo e selo premium dourado!"
                        },
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                
                if (hasElitePass) {
                    Box(
                        modifier = Modifier
                            .background(NeonCyan.copy(alpha = 0.12f), shape = RoundedCornerShape(6.dp))
                            .border(1.dp, NeonCyan, shape = RoundedCornerShape(6.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "ATIVADO",
                            color = NeonCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                } else {
                    Button(
                        onClick = onPurchase,
                        colors = ButtonDefaults.buttonColors(containerColor = ColorEspecial),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "R$ 19,90",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RewardedAdStoreItem(
    isSimulating: Boolean,
    adCountdown: Int,
    onPlay: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = if (isSimulating) NeonEmerald.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.1f),
                shape = RoundedCornerShape(12.dp)
            ),
        colors = CardDefaults.cardColors(containerColor = StadiumConcrete)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, tint = NeonEmerald, modifier = Modifier.size(20.dp))
                    Text(
                        text = "Vídeo Premiado (Rewarded Ad)",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
                Text(
                    text = "Ganhe +150 moedas gratuitamente ao assistir a um anúncio de 5 segundos.",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 11.sp
                )
            }
            
            Button(
                onClick = onPlay,
                enabled = !isSimulating,
                colors = ButtonDefaults.buttonColors(
                    containerColor = NeonEmerald,
                    disabledContainerColor = Color.Gray.copy(alpha = 0.2f)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = if (isSimulating) "Assistindo (${adCountdown}s)" else "Assistir",
                    color = Color.Black,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
