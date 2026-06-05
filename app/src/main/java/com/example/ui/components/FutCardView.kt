package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.theme.*
import coil.compose.AsyncImage

// Shield-cut standard football card shape
val FutCardShape = GenericShape { size, _ ->
    val w = size.width
    val h = size.height
    moveTo(w * 0.06f, 0f)
    lineTo(w * 0.94f, 0f)
    lineTo(w, h * 0.04f)
    lineTo(w, h * 0.88f)
    lineTo(w * 0.5f, h)
    lineTo(0f, h * 0.88f)
    lineTo(0f, h * 0.04f)
    close()
}

@Composable
fun FutCardView(
    card: PlayerCard,
    modifier: Modifier = Modifier,
    quantity: Int? = null,
    inDeck: Boolean = false,
    upgradeLevel: Int = 0,
    stickerEmoji: String? = null,
    onClick: (() -> Unit)? = null
) {
    val bgPath = remember { Path() }
    val torsoPath = remember { Path() }
    val collarPath = remember { Path() }

    // Holographic & Animated colors cycle
    val infiniteTransition = rememberInfiniteTransition(label = "hologram")
    val animatedProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "angle"
    )

    val dotAlpha by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "liveDot"
    )

    // Base colors configuration depending on rarity
    val cardColors = when (card.rarity) {
        Rarity.BRONZE -> Pair(ColorBronze, Color(0xFF5E3A1A))
        Rarity.PRATA -> Pair(ColorSilver, Color(0xFF3A3E45))
        Rarity.OURO -> Pair(ColorGold, Color(0xFF5C3D03))
        Rarity.ESPECIAL -> Pair(ColorEspecial, Color(0xFF4A0E0E))
        Rarity.LENDARIA -> Pair(ColorLendaria, Color(0xFF423502))
        Rarity.ASSINADA -> Pair(ColorAssinada, Color(0xFF2C0F4A))
        Rarity.ANIMADA -> {
            // Animated shifts slowly through neon emerald and electric cyan
            val animatedColor1 = Color.hsl(animatedProgress, 0.8f, 0.5f)
            val animatedColor2 = Color.hsl((animatedProgress + 180) % 360f, 0.8f, 0.2f)
            Pair(animatedColor1, animatedColor2)
        }
    }

    val (primaryColor, darkAccent) = cardColors

    // Holographic shine brush helper for animations
    val holographicBrush = remember(animatedProgress, card.rarity) {
        if (card.rarity in listOf(Rarity.LENDARIA, Rarity.ASSINADA, Rarity.ANIMADA)) {
            Brush.linearGradient(
                colors = listOf(
                    primaryColor.copy(alpha = 0.8f),
                    Color.White.copy(alpha = 0.9f),
                    primaryColor.copy(alpha = 0.3f),
                    Color.White.copy(alpha = 0.8f),
                    darkAccent.copy(alpha = 0.8f)
                ),
                start = Offset(0f, 0f),
                end = Offset(400f * (animatedProgress / 360f), 400f * (animatedProgress / 360f))
            )
        } else {
            Brush.verticalGradient(listOf(primaryColor, darkAccent))
        }
    }

    val finalModifier = modifier
        .width(140.dp)
        .height(220.dp)
        .shadow(12.dp, shape = FutCardShape)
        .clip(FutCardShape)
        .background(darkAccent)
        .border(
            width = if (card.rarity >= Rarity.ESPECIAL) 2.5.dp else 1.8.dp,
            brush = if (card.rarity >= Rarity.ASSINADA) holographicBrush else SolidColor(primaryColor),
            shape = FutCardShape
        )
        .clickable(enabled = onClick != null) { onClick?.invoke() }
        .semantics { contentDescription = "Card de futebol de ${card.name} raridade ${card.rarity.name}" }

    Box(modifier = finalModifier) {
        // Futuristic background lines drawn onto card
        Canvas(modifier = Modifier.fillMaxSize()) {
            bgPath.reset()
            bgPath.moveTo(size.width * 0.06f, 0f)
            bgPath.lineTo(size.width * 0.94f, 0f)
            bgPath.lineTo(size.width, size.height * 0.04f)
            bgPath.lineTo(size.width, size.height * 0.88f)
            bgPath.lineTo(size.width * 0.5f, size.height)
            bgPath.lineTo(0f, size.height * 0.88f)
            bgPath.lineTo(0f, size.height * 0.04f)
            bgPath.close()

            clipPath(bgPath) {
                // Background radial glow based on the specific rarity theme
                val bgGlowColor = when (card.rarity) {
                    Rarity.BRONZE -> Color(0xFF422E1F)
                    Rarity.PRATA -> Color(0xFF2E3541)
                    Rarity.OURO -> Color(0xFF5E451A)
                    Rarity.ESPECIAL -> Color(0xFF1E351E)
                    Rarity.LENDARIA -> Color(0xFF321F54)
                    Rarity.ASSINADA -> Color(0xFF282F24)
                    Rarity.ANIMADA -> Color(0xFF1A331E)
                }

                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(bgGlowColor, Color(0xFF0C111C)),
                        center = Offset(size.width * 0.5f, 0f),
                        radius = size.width * 1.5f
                    )
                )

                // Diagonal decorative gridlines
                for (i in 0..6) {
                    val y = size.height * (i * 0.15f)
                    drawLine(
                        color = primaryColor.copy(alpha = 0.12f),
                        start = Offset(0f, y),
                        end = Offset(size.width, y - size.height * 0.1f),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                // Geometric background pattern in center
                drawCircle(
                    color = primaryColor.copy(alpha = 0.05f),
                    center = Offset(size.width * 0.5f, size.height * 0.45f),
                    radius = size.width * 0.25f
                )

                // Reflected diagonal sheen sweep animation
                val sheenProgress = (animatedProgress / 360f) * 2.5f - 1.2f
                drawRect(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.White.copy(alpha = 0.04f),
                            Color.White.copy(alpha = 0.15f),
                            Color.White.copy(alpha = 0.04f),
                            Color.Transparent
                        ),
                        start = Offset(size.width * sheenProgress, 0f),
                        end = Offset(size.width * (sheenProgress + 0.4f), size.height)
                    )
                )
            }
        }

        // --- CARD CONTENT LAYOUT ---
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(7.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Bar: Rating, Position, Rarity Pill & Brazilian Flag
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 2.dp)
                    .padding(top = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // LeftStack: Overall Rating & Position
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = card.overall.toString(),
                        color = primaryColor,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.offset(y = (-1).dp)
                    )
                    Text(
                        text = card.position.name,
                        color = Color(0xFFCFD8E6),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.offset(y = (-4).dp)
                    )
                }

                // RightStack: Rarity Label & National Flag
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Rarity Label
                    Box(
                        modifier = Modifier
                            .background(
                                brush = Brush.verticalGradient(listOf(primaryColor, darkAccent)),
                                shape = RoundedCornerShape(999.dp)
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = card.rarity.name,
                            color = Color(0xFF0A0E14),
                            fontSize = 7.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.5.sp
                        )
                    }

                    // Green-Yellow-Blue vertical segmented country flag
                    Row(
                        modifier = Modifier
                            .width(22.dp)
                            .height(14.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .border(0.5.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(2.dp))
                    ) {
                        Box(modifier = Modifier.weight(1f).fillMaxHeight().background(Color(0xFF009B3A)))
                        Box(modifier = Modifier.weight(1f).fillMaxHeight().background(Color(0xFFFFDF00)))
                        Box(modifier = Modifier.weight(1f).fillMaxHeight().background(Color(0xFF002776)))
                    }
                }
            }

            // Photo/Silhouette Container (height = 70dp)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(70.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                // Custom Player Silhouette Canvas
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height

                    // Head: circle at cx = w * 0.5, cy = h * 0.25, r = h * 0.22
                    val headCenter = Offset(w * 0.5f, h * 0.25f)
                    val headRadius = h * 0.22f

                    val silhouetteBrush = Brush.verticalGradient(
                        colors = listOf(primaryColor.copy(alpha = 0.9f), primaryColor.copy(alpha = 0.12f)),
                        startY = h * 0.10f,
                        endY = h * 0.95f
                    )

                    drawCircle(
                        brush = silhouetteBrush,
                        radius = headRadius,
                        center = headCenter
                    )

                    // Torso path (scaled to canvas size)
                    torsoPath.reset()
                    torsoPath.moveTo(w * 0.25f, h)
                    torsoPath.cubicTo(
                        w * 0.27f, h * 0.55f,
                        w * 0.40f, h * 0.42f,
                        w * 0.50f, h * 0.42f
                    )
                    torsoPath.cubicTo(
                        w * 0.60f, h * 0.42f,
                        w * 0.73f, h * 0.55f,
                        w * 0.75f, h
                    )
                    torsoPath.close()

                    drawPath(
                        path = torsoPath,
                        brush = silhouetteBrush
                    )

                    // Collar detail
                    collarPath.reset()
                    collarPath.moveTo(w * 0.42f, h * 0.48f)
                    collarPath.lineTo(w * 0.50f, h * 0.68f)
                    collarPath.lineTo(w * 0.58f, h * 0.48f)
                    collarPath.lineTo(w * 0.55f, h * 0.44f)
                    collarPath.lineTo(w * 0.50f, h * 0.50f)
                    collarPath.lineTo(w * 0.45f, h * 0.44f)
                    collarPath.close()

                    drawPath(
                        path = collarPath,
                        color = Color(0xFF0C111C).copy(alpha = 0.55f)
                    )
                }

                // If photo URL exists, overlay the real photo in a beautifully styled circular frame
                if (!card.photoUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = card.photoUrl,
                        contentDescription = "Foto de ${card.name}",
                        modifier = Modifier
                            .size(54.dp)
                            .offset(y = (-2).dp)
                            .clip(CircleShape)
                            .border(1.2.dp, primaryColor.copy(alpha = 0.85f), CircleShape)
                            .background(Color.Transparent)
                    )
                }

                // Crest (Club badge with first letter of club name, at bottom-left)
                val firstLetter = if (card.clubAndCountry.isNotEmpty()) card.clubAndCountry.take(1).uppercase() else "F"
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .offset(x = 8.dp, y = 3.dp)
                        .size(20.dp)
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(Color(0xFF26324D), Color(0xFF141A28))
                            ),
                            shape = RoundedCornerShape(5.dp)
                        )
                        .border(0.5.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(5.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = firstLetter,
                        color = primaryColor,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                // Live indicator ("AO VIVO" with blinking dot at top right of the photo)
                val isCardLive = remember(card.id) { card.id % 3 == 0 || card.overall >= 88 }
                if (isCardLive) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = (-4).dp, y = 0.dp)
                            .background(Color(0xFF080C14).copy(alpha = 0.75f), shape = RoundedCornerShape(999.dp))
                            .border(0.5.dp, Color(0xFFFF5A5A).copy(alpha = 0.6f), shape = RoundedCornerShape(999.dp))
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(4.dp)
                                    .background(Color(0xFFFF4D4D).copy(alpha = dotAlpha), shape = CircleShape)
                            )
                            Text(
                                text = "LIVE",
                                color = Color(0xFFFF8C8C),
                                fontSize = 6.5.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(1.dp))

            // Player Identity
            Text(
                text = card.name.uppercase(),
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 3.dp)
            )

            Text(
                text = card.clubAndCountry.uppercase(),
                color = Color(0xFF8B95A7),
                fontSize = 7.5.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                letterSpacing = 0.8.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 3.dp)
            )

            // Fading Divider Line
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .padding(horizontal = 8.dp, vertical = 2.dp)
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(Color.Transparent, primaryColor.copy(alpha = 0.6f), Color.Transparent)
                        )
                    )
            )

            // Player attributes statistics block (2 rows x 3 cols)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItemRow(value = card.stats.pac, label = "RIT")
                StatItemRow(value = card.stats.sho, label = "FIN")
                StatItemRow(value = card.stats.pas, label = "PAS")
            }
            Spacer(modifier = Modifier.height(1.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItemRow(value = card.stats.dri, label = "DRI")
                StatItemRow(value = card.stats.def, label = "DEF")
                StatItemRow(value = card.stats.phy, label = "FÍS")
            }

            // Outcome History block (FORMA)
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "FORMA",
                    color = Color(0xFF8B95A7),
                    fontSize = 7.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp,
                    modifier = Modifier.padding(end = 4.dp)
                )

                val formPattern = remember(card.id, card.overall) {
                    val list = mutableListOf<String>()
                    val winsCount = when {
                        card.overall >= 90 -> 4
                        card.overall >= 80 -> 3
                        card.overall >= 70 -> 2
                        else -> 1
                    }
                    for (i in 0 until 5) {
                        val h = (card.id + i) % 5
                        if (h < winsCount) {
                            list.add("W")
                        } else if (h == winsCount) {
                            list.add("D")
                        } else {
                            list.add("L")
                        }
                    }
                    list
                }

                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    formPattern.forEach { outcome ->
                        val color = when (outcome) {
                            "W" -> Color(0xFF39D98A)
                            "D" -> Color(0xFFFFD166)
                            else -> Color(0xFFFF6B6B)
                        }
                        Box(
                            modifier = Modifier
                                .size(width = 11.dp, height = 11.dp)
                                .background(color, shape = RoundedCornerShape(2.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = outcome,
                                color = Color(0xFF0A0E14),
                                fontSize = 7.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Card footer details: Season & unique number
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "TEMP 25/26",
                    color = primaryColor,
                    fontSize = 7.5.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = "#${(1000 + card.id).toString()}",
                    color = Color(0xFF8B95A7),
                    fontSize = 7.5.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Float Indicators: quantity modifier badge
        if (quantity != null && quantity > 0) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 6.dp, y = 6.dp)
                    .background(Color.Black.copy(alpha = 0.8f), shape = RoundedCornerShape(4.dp))
                    .border(0.5.dp, primaryColor, shape = RoundedCornerShape(4.dp))
                    .padding(horizontal = 5.dp, vertical = 1.dp)
            ) {
                Text(
                    text = "x$quantity",
                    color = primaryColor,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Wager/Battle deck active badge overlay
        if (inDeck) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = (-4).dp, y = (-4).dp)
                    .background(NeonCyan, shape = RoundedCornerShape(4.dp))
                    .padding(horizontal = 4.dp, vertical = 1.dp)
            ) {
                Text(
                    text = "BATALHA",
                    color = Color.Black,
                    fontSize = 7.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }

        // Option 2: Unique Level Badge
        if (upgradeLevel > 0) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = 4.dp)
                    .background(Brush.horizontalGradient(colors = listOf(Color(0xFFFDB931), Color(0xFF917405))), shape = RoundedCornerShape(4.dp))
                    .border(0.5.dp, Color.White.copy(alpha = 0.5f), shape = RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 1.5.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(8.dp)
                    )
                    Text(
                        text = "LV $upgradeLevel",
                        color = Color.White,
                        fontSize = 7.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }

        // Option 2: Digital Sticker Patch Overlays
        if (!stickerEmoji.isNullOrBlank()) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .offset(x = 2.dp, y = (-20).dp)
                    .size(32.dp)
                    .background(StadiumConcrete.copy(alpha = 0.95f), shape = CircleShape)
                    .border(1.2.dp, NeonEmerald, shape = CircleShape)
                    .padding(4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(text = stickerEmoji, fontSize = 16.sp)
            }
        }
    }
}

