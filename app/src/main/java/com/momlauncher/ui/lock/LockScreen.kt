package com.momlauncher.ui.lock

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CallMissed
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import com.momlauncher.R
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.momlauncher.data.CallLogEntry
import com.momlauncher.data.NextPrayer
import com.momlauncher.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun LockScreen(
    nextPrayer: NextPrayer?,
    missedCalls: List<CallLogEntry>,
    onUnlock: () -> Unit,
    modifier: Modifier = Modifier
) {
    var currentTime by remember { mutableStateOf(getFormattedTime()) }
    var currentDate by remember { mutableStateOf(getFormattedDate()) }

    LaunchedEffect(Unit) {
        while (isActive) {
            currentTime = getFormattedTime()
            currentDate = getFormattedDate()
            delay(1000)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(WarmIvoryBackground)
            .padding(horizontal = 20.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Üst Kısım: Kilit İkonu, Büyük Saat, Tarih ve Namaz Bilgisi
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Üst kilit durumu rozeti
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(PureCardWhite)
                    .border(2.dp, WarmSandBorder, CircleShape)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Ekran Kilitli",
                    tint = TurkishAmber,
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Dev Saat
            Text(
                text = currentTime,
                fontSize = 68.sp,
                lineHeight = 72.sp,
                fontWeight = FontWeight.Black,
                color = DeepCharcoalText,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Okunaklı Tarih
            Text(
                text = currentDate,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = WarmMutedText,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Namaz Vakti Kartı (Varsa)
            if (nextPrayer != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .clip(RoundedCornerShape(18.dp))
                        .background(PureCardWhite)
                        .border(2.dp, WarmSandBorder, RoundedCornerShape(18.dp))
                        .padding(vertical = 12.dp, horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_mosque),
                            contentDescription = "Cami Minare",
                            tint = TurkishEmerald,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "KEÇİÖREN  •  ",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TurkishAmber
                        )
                        Text(
                            text = "${nextPrayer.prayerName.uppercase(Locale("tr", "TR"))}: ",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = DeepCharcoalText
                        )
                        Text(
                            text = nextPrayer.prayerTime,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = TurkishEmerald
                        )
                    }
                }
            }

            // Varsa Cevapsız Çağrı Özeti
            if (missedCalls.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                val recentMissed = missedCalls.first()
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.95f)
                        .clip(RoundedCornerShape(18.dp))
                        .background(PureCardWhite)
                        .border(2.dp, TurkishBordeaux.copy(alpha = 0.6f), RoundedCornerShape(18.dp))
                        .padding(vertical = 10.dp, horizontal = 14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.CallMissed,
                            contentDescription = null,
                            tint = TurkishBordeaux,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Cevapsız Arama: ${recentMissed.name}",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = TurkishBordeaux,
                                maxLines = 1
                            )
                            Text(
                                text = "Açtıktan sonra ana ekrandan geri arayabilirsiniz",
                                fontSize = 13.sp,
                                color = WarmMutedText
                            )
                        }
                    }
                }
            }
        }

        // Alt Kısım: Yaşlılar İçin Dev, Tek Dokunuşla "KİLİDİ AÇ" Butonu
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Text(
                text = "Telefonu kullanmak için dokunun",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = WarmMutedText,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(96.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(TurkishEmerald)
                    .border(3.dp, PureCardWhite, RoundedCornerShape(24.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onUnlock
                    ),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.LockOpen,
                        contentDescription = "Kilidi Aç",
                        tint = Color.White,
                        modifier = Modifier.size(42.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(
                            text = "KİLİDİ AÇ",
                            fontSize = 30.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Açmak için buraya basın",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                }
            }
        }
    }
}

private fun getFormattedTime(): String {
    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
    return sdf.format(Date())
}

private fun getFormattedDate(): String {
    val sdf = SimpleDateFormat("d MMMM EEEE", Locale("tr", "TR"))
    return sdf.format(Date())
}
