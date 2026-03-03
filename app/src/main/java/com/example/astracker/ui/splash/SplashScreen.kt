package com.example.astracker.ui.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Assignment
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

// ── Brand colours ──────────────────────────────────────────────────────────────
private val Brand        = Color(0xFF6366F1)   // Indigo-500  – primary
private val BrandLight   = Color(0xFF818CF8)   // Indigo-400  – lighter ring
private val BrandDark    = Color(0xFF4338CA)   // Indigo-700  – gradient bottom
private val AccentOrange = Color(0xFFF97316)   // Orange-400  – accent dot
private val BgDeep       = Color(0xFF0F0E2A)   // very dark navy

// ── Animation timing ───────────────────────────────────────────────────────────
private const val LOGO_ENTER_MS     = 600
private const val RING_DELAY_MS     = 200L
private const val TEXT_DELAY_MS     = 500L
private const val TAGLINE_DELAY_MS  = 750L
private const val DOTS_DELAY_MS     = 900L
private const val TOTAL_DURATION_MS = 2400L

// ── SplashScreen ───────────────────────────────────────────────────────────────
@Composable
fun SplashScreen(
    onSplashComplete : () -> Unit = {}
) {
    // ── Phase gate: drives sequential reveal ──────────────────────────────────
    var phase by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        delay(RING_DELAY_MS);    phase = 1   // rings appear
        delay(TEXT_DELAY_MS);    phase = 2   // title appears
        delay(TAGLINE_DELAY_MS); phase = 3   // tagline appears
        delay(DOTS_DELAY_MS);    phase = 4   // loading dots appear
        delay(TOTAL_DURATION_MS)
        onSplashComplete()
    }

    // ── Background gradient ────────────────────────────────────────────────────
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(BgDeep, Color(0xFF1E1B4B), Color(0xFF312E81))
                )
            ),
        contentAlignment = Alignment.Center
    ) {

        // ── Glowing background blob ────────────────────────────────────────────
        GlowBlob()

        // ── Center content ─────────────────────────────────────────────────────
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {

            // ── Logo stack ────────────────────────────────────────────────────
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(180.dp)
            ) {
                // Outer pulse ring – ring 1
                PulseRing(
                    visible      = phase >= 1,
                    delayMillis  = 0,
                    size         = 170.dp,
                    color        = BrandLight.copy(alpha = 0.15f),
                    borderWidth  = 1.dp
                )
                // Outer pulse ring – ring 2
                PulseRing(
                    visible      = phase >= 1,
                    delayMillis  = 300,
                    size         = 138.dp,
                    color        = Brand.copy(alpha = 0.22f),
                    borderWidth  = 1.5.dp
                )

                // Logo card
                LogoCard(phase = phase)

                // Accent dot (orange) – top-right corner
                if (phase >= 1) {
                    AccentDot(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = (-8).dp, y = 8.dp)
                    )
                }
            }

            Spacer(Modifier.height(28.dp))

            // ── App name ──────────────────────────────────────────────────────
            AppTitle(visible = phase >= 2)

            Spacer(Modifier.height(8.dp))

            // ── Tagline ───────────────────────────────────────────────────────
            Tagline(visible = phase >= 3)

            Spacer(Modifier.height(52.dp))

            // ── Loading dots ──────────────────────────────────────────────────
            LoadingDots(visible = phase >= 4)
        }

        // ── Version label at bottom ────────────────────────────────────────────
        Text(
            text     = "v1.0.0",
            fontSize = 11.sp,
            color    = Color.White.copy(alpha = 0.25f),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 28.dp)
        )
    }
}

// ── Glow blob ──────────────────────────────────────────────────────────────────
@Composable
private fun GlowBlob() {
    val infiniteTransition = rememberInfiniteTransition(label = "blob")
    val scale by infiniteTransition.animateFloat(
        initialValue   = 0.85f,
        targetValue    = 1.15f,
        animationSpec  = infiniteRepeatable(
            animation = tween(3200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "blob_scale"
    )

    Box(
        modifier = Modifier
            .size(320.dp)
            .scale(scale)
            .blur(60.dp)
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Brand.copy(alpha = 0.35f),
                        Color.Transparent
                    )
                ),
                CircleShape
            )
    )
}

// ── Logo card with scale + fade entrance ─────────────────────────────────────
@Composable
private fun LogoCard(phase: Int) {
    val scale by animateFloatAsState(
        targetValue   = if (phase >= 1) 1f else 0.4f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness    = Spring.StiffnessMediumLow
        ),
        label = "logo_scale"
    )
    val alpha by animateFloatAsState(
        targetValue   = if (phase >= 1) 1f else 0f,
        animationSpec = tween(LOGO_ENTER_MS),
        label = "logo_alpha"
    )

    // Subtle continuous rotation shimmer on the icon
    val infiniteTransition = rememberInfiniteTransition(label = "logo_idle")
    val iconScale by infiniteTransition.animateFloat(
        initialValue  = 1f,
        targetValue   = 1.06f,
        animationSpec = infiniteRepeatable(
            animation  = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "icon_idle_scale"
    )

    Box(
        modifier = Modifier
            .size(96.dp)
            .scale(scale)
            .alpha(alpha)
            .background(
                Brush.linearGradient(
                    colors = listOf(BrandLight, BrandDark)
                ),
                RoundedCornerShape(28.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        // Subtle inner highlight
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.18f),
                            Color.Transparent
                        )
                    ),
                    RoundedCornerShape(28.dp)
                )
        )
        Icon(
            imageVector        = Icons.Rounded.Assignment,
            contentDescription = "AsTracker logo",
            tint               = Color.White,
            modifier           = Modifier
                .size(48.dp)
                .scale(if (phase >= 1) iconScale else 1f)
        )
    }
}

