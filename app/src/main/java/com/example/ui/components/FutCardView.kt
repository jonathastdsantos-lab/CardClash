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
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
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
    isCopaEdition: Boolean = false,
    allowFlipOnClick: Boolean = true,
    forceFlipped: Boolean? = null,
    onClick: (() -> Unit)? = null
) {
    // Check if the card league is "Copa do Mundo" and treat it as a Copa edition automatically if true or if isCopaEdition parameter is provided
    val activeCopaEdition = isCopaEdition || card.league.contains("Copa", ignoreCase = true)

    // Pointer hover tracking to enhance visual 3D styling
    var isHovered by remember { mutableStateOf(false) }

    // Handle interactive flip state - click/force flips, while hover adds a subtle tactile tilt scale effect (CSS style transition)
    var isInternalFlipped by remember { mutableStateOf(false) }
    val isFlipped = forceFlipped ?: isInternalFlipped

    // Smooth fluid scale transition on hover
    val animatedScale by animateFloatAsState(
        targetValue = if (isHovered) 1.06f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "ScaleOnHover"
    )

    // Elevate shadow when hovered to produce floating 3D parallax layers
    val animatedShadowElevation by animateDpAsState(
        targetValue = if (isHovered) 22.dp else 12.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "ElevationOnHover"
    )

    // Subtle 3D tilt/rotation angles on hover for a premium CSS-like interaction
    val hoverTiltYTarget = if (isHovered) -7f else 0f
    val hoverTiltXTarget = if (isHovered) 4f else 0f
    val hoverTiltZTarget = if (isHovered) -1.5f else 0f

    val hoverTiltY by animateFloatAsState(
        targetValue = hoverTiltYTarget,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "HoverTiltY"
    )
    val hoverTiltX by animateFloatAsState(
        targetValue = hoverTiltXTarget,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "HoverTiltX"
    )
    val hoverTiltZ by animateFloatAsState(
        targetValue = hoverTiltZTarget,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "HoverTiltZ"
    )

    val rotationY by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "YRotation"
    )

    // General animation values shared
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

    // Base colors configuration depending on rarity and Copa Edition override
    val cardColors = remember(card.rarity, activeCopaEdition, animatedProgress) {
        if (activeCopaEdition) {
            // Gold Copa Theme (from HTML template specs)
            Pair(Color(0xFFFFD86B), Color(0xFFB8821F))
        } else {
            when (card.rarity) {
                Rarity.BRONZE -> Pair(ColorBronze, Color(0xFF5E3A1A))
                Rarity.PRATA -> Pair(ColorSilver, Color(0xFF3A3E45))
                Rarity.OURO -> Pair(ColorGold, Color(0xFF5C3D03))
                Rarity.ESPECIAL -> Pair(ColorEspecial, Color(0xFF4A0E0E))
                Rarity.LENDARIA -> Pair(ColorLendaria, Color(0xFF423502))
                Rarity.ASSINADA -> Pair(ColorAssinada, Color(0xFF2C0F4A))
                Rarity.ANIMADA -> {
                    val animatedColor1 = Color.hsl(animatedProgress, 0.8f, 0.5f)
                    val animatedColor2 = Color.hsl((animatedProgress + 180) % 360f, 0.8f, 0.2f)
                    Pair(animatedColor1, animatedColor2)
                }
            }
        }
    }

    val (primaryColor, darkAccent) = cardColors

    // Holographic shine brush helper for animations
    val holographicBrush = remember(animatedProgress, card.rarity, activeCopaEdition) {
        if (activeCopaEdition) {
            Brush.linearGradient(
                colors = listOf(
                    Color(0xFFFFF2C0),
                    Color(0xFFC79A2A),
                    Color(0xFFFFD25A),
                    Color(0xFFC79A2A)
                ),
                start = Offset(0f, 0f),
                end = Offset(400f * (animatedProgress / 360f), 400f * (animatedProgress / 360f))
            )
        } else if (card.rarity in listOf(Rarity.LENDARIA, Rarity.ASSINADA, Rarity.ANIMADA)) {
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

    // Border configuration
    val borderWidth = if (activeCopaEdition) 3.dp else if (card.rarity >= Rarity.ESPECIAL) 2.5.dp else 1.8.dp
    val borderBrush = if (activeCopaEdition || card.rarity >= Rarity.ASSINADA) holographicBrush else SolidColor(primaryColor)

    // Apply interactive 3D rotation graphicsLayer modifier
    val finalModifier = modifier
        .width(140.dp)
        .height(220.dp)
        .pointerInput(Unit) {
            awaitPointerEventScope {
                while (true) {
                    val event = awaitPointerEvent()
                    when (event.type) {
                        PointerEventType.Enter -> isHovered = true
                        PointerEventType.Exit -> isHovered = false
                    }
                }
            }
        }
        .shadow(animatedShadowElevation, shape = FutCardShape)
        .graphicsLayer {
            this.rotationY = rotationY + (if (isFlipped) -hoverTiltY else hoverTiltY)
            this.rotationX = hoverTiltX
            this.rotationZ = hoverTiltZ
            this.cameraDistance = 14f * density
            this.scaleX = animatedScale
            this.scaleY = animatedScale
        }
        .clip(FutCardShape)
        .background(darkAccent)
        .border(width = borderWidth, brush = borderBrush, shape = FutCardShape)
        .clickable {
            if (onClick != null) {
                onClick.invoke()
            } else if (allowFlipOnClick) {
                isInternalFlipped = !isInternalFlipped
            }
        }
        .semantics { contentDescription = "Card de futebol de ${card.name} raridade ${card.rarity.name} ${if(activeCopaEdition) "Edição Especial Copa" else ""}" }

    Box(modifier = finalModifier) {
        if (rotationY <= 90f) {
            // RENDERING THE FRONT PANEL
            CardFrontSide(
                card = card,
                primaryColor = primaryColor,
                darkAccent = darkAccent,
                activeCopaEdition = activeCopaEdition,
                quantity = quantity,
                inDeck = inDeck,
                upgradeLevel = upgradeLevel,
                stickerEmoji = stickerEmoji,
                dotAlpha = dotAlpha,
                animatedProgress = animatedProgress
            )
        } else {
            // RENDERING THE BACK PANEL
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        this.rotationY = 180f // Un-mirror contents
                    }
            ) {
                CardBackSide(
                    card = card,
                    primaryColor = primaryColor,
                    darkAccent = darkAccent,
                    activeCopaEdition = activeCopaEdition,
                    upgradeLevel = upgradeLevel
                )
            }
        }
    }
}