@Composable
fun StatItemRow(value: Int, label: String) {
    Row(
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(1.dp),
        modifier = Modifier.width(32.dp)
    ) {
        Text(
            text = value.toString(),
            color = Color.White,
            fontSize = 10.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.widthIn(min = 12.dp),
            textAlign = TextAlign.End
        )
        Text(
            text = label,
            color = Color(0xFF8B95A7),
            fontSize = 6.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun FutCardBack(
    modifier: Modifier = Modifier
) {
    val bgPath = remember { Path() }
    val infiniteTransition = rememberInfiniteTransition(label = "back_pulse")
    val borderGlowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    val finalModifier = modifier
        .width(140.dp)
        .height(220.dp)
        .shadow(12.dp, shape = FutCardShape)
        .clip(FutCardShape)
        .background(StadiumConcrete)
        .border(
            width = 2.dp,
            brush = Brush.verticalGradient(
                colors = listOf(
                    NeonEmerald.copy(alpha = borderGlowAlpha),
                    NeonCyan.copy(alpha = borderGlowAlpha * 0.7f),
                    StadiumBorder
                )
            ),
            shape = FutCardShape
        )

    Box(
        modifier = finalModifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            bgPath.reset()
            bgPath.moveTo(size.width * 0.06f, 0f)
            bgPath.lineTo(size.width * 0.94f, 0f)
            bgPath.lineTo(size.width, size.height * 0.04f)
            bgPath.lineTo(size.width, size.height * 0.88f)
            bgPath.lineTo(size.width * 0.5f, size.height)
            bgPath.lineTo(0f, size.height * 0.88f)
            bgPath.lineTo(0f, size.height * 0.04f)
            bgPath.close()

            clipPath(bgPath) {
                // Background dark radial stadium gradient
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(StadiumGlow, StadiumObsidian),
                        center = Offset(size.width * 0.5f, size.height * 0.5f),
                        radius = size.height * 0.7f
                    )
                )

                // Diagonal arena layout lines
                for (i in -3..10) {
                    val offset = i * 25.dp.toPx()
                    drawLine(
                        color = NeonEmerald.copy(alpha = 0.06f),
                        start = Offset(0f, offset),
                        end = Offset(size.width, offset + size.width),
                        strokeWidth = 1.5.dp.toPx()
                    )
                    drawLine(
                        color = NeonEmerald.copy(alpha = 0.06f),
                        start = Offset(size.width, offset),
                        end = Offset(0f, offset + size.width),
                        strokeWidth = 1.5.dp.toPx()
                    )
                }

                // Inner frame design
                drawPath(
                    path = Path().apply {
                        val insetW = size.width * 0.08f
                        val insetH = size.height * 0.08f
                        moveTo(insetW, insetH)
                        lineTo(size.width - insetW, insetH)
                        lineTo(size.width - insetW, size.height - insetH * 1.5f)
                        lineTo(size.width * 0.5f, size.height - insetH * 0.5f)
                        lineTo(insetW, size.height - insetH * 1.5f)
                        close()
                    },
                    color = NeonCyan.copy(alpha = 0.15f),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.dp.toPx())
                )

                // Center glowing sphere or soccer ball pattern
                val cx = size.width * 0.5f
                val cy = size.height * 0.45f
                val r = size.width * 0.22f
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(NeonEmerald.copy(alpha = 0.25f), Color.Transparent),
                        center = Offset(cx, cy),
                        radius = r * 1.5f
                    ),
                    radius = r * 1.5f,
                    center = Offset(cx, cy)
                )

                drawCircle(
                    color = NeonEmerald.copy(alpha = 0.3f),
                    radius = r,
                    center = Offset(cx, cy),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5.dp.toPx())
                )
            }
        }

        // Card logo in center
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Verified,
                contentDescription = null,
                tint = NeonEmerald,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "CARDCLASH",
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp,
                textAlign = TextAlign.Center
            )
            Text(
                text = "COPA '26",
                color = BrightGold,
                fontSize = 8.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 1.dp)
            )
        }
    }
}
