package com.momlauncher.ui.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.momlauncher.R
import com.momlauncher.data.EmergencyContact
import com.momlauncher.ui.components.BottomHomeButton
import com.momlauncher.ui.theme.*
import com.momlauncher.util.PermissionHelper
import java.util.Locale

@Composable
fun SettingsScreen(
    emergencyContacts: List<EmergencyContact?>,
    onSelectEmergencySlot: (Int) -> Unit,
    onRemoveEmergencySlot: (Int) -> Unit,
    onRequestDialerRole: () -> Unit,
    isDefaultDialer: Boolean,
    isDefaultHome: Boolean,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PureBlack)
    ) {
        // Üst Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkSurfaceVariant)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "AYARLAR",
                fontSize = 26.sp,
                fontWeight = FontWeight.Black,
                color = TextWhite,
                textAlign = TextAlign.Center
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // 1. Bölüm: Hızlı Arama (Acil Kişiler) Yönetimi
            Text(
                text = "HIZLI ARAMA KİŞİLERİ (6 YUVA)",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryYellow
            )

            for (i in 0 until 6) {
                val contact = emergencyContacts.getOrNull(i)
                EmergencySlotSettingCard(
                    slotNumber = i + 1,
                    contact = contact,
                    onAssign = { onSelectEmergencySlot(i) },
                    onRemove = { onRemoveEmergencySlot(i) }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 2. Bölüm: Varsayılan Uygulama Ayarları
            Text(
                text = "VARSAYILAN UYGULAMA AYARLARI",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryYellow
            )

            // Varsayılan Başlatıcı Yap Butonu
            SettingActionButton(
                title = if (isDefaultHome) "VARSAYILAN BAŞLATICI (AKTİF)" else "VARSAYILAN BAŞLATICI YAP",
                subtitle = "Ana ekran tuşuna basıldığında bu uygulamanın açılması için",
                backgroundColor = if (isDefaultHome) DarkSurfaceVariant else PrimaryYellow,
                textColor = if (isDefaultHome) TextWhite else Color.White,
                icon = Icons.Default.Home,
                onClick = { PermissionHelper.openHomeSettings(context) }
            )

            // Varsayılan Telefon Yap Butonu
            SettingActionButton(
                title = if (isDefaultDialer) "VARSAYILAN TELEFON (AKTİF)" else "VARSAYILAN TELEFON YAP",
                subtitle = "Aramaların büyük tuşlarla ekrana gelmesi için",
                backgroundColor = if (isDefaultDialer) DarkSurfaceVariant else CallGreen,
                textColor = if (isDefaultDialer) TextWhite else Color.White,
                icon = Icons.Default.Phone,
                onClick = onRequestDialerRole
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 3. Bölüm: Telefonun Kendi Cihaz Ayarları
            Text(
                text = "CİHAZ AYARLARI",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryYellow
            )

            SettingActionButton(
                title = "TELEFONUN KENDİ AYARLARI",
                subtitle = "Wi-Fi, Bluetooth, Ses, Ekran vb. ayarlara git",
                backgroundColor = DarkSurfaceElevated,
                textColor = TextWhite,
                icon = Icons.Default.Settings,
                borderColor = BorderGray,
                onClick = {
                    val intent = Intent(Settings.ACTION_SETTINGS).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(intent)
                }
            )

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Sabit Alt Ana Sayfa Butonu
        BottomHomeButton(onBack = onBack)
    }
}

@Composable
private fun EmergencySlotSettingCard(
    slotNumber: Int,
    contact: EmergencyContact?,
    onAssign: () -> Unit,
    onRemove: () -> Unit
) {
    val currentContact = contact
    val isAssigned = currentContact != null

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(DarkSurfaceVariant)
            .border(
                2.dp,
                if (isAssigned) PrimaryYellow else BorderGray,
                RoundedCornerShape(18.dp)
            )
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Yuva Numarası / Fotoğraf
            if (currentContact != null && !currentContact.photoUri.isNullOrBlank()) {
                AsyncImage(
                    model = Uri.parse(currentContact.photoUri),
                    contentDescription = currentContact.name,
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .border(2.dp, PrimaryYellow, CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(if (isAssigned) PrimaryYellow else DarkSurfaceElevated),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (currentContact != null) {
                            currentContact.name.trim().take(1).uppercase(Locale("tr", "TR"))
                        } else {
                            "$slotNumber"
                        },
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Black,
                        color = if (isAssigned) Color.White else TextMuted
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Kişi Bilgisi veya Boş Uyarısı
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (currentContact != null) currentContact.name else "$slotNumber. Yuva (Boş)",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isAssigned) TextWhite else TextMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (currentContact != null) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = currentContact.number,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Medium,
                        color = PrimaryYellow
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Değiştir / Ekle Butonu
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(PrimaryYellow)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onAssign
                    )
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isAssigned) "DEĞİŞTİR" else "KİŞİ SEÇ",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
            }

            // Sil Butonu (Atanmışsa)
            if (isAssigned) {
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(DarkSurfaceElevated)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onRemove
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Sil",
                        tint = CallRed,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingActionButton(
    title: String,
    subtitle: String,
    backgroundColor: Color,
    textColor: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    borderColor: Color = Color.Transparent,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(backgroundColor)
            .then(
                if (borderColor != Color.Transparent) {
                    Modifier.border(2.dp, borderColor, RoundedCornerShape(18.dp))
                } else Modifier
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 16.dp, vertical = 14.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(34.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = textColor
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontSize = 15.sp,
                    color = if (textColor == Color.White) Color.White.copy(alpha = 0.85f) else TextMuted
                )
            }
        }
    }
}