// ==========================================
// FRONT SIDE SUB-COMPONENT
// ==========================================
@Composable
fun CardFrontSide(
    card: PlayerCard,
    primaryColor: Color,
    darkAccent: Color,
    activeCopaEdition: Boolean,
    quantity: Int?,
    inDeck: Boolean,
    upgradeLevel: Int,
    stickerEmoji: String?,
    dotAlpha: Float,
    animatedProgress: Float
) {
    val bgPath = remember { Path() }
    val torsoPath = remember { Path() }
    val collarPath = remember { Path() }

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
            val bgGlowColor = if (activeCopaEdition) {
                Color(0xFF33270B)
            } else {
                when (card.rarity) {
                    Rarity.BRONZE -> Color(0xFF422E1F)
                    Rarity.PRATA -> Color(0xFF2E3541)
                    Rarity.OURO -> Color(0xFF5E451A)
                    Rarity.ESPECIAL -> Color(0xFF1E351E)
                    Rarity.LENDARIA -> Color(0xFF321F54)
                    Rarity.ASSINADA -> Color(0xFF282F24)
                    Rarity.ANIMADA -> Color(0xFF1A331E)
                }
            }

            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(bgGlowColor, Color(0xFF0C111C)),
                    center = Offset(size.width * 0.5f, 0f),
                    radius = size.width * 1.5f
                )
            )

            for (i in 0..7) {
                val y = size.height * (i * 0.14f)
                drawLine(
                    color = primaryColor.copy(alpha = 0.12f),
                    start = Offset(0f, y),
                    end = Offset(size.width, y - size.height * 0.08f),
                    strokeWidth = 1.dp.toPx()
                )
            }

            drawCircle(
                color = primaryColor.copy(alpha = 0.04f),
                center = Offset(size.width * 0.5f, size.height * 0.42f),
                radius = size.width * 0.28f
            )

            val sheenProgress = (animatedProgress / 360f) * 2.5f - 1.2f
            drawRect(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color.White.copy(alpha = 0.04f),
                        Color.White.copy(alpha = 0.16f),
                        Color.White.copy(alpha = 0.04f),
                        Color.Transparent
                    ),
                    start = Offset(size.width * sheenProgress, 0f),
                    end = Offset(size.width * (sheenProgress + 0.35f), size.height)
                )
            )
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (activeCopaEdition) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(20.dp)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF1A1300), Color(0xFF2C1E02))
                        )
                    )
                    .border(width = (0.5).dp, color = Color(0xFFFFD86B).copy(alpha = 0.4f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "★ MUNDIAL 2026 ★",
                    color = Color(0xFFFFD86B),
                    fontSize = 7.5.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.2.sp
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 7.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.offset(y = 1.dp)
            ) {
                Text(
                    text = card.overall.toString(),
                    color = primaryColor,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = card.position.name,
                    color = Color(0xFFCFD8E6),
                    fontSize = 8.5.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.offset(y = (-4).dp)
                )
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Box(
                    modifier = Modifier
                        .background(
                            brush = Brush.verticalGradient(listOf(primaryColor, darkAccent)),
                            shape = RoundedCornerShape(999.dp)
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (activeCopaEdition) "EDIÇÃO COPA" else card.rarity.name,
                        color = Color(0xFF0A0E14),
                        fontSize = 7.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp
                    )
                }

                Row(
                    modifier = Modifier
                        .width(22.dp)
                        .height(13.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .border(0.5.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(2.dp))
                ) {
                    Box(modifier = Modifier.weight(1f).fillMaxHeight().background(Color(0xFF009B3A)))
                    Box(modifier = Modifier.weight(1f).fillMaxHeight().background(Color(0xFFFFDF00)))
                    Box(modifier = Modifier.weight(1f).fillMaxHeight().background(Color(0xFF002776)))
                }

                if (activeCopaEdition) {
                    Box(
                        modifier = Modifier
                            .background(Color(0xFFFFD86B).copy(alpha = 0.15f), shape = RoundedCornerShape(3.dp))
                            .border(0.5.dp, Color(0xFFFFD86B).copy(alpha = 0.4f), shape = RoundedCornerShape(3.dp))
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = card.copaGroup ?: "GRUPO C",
                            color = Color(0xFFFFD86B),
                            fontSize = 6.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        val photoHeight = if (activeCopaEdition) 58.dp else 68.dp
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(photoHeight),
            contentAlignment = Alignment.BottomCenter
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                val headCenter = Offset(w * 0.5f, h * 0.23f)
                val headRadius = h * 0.21f
                val silhouetteBrush = Brush.verticalGradient(
                    colors = listOf(primaryColor.copy(alpha = 0.95f), primaryColor.copy(alpha = 0.12f)),
                    startY = h * 0.1f,
                    endY = h * 0.95f
                )

                drawCircle(
                    brush = silhouetteBrush,
                    radius = headRadius,
                    center = headCenter
                )

                torsoPath.reset()
                torsoPath.moveTo(w * 0.23f, h)
                torsoPath.cubicTo(
                    w * 0.25f, h * 0.53f,
                    w * 0.38f, h * 0.40f,
                    w * 0.50f, h * 0.40f
                )
                torsoPath.cubicTo(
                    w * 0.62f, h * 0.40f,
                    w * 0.75f, h * 0.53f,
                    w * 0.77f, h
                )
                torsoPath.close()

                drawPath(
                    path = torsoPath,
                    brush = silhouetteBrush
                )

                collarPath.reset()
                collarPath.moveTo(w * 0.42f, h * 0.46f)
                collarPath.lineTo(w * 0.50f, h * 0.65f)
                collarPath.lineTo(w * 0.58f, h * 0.46f)
                collarPath.close()

                drawPath(
                    path = collarPath,
                    color = Color(0xFF0C111C).copy(alpha = 0.5f)
                )
            }

            if (!card.photoUrl.isNullOrBlank()) {
                AsyncImage(
                    model = card.photoUrl,
                    contentDescription = "Foto de ${card.name}",
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .border(1.2.dp, primaryColor.copy(alpha = 0.85f), CircleShape)
                        .background(Color.Transparent)
                )
            }

            val clubMarker = if (card.clubAndCountry.isNotEmpty()) card.clubAndCountry.split("/").first().trim().take(2).uppercase() else "CE"
            val selMarker = if (card.clubAndCountry.contains("/")) card.clubAndCountry.split("/")[1].trim().take(2).uppercase() else "BR"

            if (activeCopaEdition) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp)
                        .offset(y = 1.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Box(
                        modifier = Modifier
                            .size(17.dp)
                            .background(
                                Brush.linearGradient(listOf(Color(0xFF26324D), Color(0xFF141A28))),
                                shape = RoundedCornerShape(4.dp)
                            )
                            .border(0.5.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                            .graphicsLayer { alpha = 0.5f },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = clubMarker, color = primaryColor, fontSize = 7.sp, fontWeight = FontWeight.Black)
                    }

                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(
                                Brush.linearGradient(
                                    listOf(Color(0xFFFFD86B), Color(0xFFB8821F))
                                ),
                                shape = RoundedCornerShape(6.dp)
                            )
                            .border(1.2.dp, Color(0xFFFFF2C0), RoundedCornerShape(6.dp))
                            .shadow(4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                            Text(text = selMarker, color = Color(0xFF0C111C), fontSize = 10.sp, fontWeight = FontWeight.Black)
                            Text(text = "SEL", color = Color(0xFF5C3D03), fontSize = 5.sp, fontWeight = FontWeight.Bold, modifier = Modifier.offset(y = (-2).dp))
                        }
                    }
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp)
                        .offset(y = 1.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Box(
                        modifier = Modifier
                            .size(19.dp)
                            .background(
                                Brush.linearGradient(listOf(Color(0xFF26324D), Color(0xFF141A28))),
                                shape = RoundedCornerShape(4.dp)
                            )
                            .border(0.5.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(4.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = clubMarker, color = primaryColor, fontSize = 7.sp, fontWeight = FontWeight.Black)
                            Text(text = "CLU", color = Color(0xFF8B95A7), fontSize = 4.sp, modifier = Modifier.offset(y = (-1).dp))
                        }
                    }

                    Box(
                        modifier = Modifier
                            .size(19.dp)
                            .background(
                                Brush.linearGradient(listOf(Color(0xFF141A28), Color(0xFF0F1420))),
                                shape = RoundedCornerShape(4.dp)
                            )
                            .border(0.5.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(4.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = selMarker, color = primaryColor, fontSize = 7.sp, fontWeight = FontWeight.Black)
                            Text(text = "SEL", color = Color(0xFF5C3D03), fontSize = 4.sp, modifier = Modifier.offset(y = (-1).dp))
                        }
                    }
                }
            }

            val isCardLive = remember(card.id) { card.id % 3 == 0 || card.overall >= 88 }
            if (isCardLive) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = (-4).dp, y = (-2).dp)
                        .background(Color(0xFF080C14).copy(alpha = 0.75f), shape = RoundedCornerShape(999.dp))
                        .border(0.5.dp, Color(0xFFFF5A5A).copy(alpha = 0.61f), shape = RoundedCornerShape(999.dp))
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

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = card.name.uppercase(),
            color = Color.White,
            fontSize = 12.5.sp,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 4.dp)
        )

        val parts = card.clubAndCountry.split("/")
        val clubName = parts.firstOrNull()?.trim() ?: "Clube"
        val countryName = parts.getOrNull(1)?.trim() ?: "Brasil"

        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 3.dp)
        ) {
            Text(
                text = if (activeCopaEdition) countryName.uppercase() else clubName,
                color = if (activeCopaEdition) primaryColor else Color(0xFFCFD8E6),
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = " · ",
                color = Color(0xFFcfd8e6).copy(alpha = 0.4f),
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = if (activeCopaEdition) "#${card.shirtNumberNational ?: "10"}" else countryName,
                color = primaryColor,
                fontSize = 8.sp,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        if (activeCopaEdition) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 2.dp)
                    .background(Color(0xFFFFD86B).copy(alpha = 0.08f), shape = RoundedCornerShape(4.dp))
                    .border(0.5.dp, Color(0xFFFFD86B).copy(alpha = 0.18f), shape = RoundedCornerShape(4.dp))
                    .padding(vertical = 2.dp, horizontal = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "PRÓX: " + (card.nextOpponent ?: "vs MARROCOS · 11/06"),
                    color = Color(0xFFFFD86B),
                    fontSize = 6.5.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .padding(horizontal = 10.dp, vertical = 1.dp)
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(Color.Transparent, primaryColor.copy(alpha = 0.6f), Color.Transparent)
                    )
                )
        )

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

        Spacer(modifier = Modifier.height(3.dp))
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
                fontWeight = FontWeight.Black,
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

            Row(horizontalArrangement = Arrangement.spacedBy(1.5.dp)) {
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

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp)
                .padding(bottom = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (activeCopaEdition) "MUNDIAL 2026" else "TEMP 25/26",
                color = primaryColor,
                fontSize = 7.5.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.5.sp
            )
            Text(
                text = if (activeCopaEdition) "#BR-${card.shirtNumberNational ?: "10"}" else "#${(1001 + card.id).toString()}",
                color = Color(0xFF8B95A7),
                fontSize = 7.5.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }

    if (quantity != null && quantity > 0) {
        Box(
            modifier = Modifier
                .padding(6.dp)
                .background(Color.Black.copy(alpha = 0.8f), shape = RoundedCornerShape(4.dp))
                .border(0.5.dp, primaryColor, shape = RoundedCornerShape(4.dp))
                .padding(horizontal = 5.dp, vertical = 2.dp)
        ) {
            Text(
                text = "x$quantity",
                color = primaryColor,
                fontSize = 8.5.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }

    if (inDeck) {
        Box(
            modifier = Modifier
                .padding(bottom = 6.dp, end = 6.dp)
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

    if (upgradeLevel > 0) {
        Box(
            modifier = Modifier
                .padding(top = 4.dp)
                .background(
                    Brush.horizontalGradient(colors = listOf(Color(0xFFFDB931), Color(0xFF917405))),
                    shape = RoundedCornerShape(4.dp)
                )
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
                    modifier = Modifier.size(7.dp)
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

    if (!stickerEmoji.isNullOrBlank()) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .background(StadiumConcrete.copy(alpha = 0.95f), shape = CircleShape)
                .border(1.dp, NeonEmerald, shape = CircleShape)
                .padding(2.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(text = stickerEmoji, fontSize = 12.sp)
        }
    }
}

// ==========================================
// BACK SIDE SUB-COMPONENT (CARD 2.0 EXCLUSIVITY)
// ==========================================
@Composable
fun CardBackSide(
    card: PlayerCard,
    primaryColor: Color,
    darkAccent: Color,
    activeCopaEdition: Boolean,
    upgradeLevel: Int
) {
    val resolvedFullName = remember(card.id, card.name, card.fullName) {
        card.fullName ?: when (card.name) {
            "Pelé" -> "EDSON ARANTES DO NASCIMENTO (PELÉ)"
            "Ronaldinho" -> "RONALDO DE ASSIS MOREIRA"
            "Vini Jr", "Vinicius Jr" -> "VINICIUS JOSÉ PAIXÃO DE OLIVEIRA JÚNIOR"
            "Neymar Jr" -> "NEYMAR DA SILVA SANTOS JÚNIOR"
            "Kaká" -> "RICARDO IZECSON DOS SANTOS LEITE"
            else -> "${card.name.uppercase()} DE OLIVEIRA SOUZA"
        }
    }

    val resolvedBirthDate = remember(card.id, card.birthDate) {
        card.birthDate ?: when (card.id) {
            1 -> "23/10/1940 (Falecido)"
            2 -> "21/03/1980 (46)"
            3 -> "12/07/2000 (25)"
            4 -> "05/02/1992 (34)"
            5 -> "22/04/1982 (44)"
            else -> "18/06/2001 (24)"
        }
    }

    val resolvedPlace = remember(card.id, card.birthPlace) {
        card.birthPlace ?: when (card.id) {
            1 -> "Três Corações, MG"
            2 -> "Porto Alegre, RS"
            3 -> "São Gonçalo, RJ"
            4 -> "Mogi das Cruzes, SP"
            5 -> "Gama, DF"
            else -> "São Paulo, SP"
        }
    }

    val resolvedShirtClub = card.shirtNumberClub ?: when(card.id) {
        1 -> "#10"
        2 -> "#10"
        3 -> "#7"
        4 -> "#10"
        5 -> "#22"
        else -> "#9"
    }

    val resolvedShirtNat = card.shirtNumberNational ?: "#10"

    // Safe split/parse of career years using string colon logic
    val resolvedCareer: List<Pair<String, String>> = remember(card.id, card.careerYears) {
        card.careerYears?.mapNotNull { item ->
            val idx = item.indexOf(':')
            if (idx != -1) {
                Pair(item.substring(0, idx).trim(), item.substring(idx + 1).trim())
            } else {
                Pair("", item)
            }
        } ?: when (card.id) {
            1 -> listOf("1956-74" to "Santos FC", "1975-77" to "NY Cosmos", "1957-71" to "Brasil")
            2 -> listOf("1998-01" to "Grêmio", "2003-08" to "Barcelona", "1999-13" to "Brasil")
            3, 33 -> listOf("2017-18" to "Flamengo", "2018-   " to "Real Madrid", "2019-   " to "Brasil")
            4 -> listOf("2009-13" to "Santos FC", "2013-17" to "FC Barcelona", "2010-   " to "Brasil")
            else -> listOf("2019-21" to "Base profissional", "2021-   " to "Time principal", "2024-   " to "Brasil")
        }
    }

    // Safe split/parse of titles list using string colon logic
    val resolvedTitles: List<Pair<String, String>> = remember(card.id, card.mainTitles) {
        card.mainTitles?.mapNotNull { item ->
            val idx = item.indexOf(':')
            if (idx != -1) {
                Pair(item.substring(0, idx).trim(), item.substring(idx + 1).trim())
            } else {
                Pair("★", item)
            }
        } ?: when (card.id) {
            1 -> listOf("★ x3" to "Copas do Mundo", "★ x2" to "Taça Libertadores", "★ x6" to "Série A Brasil")
            2 -> listOf("★ x1" to "Copa do Mundo", "★ x1" to "UCL Champions", "★ x2" to "La Liga Espanha")
            3, 33 -> listOf("★ x3" to "UCL Champions", "★ x3" to "La Liga Espanha", "★ x2" to "Mundial de Clubes")
            4 -> listOf("★ x1" to "UCL Champions", "★ x1" to "Libertadores", "★ x2" to "La Liga Espanha")
            else -> listOf("★ x1" to "Campeonato Estadual", "★ x1" to "Taça Nacional", "★ x1" to "Copa Regional")
        }
    }

    val resolvedGames = card.careerGames ?: (300 + (card.id * 18))
    val resolvedGoals = card.careerGoals ?: (75 + (card.id * 12))
    val resolvedAssists = card.careerAssists ?: (40 + (card.id * 8))

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = resolvedFullName,
            color = Color.White,
            fontSize = 7.5.sp,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        val upperRarity = if (activeCopaEdition) "EDIÇÃO COPA" else card.rarity.name
        Text(
            text = "★ $upperRarity · ${card.position.name} · BRASIL ★",
            color = primaryColor,
            fontSize = 6.5.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 0.5.sp,
            textAlign = TextAlign.Center
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(0.5.dp)
                .background(primaryColor.copy(alpha = 0.35f))
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(1.dp)
        ) {
            Text("PERFIL", color = primaryColor, fontSize = 6.sp, fontWeight = FontWeight.Black, letterSpacing = 0.8.sp)
            ProfileRow(label = "Nasc.", value = resolvedBirthDate)
            ProfileRow(label = "Naturalidade", value = resolvedPlace)
            ProfileRow(label = "Altura/Peso", value = "${card.height ?: "1.76 m"} / ${card.weight ?: "73 kg"}")
            ProfileRow(label = "Camisa", value = "Clube: $resolvedShirtClub · Seleção: $resolvedShirtNat")
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(0.5.dp)
                .background(primaryColor.copy(alpha = 0.25f))
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(1.dp)
        ) {
            Text("CARREIRA", color = primaryColor, fontSize = 6.sp, fontWeight = FontWeight.Black, letterSpacing = 0.8.sp)
            resolvedCareer.take(3).forEach { (years, club) ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = years, color = primaryColor, fontSize = 5.5.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(36.dp))
                    Text(text = club, color = Color(0xFFCFD8E6), fontSize = 5.5.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(0.5.dp)
                .background(primaryColor.copy(alpha = 0.25f))
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(1.dp)
        ) {
            Text("TÍTULOS PRINCIPAIS", color = primaryColor, fontSize = 6.sp, fontWeight = FontWeight.Black, letterSpacing = 0.8.sp)
            resolvedTitles.take(3).forEach { (stars, description) ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = stars, color = primaryColor, fontSize = 5.5.sp, fontWeight = FontWeight.Black)
                    Text(text = description, color = Color(0xFFCFD8E6), fontSize = 5.5.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(0.5.dp)
                .background(primaryColor.copy(alpha = 0.25f))
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(1.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("STATS DE CARREIRA", color = primaryColor, fontSize = 6.sp, fontWeight = FontWeight.Black, letterSpacing = 0.8.sp)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                BackStatItem(value = resolvedGames, label = "Jogos")
                BackStatItem(value = resolvedGoals, label = "Gols")
                BackStatItem(value = resolvedAssists, label = "Assist.")
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Text(text = "Atl: agora", color = Color(0xFF6C7587), fontSize = 5.sp, fontWeight = FontWeight.Normal)
            Text(
                text = "${if (activeCopaEdition) "COPA '26" else "CLUBES"} · #${(1001 + card.id)}",
                color = primaryColor,
                fontSize = 5.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun ProfileRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = Color(0xFF8B95A7), fontSize = 5.5.sp, fontWeight = FontWeight.Medium)
        Text(text = value, color = Color.White, fontSize = 5.5.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
fun BackStatItem(value: Int, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value.toString(), color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Black)
        Text(text = label.uppercase(), color = Color(0xFF8B95A7), fontSize = 5.sp, fontWeight = FontWeight.Bold)
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
            fontSize = 9.5.sp,
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

    Box(
        modifier = modifier
            .width(140.dp)
            .height(220.dp)
            .shadow(12.dp, shape = FutCardShape)
            .clip(FutCardShape)
            .background(StadiumConcrete)
            .border(2.dp, NeonEmerald, shape = FutCardShape),
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
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(StadiumGlow, StadiumObsidian),
                        center = Offset(size.width * 0.5f, size.height * 0.5f),
                        radius = size.height * 0.7f
                    )
                )

                for (i in -3..10) {
                    val offset = i * 25.dp.toPx()
                    drawLine(
                        color = NeonEmerald.copy(alpha = 0.06f),
                        start = Offset(0f, offset),
                        end = Offset(size.width, offset + size.width),
                        strokeWidth = 1.5.dp.toPx()
                    )
                }
            }
        }

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
