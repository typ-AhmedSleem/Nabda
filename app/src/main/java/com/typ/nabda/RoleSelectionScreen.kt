package com.typ.nabda

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.typ.nabda.designsystem.theme.NabdaTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoleSelectionScreen(
    onRoleSelected: (isCaregiver: Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color(0xFFFAF9F0),
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFFAF9F0),
                    titleContentColor = Color(0xFF322E2B)
                ),
                title = {
                    Text(
                        text = stringResource(R.string.brand_name),
                        style = MaterialTheme.typography.labelLarge.copy(
                            letterSpacing = 6.sp,
                            fontWeight = FontWeight.Black
                        )
                    )
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = stringResource(R.string.choose_your_role),
                style = MaterialTheme.typography.displayMedium.copy(
                    fontWeight = FontWeight.Black,
                    lineHeight = 48.sp,
                    color = Color(0xFF322E2B),
                    fontSize = 52.sp
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.select_interface_to_begin),
                style = MaterialTheme.typography.titleLarge.copy(
                    color = Color(0xFF8B8982),
                    fontWeight = FontWeight.Medium,
                    fontSize = 22.sp
                ),
                textAlign = TextAlign.Center
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(
                        vertical = 32.dp
                    ),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(
                    alignment = Alignment.Bottom,
                    space = 24.dp,
                )
            ) {
                RoleCard(
                    text = stringResource(R.string.i_am_a_deaf_user),
                    iconResId = R.drawable.ic_hearing,
                    watermarkResId = R.drawable.ic_hearing,
                    onClick = { onRoleSelected(false) }
                )

                RoleCard(
                    text = stringResource(R.string.i_am_a_caregiver),
                    iconResId = R.drawable.ic_heart,
                    watermarkResId = R.drawable.ic_heart,
                    onClick = { onRoleSelected(true) }
                )
            }
        }
    }
}

@Composable
private fun RoleCard(
    text: String,
    iconResId: Int,
    watermarkResId: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp)
            .clip(RoundedCornerShape(64.dp))
            .background(Color(0xFFFEFF59))
            .clickable(onClick = onClick)
    ) {
        // Large Watermark Icon positioned to bleed off the bottom right
        Icon(
            painter = painterResource(id = watermarkResId),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = 40.dp, y = 40.dp)
                .size(240.dp),
            tint = Color(0xFF322E2B).copy(alpha = 0.05f)
        )

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(28.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .background(Color(0xFF322E2B), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = iconResId),
                    contentDescription = null,
                    modifier = Modifier.size(52.dp),
                    tint = Color(0xFFFEFF59)
                )
            }

            Text(
                text = text,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF322E2B),
                    lineHeight = 34.sp,
                    fontSize = 28.sp
                )
            )
        }
    }
}

@Composable
@Preview(showBackground = true)
private fun RoleSelectionScreenPreview() {
    NabdaTheme {
        RoleSelectionScreen(onRoleSelected = {})
    }
}
