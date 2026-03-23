package com.typ.nabda.caregiver.pairing

import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.typ.nabda.caregiver.R
import com.typ.nabda.designsystem.theme.NabdaTheme
import com.typ.nabda.infrastructure.localnetwork.client.ConnectionStatus

@Composable
fun PairingScannerScreen(
    viewModel: WifiPairingViewModel,
    onPairingSuccess: () -> Unit,
    onBackClick: () -> Unit = {},
) {
    val status by viewModel.pairingStatus.collectAsStateWithLifecycle()

    LaunchedEffect(status) {
        if (status == ConnectionStatus.CONNECTED) {
            onPairingSuccess()
        }
    }

    PairingScannerContent(
        status = status,
        onBackClick = onBackClick,
        onRetry = { /* Could trigger retry in ViewModel/Service if exposed */ }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PairingScannerContent(
    status: ConnectionStatus,
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    onRetry: () -> Unit = {},
) {
    val backgroundColor = Color(0xFFFBF9F1)

    Scaffold(
        modifier = modifier,
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = backgroundColor
                ),
                title = {
                    Text(
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                        text = stringResource(id = R.string.brand_name),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp
                        ),
                        color = Color(0xFF333333)
                    )
                }
            )
        },
        containerColor = backgroundColor
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (status == ConnectionStatus.FAILED) {
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Button(onClick = onRetry) {
                        Text(text = stringResource(id = R.string.retry))
                    }
                }
            } else {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    ScanningIllustration()

                    Spacer(modifier = Modifier.height(16.dp))

                    val titleRes = when (status) {
                        ConnectionStatus.CONNECTING -> R.string.connecting_to_device
                        ConnectionStatus.RECONNECTING -> R.string.reconnecting_to_device
                        ConnectionStatus.PAIRING -> R.string.pairing_in_progress
                        else -> R.string.searching_for_devices
                    }

                    Text(
                        text = stringResource(id = titleRes),
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 28.sp
                        ),
                        color = Color(0xFF333333),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    val subtitleRes = when (status) {
                        ConnectionStatus.CONNECTING, ConnectionStatus.RECONNECTING -> R.string.pairing_description
                        else -> R.string.make_sure_device_on
                    }

                    Text(
                        text = stringResource(id = subtitleRes),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF757575),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            HelpCard()

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun ScanningIllustration(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "ripple")
    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "progress"
    )

    Box(
        modifier = modifier
            .size(220.dp)
            .drawBehind {
                val rippleColor = Color(0xFF346280)
                val baseRadius = size.minDimension / 2

                for (i in 0..5) {
                    val rippleProgress = (progress + i / 4f) % 1f
                    val radius = baseRadius * rippleProgress
                    val alpha = 0.5f * (1f - rippleProgress)

                    drawCircle(
                        color = rippleColor.copy(alpha = alpha),
                        radius = radius,
                        center = center
                    )
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier.size(70.dp),
            shape = CircleShape,
            color = Color(0xFF346280),
            shadowElevation = 8.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(Color.White.copy(alpha = 0.3f), CircleShape)
                )
            }
        }
    }
}

@Composable
fun DeviceCard(
    name: String,
    signal: String,
    modifier: Modifier = Modifier,
    onPairClick: () -> Unit = {},
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = CircleShape,
        color = Color.White
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFF333333)
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(Color(0xFF346280).copy(alpha = 0.6f))
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = signal,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF346280).copy(alpha = 0.7f)
                    )
                }
            }

            Button(
                onClick = onPairClick,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF346280)),
                shape = RoundedCornerShape(20.dp),
                contentPadding = PaddingValues(horizontal = 24.dp)
            ) {
                Text(text = stringResource(id = R.string.pair), color = Color.White)
            }
        }
    }
}

@Composable
fun HelpCard(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(40.dp),
        color = Color(0xFFF0F2E8).copy(alpha = 0.8f)
    ) {
        Row(
            modifier = Modifier.padding(24.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF346280).copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(id = R.string.info_icon_text),
                    color = Color(0xFF346280),
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = stringResource(id = R.string.dont_see_device),
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFF333333)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(id = R.string.pairing_help_text),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF555555),
                    lineHeight = 18.sp
                )
            }
        }
    }
}

@Preview(
    showBackground = true,
    locale = "ar"
)
@Composable
fun PairingScannerScreenScanningPreview() {
    NabdaTheme {
        PairingScannerContent(
            status = ConnectionStatus.SCANNING
        )
    }
}

//@Preview(showBackground = true)
@Composable
fun PairingScannerScreenPairingPreview() {
    NabdaTheme {
        PairingScannerContent(
            status = ConnectionStatus.PAIRING
        )
    }
}

//@Preview(showBackground = true)
@Composable
fun PairingScannerScreenFailedPreview() {
    NabdaTheme {
        PairingScannerContent(
            status = ConnectionStatus.FAILED
        )
    }
}
