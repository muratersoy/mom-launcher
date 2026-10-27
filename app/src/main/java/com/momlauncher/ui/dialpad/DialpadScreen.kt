package com.momlauncher.ui.dialpad

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.momlauncher.R
import com.momlauncher.ui.components.BottomHomeButton
import com.momlauncher.ui.theme.*

@Composable
fun DialpadScreen(
    onCall: (String) -> Unit,
    onAddContact: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var dialedNumber by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(WarmIvoryBackground),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Üst Bar: Başlık
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "NUMARA ÇEVİR",
                fontSize = 26.sp,
                fontWeight = FontWeight.Black,
                color = DeepCharcoalText
            )
        }

        // Numara Gösterge Alanı
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(PureCardWhite)
                .border(2.5.dp, TurkishEmerald, RoundedCornerShape(18.dp))
                .padding(horizontal = 14.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (dialedNumber.isEmpty()) "Numara yazın..." else dialedNumber,
                    fontSize = if (dialedNumber.length > 12) 26.sp else 32.sp,
                    fontWeight = FontWeight.Black,
                    color = if (dialedNumber.isEmpty()) WarmMutedText else DeepCharcoalText,
                    maxLines = 1,
                    modifier = Modifier.weight(1f)
                )

                if (dialedNumber.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(SoftCreamSurface)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = {
                                    if (dialedNumber.isNotEmpty()) {
                                        dialedNumber = dialedNumber.dropLast(1)
                                    }
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Backspace,
                            contentDescription = "Sil",
                            tint = TurkishBordeaux,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }
        }

        // Tuş Takımı (3x4)
        val rows = listOf(
            listOf("1", "2", "3"),
            listOf("4", "5", "6"),
            listOf("7", "8", "9"),
            listOf("*", "0", "#")
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            rows.forEach { rowKeys ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    rowKeys.forEach { key ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(68.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .background(PureCardWhite)
                                .border(2.dp, WarmSandBorder, RoundedCornerShape(18.dp))
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = {
                                        if (dialedNumber.length < 20) {
                                            dialedNumber += key
                                        }
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = key,
                                fontSize = 34.sp,
                                fontWeight = FontWeight.Bold,
                                color = DeepCharcoalText,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }

        // Alt Eylem Butonları: [REHBERE EKLE] ve [DEV ARA BUTONU]
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Rehbere Ekle Butonu
            Box(
                modifier = Modifier
                    .weight(1.1f)
                    .height(78.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(TurkishNavy)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { onAddContact(dialedNumber) }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PersonAdd,
                        contentDescription = "Rehbere Ekle",
                        tint = Color.White,
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "REHBERE\nEKLE",
                        fontSize = 15.sp,
                        lineHeight = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Dev Yeşil Arama Butonu
            Box(
                modifier = Modifier
                    .weight(1.6f)
                    .height(78.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(TurkishEmerald)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {
                            if (dialedNumber.isNotBlank()) {
                                onCall(dialedNumber)
                            }
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = "Ara",
                        tint = Color.White,
                        modifier = Modifier.size(34.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "ARA",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }
            }
        }
        }

        // Altta Sabit "ANA SAYFA" Butonu
        BottomHomeButton(onBack = onBack)
    }
}
