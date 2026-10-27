package com.momlauncher.ui.contacts

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.momlauncher.R
import com.momlauncher.data.ContactItem
import com.momlauncher.ui.theme.*
import com.momlauncher.ui.components.BottomHomeButton
import kotlinx.coroutines.launch
import java.util.Locale

private val TURKISH_ALPHABET = listOf(
    "A", "B", "C", "Ç", "D", "E", "F", "G", "H", "I", "İ", "J",
    "K", "L", "M", "N", "O", "Ö", "P", "R", "S", "Ş", "T", "U", "Ü", "V", "Y", "Z", "#"
)

@Composable
fun ContactsScreen(
    contacts: List<ContactItem>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onContactClicked: (ContactItem) -> Unit,
    onBack: () -> Unit,
    onAddNewContact: (() -> Unit)? = null,
    pickingTitle: String? = null,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    // Harflerin başlangıç indekslerini hesapla
    val letterIndexMap = remember(contacts) {
        val map = mutableMapOf<String, Int>()
        contacts.forEachIndexed { index, contact ->
            val initial = contact.initial.uppercase(Locale("tr", "TR"))
            if (!map.containsKey(initial)) {
                map[initial] = index
            }
        }
        map
    }

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
                text = pickingTitle ?: stringResource(R.string.contacts_screen_title),
                fontSize = 26.sp,
                fontWeight = FontWeight.Black,
                color = TextWhite,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Arama Kutusu
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(DarkSurfaceVariant)
                .border(2.5.dp, PrimaryYellow, RoundedCornerShape(16.dp))
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = PrimaryYellow,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))

                Box(modifier = Modifier.weight(1f)) {
                    if (searchQuery.isEmpty()) {
                        Text(
                            text = stringResource(R.string.search_contact_hint),
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Normal,
                            color = TextMuted
                        )
                    }

                    BasicTextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChange,
                        textStyle = TextStyle(
                            fontSize = 22.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextWhite
                        ),
                        cursorBrush = SolidColor(PrimaryYellow),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                if (searchQuery.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(DarkSurfaceElevated)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                onSearchQueryChange("")
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = null,
                            tint = TextWhite,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }

        // "YENİ KİŞİ EKLE" Butonu
        if (pickingTitle == null && onAddNewContact != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 10.dp)
                    .height(60.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(TurkishEmerald)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onAddNewContact
                    ),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PersonAdd,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "YENİ KİŞİ EKLE",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }
            }
        }

        // Kişi Listesi ve Sağ Tarafta Harf Kaydırma İndeksi
        if (contacts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.no_contacts_found),
                    fontSize = 24.sp,
                    color = TextMuted,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                // Rehber Listesi
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    itemsIndexed(
                        items = contacts,
                        key = { _, item -> item.id + item.number }
                    ) { _, contact ->
                        ContactRowItem(
                            contact = contact,
                            onClick = { onContactClicked(contact) }
                        )
                    }
                }

                // Kenarda Harf Kaydırma Çubuğu (Arama boşken daha kullanışlı)
                AlphabetIndexBar(
                    availableLetters = TURKISH_ALPHABET,
                    onLetterSelected = { letter ->
                        val targetIndex = letterIndexMap[letter]
                        if (targetIndex != null) {
                            coroutineScope.launch {
                                listState.scrollToItem(targetIndex)
                            }
                        }
                    }
                )
            }
        }

        // Altta Sabit "ANA SAYFA" Butonu
        BottomHomeButton(onBack = onBack)
    }
}

@Composable
private fun ContactRowItem(
    contact: ContactItem,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 78.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(DarkSurfaceVariant)
            .border(2.dp, BorderGray, RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 14.dp, vertical = 12.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Fotoğraf veya İsim Baş Harfi
            if (!contact.photoUri.isNullOrBlank()) {
                AsyncImage(
                    model = Uri.parse(contact.photoUri),
                    contentDescription = contact.name,
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
                        .background(PrimaryYellow),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = contact.initial,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                // Büyük İsim (24sp+)
                Text(
                    text = contact.name,
                    fontSize = 24.sp,
                    lineHeight = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                // Numara
                Text(
                    text = contact.number,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Medium,
                    color = PrimaryYellow
                )
            }
        }
    }
}

@Composable
private fun AlphabetIndexBar(
    availableLetters: List<String>,
    onLetterSelected: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .width(36.dp)
            .fillMaxHeight()
            .background(DarkSurface)
            .pointerInput(availableLetters) {
                detectTapGestures { offset ->
                    val itemHeight = size.height / availableLetters.size.toFloat()
                    val index = (offset.y / itemHeight).toInt().coerceIn(0, availableLetters.size - 1)
                    onLetterSelected(availableLetters[index])
                }
            }
            .pointerInput(availableLetters) {
                detectDragGestures { change, _ ->
                    val itemHeight = size.height / availableLetters.size.toFloat()
                    val index = (change.position.y / itemHeight).toInt().coerceIn(0, availableLetters.size - 1)
                    onLetterSelected(availableLetters[index])
                }
            },
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        availableLetters.forEach { letter ->
            Text(
                text = letter,
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold,
                color = AccentYellow,
                textAlign = TextAlign.Center
            )
        }
    }
}
