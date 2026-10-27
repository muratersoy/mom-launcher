package com.momlauncher.ui.home

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.momlauncher.R
import com.momlauncher.call.CallActivity
import com.momlauncher.call.CallModel
import com.momlauncher.data.CallLogEntry
import com.momlauncher.data.EmergencyContact
import com.momlauncher.data.NextPrayer
import com.momlauncher.ui.theme.*
import com.momlauncher.util.CallHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    callModel: CallModel,
    emergencyContacts: List<EmergencyContact?>,
    nextPrayer: NextPrayer?,
    missedCalls: List<CallLogEntry> = emptyList(),
    onCallBackMissedCall: (CallLogEntry) -> Unit = {},
    onDismissMissedCall: (String) -> Unit = {},
    onOpenContacts: () -> Unit,
    onOpenDialpad: () -> Unit,
    onOpenCallLog: () -> Unit,
    onOpenGallery: () -> Unit,
    onOpenNews: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenAllApps: () -> Unit = {},
    onSelectEmergencySlot: (Int) -> Unit,
    onRequestPermissions: () -> Unit,
    onRequestDialerRole: () -> Unit,
    onSetDefaultHome: () -> Unit,
    hasPermissions: Boolean,
    isDefaultDialer: Boolean,
    isDefaultHome: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    // Saat ve Tarih Dinamik Güncellemesi
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
            .background(PureBlack)
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        // Aktif Arama Varsa Tepede Tam Genişlik Yeşil "GÖRÜŞMEYE DÖN" Bandı
        if (callModel.hasCall) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(CallGreen)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        val intent = Intent(context, CallActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                                    Intent.FLAG_ACTIVITY_SINGLE_TOP or
                                    Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                        }
                        context.startActivity(intent)
                    },
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(30.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = stringResource(R.string.return_to_call_banner),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
        }

        // İzinler / Launcher / Dialer Eksikse Bildirim Kartı
        if (!hasPermissions || !isDefaultDialer || !isDefaultHome) {
            PermissionWarningCard(
                hasPermissions = hasPermissions,
                isDefaultDialer = isDefaultDialer,
                isDefaultHome = isDefaultHome,
                onGrantPermissions = onRequestPermissions,
                onSetDefaultDialer = onRequestDialerRole,
                onSetDefaultHome = onSetDefaultHome
            )
            Spacer(modifier = Modifier.height(6.dp))
        }

        // Üst Saat ve Namaz Vakti Alanı
        if (nextPrayer != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(92.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Sol Taraf: Büyük Saat ve Tarih
                Column(
                    modifier = Modifier
                        .weight(1.1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(18.dp))
                        .background(DarkSurfaceVariant)
                        .border(2.dp, BorderGray, RoundedCornerShape(18.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = currentTime,
                        fontSize = 38.sp,
                        lineHeight = 40.sp,
                        fontWeight = FontWeight.Black,
                        color = PrimaryYellow,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = currentDate,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center
                    )
                }

                // Sağ Taraf: En Yakın Namaz Vakti (Ankara Keçiören)
                Column(
                    modifier = Modifier
                        .weight(0.9f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(18.dp))
                        .background(DarkSurfaceVariant)
                        .border(2.dp, BorderGray, RoundedCornerShape(18.dp))
                        .combinedClickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onDoubleClick = onOpenAllApps,
                            onClick = { /* Tek tıkta bir şey yapmaz */ }
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "KEÇİÖREN",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = AccentYellow,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = nextPrayer.prayerName.uppercase(Locale("tr", "TR")),
                        fontSize = 20.sp,
                        lineHeight = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = TextWhite,
                        textAlign = TextAlign.Center
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_mosque),
                            contentDescription = "Cami Minare",
                            tint = TurkishEmerald,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = nextPrayer.prayerTime,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            color = PrimaryYellow,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            // Namaz verisine erişilemezse tek saat kartı
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(92.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(DarkSurfaceVariant)
                    .border(2.dp, BorderGray, RoundedCornerShape(18.dp))
                    .combinedClickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onDoubleClick = onOpenAllApps,
                        onClick = { /* Tek tıkta bir şey yapmaz */ }
                    )
                    .padding(vertical = 6.dp, horizontal = 14.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = currentTime,
                    fontSize = 44.sp,
                    lineHeight = 48.sp,
                    fontWeight = FontWeight.Black,
                    color = PrimaryYellow,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = currentDate,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Acil Kişiler (6 Kart)
        Text(
            text = stringResource(R.string.emergency_contacts_title),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = AccentYellow,
            modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
        )

        val now = System.currentTimeMillis()
        val activeMissedCalls = remember(missedCalls, now) {
            val oneDayMillis = 24 * 60 * 60 * 1000L
            missedCalls.filter { (now - it.timestamp) in -60_000L..oneDayMillis }
        }

        EmergencyContactsGrid(
            contacts = emergencyContacts,
            modifier = Modifier
                .fillMaxWidth()
                .weight(if (activeMissedCalls.isNotEmpty()) 0.95f else 1.05f),
            onCall = { number -> CallHelper.makeCall(context, number) },
            onLongClick = { slot -> onSelectEmergencySlot(slot) }
        )

        // Hızlı Arama ile Tuşlar Arasında: Son 24 Saatteki Cevapsız Aramalar
        if (activeMissedCalls.isNotEmpty()) {
            Spacer(modifier = Modifier.height(4.dp))
            MissedCallsSection(
                missedCalls = activeMissedCalls,
                onCallBack = onCallBackMissedCall,
                onDismiss = onDismissMissedCall
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Alt Butonlar (5 Kart)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(if (activeMissedCalls.isNotEmpty()) 1.2f else 1.35f),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // "REHBER" ve "TUŞLARLA ARA" Butonları (Yan Yana)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1.15f),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // "REHBER" Butonu
                BigHomeActionCard(
                    text = stringResource(R.string.contacts_button),
                    icon = Icons.Default.People,
                    backgroundColor = TurkishEmerald,
                    contentColor = Color.White,
                    iconColor = Color.White,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    fontSize = 19,
                    iconSize = 28,
                    onClick = onOpenContacts
                )

                // "TUŞLARLA ARA" Butonu
                BigHomeActionCard(
                    text = "TUŞLARLA ARA",
                    icon = Icons.Default.Dialpad,
                    backgroundColor = TurkishNavy,
                    contentColor = Color.White,
                    iconColor = Color.White,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    fontSize = 17,
                    iconSize = 26,
                    onClick = onOpenDialpad
                )
            }

            // "SON ARAMALAR" Butonu
            BigHomeActionCard(
                text = stringResource(R.string.call_log_button),
                icon = Icons.Default.PhoneCallback,
                backgroundColor = DarkSurfaceVariant,
                contentColor = TextWhite,
                iconColor = TurkishNavy,
                borderColor = BorderGray,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                fontSize = 20,
                iconSize = 26,
                onClick = onOpenCallLog
            )

            // "FOTOĞRAFLAR" Butonu (Dahili Kolay Fotoğraf Gösterici)
            BigHomeActionCard(
                text = stringResource(R.string.gallery_button),
                icon = Icons.Default.Image,
                backgroundColor = DarkSurfaceVariant,
                contentColor = TextWhite,
                iconColor = TurkishAmber,
                borderColor = BorderGray,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                fontSize = 20,
                iconSize = 26,
                onClick = onOpenGallery
            )

            // "HABERLER" Butonu (Güncel Reklamsız Haberler)
            BigHomeActionCard(
                text = stringResource(R.string.news_button),
                icon = Icons.Default.Newspaper,
                backgroundColor = DarkSurfaceVariant,
                contentColor = TextWhite,
                iconColor = TurkishEmerald,
                borderColor = BorderGray,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                fontSize = 20,
                iconSize = 26,
                onClick = onOpenNews
            )

            // "AYARLAR" Butonu (Hızlı Arama & Sistem Ayarları)
            BigHomeActionCard(
                text = stringResource(R.string.settings_button),
                icon = Icons.Default.Settings,
                backgroundColor = DarkSurfaceVariant,
                contentColor = TextWhite,
                iconColor = TurkishNavy,
                borderColor = BorderGray,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                fontSize = 20,
                iconSize = 26,
                onClick = onOpenSettings
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun EmergencyContactsGrid(
    contacts: List<EmergencyContact?>,
    modifier: Modifier = Modifier,
    onCall: (String) -> Unit,
    onLongClick: (Int) -> Unit
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        for (row in 0 until 3) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val idx0 = row * 2
                val idx1 = row * 2 + 1
                val contact0 = contacts.getOrNull(idx0)
                val contact1 = contacts.getOrNull(idx1)

                EmergencyCard(
                    contact = contact0,
                    slotIndex = idx0,
                    modifier = Modifier.weight(1f),
                    onCall = onCall,
                    onLongClick = onLongClick
                )
                EmergencyCard(
                    contact = contact1,
                    slotIndex = idx1,
                    modifier = Modifier.weight(1f),
                    onCall = onCall,
                    onLongClick = onLongClick
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun EmergencyCard(
    contact: EmergencyContact?,
    slotIndex: Int,
    modifier: Modifier = Modifier,
    onCall: (String) -> Unit,
    onLongClick: (Int) -> Unit
) {
    val isAssigned = contact != null

    Box(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(16.dp))
            .background(if (isAssigned) DarkSurfaceElevated else DarkSurface)
            .border(
                width = if (isAssigned) 2.dp else 1.5.dp,
                color = if (isAssigned) PrimaryYellow else BorderGray,
                shape = RoundedCornerShape(16.dp)
            )
            .combinedClickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {
                    if (contact != null) {
                        onCall(contact.number)
                    } else {
                        onLongClick(slotIndex)
                    }
                },
                onLongClick = {
                    onLongClick(slotIndex)
                }
            )
            .padding(horizontal = 6.dp, vertical = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        if (contact != null) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Fotoğraf veya Harf İkonu
                if (!contact.photoUri.isNullOrBlank()) {
                    AsyncImage(
                        model = Uri.parse(contact.photoUri),
                        contentDescription = contact.name,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .border(2.dp, PrimaryYellow, CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(PrimaryYellow),
                        contentAlignment = Alignment.Center
                    ) {
                        val initial = contact.name.trim().take(1).uppercase(Locale("tr", "TR"))
                        Text(
                            text = initial,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = contact.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AddCircleOutline,
                    contentDescription = null,
                    tint = PrimaryYellow,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = stringResource(R.string.empty_slot_hint),
                    fontSize = 12.sp,
                    lineHeight = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextMuted,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun BigHomeActionCard(
    text: String,
    icon: ImageVector,
    backgroundColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
    fontSize: Int = 20,
    iconSize: Int = 26,
    iconColor: Color = contentColor,
    borderColor: Color = Color.Transparent,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .then(
                if (borderColor != Color.Transparent) {
                    Modifier.border(2.dp, borderColor, RoundedCornerShape(16.dp))
                } else Modifier
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(iconSize.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = text,
                fontSize = fontSize.sp,
                fontWeight = FontWeight.ExtraBold,
                color = contentColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun QuickAppShortcut(
    text: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(84.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(DarkSurfaceVariant)
            .border(2.dp, BorderGray, RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = PrimaryYellow,
                modifier = Modifier.size(30.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = text,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = TextWhite,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun PermissionWarningCard(
    hasPermissions: Boolean,
    isDefaultDialer: Boolean,
    isDefaultHome: Boolean,
    onGrantPermissions: () -> Unit,
    onSetDefaultDialer: () -> Unit,
    onSetDefaultHome: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(DarkSurfaceVariant)
            .border(2.dp, PrimaryYellow, RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Text(
            text = stringResource(R.string.permission_title),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = PrimaryYellow
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = stringResource(R.string.permission_desc),
            fontSize = 16.sp,
            color = TextWhite
        )
        Spacer(modifier = Modifier.height(10.dp))

        if (!isDefaultHome) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(PrimaryYellow)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onSetDefaultHome
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.set_default_home_button),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (!hasPermissions) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(TurkishNavy)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onGrantPermissions
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.grant_permissions_button),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (!isDefaultDialer) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(CallGreen)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onSetDefaultDialer
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.set_default_dialer_button),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun MissedCallsSection(
    missedCalls: List<CallLogEntry>,
    onCallBack: (CallLogEntry) -> Unit,
    onDismiss: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        missedCalls.take(3).forEach { call ->
            MissedCallItem(
                entry = call,
                onCallBack = { onCallBack(call) },
                onDismiss = { onDismiss(call.id) }
            )
        }
    }
}

@Composable
private fun MissedCallItem(
    entry: CallLogEntry,
    onCallBack: () -> Unit,
    onDismiss: () -> Unit
) {
    val relativeTime = formatRelativeTime(entry.timestamp)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(SoftCreamSurface)
            .border(1.5.dp, TurkishBordeaux.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
            .padding(horizontal = 12.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Sol Alan: İkon + 2 Satır İsim ve Zaman Bilgisi
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(TurkishBordeaux.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CallMissed,
                    contentDescription = null,
                    tint = TurkishBordeaux,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                // 1. Satır: Arayan Kişinin İsmi (Net, büyük, kısaltılmadan rahatça okunur)
                Text(
                    text = entry.name,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Black,
                    color = DeepCharcoalText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                // 2. Satır: Ne zaman aradığı (Örn: 1 saat önce aradı)
                Text(
                    text = "$relativeTime aradı",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TurkishBordeaux,
                    maxLines = 1
                )
            }
        }

        Spacer(modifier = Modifier.width(10.dp))

        // Sağ Alan: GERİ ARA ve BOŞVER Butonları
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // GERİ ARA Butonu (Yeşil)
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(TurkishEmerald)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onCallBack
                    )
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "GERİ ARA",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
            }

            // BOŞVER Butonu (Açık kart, ince çerçeve)
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(PureCardWhite)
                    .border(1.dp, BorderGray, RoundedCornerShape(10.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onDismiss
                    )
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "BOŞVER",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = DeepCharcoalText
                )
            }
        }
    }
}

private fun formatRelativeTime(timestamp: Long): String {
    if (timestamp <= 0L) return "az önce"
    val diffMillis = System.currentTimeMillis() - timestamp
    if (diffMillis < 0L) return "az önce"

    val minutes = diffMillis / (60 * 1000)
    val hours = diffMillis / (60 * 60 * 1000)

    return when {
        minutes < 2 -> "az önce"
        minutes < 60 -> "$minutes dakika önce"
        hours == 1L -> "1 saat önce"
        hours < 24 -> "$hours saat önce"
        else -> "dün"
    }
}

private fun getFormattedTime(): String {
    val sdf = SimpleDateFormat("HH:mm", Locale("tr", "TR"))
    return sdf.format(Date())
}

private fun getFormattedDate(): String {
    val sdf = SimpleDateFormat("d MMMM EEEE", Locale("tr", "TR"))
    return sdf.format(Date())
}
