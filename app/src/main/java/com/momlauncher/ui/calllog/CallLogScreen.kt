package com.momlauncher.ui.calllog

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CallMade
import androidx.compose.material.icons.filled.CallMissed
import androidx.compose.material.icons.filled.CallReceived
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.momlauncher.R
import com.momlauncher.data.CallDirection
import com.momlauncher.data.CallLogEntry
import com.momlauncher.ui.components.BottomHomeButton
import com.momlauncher.ui.theme.*
import com.momlauncher.util.CallHelper

@Composable
fun CallLogScreen(
    callLogs: List<CallLogEntry>,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PureBlack)
    ) {
        // Üst Bar: Başlık
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkSurfaceVariant)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(R.string.call_log_screen_title),
                fontSize = 26.sp,
                fontWeight = FontWeight.Black,
                color = TextWhite
            )
        }

        if (callLogs.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.no_call_log_found),
                    fontSize = 24.sp,
                    color = TextMuted,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(
                    items = callLogs,
                    key = { it.id }
                ) { entry ->
                    CallLogRowItem(
                        entry = entry,
                        onClick = {
                            CallHelper.makeCall(context, entry.number)
                        }
                    )
                }
            }
        }

        // Altta Sabit "ANA SAYFA" Butonu
        BottomHomeButton(onBack = onBack)
    }
}

@Composable
private fun CallLogRowItem(
    entry: CallLogEntry,
    onClick: () -> Unit
) {
    val (icon, iconColor, directionText) = when (entry.type) {
        CallDirection.INCOMING -> Triple(Icons.Default.CallReceived, CallGreen, stringResource(R.string.call_type_incoming))
        CallDirection.OUTGOING -> Triple(Icons.Default.CallMade, CallBlue, stringResource(R.string.call_type_outgoing))
        CallDirection.MISSED -> Triple(Icons.Default.CallMissed, CallRed, stringResource(R.string.call_type_missed))
        CallDirection.UNKNOWN -> Triple(Icons.Default.Phone, TextMuted, "")
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 80.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(DarkSurfaceVariant)
            .border(2.dp, BorderGray, RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Arama Yön İkonu
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(DarkSurfaceElevated)
                    .border(2.dp, iconColor, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = directionText,
                    tint = iconColor,
                    modifier = Modifier.size(34.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                // İsim / Numara (24sp+)
                Text(
                    text = entry.name,
                    fontSize = 24.sp,
                    lineHeight = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (entry.type == CallDirection.MISSED) CallRed else TextWhite,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                // Tarih & Saat
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = entry.formattedDate,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextMuted
                    )
                    if (entry.number != entry.name && entry.number.isNotBlank()) {
                        Text(
                            text = " • ${entry.number}",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Normal,
                            color = PrimaryYellow,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}