// ── Animated pulse ring ────────────────────────────────────────────────────────
@Composable
private fun PulseRing(
    visible     : Boolean,
    delayMillis : Int,
    size        : Dp,
    color       : Color,
    borderWidth : Dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ring_$delayMillis")
    val scale by infiniteTransition.animateFloat(
        initialValue  = 0.88f,
        targetValue   = 1.05f,
        animationSpec = infiniteRepeatable(
            animation  = tween(1600 + delayMillis, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ring_scale_$delayMillis"
    )
    val alpha by infiniteTransition.animateFloat(
        initialValue  = 0.4f,
        targetValue   = 1f,
        animationSpec = infiniteRepeatable(
            animation  = tween(1600 + delayMillis, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ring_alpha_$delayMillis"
    )

    val enterAlpha by animateFloatAsState(
        targetValue   = if (visible) 1f else 0f,
        animationSpec = tween(500),
        label         = "ring_enter_$delayMillis"
    )

    Box(
        modifier = Modifier
            .size(size)
            .scale(scale)
            .alpha(alpha * enterAlpha)
            .background(color, CircleShape)
    )
}

// ── Accent dot ────────────────────────────────────────────────────────────────
@Composable
private fun AccentDot(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "dot")
    val scale by infiniteTransition.animateFloat(
        initialValue  = 0.8f,
        targetValue   = 1.2f,
        animationSpec = infiniteRepeatable(
            animation  = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot_scale"
    )

    Box(
        modifier = modifier
            .size(14.dp)
            .scale(scale)
            .background(
                Brush.radialGradient(
                    colors = listOf(AccentOrange, AccentOrange.copy(alpha = 0.6f))
                ),
                CircleShape
            )
    )
}

// ── App title ─────────────────────────────────────────────────────────────────
@Composable
private fun AppTitle(visible: Boolean) {
    val offsetY by animateFloatAsState(
        targetValue   = if (visible) 0f else 24f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness    = Spring.StiffnessMediumLow
        ),
        label = "title_offset"
    )
    val alpha by animateFloatAsState(
        targetValue   = if (visible) 1f else 0f,
        animationSpec = tween(400),
        label         = "title_alpha"
    )

    // Shimmer sweep across the text
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    val shimmerX by infiniteTransition.animateFloat(
        initialValue  = -200f,
        targetValue   =  400f,
        animationSpec = infiniteRepeatable(
            animation  = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_x"
    )

    Box(
        modifier = Modifier
            .graphicsLayer { translationY = offsetY }
            .alpha(alpha)
    ) {
        Text(
            text       = "AsTracker",
            fontSize   = 36.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 1.sp,
            color      = Color.Transparent,
            modifier   = Modifier.background(
                Brush.horizontalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.90f),
                        BrandLight,
                        Color.White.copy(alpha = 0.90f)
                    ),
                    startX = shimmerX,
                    endX   = shimmerX + 300f
                )
            )
        )
        // Solid white text on top so it's always readable
        Text(
            text          = "AsTracker",
            fontSize      = 36.sp,
            fontWeight    = FontWeight.ExtraBold,
            letterSpacing = 1.sp,
            color         = Color.White
        )
    }
}

// ── Tagline ───────────────────────────────────────────────────────────────────
@Composable
private fun Tagline(visible: Boolean) {
    val offsetY by animateFloatAsState(
        targetValue   = if (visible) 0f else 16f,
        animationSpec = tween(500, easing = FastOutSlowInEasing),
        label         = "tagline_offset"
    )
    val alpha by animateFloatAsState(
        targetValue   = if (visible) 1f else 0f,
        animationSpec = tween(450),
        label         = "tagline_alpha"
    )

    Text(
        text      = "Track · Organise · Succeed",
        fontSize  = 13.sp,
        fontWeight = FontWeight.Medium,
        color     = Color.White.copy(alpha = 0.55f),
        textAlign = TextAlign.Center,
        letterSpacing = 1.5.sp,
        modifier  = Modifier
            .graphicsLayer { translationY = offsetY }
            .alpha(alpha)
    )
}

// ── Loading dots ──────────────────────────────────────────────────────────────
@Composable
private fun LoadingDots(visible: Boolean) {
    val enterAlpha by animateFloatAsState(
        targetValue   = if (visible) 1f else 0f,
        animationSpec = tween(300),
        label         = "dots_enter"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "dots")

    // Three dots, each offset 200 ms
    val scales = (0..2).map { i ->
        infiniteTransition.animateFloat(
            initialValue  = 0.5f,
            targetValue   = 1f,
            animationSpec = infiniteRepeatable(
                animation   = tween(600, easing = FastOutSlowInEasing),
                repeatMode  = RepeatMode.Reverse,
                initialStartOffset = StartOffset(i * 200)
            ),
            label = "dot_scale_$i"
        )
    }

    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment     = Alignment.CenterVertically,
        modifier              = Modifier.alpha(enterAlpha)
    ) {
        scales.forEachIndexed { i, scaleState ->
            val dotColor = when (i) {
                0    -> BrandLight
                1    -> Brand
                else -> AccentOrange
            }
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .scale(scaleState.value)
                    .clip(CircleShape)
                    .background(dotColor)
            )
        }
    }
}

// ── Preview ───────────────────────────────────────────────────────────────────
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun SplashScreenPreview() {
    SplashScreen()
}
