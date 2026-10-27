package com.momlauncher.ui.call

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.momlauncher.R
import com.momlauncher.call.CallManager
import com.momlauncher.call.CallModel
import com.momlauncher.ui.theme.*

@Composable
fun InCallScreen(
    callModel: CallModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PureBlack)
            .padding(horizontal = 24.dp)
            .padding(top = 70.dp, bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Üst Başlık & Durum
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            val isCallActive = callModel.isCallActive
            val statusText = when {
                callModel.isRinging -> stringResource(R.string.incoming_call_title)
                isCallActive -> stringResource(R.string.call_active_title)
                callModel.isActiveOrDialing -> stringResource(R.string.outgoing_call_title)
                else -> stringResource(R.string.call_ended_title)
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (isCallActive) SoftCreamSurface else DarkSurfaceVariant)
                    .border(
                        width = if (isCallActive) 3.dp else 2.dp,
                        color = if (isCallActive) TurkishBordeaux else PrimaryYellow,
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(vertical = if (isCallActive) 14.dp else 12.dp, horizontal = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = statusText,
                    fontSize = if (isCallActive) 21.sp else 24.sp,
                    lineHeight = if (isCallActive) 28.sp else 30.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isCallActive) TurkishBordeaux else PrimaryYellow,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(36.dp))

            var lastKnownName by remember { mutableStateOf(callModel.callerName) }
            var lastKnownNumber by remember { mutableStateOf(callModel.callerNumber) }

            if (callModel.callerName.isNotBlank()) {
                lastKnownName = callModel.callerName
            }
            if (callModel.callerNumber.isNotBlank()) {
                lastKnownNumber = callModel.callerNumber
            }

            val finalCallerName = callModel.callerName.ifBlank { lastKnownName }
            val finalCallerNumber = callModel.callerNumber.ifBlank { lastKnownNumber }

            // Arayan Kişi Adı (Çok Büyük)
            val displayName = finalCallerName.ifBlank {
                finalCallerNumber.ifBlank {
                    stringResource(R.string.unknown_caller)
                }
            }

            Text(
                text = displayName,
                fontSize = 38.sp,
                lineHeight = 44.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextWhite,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            // Arayan Numarası
            if (finalCallerNumber.isNotBlank() && finalCallerNumber != displayName) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = finalCallerNumber,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextMuted,
                    textAlign = TextAlign.Center
                )
            }

            // Görüşme Süresi
            if (callModel.isActiveOrDialing && callModel.durationSeconds > 0) {
                Spacer(modifier = Modifier.height(20.dp))
                val minutes = callModel.durationSeconds / 60
                val seconds = callModel.durationSeconds % 60
                val formattedTime = String.format("%02d:%02d", minutes, seconds)

                Text(
                    text = formattedTime,
                    fontSize = 42.sp,
                    fontWeight = FontWeight.Bold,
                    color = AccentYellow,
                    textAlign = TextAlign.Center
                )
            }
        }

        // Alt Butonlar (Cevapla / Reddet veya Hoparlör / Kapat)
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            if (callModel.isRinging) {
                // Dev Yeşil CEVAPLA Butonu (en az 84dp)
                BigCallActionButton(
                    text = stringResource(R.string.answer_call),
                    backgroundColor = CallGreen,
                    textColor = Color.White,
                    icon = Icons.Default.Call,
                    height = 88,
                    onClick = { CallManager.answer() }
                )

                // Dev Kırmızı REDDET Butonu
                BigCallActionButton(
                    text = stringResource(R.string.reject_call),
                    backgroundColor = CallRed,
                    textColor = Color.White,
                    icon = Icons.Default.CallEnd,
                    height = 84,
                    onClick = { CallManager.reject() }
                )
            } else {
                // Hoparlör Aç / Kapat
                val speakerBg = if (callModel.isSpeakerOn) TurkishNavy else DarkSurfaceVariant
                val speakerTextColor = if (callModel.isSpeakerOn) Color.White else TextWhite
                val speakerBorder = if (callModel.isSpeakerOn) TurkishNavy else BorderGray
                val speakerText = if (callModel.isSpeakerOn) {
                    stringResource(R.string.speaker_on)
                } else {
                    stringResource(R.string.speaker_off)
                }

                BigCallActionButton(
                    text = speakerText,
                    backgroundColor = speakerBg,
                    textColor = speakerTextColor,
                    borderColor = speakerBorder,
                    icon = Icons.Default.VolumeUp,
                    height = 76,
                    onClick = { CallManager.toggleSpeaker(context) }
                )

                // Dev Kırmızı KAPAT Butonu
                BigCallActionButton(
                    text = stringResource(R.string.end_call),
                    backgroundColor = CallRed,
                    textColor = Color.White,
                    icon = Icons.Default.CallEnd,
                    height = 88,
                    onClick = { CallManager.disconnect() }
                )
            }
        }
    }
}

@Composable
private fun BigCallActionButton(
    text: String,
    backgroundColor: Color,
    textColor: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    height: Int,
    borderColor: Color = Color.Transparent,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(backgroundColor)
            .then(
                if (borderColor != Color.Transparent) {
                    Modifier.border(3.dp, borderColor, RoundedCornerShape(20.dp))
                } else Modifier
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null, // Animasyonsuz anında tepki
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(38.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = text,
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = textColor
            )
        }
    }
}
